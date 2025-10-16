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


@ApplicationScoped
@Transactional
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


    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<MusicBand> root, Map<String, String> filters) {
        List<Predicate> predicates = new ArrayList<>();

        if (filters == null || filters.isEmpty()) {
            return predicates;
        }

        for (Map.Entry<String, String> entry : filters.entrySet()) {
            String filterExpression = entry.getValue();

            // Разбираем фильтр: field:operator:value
            String[] parts = filterExpression.split(":", 3);
            if (parts.length != 3) {
                continue; // Пропускаем невалидные фильтры
            }

            String field = parts[0].trim();
            String operator = parts[1].trim().toLowerCase();
            String value = parts[2].trim();

            try {
                Predicate predicate = createPredicate(cb, root, field, operator, value);
                if (predicate != null) {
                    predicates.add(predicate);
                }
            } catch (Exception e) {
                // Логируем и пропускаем проблемные фильтры
                System.err.println("Error creating predicate for filter: " + filterExpression + ", error: " + e.getMessage());
            }
        }

        return predicates;
    }


    private Predicate createPredicate(CriteriaBuilder cb, Root<MusicBand> root,
                                      String field, String operator, String value) {

        // Получаем путь к полю (поддержка вложенных полей через точку)
        Path<?> path = getPath(root, field);
        Class<?> fieldType = path.getJavaType();

        switch (operator) {
            case "eq": // Equals
                if (fieldType.isEnum()) {
                    // Для enum делаем точное совпадение с учетом регистра
                    return cb.equal(path, parseEnumValue(fieldType, value));
                } else if (isNumericType(fieldType)) {
                    return cb.equal(path, parseNumericValue(fieldType, value));
                } else {
                    // Для строк - точное совпадение
                    return cb.equal(path, value);
                }

            case "ne": // Not equals
                if (fieldType.isEnum()) {
                    return cb.notEqual(path, parseEnumValue(fieldType, value));
                } else if (isNumericType(fieldType)) {
                    return cb.notEqual(path, parseNumericValue(fieldType, value));
                } else {
                    return cb.notEqual(path, value);
                }

            case "gt": // Greater than
                if (!isNumericType(fieldType)) {
                    throw new IllegalArgumentException("Operator 'gt' only works with numeric fields");
                }
                return cb.gt(path.as(Number.class), parseNumericValue(fieldType, value));

            case "gte": // Greater than or equal
                if (!isNumericType(fieldType)) {
                    throw new IllegalArgumentException("Operator 'gte' only works with numeric fields");
                }
                return cb.ge(path.as(Number.class), parseNumericValue(fieldType, value));

            case "lt": // Less than
                if (!isNumericType(fieldType)) {
                    throw new IllegalArgumentException("Operator 'lt' only works with numeric fields");
                }
                return cb.lt(path.as(Number.class), parseNumericValue(fieldType, value));

            case "lte": // Less than or equal
                if (!isNumericType(fieldType)) {
                    throw new IllegalArgumentException("Operator 'lte' only works with numeric fields");
                }
                return cb.le(path.as(Number.class), parseNumericValue(fieldType, value));

            case "contains": // Contains substring (case-insensitive)
                if (fieldType != String.class) {
                    throw new IllegalArgumentException("Operator 'contains' only works with string fields");
                }
                return cb.like(cb.lower(path.as(String.class)), "%" + value.toLowerCase() + "%");

            default:
                System.err.println("Unknown operator: " + operator);
                return null;
        }
    }

    // Вспомогательный метод для получения пути к полю (поддержка вложенных полей)
    private Path<?> getPath(Root<MusicBand> root, String field) {
        if (field.contains(".")) {
            // Вложенное поле, например: coordinates.x или label.sales
            String[] parts = field.split("\\.", 2);
            return root.get(parts[0]).get(parts[1]);
        } else {
            // Простое поле
            return root.get(field);
        }
    }

    private boolean isNumericType(Class<?> type) {
        return Number.class.isAssignableFrom(type)
                || type == int.class
                || type == long.class
                || type == double.class
                || type == float.class
                || type == short.class
                || type == byte.class;
    }

    // Парсинг числового значения
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
            } else {
                return Double.parseDouble(value); // По умолчанию
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric value: " + value + " for type: " + type.getSimpleName());
        }
    }

    // Парсинг enum значения
    private Object parseEnumValue(Class<?> enumType, String value) {
        try {
            @SuppressWarnings("unchecked")
            Class<? extends Enum> enumClass = (Class<? extends Enum>) enumType;
            // Пытаемся найти точное совпадение с учетом регистра
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid enum value: '" + value + "' for type: " + enumType.getSimpleName()
                    + ". Valid values are: " + java.util.Arrays.toString(enumType.getEnumConstants()));
        }
    }


    private Object parseValue(Class<?> type, String value) {
        if (type == Integer.class || type == int.class) {
            return Integer.parseInt(value);
        } else if (type == Long.class || type == long.class) {
            return Long.parseLong(value);
        } else if (type == Double.class || type == double.class) {
            return Double.parseDouble(value);
        } else if (type.isEnum()) {
            try {
                @SuppressWarnings("unchecked")
                Class<? extends Enum> enumClass = (Class<? extends Enum>) type;
                return Enum.valueOf(enumClass, value.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid enum value: " + value + " for type: " + type.getSimpleName());
            }
        }
        return value;
    }

    
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