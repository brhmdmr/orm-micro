package org.ormmicro.handler;

import org.fesoft.handler.ListHandler;
import org.junit.jupiter.api.Test;
import org.ormmicro.entity.User;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ListHandlerTest {

    @Test
    public void read() throws SQLException {
        List<User> userList = createExpectedUserList();
        ResultSetMetaData rsmd = mockResultSetMetaData();
        ResultSet rs = mockResultSet();
        when(rs.getMetaData()).thenReturn(rsmd);
        ListHandler<User> listHandler = new ListHandler<>();
        List<User> dbUserList = listHandler.readList(rs, User.class);
        assertEquals(userList, dbUserList);

    }

    private List<User> createExpectedUserList() {
        List<User> userList = new ArrayList<>();
        userList.add(User.builder().id(1).username("user1").password("pass1").build());
        userList.add(User.builder().id(2).username("user2").password("pass2").build());
        userList.add(User.builder().id(3).username("user3").password("pass3").build());
        return userList;
    }

    private ResultSet mockResultSet() throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
        when(rs.getObject("id")).thenReturn(1).thenReturn(2).thenReturn(3);
        when(rs.getObject("username")).thenReturn("user1").thenReturn("user2").thenReturn("user3");
        when(rs.getObject("password")).thenReturn("pass1").thenReturn("pass2").thenReturn("pass3");
        return rs;
    }

    private ResultSetMetaData mockResultSetMetaData() throws SQLException {
        ResultSetMetaData rsmd = mock(ResultSetMetaData.class);
        when(rsmd.getColumnCount()).thenReturn(3);
        when(rsmd.getColumnName(1)).thenReturn("id");
        when(rsmd.getColumnName(2)).thenReturn("username");
        when(rsmd.getColumnName(3)).thenReturn("password");
        return rsmd;
    }
}
