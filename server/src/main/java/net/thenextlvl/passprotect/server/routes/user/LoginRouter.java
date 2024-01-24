package net.thenextlvl.passprotect.server.routes.user;

import net.thenextlvl.passprotect.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Spark;

import static net.thenextlvl.passprotect.server.routes.RouteHelper.*;

public class LoginRouter {
    private static final Logger logger = LoggerFactory.getLogger(LoginRouter.class);

    public static void register() {
        options("/user/login", "POST");
        Spark.post("/user/login", LoginRouter::requestLogin);
    }

    /**
     * Authenticates the user by checking their email and password.
     * If the email or password is missing or incorrect, an appropriate error response is returned.
     * If the authentication is successful, the account token is validated and returned.
     *
     * @param request  The request object.
     * @param response The response object.
     * @return The token if the authentication is successful, or an error response.
     */
    private static Object requestLogin(Request request, Response response) {
        try {
            var email = request.queryParams("email");
            if (email == null) return respond(response, 400, "Email cannot be null");

            var password = request.queryParams("password");
            if (password == null) return respond(response, 400, "Password cannot be null");

            var account = fetchAccount(email);
            if (account == null) return respond(response, 404, "Account does not exist");

            if (!account.passwordMatches(password)) return respond(response, 401, "Wrong password");

            if (!account.isTokenValid()) {
                account.regenerateToken();
                Server.STORAGE.updateToken(account);
            }
            return respond(response, 200, account);
        } catch (Exception e) {
            logger.error("Failed to list accounts", e);
            return respond(response, 500, "Something went wrong during accounts request");
        }
    }
}
