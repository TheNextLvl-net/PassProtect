package net.thenextlvl.passprotect.server.routes.account;

import net.thenextlvl.passprotect.server.Server;
import net.thenextlvl.passprotect.server.model.Account;
import org.jetbrains.annotations.Nullable;
import spark.Response;
import spark.Spark;

class AccountRouter {

    static Object respond(Response response, int status, Object body) {
        response.status(status);
        return body;
    }

    static @Nullable Account fetchAccount(String email) {
        try {
            return Server.STORAGE.getAccount(email);
        } catch (Exception e) {
            return null;
        }
    }

    static void options(String path, String options) {
        Spark.options(path, (request, response) -> {
            response.header("Access-Control-Allow-Methods", path);
            response.status(200);
            return null;
        });
    }
}
