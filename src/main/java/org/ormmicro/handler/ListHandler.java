package org.ormmicro.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListHandler<T> {


    public List<T> readList(ResultSet rs, Class<T> entityClass) throws SQLException {
        return readList(rs, entityClass, null);
    }

    public List<T> readList(ResultSet rs, Class<T> entityClass, Map<String, String> columnMappings) throws SQLException {
        List<T> entityList = new ArrayList<>();
        RowHandler<T> rowHandler = new RowHandler<>();
        while (rs.next()) {
            entityList.add(rowHandler.readEntity(rs, entityClass, columnMappings));
        }
        return entityList;
    }


}
