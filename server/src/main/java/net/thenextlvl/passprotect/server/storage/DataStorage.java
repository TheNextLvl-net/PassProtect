package net.thenextlvl.passprotect.server.storage;

import net.thenextlvl.passprotect.server.model.Account;

import java.sql.SQLException;
import java.util.List;

public interface DataStorage {
    List<Account> getAccounts() throws SQLException;

    Account getAccount(String email) throws SQLException;

    void createAccount(Account account) throws SQLException;

    void deleteAccount(String email) throws SQLException;

    void updatePassword(Account account) throws SQLException;
}
