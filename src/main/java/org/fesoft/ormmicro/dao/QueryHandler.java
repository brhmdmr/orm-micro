package org.fesoft.ormmicro.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class QueryHandler<T> {


    public List<T> readAll(Connection connection, String query, Class<T> entityClass, Object... params) throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement(query);
        setParameters(pstmt, params);
        ResultSet rs = pstmt.executeQuery();
        ListHandler<T> listHandler = new ListHandler<>();
        List<T> entityList = listHandler.readList(rs, entityClass);
        rs.close();
        pstmt.close();
        return entityList;
    }

    private void setParameters(PreparedStatement pstmt, Object[] params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
        }

    }


    public List<T> readOne(Connection connection, String query, Class<T> userClass) {
        return null;
    }
}
