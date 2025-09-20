package com.cagri.hrms.dto.response.review;

import java.util.List;

public record PagedResponse<T>(List<T> items, long total, int page, int pageSize) {}
