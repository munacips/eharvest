package com.munashechipanga.eharvest.specs;

import com.munashechipanga.eharvest.dtos.FarmerFilter;
import com.munashechipanga.eharvest.entities.Farmer;
import org.springframework.data.jpa.domain.Specification;

public class FarmerSpecifications {
    public static Specification<Farmer> withFilters(FarmerFilter f) {
        if (f == null) return null;
        return Specification.where(firstNameLike(f.getFirstName()))
                .and(lastNameLike(f.getLastName()))
                .and(usernameLike(f.getUsername()))
                .and(emailLike(f.getEmail()))
                .and(farmNameLike(f.getFarmName()))
                .and(farmLocationLike(f.getFarmLocation()))
                .and(activeEquals(f.getActive()))
                .and(verifiedEquals(f.getVerified()))
                .and(minTrustScore(f.getMinTrustScore()))
                .and(maxTrustScore(f.getMaxTrustScore()))
                .and(searchLike(f.getSearch()));
    }

    private static Specification<Farmer> firstNameLike(String v) {
        return (root, q, cb) -> v == null || v.isBlank() ? null : cb.like(cb.lower(root.get("firstName")), "%" + v.toLowerCase() + "%");
    }

    private static Specification<Farmer> lastNameLike(String v) {
        return (root, q, cb) -> v == null || v.isBlank() ? null : cb.like(cb.lower(root.get("lastName")), "%" + v.toLowerCase() + "%");
    }

    private static Specification<Farmer> usernameLike(String v) {
        return (root, q, cb) -> v == null || v.isBlank() ? null : cb.like(cb.lower(root.get("username")), "%" + v.toLowerCase() + "%");
    }

    private static Specification<Farmer> emailLike(String v) {
        return (root, q, cb) -> v == null || v.isBlank() ? null : cb.like(cb.lower(root.get("email")), "%" + v.toLowerCase() + "%");
    }

    private static Specification<Farmer> farmNameLike(String v) {
        return (root, q, cb) -> v == null || v.isBlank() ? null : cb.like(cb.lower(root.get("farmName")), "%" + v.toLowerCase() + "%");
    }

    private static Specification<Farmer> farmLocationLike(String v) {
        return (root, q, cb) -> v == null || v.isBlank() ? null : cb.like(cb.lower(root.get("farmLocation")), "%" + v.toLowerCase() + "%");
    }

    private static Specification<Farmer> activeEquals(Boolean v) {
        return (root, q, cb) -> v == null ? null : cb.equal(root.get("active"), v);
    }

    private static Specification<Farmer> verifiedEquals(Boolean v) {
        return (root, q, cb) -> v == null ? null : cb.equal(root.get("verified"), v);
    }

    private static Specification<Farmer> minTrustScore(Integer v) {
        return (root, q, cb) -> v == null ? null : cb.greaterThanOrEqualTo(root.get("trustScore"), v);
    }

    private static Specification<Farmer> maxTrustScore(Integer v) {
        return (root, q, cb) -> v == null ? null : cb.lessThanOrEqualTo(root.get("trustScore"), v);
    }

    private static Specification<Farmer> searchLike(String s) {
        return (root, q, cb) -> {
            if (s == null || s.isBlank()) return null;
            String like = "%" + s.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("firstName")), like),
                    cb.like(cb.lower(root.get("lastName")), like),
                    cb.like(cb.lower(root.get("username")), like),
                    cb.like(cb.lower(root.get("email")), like),
                    cb.like(cb.lower(root.get("farmName")), like),
                    cb.like(cb.lower(root.get("farmLocation")), like)
            );
        };
    }
}
