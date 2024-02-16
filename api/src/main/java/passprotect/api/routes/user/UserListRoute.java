package passprotect.api.routes.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import passprotect.api.Server;
import passprotect.api.model.Account;
import spark.Request;
import spark.Response;
import spark.Spark;

import static passprotect.api.routes.RouteHelper.options;
import static passprotect.api.routes.RouteHelper.respond;

public class UserListRoute {
    private static final Logger logger = LoggerFactory.getLogger(UserListRoute.class);

    public static void register() {
        options("/user/list", "GET");
        Spark.get("/user/list", UserListRoute::listAccounts);
    }

    private static Object listAccounts(Request request, Response response) {
        try {
            var email = request.queryParams("email");
            if (email == null || email.isBlank()) return respond(response, 400, "Email cannot be null");
            var accounts = Server.STORAGE.getAccounts(email).stream()
                    .map(Account::username)
                    .toList();
            return respond(response, 200, accounts);
        } catch (Exception e) {
            logger.error("Failed to list accounts", e);
            return respond(response, 500, "Something went wrong during accounts request");
        }
    }
}
