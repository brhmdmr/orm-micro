package org.ormmicro.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class GenericDAOTestDataUtil {


    public static void createData(Connection connection) throws SQLException {
        prepareUser(connection);
        prepareRole(connection);
    }

    private static void prepareUser(Connection connection) throws SQLException {
        String createQuery = "CREATE TABLE User (ID INTEGER identity primary key, USERNAME VARCHAR(100),  PASSWORD VARCHAR(100))";
        executeUpdate(connection, createQuery);
        String insertQuery = "INSERT INTO User (USERNAME,PASSWORD) VALUES (?, ?)";
        for (int i = 1; i <= 3; i++) {
            executeUpdate(connection, insertQuery, ("user" + i), ("pass" + i));
        }
    }


    private static void prepareRole(Connection connection) throws SQLException {
        String createQuery = "CREATE TABLE Role (ID INTEGER identity primary key, CODE VARCHAR(100),  NAME VARCHAR(100))";
        executeUpdate(connection, createQuery);
        String insertQuery = "INSERT  INTO Role (CODE,NAME) VALUES (?, ?)";
        for (int i = 1; i <= 3; i++) {
            executeUpdate(connection, insertQuery, ("code" + i), ("name" + i));
        }

    }

    private static void executeUpdate(Connection connection, String query, Object... params) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++)
                preparedStatement.setObject(i + 1, params[i]);
        }
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

}
