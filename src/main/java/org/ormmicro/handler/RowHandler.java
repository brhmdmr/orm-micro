package org.ormmicro.handler;

import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RowHandler<T> {


    @SneakyThrows
    private Map<String, Object> readMap(ResultSet rs) {
        Map<String, Object> row = new HashMap<>();
        ResultSetMetaData rsmd = rs.getMetaData();
        for (int i = 0; i < rsmd.getColumnCount(); i++) {
            row.put(rsmd.getColumnName(i + 1), rs.getObject(rsmd.getColumnName(i + 1)));
        }
        return row;
    }

    @SneakyThrows
    public T readEntity(ResultSet rs, Class<T> clazz) {
        return readEntity(rs, clazz, null);

    }


    @SneakyThrows
    public T readEntity(ResultSet rs, Class<T> clazz, Map<String, String> columnMappings) {
        T entity = clazz.getConstructor().newInstance();
        Map<String, Object> row = readMap(rs);
        if (columnMappings == null)
            columnMappings = createColumnMappings(row);
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            String fieldName = columnMappings.get(entry.getKey());
            Field field = clazz.getDeclaredField(fieldName.toLowerCase(Locale.ENGLISH));
            field.setAccessible(true);
            field.set(entity, entry.getValue());
        }
        return entity;
    }

    private Map<String, String> createColumnMappings(Map<String, Object> row) {
        Map<String, String> columnMappings = new HashMap<>();
        for (String key : row.keySet())
            columnMappings.put(key, key);
        return columnMappings;
    }


}
