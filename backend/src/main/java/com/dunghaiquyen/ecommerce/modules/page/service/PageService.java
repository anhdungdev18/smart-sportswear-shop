package com.dunghaiquyen.ecommerce.modules.page.service;

import com.dunghaiquyen.ecommerce.common.exception.BusinessRuleException;
import com.dunghaiquyen.ecommerce.common.exception.ResourceNotFoundException;
import com.dunghaiquyen.ecommerce.common.response.PageMeta;
import com.dunghaiquyen.ecommerce.modules.page.dto.PageCreateRequest;
import com.dunghaiquyen.ecommerce.modules.page.dto.PageListQuery;
import com.dunghaiquyen.ecommerce.modules.page.dto.PageResponse;
import com.dunghaiquyen.ecommerce.modules.page.dto.PageUpdateRequest;
import com.dunghaiquyen.ecommerce.modules.page.dto.PublicPageResponse;
import com.dunghaiquyen.ecommerce.modules.page.entity.Page;
import com.dunghaiquyen.ecommerce.modules.page.entity.PageStatus;
import com.dunghaiquyen.ecommerce.modules.page.repository.PageRepository;
import com.dunghaiquyen.ecommerce.modules.user.entity.User;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PageService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final PageRepository pageRepository;

    public PageService(PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    public record ListResult(List<PageResponse> items, PageMeta meta) {
    }

    /** GET /api/v1/pages/{slug} - 404 for anything not PUBLISHED (a DRAFT/ARCHIVED page must not be guessable/visible by slug). */
    @Transactional(readOnly = true)
    public PublicPageResponse getPublishedBySlug(String slug) {
        Page page = pageRepository.findBySlugAndStatus(slug, PageStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found"));
        return new PublicPageResponse(page.getTitle(), page.getSlug(), page.getSummary(), page.getContentHtml(), page.getPublishedAt());
    }

    @Transactional(readOnly = true)
    public ListResult list(PageListQuery query) {
        Specification<Page> spec = Specification.unrestricted();
        if (query.status() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), query.status()));
        }
        Pageable pageable = PageRequest.of(
                resolvePageIndex(query.page()), resolveLimit(query.limit()), Sort.by(Sort.Direction.DESC, "createdAt"));
        org.springframework.data.domain.Page<Page> page = pageRepository.findAll(spec, pageable);
        List<PageResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return new ListResult(items, PageMeta.from(page));
    }

    @Transactional(readOnly = true)
    public PageResponse getDetail(UUID id) {
        return toResponse(pageRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Page not found")));
    }

    @Transactional
    public PageResponse create(PageCreateRequest request, User actor) {
        if (pageRepository.existsBySlug(request.slug())) {
            throw new BusinessRuleException("Slug already exists: " + request.slug());
        }
        Page page = new Page();
        page.setTitle(request.title().trim());
        page.setSlug(request.slug());
        page.setSummary(request.summary());
        page.setContentHtml(request.contentHtml());
        page.setStatus(request.status() != null ? request.status() : PageStatus.DRAFT);
        page.setCreatedBy(actor);
        if (page.getStatus() == PageStatus.PUBLISHED) {
            page.setPublishedAt(Instant.now());
        }

        try {
            page = pageRepository.save(page);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessRuleException("Slug already exists: " + request.slug());
        }
        return toResponse(page);
    }

    @Transactional
    public PageResponse update(UUID id, PageUpdateRequest request) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found"));

        if (request.slug() != null && pageRepository.existsBySlugAndIdNot(request.slug(), id)) {
            throw new BusinessRuleException("Slug already exists: " + request.slug());
        }

        if (request.title() != null) {
            page.setTitle(request.title().trim());
        }
        if (request.slug() != null) {
            page.setSlug(request.slug());
        }
        if (request.summary() != null) {
            page.setSummary(request.summary());
        }
        if (request.contentHtml() != null) {
            page.setContentHtml(request.contentHtml());
        }
        if (request.status() != null) {
            page.setStatus(request.status());
            // First publish only - re-saving while already PUBLISHED must not bump publishedAt.
            if (request.status() == PageStatus.PUBLISHED && page.getPublishedAt() == null) {
                page.setPublishedAt(Instant.now());
            }
        }

        return toResponse(pageRepository.save(page));
    }

    private int resolvePageIndex(Integer page) {
        return (page != null && page > 0) ? page - 1 : 0;
    }

    private int resolveLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private PageResponse toResponse(Page page) {
        return new PageResponse(
                page.getId(),
                page.getTitle(),
                page.getSlug(),
                page.getSummary(),
                page.getContentHtml(),
                page.getStatus(),
                page.getPublishedAt(),
                page.getCreatedBy() != null ? page.getCreatedBy().getId() : null,
                page.getCreatedAt(),
                page.getUpdatedAt());
    }
}
