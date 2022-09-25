package net.nonswag.tnl.passprotect;

import net.nonswag.tnl.core.api.errors.file.FileException;
import net.nonswag.tnl.core.api.file.formats.ShellFile;
import net.nonswag.tnl.core.api.file.formats.TextFile;
import net.nonswag.tnl.core.api.file.helper.FileDownloader;
import net.nonswag.tnl.core.api.file.helper.FileHelper;
import net.nonswag.tnl.core.utils.SystemUtil;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Installer {

    public static void init(boolean update) throws FileNotFoundException {
        if (Launcher.getFile() == null) throw new FileNotFoundException("Installation file not found");
        String home = System.getProperty("user.home");
        boolean root = "root".equals(System.getenv("USER"));
        if (!update && root && JOptionPane.showConfirmDialog(PassProtect.getInstance().getWindow(), "Installing PassProtect as root is not recommended\n" +
                        "Do you really want to continue as root?",
                "Warning", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) System.exit(1);
        File destination = new File(home, ".pass-protect");
        try {
            if (update && !destination.exists()) {
                throw new FileNotFoundException("PassProtect is not installed on this user account");
            } else if (!update && !destination.exists()) FileHelper.createDirectory(destination);
            Files.copy(Launcher.getFile().toPath(), new File(destination, "PassProtect.jar").toPath(), StandardCopyOption.REPLACE_EXISTING);
            try {
                FileHelper.copyResourceFile(PassProtect.class, "images/icons/icon.png", destination.getAbsolutePath(), true);
            } catch (FileException e) {
                PassProtect.showErrorDialog("Failed to copy resource file", e);
            }
            if (SystemUtil.TYPE.isWindows()) installADB(destination);
            if (!update) {
                if (JOptionPane.showConfirmDialog(PassProtect.getInstance().getWindow(), "Create desktop entry?", null, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    createDesktopEntry("%s/Desktop".formatted(home), destination.getAbsolutePath());
                }
                if (JOptionPane.showConfirmDialog(PassProtect.getInstance().getWindow(), "Create activities entry?", null, JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
                    createDesktopEntry("%s/.local/share/applications".formatted(home), destination.getAbsolutePath());
                }
                JOptionPane.showMessageDialog(PassProtect.getInstance().getWindow(), "Successfully installed PassProtect", "Installer", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(PassProtect.getInstance().getWindow(), "Successfully updated PassProtect", "Updated", JOptionPane.INFORMATION_MESSAGE);
            }
            System.exit(0);
        } catch (Exception e) {
            PassProtect.showErrorDialog("Failed to %s PassProtect".formatted(update ? "update" : "install"), e);
            System.exit(1);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void installADB(@Nonnull File destination) throws IOException {
        Thread thread = new Thread(() -> JOptionPane.showMessageDialog(PassProtect.getInstance().getWindow(), "Installing ADB-Drivers", "Installing…", JOptionPane.INFORMATION_MESSAGE));
        thread.start();
        File file = new File(destination, "platform-tools.zip");
        FileDownloader.download("https://dl.google.com/android/repository/platform-tools-latest-windows.zip", file);
        thread.interrupt();
        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File entryDestination = new File(destination, entry.getName());
                if (!entry.isDirectory()) {
                    entryDestination.getParentFile().mkdirs();
                    try (InputStream in = zipFile.getInputStream(entry); OutputStream out = new FileOutputStream(entryDestination)) {
                        out.write(in.readAllBytes());
                    }
                } else entryDestination.mkdirs();
            }
            JOptionPane.showMessageDialog(PassProtect.getInstance().getWindow(), "Successfully installed ADB-Drivers", "Success", JOptionPane.INFORMATION_MESSAGE);
        } finally {
            FileHelper.delete(file);
        }
    }

    public static void createCommand() throws FileException {
        ShellFile file = new ShellFile("/usr/bin/", "pass-protect");
        file.setContent(new String[]{"cd /home/david/.pass-protect/", "java -jar PassProtect.jar ${@}"});
        if (!file.getFile().setExecutable(true, false)) throw new FileException("Failed to mark file as executable");
        file.save();
    }

    private static void createDesktopEntry(@Nonnull String location, @Nonnull String destination) {
        try {
            if (SystemUtil.TYPE.isLinux()) createLinuxDesktopEntry(location, destination);
            else if (SystemUtil.TYPE.isWindows()) createWindowsDesktopEntry(location, destination);
            else if (SystemUtil.TYPE.isMacOS()) createMacDesktopEntry(location, destination);
            else throw new UnsupportedOperationException("Your os is not supported: " + SystemUtil.TYPE.getName());
        } catch (Exception e) {
            PassProtect.showErrorDialog("Failed to create desktop entry", e);
        }
    }

    private static void createLinuxDesktopEntry(@Nonnull String location, @Nonnull String destination) {
        new TextFile(location, "pass-protect.desktop").setContent("""
                [Desktop Entry]
                Type=Application
                Version=1.0
                Name=PassProtect
                Exec=java -jar PassProtect.jar start installed
                Path=%s
                Icon=%s/icon.png
                Terminal=false
                Categories=Security;
                """.formatted(destination, destination)).save();
    }

    private static void createWindowsDesktopEntry(@Nonnull String location, @Nonnull String destination) {
        throw new UnsupportedOperationException("This feature is currently not working on windows");
    }

    private static void createMacDesktopEntry(@Nonnull String location, @Nonnull String destination) {
        throw new UnsupportedOperationException("This feature is currently not working on mac");
    }
}
