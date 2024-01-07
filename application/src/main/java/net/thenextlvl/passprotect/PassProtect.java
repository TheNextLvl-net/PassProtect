package net.thenextlvl.passprotect;

import ch.bailu.gtk.adw.Application;
import ch.bailu.gtk.adw.ColorScheme;
import ch.bailu.gtk.adw.StyleManager;
import ch.bailu.gtk.gio.ApplicationFlags;
import ch.bailu.gtk.type.Strs;
import com.google.gson.GsonBuilder;
import core.file.FileIO;
import core.file.format.GsonFile;
import core.io.IO;
import lombok.Getter;
import net.thenextlvl.passprotect.adapter.FileAdapter;
import net.thenextlvl.passprotect.application.ApplicationData;
import net.thenextlvl.passprotect.application.ApplicationState;
import net.thenextlvl.passprotect.form.InstallationForm;
import net.thenextlvl.passprotect.util.Version;

import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;

public class PassProtect {
    public static final File USER_HOME = new File(System.getProperty("user.home"));
    public static final File DATA_FOLDER = new File(USER_HOME, ".pass-protect");
    public static final File USER_DATA = new File(DATA_FOLDER, "saves.pp");
    public static final File DATA_FILE = new File(DATA_FOLDER, ".data");
    public static final File SESSION_FILE = new File(DATA_FOLDER, ".pid");
    public static final File INSTALLATION = new File(DATA_FOLDER, "pass-protect.jar");
    public static final File ICON_FILE = new File(DATA_FOLDER, "icon.png");

    public static final File ACTIVITIES = new File(USER_HOME, ".local/share/applications");
    public static final File ACTIVITIES_ENTRY = new File(ACTIVITIES, "pass-protect.desktop");

    public static final File MIME_FOLDER = new File(USER_HOME, ".local/share/mime");
    public static final File MIME_PACKAGES = new File(MIME_FOLDER, "packages");
    public static final File MIME_TYPE_FILE = new File(MIME_PACKAGES, "pass-protect.xml");

    public static final File LOGS_FOLDER = new File(DATA_FOLDER, "logs");
    public static final File LOG_FILE = new File(LOGS_FOLDER, getProcessId() + ".log");

    public static final File JAVA = new File(System.getProperty("java.home"), "bin/java");

    public static final File FILE;
    public static final Version VERSION;

    @Getter
    private static final FileIO<ApplicationData> dataFile = new GsonFile<>(IO.of(DATA_FILE), new ApplicationData(
            new File(USER_HOME, "Desktop")
    ), new GsonBuilder()
            .registerTypeAdapter(File.class, new FileAdapter())
            .setPrettyPrinting()
            .create()
    );

    public static final ApplicationState APPLICATION_STATE = isInstalled() ?
            ApplicationState.AUTHENTICATION : ApplicationState.INSTALLATION;

    private static final Application application = new Application(PassProtect.class.getName(), ApplicationFlags.FLAGS_NONE);

    static {
        try {
            redirectOutput();
            cleanupLogs();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            var path = PassProtect.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            System.out.printf("Executable: %s%n", path);
            FILE = path.endsWith(".jar") ? new File(path).getAbsoluteFile() : null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        VERSION = getVersion(FILE);
        System.out.printf("Running version: %s%n", VERSION);
    }

    public static void main(String[] args) {
        if (args.length != 0) System.out.printf("Arguments: %s%n", String.join(" ", args));
        System.out.printf("Process Id: %s%n", getProcessId());
        System.out.printf("Java: %s%n", JAVA.getAbsolutePath());
        System.out.printf("User home: %s%n", USER_HOME.getAbsolutePath());
        System.out.printf("App data: %s%n", getAppData());
        application.onStartup(() -> StyleManager.getDefault().setColorScheme(ColorScheme.PREFER_DARK));
        application.onShutdown(() -> System.out.println("bye!"));
        // initApplication();
        initInstallation();
        application.run(args.length, new Strs(args));
    }

    private static void cleanupLogs() {
        var time = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);
        var files = LOGS_FOLDER.listFiles((file, name) -> name.matches("\\d+\\.log"));
        if (files != null) for (var file : files) if (file.lastModified() <= time) file.delete();
    }

    private static void redirectOutput() throws FileNotFoundException {
        LOGS_FOLDER.mkdirs();
        var stream = new PrintStream(new BufferedOutputStream(new FileOutputStream(LOG_FILE)), true);
        System.setOut(stream);
        System.setErr(stream);
    }

    public static void initApplication() {
        if (APPLICATION_STATE.equals(ApplicationState.INSTALLATION)) initInstallation();
        else if (APPLICATION_STATE.equals(ApplicationState.AUTHENTICATION)) initAuthentication();
        else initAuthenticated();
    }

    private static void initInstallation() {
        application.onActivate(() -> new InstallationForm(application).present());
    }

    private static void initAuthentication() {
    }

    private static void initAuthenticated() {
    }

    public static boolean isInstalled() {
        return INSTALLATION.exists();
    }

    public static ApplicationData getAppData() {
        return getDataFile().getRoot();
    }

    public static File resolveDesktopEntry() {
        return new File(getDataFile().getRoot().getDesktop(), "pass-protect.desktop");
    }

    public static Version getVersion(File file) {
        if (file == null || !file.isFile()) return Version.NONE;
        try (var jar = new JarFile(file)) {
            var attributes = jar.getManifest().getMainAttributes();
            var version = attributes.getValue("Version");
            return Version.parse(version);
        } catch (Exception e) {
            e.printStackTrace();
            return Version.ERROR;
        }
    }

    public static boolean hasJarFile() {
        return FILE != null;
    }

    public static void sendNotification(String message) {
        try {
            new ProcessBuilder("notify-send", "PassProtect", message, "--icon=" + ICON_FILE)
                    .inheritIO()
                    .start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static long getProcessId() {
        return ProcessHandle.current().pid();
    }

    public static Version resolveInstalledVersion() {
        return getVersion(PassProtect.INSTALLATION);
    }
}
