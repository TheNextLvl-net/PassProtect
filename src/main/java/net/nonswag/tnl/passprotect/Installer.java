package net.nonswag.tnl.passprotect;

import net.nonswag.tnl.core.api.errors.file.FileException;
import net.nonswag.tnl.core.api.file.formats.ShellFile;
import net.nonswag.tnl.core.api.file.formats.TextFile;
import net.nonswag.tnl.core.api.file.helper.FileHelper;
import net.nonswag.tnl.core.utils.SystemUtil;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Installer {

    public static void init(boolean update) throws FileNotFoundException {
        if (Launcher.getFile() == null) throw new FileNotFoundException("Installation file not found");
        String home = System.getProperty("user.home");
        boolean root = "root".equals(System.getenv("USER"));
        if (!update && root && JOptionPane.showConfirmDialog(null, "Installing PassProtect as root is not recommended\n" +
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
            if (!update) {
                if (JOptionPane.showConfirmDialog(null, "Create desktop entry?", null, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    createDesktopEntry("%s/Desktop".formatted(home), destination.getAbsolutePath());
                }
                if (JOptionPane.showConfirmDialog(null, "Create activities entry?", null, JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
                    createDesktopEntry("%s/.local/share/applications".formatted(home), destination.getAbsolutePath());
                }
                JOptionPane.showInternalMessageDialog(null, "Successfully installed PassProtect", "Installer", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showInternalMessageDialog(null, "Successfully updated PassProtect", "Updated", JOptionPane.INFORMATION_MESSAGE);
            }
            System.exit(0);
        } catch (Exception e) {
            PassProtect.showErrorDialog("Failed to %s PassProtect".formatted(update ? "update" : "install"), e);
            System.exit(1);
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
