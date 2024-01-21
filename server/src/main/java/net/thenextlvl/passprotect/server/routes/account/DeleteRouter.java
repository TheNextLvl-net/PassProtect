package net.thenextlvl.passprotect.server.routes.account;

import net.thenextlvl.passprotect.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Spark;

import static net.thenextlvl.passprotect.server.routes.account.AccountRouter.*;

public class DeleteRouter {
    private static final Logger logger = LoggerFactory.getLogger(DeleteRouter.class);

    public static void register() {
        options("/account/delete", "HEAD, DELETE");
        Spark.head("/account/delete", (request, response) -> {
            var email = request.queryParams("email");
            if (email == null) return respond(response, 400, "Email cannot be null");
            var account = fetchAccount(email);
            return respond(response, account != null ? 200 : 404, account != null ?
                    "Account does exist" : "Account does not exist");
        });
        Spark.delete("/account/delete", DeleteRouter::deleteAccount);
    }

    private static Object deleteAccount(Request request, Response response) {
        var email = request.queryParams("email");
        if (email == null) return respond(response, 400, "Email cannot be null");
        if (fetchAccount(email) == null) {
            logger.error("Tried to delete non existing account: {}", email);
            return respond(response, 404, "Account does not exist");
        }
        try {
            Server.STORAGE.deleteAccount(email);
            logger.info("Deleted account {}", email);
            return respond(response, 200, "Successfully deleted account");
        } catch (Exception e) {
            logger.error("Failed to delete account", e);
            return respond(response, 500, "Something went wrong during account deletion");
        }
    }
}
