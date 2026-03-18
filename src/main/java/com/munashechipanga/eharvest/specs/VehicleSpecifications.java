package com.munashechipanga.eharvest.specs;

import com.munashechipanga.eharvest.dtos.VehicleFilter;
import com.munashechipanga.eharvest.entities.Vehicle;
import org.springframework.data.jpa.domain.Specification;

public class VehicleSpecifications {
    public static Specification<Vehicle> withFilters(VehicleFilter f) {
        if (f == null) return null;
        return Specification.where(typeEquals(f.getType()))
                .and(colourEquals(f.getColour()))
                .and(plateNumberLike(f.getPlateNumber()))
                .and(ownerIdEquals(f.getOwnerId()))
                .and(searchLike(f.getSearch()));
    }

    private static Specification<Vehicle> typeEquals(String type) {
        return (root, q, cb) -> (type == null || type.isBlank()) ? null : cb.equal(cb.lower(root.get("type")), type.toLowerCase());
    }

    private static Specification<Vehicle> colourEquals(String colour) {
        return (root, q, cb) -> (colour == null || colour.isBlank()) ? null : cb.equal(cb.lower(root.get("colour")), colour.toLowerCase());
    }

    private static Specification<Vehicle> plateNumberLike(String plate) {
        return (root, q, cb) -> {
            if (plate == null || plate.isBlank()) return null;
            String like = "%" + plate.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("plateNumber")), like);
        };
    }

    private static Specification<Vehicle> ownerIdEquals(Long ownerId) {
        return (root, q, cb) -> ownerId == null ? null : cb.equal(root.get("owner").get("id"), ownerId);
    }

    private static Specification<Vehicle> searchLike(String s) {
        return (root, q, cb) -> {
            if (s == null || s.isBlank()) return null;
            String like = "%" + s.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("plateNumber")), like),
                    cb.like(cb.lower(root.get("type")), like),
                    cb.like(cb.lower(root.get("colour")), like)
            );
        };
    }
}
