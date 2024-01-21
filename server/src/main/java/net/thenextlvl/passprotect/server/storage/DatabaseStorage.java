package net.thenextlvl.passprotect.server.storage;

import net.thenextlvl.passprotect.server.Server;
import net.thenextlvl.passprotect.server.model.Account;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseStorage implements DataStorage {
    private final Connection connection;

    public DatabaseStorage() {
        try {
            var url = "jdbc:sqlite:" + new File(Server.DATA_FOLDER, "database.db");
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(url);
            migrateAccounts();
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to initialize Database", e);
        }
    }

    @Override
    public List<Account> getAccounts() throws SQLException {
        return executeQuery("SELECT * FROM accounts", this::transformRows);
    }

    @Override
    public Account getAccount(String email) throws SQLException {
        return executeQuery("SELECT * FROM accounts WHERE email = ? LIMIT 1", this::transformRow, email);
    }

    @Override
    public void createAccount(Account account) throws SQLException {
        executeUpdate(
                "INSERT INTO accounts (email, password, salt) VALUES (?, ?, ?)",
                account.email(), account.password(), account.salt()
        );
    }

    @Override
    public void deleteAccount(String email) throws SQLException {
        executeUpdate("DELETE FROM accounts WHERE email = ?", email);
    }

    private void migrateAccounts() throws SQLException {
        executeUpdate("""
                CREATE TABLE IF NOT EXISTS accounts (
                    email TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    salt TEXT NOT NULL
                )""");
    }

    private Account transformRow(ResultSet resultSet) throws SQLException {
        return new Account(
                resultSet.getString("email"),
                resultSet.getString("password"),
                resultSet.getString("salt")
        );
    }

    private List<Account> transformRows(ResultSet resultSet) throws SQLException {
        var schematics = new ArrayList<Account>();
        while (resultSet.next()) schematics.add(transformRow(resultSet));
        return schematics;
    }

    private <T> T executeQuery(String query, ThrowingFunction<ResultSet, T> mapper, Object... parameters) throws SQLException {
        try (var preparedStatement = connection.prepareStatement(query)) {
            for (var i = 0; i < parameters.length; i++)
                preparedStatement.setObject(i + 1, parameters[i]);
            try (var resultSet = preparedStatement.executeQuery()) {
                return ThrowingFunction.unchecked(mapper).apply(resultSet);
            }
        }
    }

    private void executeUpdate(String query, Object... parameters) throws SQLException {
        try (var preparedStatement = connection.prepareStatement(query)) {
            for (var i = 0; i < parameters.length; i++)
                preparedStatement.setObject(i + 1, parameters[i]);
            preparedStatement.executeUpdate();
        }
    }

    private interface ThrowingFunction<T, R> {
        R apply(T t) throws SQLException;

        static <T, R> ThrowingFunction<T, R> unchecked(ThrowingFunction<T, R> f) {
            return f;
        }
    }
}
