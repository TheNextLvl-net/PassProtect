package net.thenextlvl.passprotect.server.routes.account;

import net.thenextlvl.passprotect.server.Server;
import net.thenextlvl.passprotect.server.model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Spark;

import static net.thenextlvl.passprotect.server.routes.RouteHelper.*;

public class CreateRouter {
    private static final Logger logger = LoggerFactory.getLogger(CreateRouter.class);

    public static void register() {
        options("/account/create", "POST");
        Spark.post("/account/create", CreateRouter::createAccount);
    }

    private static Object createAccount(Request request, Response response) {
        var email = request.queryParams("email");
        if (email == null) return respond(response, 400, "Email cannot be null");
        if (fetchAccount(email) != null) {
            logger.error("Tried to register already existing account: {}", email);
            return respond(response, 409, "Email address is already registered");
        }
        try {
            var password = request.queryParams("password");
            if (password == null) return respond(response, 400, "Password cannot be null");
            var account = new Account(email, password);
            Server.STORAGE.createAccount(account);
            logger.info("Created account {}", email);
            return respond(response, 201, account);
        } catch (Exception e) {
            logger.error("Failed to create account", e);
            return respond(response, 500, "Something went wrong during account creation");
        }
    }
}
