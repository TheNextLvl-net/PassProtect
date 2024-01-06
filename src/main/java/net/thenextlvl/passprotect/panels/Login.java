package net.thenextlvl.passprotect.panels;

import lombok.Getter;
import net.thenextlvl.adb.Device;
import net.thenextlvl.passprotect.PassProtect;
import net.thenextlvl.passprotect.api.fields.PasswordField;
import net.thenextlvl.adb.ADB;
import net.thenextlvl.passprotect.Launcher;
import net.thenextlvl.passprotect.api.files.Config;
import net.thenextlvl.passprotect.api.files.Storage;
import net.thenextlvl.passprotect.api.renderer.TreeIconRenderer;
import net.thenextlvl.passprotect.dialogs.ResetPassword;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

@Getter
public class Login extends Panel {

    @Nonnull
    private static final HashMap<String, Integer> failedAttempts = new HashMap<>();

    @Nonnull
    private JPanel panel;
    @Nonnull
    private JPasswordField password;
    @Nonnull
    private JButton login, forgotPassword, register;
    @Nonnull
    private JCheckBox checkBox;
    @Nonnull
    private JComboBox<String> username;
    @Nonnull
    private JLabel passwordImage, userImage, profilePicture;

    public Login() {
        userImage.setIcon(TreeIconRenderer.Logo.USER.getIcon(Config.APP));
        passwordImage.setIcon(TreeIconRenderer.Logo.PASSWORD.getIcon(Config.APP));
        List<String> users = PassProtect.retrieveUsers();
        String user = Config.APP.getLastUser();
        users.forEach(username::addItem);
        if (users.contains(user)) username.setSelectedItem(user);
        JTextComponent editor = (JTextComponent) username.getEditor().getEditorComponent();
        username.addItemListener(itemEvent -> {
            if (itemEvent.getItem().equals(this.username.getSelectedItem())) return;
            String username = (String) this.username.getSelectedItem();
            if (username != null && new File(username, "config.json").exists()) updatePanel(new Config(username));
        });
        login.addActionListener(actionEvent -> handleLogin());
        forgotPassword.addActionListener(actionEvent -> {
            String username = (String) this.username.getSelectedItem();
            if (username != null) {
                if (new File(username, "config.json").exists()) new ResetPassword(new Config(username));
                else PassProtect.invalidUser(username);
            } else PassProtect.showErrorDialog("Enter a username");
        });
        password.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_UP) username.requestFocus();
                if (event.getKeyChar() == '\n') handleLogin();
            }
        });
        checkBox.addActionListener(actionEvent -> ((PasswordField) password).setPasswordVisible(checkBox.isSelected()));
        register.addActionListener(actionEvent -> PassProtect.getInstance().init(new Registration()));
    }

    private void updatePanel(@Nullable String username) {
        if (username == null) return;
        if (!new File(username, "config.json").exists()) return;
        updatePanel(new Config(username));
    }

    private void updatePanel(Config config) {
        try {
            for (Device device : ADB.getDevices()) {
                if (!config.getTrustedDevices().isTrusted(device)) continue;
                this.password.setText(config.getTrustedDevices().getPassword(device));
                this.login.requestFocus();
                return;
            }
            this.password.setText(null);
            this.password.requestFocus();
        } catch (Exception ignored) {
        } finally {
            Launcher.applyAppearance(config);
            if (!config.isProfilePictureSet()) profilePicture.setIcon(null);
            else profilePicture.setIcon(new ImageIcon(config.getProfilePicture()));
        }
    }

    private void handleLogin() {
        String password = String.valueOf(this.password.getPassword());
        String username = this.username.getSelectedItem() == null ? "" : (String) this.username.getSelectedItem();
        File saves = new File(username, "saves.pp");
        if (!saves.exists() || !saves.isFile() || !username.matches("^[a-zA-Z0-9]+$")) {
            PassProtect.invalidUser(username);
            this.username.requestFocus();
        } else try {
            Config config = new Config(username);
            Storage.setInstance(new Storage(username, password.getBytes(StandardCharsets.UTF_8)));
            try {
                Config.setInstance(config);
                failedAttempts.remove(username);
                Config.APP.setLastUser(username);
                Launcher.applyAppearance(config);
                PassProtect.getInstance().setLoggedIn(true);
            } catch (Exception e) {
                PassProtect.showErrorDialog("Something went wrong while logging in", e);
                Config.setInstance(null);
                Storage.setInstance(null);
            }
        } catch (Exception e) {
            if (!password.isEmpty()) {
                try {
                    failedAttempts.put(username, failedAttempts.getOrDefault(username, 0) + 1);
                    long delay = failedAttempts.get(username) * 750L;
                    PassProtect.showErrorDialog("Wrong password\nYou have to wait %s seconds before you can try again".formatted(delay / 1000d));
                    Thread.sleep(delay);
                    this.password.setText("");
                } catch (InterruptedException ignored) {
                }
            } else PassProtect.showErrorDialog("Enter a password");
            if (failedAttempts.getOrDefault(username, 0) >= 3) forgotPassword.requestFocus();
            else this.password.requestFocus();
        }
    }

    @Override
    public void onFocus() {
        updatePanel((String) this.username.getSelectedItem());
        if (this.password.getPassword().length == 0) this.password.requestFocus();
        else this.login.requestFocus();
    }

    private void createUIComponents() {
        this.password = new PasswordField().setPlaceholder("Password");
    }

    @Nonnull
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 340);
    }
}
