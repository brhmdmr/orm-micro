package org.ormmicro.handler;

import org.junit.jupiter.api.Test;
import org.ormmicro.entity.User;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RowHandlerTest {


    @Test
    public void readRowToEntity() throws SQLException {
        User user = User.builder().id(1).username("user1").password("pass1").build();
        ResultSetMetaData rsmd = mock(ResultSetMetaData.class);
        ResultSet rs = mock(ResultSet.class);
        when(rs.getMetaData()).thenReturn(rsmd);
        when(rsmd.getColumnCount()).thenReturn(3);
        when(rsmd.getColumnName(1)).thenReturn("id");
        when(rsmd.getColumnName(2)).thenReturn("username");
        when(rsmd.getColumnName(3)).thenReturn("password");
        when(rs.getObject("id")).thenReturn(1);
        when(rs.getObject("username")).thenReturn("user1");
        when(rs.getObject("password")).thenReturn("pass1");
        RowHandler<User> rowHandler = new RowHandler<>();
        User dbUser = rowHandler.readEntity(rs, User.class);
        assertEquals(user, dbUser);
    }


    @Test
    public void readRowToEntityWithColumnName() throws SQLException {
        User user = User.builder().id(1).username("user1").password("pass1").build();
        ResultSetMetaData rsmd = mock(ResultSetMetaData.class);
        ResultSet rs = mock(ResultSet.class);
        when(rs.getMetaData()).thenReturn(rsmd);
        when(rsmd.getColumnCount()).thenReturn(3);
        when(rsmd.getColumnName(1)).thenReturn("id");
        when(rsmd.getColumnName(2)).thenReturn("username");
        when(rsmd.getColumnName(3)).thenReturn("pwd");
        when(rs.getObject("id")).thenReturn(1);
        when(rs.getObject("username")).thenReturn("user1");
        when(rs.getObject("pwd")).thenReturn("pass1");
        Map<String, String> columnMappings = new HashMap<>();
        columnMappings.put("id", "id");
        columnMappings.put("username", "username");
        columnMappings.put("pwd", "password");
        RowHandler<User> rowHandler = new RowHandler<>();
        User dbUser = rowHandler.readEntity(rs, User.class, columnMappings);
        assertEquals(user, dbUser);
    }
}

