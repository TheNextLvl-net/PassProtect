package net.thenextlvl.passprotect.server.storage;

import net.thenextlvl.passprotect.server.model.Account;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * DataStorage interface represents a data storage system that stores and retrieves Account objects.
 */
public interface DataStorage {
    /**
     * Retrieves a list of accounts from the data storage.
     *
     * @return the list of accounts
     * @throws SQLException if an SQL error occurs during retrieval
     */
    List<Account> getAccounts() throws SQLException;

    /**
     * Retrieves the account associated with the given email.
     *
     * @param email the email address of the account
     * @return the Account object associated with the email
     * @throws SQLException if an SQL error occurs during retrieval
     */
    Account getAccount(String email) throws SQLException;

    /**
     * Retrieves the account associated with the given uuid.
     *
     * @param uuid the uuid of the account
     * @return the Account object associated with the uuid
     * @throws SQLException if an SQL error occurs during retrieval
     */
    Account getAccount(UUID uuid) throws SQLException;

    /**
     * Creates a new account and stores it in the data storage.
     *
     * @param account the Account object representing the account to create
     * @throws SQLException if an SQL error occurs during account creation
     */
    void createAccount(Account account) throws SQLException;

    /**
     * Deletes the specified account from the data storage.
     *
     * @param account the Account object representing the account to delete
     * @throws SQLException if an SQL error occurs during deletion
     */
    void deleteAccount(Account account) throws SQLException;

    /**
     * Updates the access token of an account.
     *
     * @param account the Account object representing the account to update
     * @throws SQLException if an SQL error occurs during the update
     */
    void updateToken(Account account) throws SQLException;

    /**
     * Updates the password for the provided account.
     *
     * @param account the Account object representing the account to update
     * @throws SQLException if an SQL error occurs during the update
     */
    void updatePassword(Account account) throws SQLException;

    /**
     * Updates the email address of the provided account.
     *
     * @param account the Account object representing the account to update
     * @throws SQLException if an SQL error occurs during the update
     */
    void updateEmail(Account account) throws SQLException;
}
