package org.ormmicro.dao;

import lombok.SneakyThrows;
import org.ormmicro.annotation.Id;
import org.ormmicro.handler.ListHandler;
import org.ormmicro.handler.RowHandler;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class GenericDAO<T, K> {

    private final Connection connection;
    private final Class<T> entityClass;

    public GenericDAO(Connection connection, Class<T> entityClass) {
        this.connection = connection;
        this.entityClass = entityClass;
    }

    @SneakyThrows
    public List<T> findAll() {
        String query = findAllQuery();
        PreparedStatement preparedStatement = createPreparedStatement(query);
        ResultSet rs = preparedStatement.executeQuery();
        ListHandler<T> listHandler = new ListHandler<>();
        return listHandler.readList(rs, entityClass);
    }

    @SneakyThrows
    public T findById(K id) {
        String query = findByIdQuery();
        System.out.println(query);
        PreparedStatement preparedStatement = createPreparedStatement(query, 1);
        ResultSet rs = preparedStatement.executeQuery();
        if (rs.next()) {
            RowHandler<T> rowHandler = new RowHandler<>();
            return rowHandler.readEntity(rs, entityClass);
        }
        return null;
    }

    @SneakyThrows
    public void update(T entity) {
        String query = updateQuery();
        System.out.println(query);
        Object[] values = updateValues(entity);
        PreparedStatement preparedStatement = createPreparedStatement(query, values);
        preparedStatement.executeUpdate();
    }

    /**
     * Private methods will be extracted to helper class
     */

    private String findAllQuery() {
        String fields = String.join(", ", getFieldNames());
        return String.format("SELECT %s FROM %s", fields, entityClass.getSimpleName());
    }

    private String findByIdQuery() {
        String fields = String.join(", ", getFieldNames());
        String idField = getIdFieldName();
        return String.format("SELECT %s FROM %s WHERE %s = ?", fields, entityClass.getSimpleName(), idField);
    }

    private String updateQuery() {
        String[] fieldNames = getFieldNames();
        String idFieldName = getIdFieldName();
        fieldNames = Arrays.stream(fieldNames)
                .filter(fn -> !fn.equalsIgnoreCase(idFieldName))
                .toArray(String[]::new);
        for (int i = 0; i < fieldNames.length; i++) {
            fieldNames[i] = fieldNames[i] + " = ?";
        }

        return String.format("UPDATE %s SET %s WHERE %s = ?"
                , entityClass.getSimpleName()
                , String.join(", ", fieldNames)
                , idFieldName);
    }

    @SneakyThrows
    private Object[] updateValues(T entity) {
        List<Object> values = new ArrayList<>();
        List<String> fieldNames = Arrays.asList(getFieldNames());
        String idFieldName = getIdFieldName();
        fieldNames = fieldNames.stream().filter(f -> !f.equalsIgnoreCase(idFieldName)).collect(Collectors.toList());
        fieldNames.add(idFieldName);
        for (String fieldName : fieldNames) {
            Field field = entityClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            values.add(field.get(entity));
        }
        return values.toArray();
    }


    private String getIdFieldName() {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.getAnnotation(Id.class) != null)
                .findFirst().get().getName();
    }

    private String[] getFieldNames() {
        return propertiesToFieldMappings().keySet().toArray(new String[0]);
    }

    private LinkedHashMap<String, String> propertiesToFieldMappings() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        Arrays.stream(entityClass.getDeclaredFields())
                .forEach(field -> map.put(field.getName(), field.getName()));
        return map;
    }

    private PreparedStatement createPreparedStatement(String query, Object... params) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }
        }
        return preparedStatement;
    }

}
