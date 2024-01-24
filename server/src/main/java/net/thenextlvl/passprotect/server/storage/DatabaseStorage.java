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
import java.util.UUID;

/**
 * The `DatabaseStorage` class represents a data storage system that uses an SQLite
 * database to store and retrieve `Account` objects.
 */
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
    public Account getAccount(UUID uuid) throws SQLException {
        return executeQuery("SELECT * FROM accounts WHERE uuid = ? LIMIT 1", this::transformRow, uuid.toString());
    }

    @Override
    public void createAccount(Account account) throws SQLException {
        executeUpdate("""
                        INSERT INTO accounts (
                            uuid, email,
                            password, salt, iterations,
                            token, token_validity
                        ) VALUES (?, ?, ?, ?, ?, ?, ?)""",
                account.uuid().toString(), account.email(),
                account.password(), account.salt(), account.iterations(),
                account.token(), account.tokenValidity()
        );
    }

    @Override
    public void deleteAccount(Account account) throws SQLException {
        executeUpdate("DELETE FROM accounts WHERE uuid = ?", account.uuid().toString());
    }

    @Override
    public void updateToken(Account account) throws SQLException {
        executeUpdate(
                "UPDATE accounts SET token = ?, token_validity = ? WHERE uuid = ?",
                account.token(), account.tokenValidity(), account.uuid().toString()
        );
    }

    @Override
    public void updatePassword(Account account) throws SQLException {
        executeUpdate("""
                        UPDATE accounts SET
                        password = ?, salt = ?, iterations = ?, token = ?, token_validity = ?
                        WHERE uuid = ?""",
                account.password(), account.salt(), account.iterations(),
                account.token(), account.tokenValidity(),
                account.uuid().toString()
        );
    }


    @Override
    public void updateEmail(Account account) throws SQLException {
        executeUpdate(
                "UPDATE accounts SET email = ? WHERE uuid = ?",
                account.email(), account.uuid().toString()
        );
    }

    private void migrateAccounts() throws SQLException {
        executeUpdate("""
                CREATE TABLE IF NOT EXISTS accounts (
                    uuid TEXT NOT NULL UNIQUE,
                    email TEXT NOT NULL UNIQUE,
                    password BLOB NOT NULL,
                    salt BLOB NOT NULL,
                    iterations INTEGER NOT NULL,
                    token TEXT NOT NULL UNIQUE,
                    token_validity DATE NOT NULL
                )""");
    }

    private Account transformRow(ResultSet resultSet) throws SQLException {
        return new Account(
                UUID.fromString(resultSet.getString("uuid")),
                resultSet.getString("email"),
                resultSet.getInt("iterations"),
                resultSet.getBytes("password"),
                resultSet.getBytes("salt"),
                resultSet.getString("token"),
                resultSet.getDate("token_validity")
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

    @FunctionalInterface
    private interface ThrowingFunction<T, R> {
        R apply(T t) throws SQLException;

        static <T, R> ThrowingFunction<T, R> unchecked(ThrowingFunction<T, R> f) {
            return f;
        }
    }
}
