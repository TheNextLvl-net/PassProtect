package net.thenextlvl.passprotect.server.routes.account;

import net.thenextlvl.passprotect.server.Server;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.util.UUID;

import static net.thenextlvl.passprotect.server.routes.RouteHelper.*;

public class DeleteRouter {
    private static final Logger logger = LoggerFactory.getLogger(DeleteRouter.class);

    public static void register() {
        options("/account/delete", "DELETE");
        Spark.delete("/account/delete", DeleteRouter::deleteAccount);
    }

    private static Object deleteAccount(Request request, Response response) {
        var uuid = parse(request.queryParams("uuid"));
        if (uuid == null) return respond(response, 400, "UUID cannot be null");
        var token = request.queryParams("token");
        if (token == null) return respond(response, 400, "Token cannot be null");
        var account = fetchAccount(uuid);
        if (account == null) {
            logger.error("Tried to delete non existing account: {}", uuid);
            return respond(response, 404, "Account does not exist");
        } else if (!account.token().equals(token)) {
            logger.error("Invalid token for account deletion: {}", account.email());
            return respond(response, 401, "Invalid token");
        } else try {
            Server.STORAGE.deleteAccount(account);
            logger.info("Deleted account {}", account.email());
            return respond(response, 200, "Successfully deleted account");
        } catch (Exception e) {
            logger.error("Failed to delete account", e);
            return respond(response, 500, "Something went wrong during account deletion");
        }
    }

    private static @Nullable UUID parse(String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch (Exception ignored) {
            return null;
        }
    }
}
