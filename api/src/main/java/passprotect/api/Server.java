package passprotect.api;

import core.file.format.GsonFile;
import core.io.IO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import passprotect.api.config.Config;
import passprotect.api.routes.account.AccountCreateRoute;
import passprotect.api.routes.account.AccountDeleteRoute;
import passprotect.api.routes.account.AccountListRoute;
import passprotect.api.routes.user.*;
import passprotect.api.storage.DataStorage;
import passprotect.api.storage.DatabaseStorage;
import spark.Spark;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public static final File DATA_FOLDER = new File("data");
    public static final File USER_DATA = new File("users");
    public static Config CONFIG = new GsonFile<>(IO.of(DATA_FOLDER, "config.json"), new Config(
            3000, "*", TimeUnit.DAYS.toMillis(30)
    )).validate().save().getRoot();
    public static DataStorage STORAGE = new DatabaseStorage();

    public static void main(String[] args) {
        Spark.port(CONFIG.port());

        registerAccessControl();

        AccountCreateRoute.register();
        AccountDeleteRoute.register();
        AccountListRoute.register();

        UserDataRoute.register();
        UserListRoute.register();
        UserLoginRoute.register();
        UserEmailRoute.register();
        UserRenameRoute.register();
    }

    private static void registerAccessControl() {
        Spark.before((request, response) -> response.header("Access-Control-Allow-Origin", CONFIG.allowedOrigin()));
    }
}
