package passprotect.api.routes.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import passprotect.api.Server;
import spark.Request;
import spark.Response;
import spark.Spark;

import static passprotect.api.routes.RouteHelper.*;

public class UserRenameRoute {
    private static final Logger logger = LoggerFactory.getLogger(UserRenameRoute.class);

    public static void register() {
        options("/user/rename", "POST");
        Spark.post("/user/rename", UserRenameRoute::performRename);
    }

    private static Object performRename(Request request, Response response) {
        return fetchAuthorization(request, response, account -> {
            try {
                var username = request.queryParams("username");
                if (username == null || username.isBlank()) return respond(response, 400, "Username cannot be null");
                account.username(username);
                Server.STORAGE.updateUsername(account);
                return respond(response, 200, account);
            } catch (Exception e) {
                logger.error("Failed to rename account", e);
                return respond(response, 500, "Something went wrong during accounts request");
            }
        });
    }
}
