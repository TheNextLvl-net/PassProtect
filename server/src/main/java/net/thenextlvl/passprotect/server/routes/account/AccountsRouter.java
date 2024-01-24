package net.thenextlvl.passprotect.server.routes.account;

import net.thenextlvl.passprotect.server.Server;
import net.thenextlvl.passprotect.server.model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Spark;

import static net.thenextlvl.passprotect.server.routes.RouteHelper.options;
import static net.thenextlvl.passprotect.server.routes.RouteHelper.respond;

public class AccountsRouter {
    private static final Logger logger = LoggerFactory.getLogger(AccountsRouter.class);

    public static void register() {
        options("/accounts", "GET");
        Spark.get("/accounts", AccountsRouter::getAccounts);
    }

    private static Object getAccounts(Request request, Response response) {
        try {
            var accounts = Server.STORAGE.getAccounts().stream()
                    .map(Account::email)
                    //.map(Account::uuid)
                    //.map(UUID::toString)
                    .toList();
            return respond(response, 200, accounts);
        } catch (Exception e) {
            logger.error("Failed to list accounts", e);
            return respond(response, 500, "Something went wrong during accounts request");
        }
    }
}
