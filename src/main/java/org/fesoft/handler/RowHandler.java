package org.fesoft.handler;

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
        T entity = clazz.getConstructor().newInstance();
        Map<String, Object> row = readMap(rs);
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            Field field = clazz.getDeclaredField(entry.getKey().toLowerCase(Locale.ENGLISH));
            field.setAccessible(true);
            field.set(entity, entry.getValue());
        }
        return entity;
    }

}
