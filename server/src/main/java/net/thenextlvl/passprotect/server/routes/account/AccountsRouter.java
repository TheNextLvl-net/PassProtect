package net.thenextlvl.passprotect.server.routes.account;

import com.google.gson.JsonArray;
import net.thenextlvl.passprotect.server.Server;
import net.thenextlvl.passprotect.server.model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Spark;

import static net.thenextlvl.passprotect.server.routes.account.AccountRouter.options;
import static net.thenextlvl.passprotect.server.routes.account.AccountRouter.respond;

public class AccountsRouter {
    private static final Logger logger = LoggerFactory.getLogger(AccountsRouter.class);

    public static void register() {
        options("/accounts", "GET");
        Spark.get("/accounts", AccountsRouter::getAccounts);
    }

    private static Object getAccounts(Request request, Response response) {
        try {
            var array = new JsonArray();
            Server.STORAGE.getAccounts().stream()
                    .map(Account::email)
                    .forEach(array::add);
            response.type("text/html");
            return respond(response, 200, array);
        } catch (Exception e) {
            logger.error("Failed to list accounts", e);
            return respond(response, 500, "Something went wrong during accounts request");
        }
    }
}
