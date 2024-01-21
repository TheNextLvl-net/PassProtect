package net.thenextlvl.passprotect.server.routes.account;

import net.thenextlvl.passprotect.server.Server;
import net.thenextlvl.passprotect.server.model.Account;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Spark;

public class CreateRouter {
    private static final Logger logger = LoggerFactory.getLogger(CreateRouter.class);

    public static void register() {
        Spark.options("/account/create/:email", (request, response) -> {
            response.header("Access-Control-Allow-Methods", "HEAD, GET");
            response.status(200);
            return null;
        });
        Spark.head("/account/create/:email", (request, response) -> {
            var account = fetchAccount(request.params(":email"));
            response.status(account != null ? 409 : 200);
            return account != null ? "Email address is already registered" : "Account does not exist";
        });
        Spark.get("/account/create/:email", CreateRouter::createAccount);
    }

    private static String createAccount(Request request, Response response) {
        var email = request.params(":email");
        if (fetchAccount(email) != null) {
            response.status(409);
            logger.error("Tried to register already existing account: {}", email);
            return "Email address is already registered";
        }
        try {
            var password = request.params(":password");
            Server.STORAGE.createAccount(new Account(email, password));
            response.status(201);
            logger.info("Created account {}", email);
            return "Successfully created new account";
        } catch (Exception e) {
            response.status(500);
            logger.error("Failed to create account", e);
            return "Something went wrong during account creation";
        }
    }

    private static @Nullable Account fetchAccount(String email) {
        try {
            return Server.STORAGE.getAccount(email);
        } catch (Exception e) {
            return null;
        }
    }
}
