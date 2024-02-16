package passprotect.api.routes.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Spark;

import static passprotect.api.routes.RouteHelper.*;

public class UserLoginRoute {
    private static final Logger logger = LoggerFactory.getLogger(UserLoginRoute.class);

    public static void register() {
        options("/user/login", "POST");
        Spark.post("/user/login", UserLoginRoute::requestLogin);
    }

    private static Object requestLogin(Request request, Response response) {
        return fetchAccount(request, response, account -> {
            try {
                var password = request.queryParams("password");
                if (password == null || password.isBlank()) return respond(response, 400, "Password cannot be null");
                if (!account.passwordMatches(password)) return respond(response, 401, "Invalid password");
                return respond(response, 200, account);
            } catch (Exception e) {
                logger.error("Failed to verify authorization", e);
                return respond(response, 500, "An unexpected error occurred during verification");
            }
        });
    }
}
