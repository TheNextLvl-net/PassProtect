package net.thenextlvl.passprotect;

import ch.bailu.gtk.adw.Application;
import ch.bailu.gtk.adw.ColorScheme;
import ch.bailu.gtk.adw.StyleManager;
import ch.bailu.gtk.gio.ApplicationFlags;
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

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

public class PassProtect {
    public static final File USER_HOME = new File(System.getProperty("user.home"));
    public static final File DATA_FOLDER = new File(USER_HOME, ".pass-protect");
    public static final File DATA_FILE = new File(DATA_FOLDER, ".data");
    public static final File SESSION_FILE = new File(DATA_FOLDER, ".pid");
    public static final File INSTALLATION = new File(DATA_FOLDER, "PassProtect.jar");
    public static final File ICON_FILE = new File(DATA_FOLDER, "icon.png");

    public static final File JAVA = new File(System.getProperty("java.home"), "bin/java");

    public static final File FILE;
    public static final Version VERSION;

    @Getter
    private static final FileIO<ApplicationData> dataFile = new GsonFile<>(IO.of(DATA_FILE), new ApplicationData(
            new File(USER_HOME, "Desktop"),
            new File(USER_HOME, ".local/share/applications")
    ), new GsonBuilder()
            .registerTypeAdapter(File.class, new FileAdapter())
            .setPrettyPrinting()
            .create()
    );

    public static final ApplicationState APPLICATION_STATE = isInstalled() ?
            ApplicationState.AUTHENTICATION : ApplicationState.INSTALLATION;

    private static final Application application = new Application("PassProtect", ApplicationFlags.FLAGS_NONE);

    static {
        try {
            var path = PassProtect.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            System.out.printf("Executable: %s%n", path);
            FILE = path.endsWith(".jar") ? new File(path).getAbsoluteFile() : null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        VERSION = getVersion(FILE);
    }

    public static void main(String[] args) {
        System.out.printf("Process Id: %s%n", getProcessId());
        System.out.printf("Java: %s%n", JAVA.getAbsolutePath());
        System.out.printf("User home: %s%n", USER_HOME.getAbsolutePath());
        System.out.printf("App data: %s%n", getAppData());
        application.onStartup(() -> StyleManager.getDefault().setColorScheme(ColorScheme.PREFER_DARK));
        application.onShutdown(() -> System.out.println("bye!"));
        // initApplication();
        initInstallation();
        application.run(0, null);
        application.unref();
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

    public static File resolveActivitiesEntry() {
        return new File(getDataFile().getRoot().getActivities(), "pass-protect.desktop");
    }

    public static Version getVersion(File file) {
        if (file == null || !file.isFile()) return Version.NONE;
        try (var jar = new JarFile(file)) {
            var attributes = jar.getManifest().getMainAttributes();
            var version = attributes.getValue("Version");
            System.out.printf("Version: %s%n", version);
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