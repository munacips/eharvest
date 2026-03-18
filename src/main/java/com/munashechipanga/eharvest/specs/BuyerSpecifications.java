package com.munashechipanga.eharvest.specs;

import com.munashechipanga.eharvest.dtos.BuyerFilter;
import com.munashechipanga.eharvest.entities.Buyer;
import org.springframework.data.jpa.domain.Specification;

public class BuyerSpecifications {
    public static Specification<Buyer> withFilters(BuyerFilter f) {
        if (f == null) return null;
        return Specification.where(firstNameLike(f.getFirstName()))
                .and(lastNameLike(f.getLastName()))
                .and(usernameLike(f.getUsername()))
                .and(emailLike(f.getEmail()))
                .and(companyLike(f.getCompanyName()))
                .and(activeEquals(f.getActive()))
                .and(verifiedEquals(f.getVerified()))
                .and(minTrustScore(f.getMinTrustScore()))
                .and(maxTrustScore(f.getMaxTrustScore()))
                .and(searchLike(f.getSearch()));
    }

    private static Specification<Buyer> firstNameLike(String v) {
        return (root, q, cb) -> v == null || v.isBlank() ? null : cb.like(cb.lower(root.get("firstName")), "%" + v.toLowerCase() + "%");
    }

    private static Specification<Buyer> lastNameLike(String v) {
        return (root, q, cb) -> v == null || v.isBlank() ? null : cb.like(cb.lower(root.get("lastName")), "%" + v.toLowerCase() + "%");
    }

    private static Specification<Buyer> usernameLike(String v) {
        return (root, q, cb) -> v == null || v.isBlank() ? null : cb.like(cb.lower(root.get("username")), "%" + v.toLowerCase() + "%");
    }

    private static Specification<Buyer> emailLike(String v) {
        return (root, q, cb) -> v == null || v.isBlank() ? null : cb.like(cb.lower(root.get("email")), "%" + v.toLowerCase() + "%");
    }

    private static Specification<Buyer> companyLike(String v) {
        return (root, q, cb) -> v == null || v.isBlank() ? null : cb.like(cb.lower(root.get("companyName")), "%" + v.toLowerCase() + "%");
    }

    private static Specification<Buyer> activeEquals(Boolean v) {
        return (root, q, cb) -> v == null ? null : cb.equal(root.get("active"), v);
    }

    private static Specification<Buyer> verifiedEquals(Boolean v) {
        return (root, q, cb) -> v == null ? null : cb.equal(root.get("verified"), v);
    }

    private static Specification<Buyer> minTrustScore(Integer v) {
        return (root, q, cb) -> v == null ? null : cb.greaterThanOrEqualTo(root.get("trustScore"), v);
    }

    private static Specification<Buyer> maxTrustScore(Integer v) {
        return (root, q, cb) -> v == null ? null : cb.lessThanOrEqualTo(root.get("trustScore"), v);
    }

    private static Specification<Buyer> searchLike(String s) {
        return (root, q, cb) -> {
            if (s == null || s.isBlank()) return null;
            String like = "%" + s.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("firstName")), like),
                    cb.like(cb.lower(root.get("lastName")), like),
                    cb.like(cb.lower(root.get("username")), like),
                    cb.like(cb.lower(root.get("email")), like),
                    cb.like(cb.lower(root.get("companyName")), like)
            );
        };
    }
}
