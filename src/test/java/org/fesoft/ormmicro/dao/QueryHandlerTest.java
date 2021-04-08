package org.fesoft.ormmicro.dao;

import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class QueryHandlerTest {


    @Test
    public void readAll() throws SQLException {
        List<User> expectedUserList = getExpectedUserList();
        String query = "SELECT * FROM User";
        QueryHandler<User> queryHandler = new QueryHandler<>();
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSetMetaData rsmd = mockResultSetMetaData();
        ResultSet rs = mockResultSet();
        when(connection.prepareStatement(query)).thenReturn(preparedStatement);
        when(rs.getMetaData()).thenReturn(rsmd);
        when(preparedStatement.executeQuery()).thenReturn(rs);
        List<User> users = queryHandler.readAll(connection, query, User.class);
        assertEquals(expectedUserList, users);
    }

    @Test
    public void readAllWithParameters() throws SQLException {
        String query = "SELECT * FROM User WHERE username = ? and password = ?";
        QueryHandler<User> queryHandler = new QueryHandler<>();
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSetMetaData rsmd = mockResultSetMetaData();
        ResultSet rs = mockResultSet();
        when(connection.prepareStatement(query)).thenReturn(preparedStatement);
        when(rs.getMetaData()).thenReturn(rsmd);
        when(preparedStatement.executeQuery()).thenReturn(rs);
        queryHandler.readAll(connection, query, User.class, "user1", "pass1");
        verify(preparedStatement).setObject(1, "user1");
        verify(preparedStatement).setObject(2, "pass1");
    }

/*
    @Test
    public void readOne() throws SQLException {
        User user = User.builder().id(1).username("user1").password("pass1").build();

        String query = "SELECT * FROM User";
        QueryHandler<User> queryHandler = new QueryHandler<User>();
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSetMetaData rsmd = mockResultSetMetaData();
        ResultSet rs = mockResultSet();
        when(connection.prepareStatement(query)).thenReturn(preparedStatement);
        when(rs.getMetaData()).thenReturn(rsmd);
        when(preparedStatement.executeQuery()).thenReturn(rs);
        List<User> users = queryHandler.readOne(connection, query, User.class);
        assertEquals(user, users);
    }*/


    private List<User> getExpectedUserList() {
        List<User> list = new ArrayList<>();
        list.add(User.builder().id(1).username("user1").password("pass1").build());
        list.add(User.builder().id(2).username("user2").password("pass2").build());
        list.add(User.builder().id(3).username("user3").password("pass3").build());
        return list;
    }

    private ResultSetMetaData mockResultSetMetaData() throws SQLException {
        ResultSetMetaData rsmd = mock(ResultSetMetaData.class);
        when(rsmd.getColumnCount()).thenReturn(3);
        when(rsmd.getColumnName(0)).thenReturn("id");
        when(rsmd.getColumnName(1)).thenReturn("username");
        when(rsmd.getColumnName(2)).thenReturn("password");
        return rsmd;
    }

    private ResultSet mockResultSet() throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
        when(rs.getObject("id")).thenReturn(1).thenReturn(2).thenReturn(3);
        when(rs.getObject("username")).thenReturn("user1").thenReturn("user2").thenReturn("user3");
        when(rs.getObject("password")).thenReturn("pass1").thenReturn("pass2").thenReturn("pass3");
        return rs;
    }

}
