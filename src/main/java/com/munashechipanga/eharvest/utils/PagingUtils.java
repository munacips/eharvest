package com.munashechipanga.eharvest.utils;

import org.springframework.data.domain.Sort;

public final class PagingUtils {
    private PagingUtils() {}

    public static Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) return Sort.unsorted();
        String[] parts = sort.split(",");
        String field = parts[0].trim();
        String dir = parts.length > 1 ? parts[1].trim().toLowerCase() : "asc";
        return "desc".equals(dir) ? Sort.by(field).descending() : Sort.by(field).ascending();
    }
}
