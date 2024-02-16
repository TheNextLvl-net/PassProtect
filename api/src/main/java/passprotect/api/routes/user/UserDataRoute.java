package passprotect.api.routes.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Spark;

import static passprotect.api.routes.RouteHelper.*;

public class UserDataRoute {
    private static final Logger logger = LoggerFactory.getLogger(UserDataRoute.class);

    public static void register() {
        options("/user/data", "POST");
        Spark.post("/user/data", UserDataRoute::requestFile);
    }

    private static Object requestFile(Request request, Response response) {
        return fetchAuthorization(request, response, account -> {
            try {
                return respond(response, 200, "todo"); // todo: return data
            } catch (Exception e) {
                logger.error("Failed to retrieve data", e);
                return respond(response, 500, "Something went wrong during data retrieval");
            }
        });
    }
}
