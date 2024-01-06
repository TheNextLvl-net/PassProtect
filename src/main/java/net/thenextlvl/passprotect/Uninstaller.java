package net.thenextlvl.passprotect;

import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;

public class Uninstaller {

    public static void init(@Nullable Boolean eraseData) throws FileNotFoundException {
        String home = System.getProperty("user.home");
        File location = new File(home, ".pass-protect");
        if (!location.exists()) throw new FileNotFoundException("PassProtect is not installed on this user account");
        boolean success = new File(new File(home, ".local/share/applications"), "pass-protect.desktop").delete();
        if (eraseData == null) {
            int code = JOptionPane.showConfirmDialog(PassProtect.getInstance().getWindow(), "Do you want to delete all user data?\nYour data will be gone forever!", null, JOptionPane.YES_NO_OPTION);
            if (code == JOptionPane.CLOSED_OPTION) {
                JOptionPane.showMessageDialog(PassProtect.getInstance().getWindow(), "Interrupted the uninstallation process", "Uninstaller", JOptionPane.INFORMATION_MESSAGE);
                System.exit(1);
            } else if (code == JOptionPane.YES_OPTION && !location.delete()) success = false;
        } else if (eraseData && !location.delete()) success = false;
        if (!new File(new File(home, "/Desktop"), "pass-protect.desktop").delete()) success = false;
        if (success) {
            JOptionPane.showMessageDialog(PassProtect.getInstance().getWindow(), "Successfully uninstalled PassProtect", "Uninstaller", JOptionPane.INFORMATION_MESSAGE);
        } else PassProtect.showErrorDialog("Could not uninstall PassProtect properly");
        System.exit(success ? 0 : 1);
    }
}
