package com.munashechipanga.eharvest.specs;

import com.munashechipanga.eharvest.dtos.TransactionFilter;
import com.munashechipanga.eharvest.entities.TransactionHistory;
import org.springframework.data.jpa.domain.Specification;

public class TransactionSpecifications {
    public static Specification<TransactionHistory> withFilters(TransactionFilter filter) {
        Specification<TransactionHistory> spec = Specification.where(null);

        if (filter == null) {
            return spec;
        }
        if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }
        if (filter.getType() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("type"), filter.getType()));
        }
        if (filter.getCurrency() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("currency"), filter.getCurrency()));
        }

        return spec;
    }
}
