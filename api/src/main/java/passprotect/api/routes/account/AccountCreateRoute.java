package passprotect.api.routes.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import passprotect.api.Server;
import passprotect.api.model.Account;
import spark.Request;
import spark.Response;
import spark.Spark;

import static passprotect.api.routes.RouteHelper.*;

public class AccountCreateRoute {
    private static final Logger logger = LoggerFactory.getLogger(AccountCreateRoute.class);

    public static void register() {
        options("/account/create", "POST");
        Spark.post("/account/create", AccountCreateRoute::createAccount);
    }

    private static Object createAccount(Request request, Response response) {
        return fetchDetails(request, response, (email, username) -> {
            try {
                if (Server.STORAGE.accountExists(email, username)) {
                    logger.error("Tried to register already existing account: {}", email);
                    return respond(response, 409, "Username and email are already in use");
                }
                if (username.length() < 3) return respond(response, 400, "Username is too short");
                if (username.length() > 16) return respond(response, 400, "Username is too long");
                var password = request.queryParams("password");
                if (password == null || password.isBlank()) return respond(response, 400, "Password cannot be null");
                if (password.length() < 6) return respond(response, 400, "Password is too short");
                if (!username.matches(USERNAME_PATTERN)) return respond(response, 400, "Invalid username: " + username);
                if (!email.matches(EMAIL_PATTERN)) return respond(response, 400, "Invalid email: " + email);
                var account = new Account(email, username, password);
                Server.STORAGE.createAccount(account);
                logger.info("Created account {}", email);
                return respond(response, 201, account);
            } catch (Exception e) {
                logger.error("Failed to create account", e);
                return respond(response, 500, "Something went wrong during account creation");
            }
        });
    }
}
