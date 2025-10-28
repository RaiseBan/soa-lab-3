package com.musicband.api.repository;

import com.musicband.api.model.MusicBand;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Stateless
public class MusicBandRepository {

    @PersistenceContext(unitName = "musicBandPU")
    private EntityManager entityManager;

    public MusicBand create(MusicBand band) {
        entityManager.persist(band);
        entityManager.flush();
        return band;
    }

    public Optional<MusicBand> findById(Integer id) {
        MusicBand band = entityManager.find(MusicBand.class, id);
        return Optional.ofNullable(band);
    }

    public MusicBand update(MusicBand band) {
        return entityManager.merge(band);
    }

    public boolean delete(Integer id) {
        MusicBand band = entityManager.find(MusicBand.class, id);
        if (band != null) {
            entityManager.remove(band);
            return true;
        }
        return false;
    }

    public List<MusicBand> findAll(int page, int size, List<String> sortFields, Map<String, String> filters) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<MusicBand> cq = cb.createQuery(MusicBand.class);
        Root<MusicBand> root = cq.from(MusicBand.class);

        List<Predicate> predicates = buildPredicates(cb, root, filters);
        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        List<Order> orders = buildOrders(cb, root, sortFields);
        if (!orders.isEmpty()) {
            cq.orderBy(orders);
        }

        TypedQuery<MusicBand> query = entityManager.createQuery(cq);
        query.setFirstResult(page * size);
        query.setMaxResults(size);

        return query.getResultList();
    }

    public long count(Map<String, String> filters) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<MusicBand> root = cq.from(MusicBand.class);

        cq.select(cb.count(root));

        List<Predicate> predicates = buildPredicates(cb, root, filters);
        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        return entityManager.createQuery(cq).getSingleResult();
    }

    public Double getAverageParticipants() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Double> cq = cb.createQuery(Double.class);
        Root<MusicBand> root = cq.from(MusicBand.class);

        cq.select(cb.avg(root.get("numberOfParticipants")));

        Double result = entityManager.createQuery(cq).getSingleResult();
        return result != null ? result : 0.0;
    }

    private List<Order> buildOrders(CriteriaBuilder cb, Root<MusicBand> root, List<String> sortFields) {
        List<Order> orders = new ArrayList<>();

        if (sortFields == null || sortFields.isEmpty()) {
            return orders;
        }

        for (String sortField : sortFields) {
            String[] parts = sortField.split(",");
            if (parts.length < 1) {
                continue;
            }

            String field = parts[0].trim();
            String direction = parts.length > 1 ? parts[1].trim().toLowerCase() : "asc";

            try {
                Path<?> path = getPath(root, field);
                if ("desc".equals(direction)) {
                    orders.add(cb.desc(path));
                } else {
                    orders.add(cb.asc(path));
                }
            } catch (Exception e) {
                System.err.println("Error creating order for field: " + field + ", error: " + e.getMessage());
            }
        }

        return orders;
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<MusicBand> root, Map<String, String> filters) {
        List<Predicate> predicates = new ArrayList<>();

        if (filters == null || filters.isEmpty()) {
            return predicates;
        }

        for (Map.Entry<String, String> entry : filters.entrySet()) {
            String filterExpression = entry.getValue();

            String[] parts = filterExpression.split(":", 3);
            if (parts.length != 3) {
                System.err.println("Invalid filter format: " + filterExpression + " (expected field:operator:value)");
                continue;
            }

            String field = parts[0].trim();
            String operator = parts[1].trim().toLowerCase();
            String value = parts[2].trim();

            try {
                Predicate predicate = createPredicate(cb, root, field, operator, value);
                if (predicate != null) {
                    predicates.add(predicate);
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Filter validation error: " + e.getMessage() + " (filter: " + filterExpression + ")");
                predicates.add(cb.disjunction());
                break;
            } catch (Exception e) {
                System.err.println("Error creating predicate for filter: " + filterExpression + ", error: " + e.getMessage());
                predicates.add(cb.disjunction());
                break;
            }
        }

        return predicates;
    }

    private Predicate createPredicate(CriteriaBuilder cb, Root<MusicBand> root,
                                      String field, String operator, String value) {

        Path<?> path = getPath(root, field);
        Class<?> fieldType = path.getJavaType();

        validateFilterValue(field, operator, value, fieldType);

        switch (operator) {
            case "eq":
                if (fieldType.isEnum()) {
                    Enum<?> enumValue = parseEnumValue(fieldType, value);
                    return cb.equal(path, enumValue);
                } else if (isNumericType(fieldType)) {
                    Number numValue = parseNumericValue(fieldType, value);
                    return cb.equal(path, numValue);
                } else if (fieldType == LocalDate.class) {
                    LocalDate dateValue = parseDate(value);
                    return cb.equal(path, dateValue);
                } else {
                    return cb.equal(path, value);
                }

            case "ne":
                if (fieldType.isEnum()) {
                    Enum<?> enumValue = parseEnumValue(fieldType, value);
                    return cb.notEqual(path, enumValue);
                } else if (isNumericType(fieldType)) {
                    Number numValue = parseNumericValue(fieldType, value);
                    return cb.notEqual(path, numValue);
                } else if (fieldType == LocalDate.class) {
                    LocalDate dateValue = parseDate(value);
                    return cb.notEqual(path, dateValue);
                } else {
                    return cb.notEqual(path, value);
                }

            case "gt":
                if (isNumericType(fieldType)) {
                    return createNumericComparison(cb, path, value, fieldType, "gt");
                } else if (fieldType == LocalDate.class) {
                    LocalDate dateValue = parseDate(value);
                    return cb.greaterThan(path.as(LocalDate.class), dateValue);
                } else {
                    throw new IllegalArgumentException("Operator 'gt' only works with numeric fields and dates. Field '" + field + "' is " + fieldType.getSimpleName());
                }

            case "gte":
                if (isNumericType(fieldType)) {
                    return createNumericComparison(cb, path, value, fieldType, "gte");
                } else if (fieldType == LocalDate.class) {
                    LocalDate dateValue = parseDate(value);
                    return cb.greaterThanOrEqualTo(path.as(LocalDate.class), dateValue);
                } else {
                    throw new IllegalArgumentException("Operator 'gte' only works with numeric fields and dates. Field '" + field + "' is " + fieldType.getSimpleName());
                }

            case "lt":
                if (isNumericType(fieldType)) {
                    return createNumericComparison(cb, path, value, fieldType, "lt");
                } else if (fieldType == LocalDate.class) {
                    LocalDate dateValue = parseDate(value);
                    return cb.lessThan(path.as(LocalDate.class), dateValue);
                } else {
                    throw new IllegalArgumentException("Operator 'lt' only works with numeric fields and dates. Field '" + field + "' is " + fieldType.getSimpleName());
                }

            case "lte":
                if (isNumericType(fieldType)) {
                    return createNumericComparison(cb, path, value, fieldType, "lte");
                } else if (fieldType == LocalDate.class) {
                    LocalDate dateValue = parseDate(value);
                    return cb.lessThanOrEqualTo(path.as(LocalDate.class), dateValue);
                } else {
                    throw new IllegalArgumentException("Operator 'lte' only works with numeric fields and dates. Field '" + field + "' is " + fieldType.getSimpleName());
                }

            case "contains":
                if (fieldType == String.class) {
                    return cb.like(cb.lower(path.as(String.class)), "%" + value.toLowerCase() + "%");
                } else {
                    throw new IllegalArgumentException("Operator 'contains' only works with string fields. Field '" + field + "' is " + fieldType.getSimpleName());
                }

            default:
                throw new IllegalArgumentException("Unknown operator: " + operator + ". Supported operators: eq, ne, gt, gte, lt, lte, contains");
        }
    }

    private Predicate createNumericComparison(CriteriaBuilder cb, Path<?> path, String value, Class<?> fieldType, String operator) {
        if (fieldType == Integer.class || fieldType == int.class) {
            Integer intValue = Integer.parseInt(value);
            switch (operator) {
                case "gt": return cb.gt(path.as(Integer.class), intValue);
                case "gte": return cb.ge(path.as(Integer.class), intValue);
                case "lt": return cb.lt(path.as(Integer.class), intValue);
                case "lte": return cb.le(path.as(Integer.class), intValue);
            }
        } else if (fieldType == Long.class || fieldType == long.class) {
            Long longValue = Long.parseLong(value);
            switch (operator) {
                case "gt": return cb.gt(path.as(Long.class), longValue);
                case "gte": return cb.ge(path.as(Long.class), longValue);
                case "lt": return cb.lt(path.as(Long.class), longValue);
                case "lte": return cb.le(path.as(Long.class), longValue);
            }
        } else if (fieldType == Double.class || fieldType == double.class) {
            Double doubleValue = Double.parseDouble(value);
            switch (operator) {
                case "gt": return cb.gt(path.as(Double.class), doubleValue);
                case "gte": return cb.ge(path.as(Double.class), doubleValue);
                case "lt": return cb.lt(path.as(Double.class), doubleValue);
                case "lte": return cb.le(path.as(Double.class), doubleValue);
            }
        } else if (fieldType == Float.class || fieldType == float.class) {
            Float floatValue = Float.parseFloat(value);
            switch (operator) {
                case "gt": return cb.gt(path.as(Float.class), floatValue);
                case "gte": return cb.ge(path.as(Float.class), floatValue);
                case "lt": return cb.lt(path.as(Float.class), floatValue);
                case "lte": return cb.le(path.as(Float.class), floatValue);
            }
        }
        throw new IllegalArgumentException("Unsupported numeric type: " + fieldType.getSimpleName());
    }

    private void validateFilterValue(String field, String operator, String value, Class<?> fieldType) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Filter value cannot be empty for field '" + field + "'");
        }

        if (fieldType.isEnum()) {
            try {
                parseEnumValue(fieldType, value);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid value '" + value + "' for enum field '" + field + "'. " + e.getMessage());
            }
        }

        if (isNumericType(fieldType)) {
            try {
                if (fieldType == Integer.class || fieldType == int.class ||
                        fieldType == Long.class || fieldType == long.class) {
                    if (value.contains(".")) {
                        throw new NumberFormatException("Integer field cannot accept decimal values");
                    }
                }
                parseNumericValue(fieldType, value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid numeric value '" + value + "' for field '" + field + "'. Expected " + fieldType.getSimpleName() + " (whole number for Integer fields)");
            }
        }

        if (fieldType == LocalDate.class) {
            try {
                parseDate(value);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date value '" + value + "' for field '" + field + "'. Expected format: YYYY-MM-DD");
            }
        }
    }

    private Path<?> getPath(Root<MusicBand> root, String field) {
        if (field.contains(".")) {
            String[] parts = field.split("\\.");
            Path<?> path = root;
            for (String part : parts) {
                path = path.get(part);
            }
            return path;
        } else {
            return root.get(field);
        }
    }

    private boolean isNumericType(Class<?> type) {
        return type == Integer.class || type == int.class ||
                type == Long.class || type == long.class ||
                type == Double.class || type == double.class ||
                type == Float.class || type == float.class;
    }

    private Number parseNumericValue(Class<?> type, String value) {
        try {
            if (type == Integer.class || type == int.class) {
                return Integer.parseInt(value);
            } else if (type == Long.class || type == long.class) {
                return Long.parseLong(value);
            } else if (type == Double.class || type == double.class) {
                return Double.parseDouble(value);
            } else if (type == Float.class || type == float.class) {
                return Float.parseFloat(value);
            }
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Cannot parse '" + value + "' as " + type.getSimpleName());
        }
        throw new IllegalArgumentException("Unsupported numeric type: " + type.getSimpleName());
    }

    @SuppressWarnings("unchecked")
    private Enum<?> parseEnumValue(Class<?> enumType, String value) {
        try {
            return Enum.valueOf((Class<Enum>) enumType, value);
        } catch (IllegalArgumentException e) {
            for (Object enumConstant : enumType.getEnumConstants()) {
                if (((Enum<?>) enumConstant).name().equalsIgnoreCase(value)) {
                    return (Enum<?>) enumConstant;
                }
            }

            StringBuilder availableValues = new StringBuilder();
            for (Object enumConstant : enumType.getEnumConstants()) {
                if (availableValues.length() > 0) {
                    availableValues.append(", ");
                }
                availableValues.append(((Enum<?>) enumConstant).name());
            }

            throw new IllegalArgumentException("Available values: " + availableValues.toString());
        }
    }

    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new DateTimeParseException("Invalid date format: " + value + ". Expected format: YYYY-MM-DD", value, 0);
        }
    }
}
