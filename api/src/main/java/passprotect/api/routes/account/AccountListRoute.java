package passprotect.api.routes.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import passprotect.api.Server;
import passprotect.api.model.Account;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.util.stream.Collectors;

import static passprotect.api.routes.RouteHelper.options;
import static passprotect.api.routes.RouteHelper.respond;

public class AccountListRoute {
    private static final Logger logger = LoggerFactory.getLogger(AccountListRoute.class);

    public static void register() {
        options("/account/list", "GET");
        Spark.get("/account/list", AccountListRoute::listAccounts);
    }

    private static Object listAccounts(Request request, Response response) {
        try {
            var accounts = Server.STORAGE.getAccounts().stream()
                    .map(Account::email)
                    .collect(Collectors.toSet());
            return respond(response, 200, accounts);
        } catch (Exception e) {
            logger.error("Failed to list accounts", e);
            return respond(response, 500, "Something went wrong during accounts request");
        }
    }
}
