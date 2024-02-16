package passprotect.api.routes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import org.jetbrains.annotations.Nullable;
import passprotect.api.Server;
import passprotect.api.model.Account;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.sql.SQLException;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * This class provides utility methods for handling routes in a web server application.
 */
public final class RouteHelper {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final String EMAIL_PATTERN = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    public static final String USERNAME_PATTERN = "^[a-zA-Z0-9_-]*";

    /**
     * Set the options for the specified path.
     *
     * @param path    The path for which to set the options.
     * @param options The options to set for the path.
     */
    public static void options(String path, String options) {
        Spark.options(path, (request, response) -> {
            response.header("Access-Control-Allow-Methods", path);
            response.status(200);
            return null;
        });
    }

    /**
     * Sets the response type to "application/json", sets the response status code,
     * and converts the content to a JSON element using the GSON library.
     *
     * @param response The response object to modify.
     * @param status   The status code to set.
     * @param content  The content to convert to a JSON element.
     * @return The converted content as a JSON element.
     */
    public static JsonElement respond(Response response, int status, Object content) {
        response.type("application/json");
        response.status(status);
        return GSON.toJsonTree(content);
    }

    /**
     * Fetches the authorization details for the provided request and executes
     * the success callback function if authorization is successful.
     *
     * @param request  The request object containing the query parameters.
     * @param response The response object to modify.
     * @param success  The success callback function where the fetched Account object will be passed.
     * @return The result of calling the success callback function with the fetched Account object.
     */
    public static Object fetchAuthorization(Request request, Response response, Function<Account, Object> success) {
        var uuid = parseUniqueId(request.queryParams("uuid"));
        if (uuid == null) return respond(response, 400, "UUID cannot be null");
        var token = request.queryParams("token");
        if (token == null || token.isBlank()) return respond(response, 400, "Token cannot be null");
        var account = fetchAccount(uuid);
        if (account == null) return respond(response, 404, "Account does not exist");
        if (!account.tokenMatches(token)) return respond(response, 401, "Invalid token");
        return success.apply(account);
    }

    /**
     * Fetches the account using the provided request, response, and account function.
     *
     * @param request  The request object containing the query parameters.
     * @param response The response object to modify.
     * @param account  The function that takes an Account and returns the desired result.
     * @return The result of calling the account function with the fetched Account object.
     */
    public static Object fetchAccount(Request request, Response response, Function<Account, Object> account) {
        return fetchDetails(request, response, (email, username) -> {
            var result = fetchAccount(email, username);
            if (result == null) return respond(response, 404, "Account does not exist");
            return account.apply(result);
        });
    }

    /**
     * Fetches the account details using the provided request, response, and details function.
     *
     * @param request  The request object containing the query parameters.
     * @param response The response object to modify.
     * @param details  The function that takes an email and username and returns the details.
     * @return The details object obtained from calling the details function.
     */
    public static Object fetchDetails(Request request, Response response, BiFunction<String, String, Object> details) {
        var email = request.queryParams("email");
        if (email == null) return respond(response, 400, "Email cannot be null");
        var username = request.queryParams("username");
        if (username == null) return respond(response, 400, "Username cannot be null");
        return details.apply(email, username);
    }

    private static @Nullable UUID parseUniqueId(String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static @Nullable Account fetchAccount(UUID uuid) {
        try {
            return Server.STORAGE.getAccount(uuid);
        } catch (SQLException e) {
            return null;
        }
    }

    private static @Nullable Account fetchAccount(String email, String username) {
        try {
            return Server.STORAGE.getAccount(email, username);
        } catch (SQLException e) {
            return null;
        }
    }
}
