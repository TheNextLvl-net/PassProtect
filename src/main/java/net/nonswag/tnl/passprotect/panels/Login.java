package net.nonswag.tnl.passprotect.panels;

import lombok.Getter;
import net.nonswag.tnl.passprotect.Launcher;
import net.nonswag.tnl.passprotect.PassProtect;
import net.nonswag.tnl.passprotect.api.fields.PasswordField;
import net.nonswag.tnl.passprotect.api.files.Config;
import net.nonswag.tnl.passprotect.api.files.Storage;
import net.nonswag.tnl.passprotect.api.renderer.TreeIconRenderer;
import net.nonswag.tnl.passprotect.dialogs.ResetPassword;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Getter
public class Login extends Panel {
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
    private JLabel passwordImage, userImage;

    public Login() {
        userImage.setIcon(TreeIconRenderer.Logo.USER.getIcon(Config.APP));
        passwordImage.setIcon(TreeIconRenderer.Logo.PASSWORD.getIcon(Config.APP));
        List<String> users = PassProtect.retrieveUsers();
        String user = Config.APP.getLastUser();
        users.forEach(username::addItem);
        if (users.contains(user)) username.setSelectedItem(user);
        JTextComponent editor = (JTextComponent) username.getEditor().getEditorComponent();
        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(@Nonnull KeyEvent event) {
                if (event.getKeyCode() != KeyEvent.VK_DOWN) return;
                password.requestFocus();
                event.consume();
            }

            @Override
            public void keyReleased(@Nonnull KeyEvent event) {
                if (event.getKeyCode() != KeyEvent.VK_ENTER) return;
                String user = username.getSelectedItem() == null ? "" : (String) username.getSelectedItem();
                File saves = new File(user, "saves.pp");
                if (saves.exists() && saves.isFile()) password.requestFocus();
                else PassProtect.invalidUser(user);
            }
        });
        login.addActionListener(actionEvent -> handleLogin());
        forgotPassword.addActionListener(actionEvent -> new ResetPassword((String) username.getSelectedItem()));
        password.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(@Nonnull KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_UP) username.requestFocus();
                if (event.getKeyChar() == '\n') handleLogin();
            }
        });
        checkBox.addActionListener(actionEvent -> ((PasswordField) password).setPasswordVisible(checkBox.isSelected()));
        register.addActionListener(actionEvent -> PassProtect.getInstance().init(new Registration()));
    }

    private void handleLogin() {
        String password = String.valueOf(this.password.getPassword());
        String username = this.username.getSelectedItem() == null ? "" : (String) this.username.getSelectedItem();
        File saves = new File(username, "saves.pp");
        if (!saves.exists() || !saves.isFile()) {
            PassProtect.invalidUser(username);
            this.username.requestFocus();
        } else try {
            Storage.setInstance(new Storage(username, password.getBytes(StandardCharsets.UTF_8)));
            Config config = new Config(username);
            Config.setInstance(config);
            try {
                Config.APP.setLastUser(username);
                Launcher.applyAppearance(config);
                PassProtect.getInstance().setLoggedIn(true);
            } catch (Exception e) {
                PassProtect.showErrorDialog("Something went wrong while logging in", e);
            }
        } catch (Exception e) {
            if (!password.isEmpty()) {
                try {
                    Thread.sleep(750);
                    PassProtect.showErrorDialog("Wrong password");
                    this.password.setText("");
                    this.password.setForeground(Color.GRAY);
                } catch (InterruptedException ignored) {
                }
            } else PassProtect.showErrorDialog("Enter a password");
            this.password.requestFocus();
        }
    }

    @Override
    public void onFocus() {
        this.password.requestFocus();
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
