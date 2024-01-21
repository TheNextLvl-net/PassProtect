package net.thenextlvl.passprotect.server;

import core.file.format.GsonFile;
import core.io.IO;
import net.thenextlvl.passprotect.server.config.Config;
import net.thenextlvl.passprotect.server.routes.account.AccountsRouter;
import net.thenextlvl.passprotect.server.routes.account.CreateRouter;
import net.thenextlvl.passprotect.server.routes.account.DeleteRouter;
import net.thenextlvl.passprotect.server.storage.DataStorage;
import net.thenextlvl.passprotect.server.storage.DatabaseStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import java.io.File;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public static final File DATA_FOLDER = new File("data");
    public static Config CONFIG = new GsonFile<>(IO.of(DATA_FOLDER, "config.json"), new Config(
            3000, "*"
    )).validate().save().getRoot();
    public static DataStorage STORAGE = new DatabaseStorage();

    public static void main(String[] args) {
        Spark.port(CONFIG.port());
        registerAccessControl();
        CreateRouter.register();
        DeleteRouter.register();
        AccountsRouter.register();
    }

    private static void registerAccessControl() {
        Spark.before((request, response) -> response.header("Access-Control-Allow-Origin", CONFIG.allowedOrigin()));
    }
}
