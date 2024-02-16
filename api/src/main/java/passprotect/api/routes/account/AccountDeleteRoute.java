package passprotect.api.routes.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import passprotect.api.Server;
import spark.Request;
import spark.Response;
import spark.Spark;

import static passprotect.api.routes.RouteHelper.*;

public class AccountDeleteRoute {
    private static final Logger logger = LoggerFactory.getLogger(AccountDeleteRoute.class);

    public static void register() {
        options("/account/delete", "DELETE");
        Spark.delete("/account/delete", AccountDeleteRoute::deleteAccount);
    }

    private static Object deleteAccount(Request request, Response response) {
        return fetchAuthorization(request, response, account -> {
            try {
                Server.STORAGE.deleteAccount(account);
                logger.info("Deleted account {}", account.email());
                return respond(response, 200, "Successfully deleted account");
            } catch (Exception e) {
                logger.error("Failed to delete account", e);
                return respond(response, 500, "Something went wrong during account deletion");
            }
        });
    }
}
