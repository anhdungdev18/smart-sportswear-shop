package com.dunghaiquyen.ecommerce.modules.setting.service;

import com.dunghaiquyen.ecommerce.common.exception.BusinessRuleException;
import com.dunghaiquyen.ecommerce.modules.setting.dto.PublicSiteSettingResponse;
import com.dunghaiquyen.ecommerce.modules.setting.dto.SiteSettingResponse;
import com.dunghaiquyen.ecommerce.modules.setting.dto.UpsertSiteSettingRequest;
import com.dunghaiquyen.ecommerce.modules.setting.entity.SettingValueType;
import com.dunghaiquyen.ecommerce.modules.setting.entity.SiteSetting;
import com.dunghaiquyen.ecommerce.modules.setting.repository.SiteSettingRepository;
import com.dunghaiquyen.ecommerce.modules.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Key-addressed upsert (PUT /api/v1/admin/settings/{key}) rather than
 * id-addressed create+update: a setting's identity IS its key (e.g.
 * "site.contact.email"), an admin editing config never has - and should
 * never need - the row's UUID, only the key they already know they want to
 * set. {@link #validateShape} is the same "minimal validation to prevent a
 * broken save" discipline NotificationTemplateService already applies to
 * placeholders, just for settingValue/valueType agreement here instead.
 */
@Service
public class SiteSettingService {

    private final SiteSettingRepository siteSettingRepository;
    private final ObjectMapper objectMapper;

    public SiteSettingService(SiteSettingRepository siteSettingRepository, ObjectMapper objectMapper) {
        this.siteSettingRepository = siteSettingRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<SiteSettingResponse> list() {
        return siteSettingRepository.findAllByOrderBySettingKeyAsc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PublicSiteSettingResponse> listPublic() {
        return siteSettingRepository.findAllByIsPublicTrue().stream()
                .map(s -> new PublicSiteSettingResponse(s.getSettingKey(), s.getSettingValue(), s.getValueType()))
                .toList();
    }

    @Transactional
    public SiteSettingResponse upsert(String key, UpsertSiteSettingRequest request, User actor) {
        validateShape(request.valueType(), request.settingValue());

        SiteSetting setting = siteSettingRepository.findBySettingKey(key).orElseGet(() -> {
            SiteSetting created = new SiteSetting();
            created.setSettingKey(key);
            return created;
        });
        setting.setSettingValue(request.settingValue());
        setting.setValueType(request.valueType());
        setting.setDescription(request.description());
        setting.setPublic(request.isPublic());
        setting.setUpdatedBy(actor);

        return toResponse(siteSettingRepository.save(setting));
    }

    private void validateShape(SettingValueType type, String value) {
        if (value == null) {
            return;
        }
        switch (type) {
            case NUMBER -> {
                try {
                    Double.parseDouble(value);
                } catch (NumberFormatException ex) {
                    throw new BusinessRuleException(
                            HttpStatus.UNPROCESSABLE_ENTITY, "settingValue is not a valid NUMBER: " + value);
                }
            }
            case BOOLEAN -> {
                if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                    throw new BusinessRuleException(
                            HttpStatus.UNPROCESSABLE_ENTITY, "settingValue is not a valid BOOLEAN: " + value);
                }
            }
            case JSON -> {
                try {
                    objectMapper.readTree(value);
                } catch (Exception ex) {
                    throw new BusinessRuleException(
                            HttpStatus.UNPROCESSABLE_ENTITY, "settingValue is not valid JSON: " + ex.getMessage());
                }
            }
            case STRING -> {
                // No shape constraint.
            }
        }
    }

    private SiteSettingResponse toResponse(SiteSetting setting) {
        return new SiteSettingResponse(
                setting.getId(),
                setting.getSettingKey(),
                setting.getSettingValue(),
                setting.getValueType(),
                setting.getDescription(),
                setting.isPublic(),
                setting.getUpdatedBy() != null ? setting.getUpdatedBy().getId() : null,
                setting.getCreatedAt(),
                setting.getUpdatedAt());
    }
}
