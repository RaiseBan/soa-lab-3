package com.musicband.api.repository;

import com.musicband.api.model.MusicBand;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository for MusicBand entity operations
 */
@ApplicationScoped
@Transactional
public class MusicBandRepository {

    @PersistenceContext(unitName = "musicBandPU")
    private EntityManager entityManager;

    /**
     * Create new music band
     */
    public MusicBand create(MusicBand band) {
        entityManager.persist(band);
        entityManager.flush();
        return band;
    }

    /**
     * Find band by ID
     */
    public Optional<MusicBand> findById(Integer id) {
        MusicBand band = entityManager.find(MusicBand.class, id);
        return Optional.ofNullable(band);
    }

    /**
     * Update existing band
     */
    public MusicBand update(MusicBand band) {
        return entityManager.merge(band);
    }

    /**
     * Delete band by ID
     */
    public boolean delete(Integer id) {
        MusicBand band = entityManager.find(MusicBand.class, id);
        if (band != null) {
            entityManager.remove(band);
            return true;
        }
        return false;
    }

    /**
     * Find all bands with pagination, filtering and sorting
     */
    public List<MusicBand> findAll(int page, int size, List<String> sortFields, Map<String, String> filters) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<MusicBand> cq = cb.createQuery(MusicBand.class);
        Root<MusicBand> root = cq.from(MusicBand.class);

        // Apply filters
        List<Predicate> predicates = buildPredicates(cb, root, filters);
        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        // Apply sorting
        List<Order> orders = buildOrders(cb, root, sortFields);
        if (!orders.isEmpty()) {
            cq.orderBy(orders);
        }

        TypedQuery<MusicBand> query = entityManager.createQuery(cq);
        query.setFirstResult(page * size);
        query.setMaxResults(size);

        return query.getResultList();
    }

    /**
     * Count total bands with filters
     */
    public long count(Map<String, String> filters) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<MusicBand> root = cq.from(MusicBand.class);

        cq.select(cb.count(root));

        // Apply filters
        List<Predicate> predicates = buildPredicates(cb, root, filters);
        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        return entityManager.createQuery(cq).getSingleResult();
    }

    /**
     * Calculate average number of participants
     */
    public Double getAverageParticipants() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Double> cq = cb.createQuery(Double.class);
        Root<MusicBand> root = cq.from(MusicBand.class);

        cq.select(cb.avg(root.get("numberOfParticipants")));

        Double result = entityManager.createQuery(cq).getSingleResult();
        return result != null ? result : 0.0;
    }

    /**
     * Build predicates for filtering
     */
    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<MusicBand> root, Map<String, String> filters) {
        List<Predicate> predicates = new ArrayList<>();

        if (filters == null || filters.isEmpty()) {
            return predicates;
        }

        for (Map.Entry<String, String> entry : filters.entrySet()) {
            String[] parts = entry.getValue().split(":", 3);
            if (parts.length != 3) continue;

            String field = parts[0];
            String operator = parts[1];
            String value = parts[2];

            try {
                Predicate predicate = createPredicate(cb, root, field, operator, value);
                if (predicate != null) {
                    predicates.add(predicate);
                }
            } catch (Exception e) {
                // Skip invalid filters
            }
        }

        return predicates;
    }

    /**
     * Create predicate based on field, operator and value
     */
    private Predicate createPredicate(CriteriaBuilder cb, Root<MusicBand> root, 
                                     String field, String operator, String value) {
        
        // Handle nested fields (coordinates.x, coordinates.y, label.sales)
        Path<?> path;
        if (field.contains(".")) {
            String[] fieldParts = field.split("\\.");
            path = root.get(fieldParts[0]).get(fieldParts[1]);
        } else {
            path = root.get(field);
        }

        switch (operator.toLowerCase()) {
            case "eq":
                return cb.equal(path, parseValue(path.getJavaType(), value));
            case "ne":
                return cb.notEqual(path, parseValue(path.getJavaType(), value));
            case "gt":
                return cb.gt((Expression<? extends Number>) path, 
                           (Number) parseValue(path.getJavaType(), value));
            case "gte":
                return cb.ge((Expression<? extends Number>) path, 
                           (Number) parseValue(path.getJavaType(), value));
            case "lt":
                return cb.lt((Expression<? extends Number>) path, 
                           (Number) parseValue(path.getJavaType(), value));
            case "lte":
                return cb.le((Expression<? extends Number>) path, 
                           (Number) parseValue(path.getJavaType(), value));
            case "contains":
                return cb.like(cb.lower((Expression<String>) path), 
                             "%" + value.toLowerCase() + "%");
            default:
                return null;
        }
    }

    /**
     * Parse string value to appropriate type
     */
    private Object parseValue(Class<?> type, String value) {
        if (type == Integer.class) {
            return Integer.parseInt(value);
        } else if (type == Long.class) {
            return Long.parseLong(value);
        } else if (type == Double.class) {
            return Double.parseDouble(value);
        } else if (type.isEnum()) {
            return Enum.valueOf((Class<? extends Enum>) type, value);
        }
        return value;
    }

    /**
     * Build orders for sorting
     */
    private List<Order> buildOrders(CriteriaBuilder cb, Root<MusicBand> root, List<String> sortFields) {
        List<Order> orders = new ArrayList<>();

        if (sortFields == null || sortFields.isEmpty()) {
            return orders;
        }

        for (String sortField : sortFields) {
            String[] parts = sortField.split(",");
            if (parts.length == 0) continue;

            String field = parts[0];
            String direction = parts.length > 1 ? parts[1] : "asc";

            // Handle nested fields
            Path<?> path;
            if (field.contains(".")) {
                String[] fieldParts = field.split("\\.");
                path = root.get(fieldParts[0]).get(fieldParts[1]);
            } else {
                path = root.get(field);
            }

            if ("desc".equalsIgnoreCase(direction)) {
                orders.add(cb.desc(path));
            } else {
                orders.add(cb.asc(path));
            }
        }

        return orders;
    }
}