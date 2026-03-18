package com.munashechipanga.eharvest.specs;

import com.munashechipanga.eharvest.dtos.ProduceFilter;
import com.munashechipanga.eharvest.entities.Produce;
import org.springframework.data.jpa.domain.Specification;

public class ProduceSpecifications {
    public static Specification<Produce> withFilters(ProduceFilter f) {
        if (f == null) return null;
        return Specification.where(minPrice(f.getMinPrice()))
                .and(maxPrice(f.getMaxPrice()))
                .and(categoryEquals(f.getCategory()))
                .and(nameLike(f.getName()))
                .and(qualityEquals(f.getQualityGrade()))
                .and(harvestFrom(f.getHarvestFrom()))
                .and(harvestTo(f.getHarvestTo()))
                .and(searchLike(f.getSearch()));
    }

    private static Specification<Produce> minPrice(Double min) {
        return (root, q, cb) -> min == null ? null : cb.greaterThanOrEqualTo(root.get("price"), min);
    }

    private static Specification<Produce> maxPrice(Double max) {
        return (root, q, cb) -> max == null ? null : cb.lessThanOrEqualTo(root.get("price"), max);
    }

    private static Specification<Produce> categoryEquals(String category) {
        return (root, q, cb) -> category == null || category.isBlank() ? null : cb.equal(cb.lower(root.get("category")), category.toLowerCase());
    }

    private static Specification<Produce> nameLike(String name) {
        return (root, q, cb) -> {
            if (name == null || name.isBlank()) return null;
            String like = "%" + name.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("name")), like);
        };
    }

    private static Specification<Produce> qualityEquals(String qg) {
        return (root, q, cb) -> qg == null || qg.isBlank() ? null : cb.equal(cb.lower(root.get("qualityGrade")), qg.toLowerCase());
    }

    private static Specification<Produce> harvestFrom(java.time.LocalDate from) {
        return (root, q, cb) -> from == null ? null : cb.greaterThanOrEqualTo(root.get("harvestDate"), from);
    }

    private static Specification<Produce> harvestTo(java.time.LocalDate to) {
        return (root, q, cb) -> to == null ? null : cb.lessThanOrEqualTo(root.get("harvestDate"), to);
    }

    private static Specification<Produce> searchLike(String s) {
        return (root, q, cb) -> {
            if (s == null || s.isBlank()) return null;
            String like = "%" + s.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("description")), like)
            );
        };
    }
}
