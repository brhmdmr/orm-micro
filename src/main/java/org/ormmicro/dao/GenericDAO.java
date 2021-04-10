package org.ormmicro.dao;

import lombok.SneakyThrows;
import org.ormmicro.annotation.Column;
import org.ormmicro.annotation.Id;
import org.ormmicro.handler.ListHandler;
import org.ormmicro.handler.RowHandler;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class GenericDAO<T, K> {

    private final Connection connection;
    private final Class<T> entityClass;
    private LinkedHashMap<String, String> columnMappings;

    public GenericDAO(Connection connection, Class<T> entityClass) {
        this.connection = connection;
        this.entityClass = entityClass;
        createColumnMappings();
    }


    @SneakyThrows
    public List<T> findAll() {
        String query = findAllQuery();
        PreparedStatement preparedStatement = createPreparedStatement(query);
        ResultSet rs = preparedStatement.executeQuery();
        ListHandler<T> listHandler = new ListHandler<>();
        return listHandler.readList(rs, entityClass, columnMappings);
    }

    @SneakyThrows
    public T findById(K id) {
        String query = findByIdQuery();
        PreparedStatement preparedStatement = createPreparedStatement(query, id);
        ResultSet rs = preparedStatement.executeQuery();
        if (rs.next()) {
            RowHandler<T> rowHandler = new RowHandler<>();
            return rowHandler.readEntity(rs, entityClass, columnMappings);
        }
        return null;
    }

    @SneakyThrows
    public void update(T entity) {
        String query = updateQuery();
        Object[] values = updateValues(entity);
        PreparedStatement preparedStatement = createPreparedStatement(query, values);
        preparedStatement.executeUpdate();
    }

    @SneakyThrows
    public void insert(T entity) {
        String query = insertQuery();
        Object[] values = insertValues(entity);
        PreparedStatement preparedStatement = createPreparedStatement(query, values);
        preparedStatement.executeUpdate();
    }

    @SneakyThrows
    public void delete(K id) {
        String query = deleteQuery();
        PreparedStatement preparedStatement = createPreparedStatement(query, id);
        preparedStatement.executeUpdate();
    }

    /**
     * Private methods will be extracted to helper class
     */


    private void createColumnMappings() {
        columnMappings = new LinkedHashMap<>();
        for (Field field : entityClass.getDeclaredFields()) {
            if (!field.getName().equalsIgnoreCase("this$0")) {
                columnMappings.put(getColumnName(field).toUpperCase(Locale.ENGLISH), field.getName());
            }
        }
    }

    private String getColumnName(Field field) {
        String columnName = field.getName();
        if (field.isAnnotationPresent(Column.class)
                && annotatedColumnNameIsNotEmpty(field)) {
            columnName = field.getAnnotation(Column.class).name();
        }
        return columnName;
    }

    private boolean annotatedColumnNameIsNotEmpty(Field field) {
        return !field.getAnnotation(Column.class).name().trim().isEmpty();
    }

    private String findAllQuery() {
        String fields = String.join(", ", getColumnNames());
        return String.format("SELECT %s FROM %s", fields, entityClass.getSimpleName());
    }

    private String findByIdQuery() {
        String fields = String.join(", ", getColumnNames());
        String idField = getIdFieldName();
        return String.format("SELECT %s FROM %s WHERE %s = ?", fields, entityClass.getSimpleName(), idField);
    }

    private String updateQuery() {
        String[] fieldNames = getColumnNames();
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
        List<String> columnNames = Arrays.asList(getColumnNames());

        String idFieldName = getIdFieldName();
        if (isIdAutoIncrement()) {
            columnNames = columnNames.stream().filter(f -> !f.equalsIgnoreCase(idFieldName)).collect(Collectors.toList());
            columnNames.add(idFieldName.toUpperCase(Locale.ENGLISH));
        }

        for (String columnName : columnNames) {
            String fieldName = columnMappings.get(columnName);
            Field field = entityClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            values.add(field.get(entity));
        }
        return values.toArray();
    }

    private String insertQuery() {
        String[] fieldNames = getColumnNames();

        if (isIdAutoIncrement())
            fieldNames = Arrays.stream(fieldNames)
                    .filter(fn -> !fn.equalsIgnoreCase(getIdFieldName()))
                    .toArray(String[]::new);
        String[] qmList = new String[fieldNames.length];
        for (int i = 0; i < fieldNames.length; i++)
            qmList[i] = "?";

        return String.format("INSERT INTO %s (%s) VALUES(%s)"
                , entityClass.getSimpleName()
                , String.join(", ", fieldNames)
                , String.join(" ,", qmList));
    }

    @SneakyThrows
    private Object[] insertValues(T entity) {
        List<Object> values = new ArrayList<>();
        List<String> columnNames = Arrays.asList(getColumnNames());

        String idFieldName = getIdFieldName();
        if (isIdAutoIncrement())
            columnNames = columnNames.stream().filter(f -> !f.equalsIgnoreCase(idFieldName)).collect(Collectors.toList());
        for (String columnName : columnNames) {
            String fieldName = columnMappings.get(columnName);
            Field field = entityClass.getDeclaredField(fieldName.toLowerCase(Locale.ENGLISH));
            field.setAccessible(true);
            values.add(field.get(entity));
        }
        return values.toArray();
    }

    private String deleteQuery() {
        String idField = getIdFieldName();
        return String.format("DELETE FROM %s WHERE %s = ?", entityClass.getSimpleName(), idField);
    }

    private String getIdFieldName() {
        return Objects.requireNonNull(Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.getAnnotation(Id.class) != null)
                .findAny().orElse(null)).getName();
    }

    @SneakyThrows
    private boolean isIdAutoIncrement() {
        return entityClass
                .getDeclaredField(getIdFieldName())
                .getAnnotation(Id.class)
                .autoIncrement();
    }

    private String[] getColumnNames() {
        return columnMappings.keySet().toArray(new String[0]);
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
