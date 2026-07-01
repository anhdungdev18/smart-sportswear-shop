package com.dunghaiquyen.ecommerce.modules.page.dto;

import com.dunghaiquyen.ecommerce.modules.page.entity.PageStatus;

public record PageListQuery(Integer page, Integer limit, PageStatus status) {
}
