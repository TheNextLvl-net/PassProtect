package net.nonswag.tnl.passprotect;

import lombok.Getter;
import lombok.Setter;
import net.nonswag.tnl.core.api.logger.Logger;
import net.nonswag.tnl.passprotect.panels.Login;
import net.nonswag.tnl.passprotect.panels.Menu;
import net.nonswag.tnl.passprotect.panels.Panel;
import net.nonswag.tnl.passprotect.panels.Registration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PassProtect {

    @Getter
    @Nonnull
    private static final PassProtect instance = new PassProtect();
    @Getter
    @Setter
    private static boolean installed = false;

    @Nullable
    private JFrame window;
    private boolean loggedIn = false;
    @Nullable
    private final Image icon;

    {
        URL url = getClass().getClassLoader().getResource("images/icons/icon.png");
        icon = url == null ? null : new ImageIcon(url).getImage();
        Logger.debug.setCondition(() -> !isInstalled());
    }

    private void init() {
        init(null);
    }

    public void init(@Nullable Panel panel) {
        boolean register = PassProtect.retrieveUsers().isEmpty();
        panel = panel != null ? panel : isLoggedIn() ? new Menu() : register ? new Registration() : new Login();
        JFrame previousWindow = window != null ? window : null;
        if (window != null) window.setVisible(false);
        window = new JFrame(panel.getName());
        window.setContentPane(panel.getPanel());
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(panel.isResizable());
        window.setPreferredSize(panel.getPreferredSize());
        window.setMinimumSize(window.getPreferredSize());
        window.pack();
        window.setLocationRelativeTo(previousWindow);
        if (previousWindow != null) window.setLocation(previousWindow.getLocation());
        window.setVisible(true);
        if (icon != null) window.setIconImage(icon);
        window.requestFocus(FocusEvent.Cause.ACTIVATION);
        panel.onFocus();
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
        init();
    }

    public static void main(boolean installed) {
        PassProtect.installed = installed;
        getInstance().setLoggedIn(false);
    }

    @Nonnull
    public static List<String> retrieveUsers() {
        List<String> users = new ArrayList<>();
        File[] directories = new File("").getAbsoluteFile().listFiles((file) -> {
            if (!file.getName().matches("^[a-zA-Z0-9]+$")) return false;
            File saves = new File(file, "saves.pp");
            return saves.exists() && saves.isFile();
        });
        if (directories == null) return users;
        for (File directory : directories) users.add(directory.getName());
        return users;
    }

    public static void showErrorDialog(@Nonnull String message, @Nonnull Throwable t) {
        showErrorDialog("%s\n%s\n%s".formatted(message, t.getClass().getSimpleName(), t.getMessage()));
    }

    public static void showErrorDialog(@Nonnull String message) {
        JOptionPane.showMessageDialog(getInstance().getWindow(), message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void invalidUser(@Nullable String username) {
        if (username == null || username.isEmpty()) PassProtect.showErrorDialog("Enter a username");
        else PassProtect.showErrorDialog("A user named '%s' does not exist".formatted(username));
    }
}
