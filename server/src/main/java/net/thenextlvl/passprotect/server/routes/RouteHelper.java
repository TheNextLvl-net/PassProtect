package net.thenextlvl.passprotect.server.routes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.thenextlvl.passprotect.server.Server;
import net.thenextlvl.passprotect.server.model.Account;
import org.jetbrains.annotations.Nullable;
import spark.Response;
import spark.Spark;

import java.sql.SQLException;
import java.util.UUID;

/**
 * This class provides utility methods for handling routes in a web server application.
 */
public final class RouteHelper {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

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
     * Retrieves the Account object associated with the given email address.
     *
     * @param email the email address of the account
     * @return the Account object associated with the email, or null if no account is found
     */
    public static @Nullable Account fetchAccount(String email) {
        try {
            return Server.STORAGE.getAccount(email);
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * Retrieves the Account object associated with the given uuid.
     *
     * @param uuid the uuid of the account
     * @return the Account object associated with the uuid, or null if no account is found
     */
    public static @Nullable Account fetchAccount(UUID uuid) {
        try {
            return Server.STORAGE.getAccount(uuid);
        } catch (SQLException e) {
            return null;
        }
    }

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
}
