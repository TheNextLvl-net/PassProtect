package passprotect.api.routes.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import passprotect.api.Server;
import spark.Request;
import spark.Response;
import spark.Spark;

import static passprotect.api.routes.RouteHelper.*;

public class UserEmailRoute {
    private static final Logger logger = LoggerFactory.getLogger(UserEmailRoute.class);

    public static void register() {
        options("/user/email", "POST");
        Spark.post("/user/email", UserEmailRoute::performRename);
    }

    private static Object performRename(Request request, Response response) {
        return fetchAuthorization(request, response, account -> {
            try {
                var email = request.queryParams("email");
                if (email == null || email.isBlank()) return respond(response, 400, "Email cannot be null");
                if (!email.matches(EMAIL_PATTERN)) return respond(response, 400, "Invalid email: " + email);
                account.email(email);
                Server.STORAGE.updateEmail(account);
                return respond(response, 200, account);
            } catch (Exception e) {
                logger.error("Failed to change email", e);
                return respond(response, 500, "Something went wrong during accounts request");
            }
        });
    }
}
