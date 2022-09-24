package net.nonswag.tnl.passprotect.panels;

import lombok.Getter;
import net.nonswag.tnl.passprotect.Launcher;
import net.nonswag.tnl.passprotect.PassProtect;
import net.nonswag.tnl.passprotect.api.fields.PasswordField;
import net.nonswag.tnl.passprotect.api.fields.TextField;
import net.nonswag.tnl.passprotect.api.files.Config;
import net.nonswag.tnl.passprotect.api.files.Storage;
import net.nonswag.tnl.passprotect.api.renderer.TreeIconRenderer;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Getter
public class Registration extends Panel {

    @Nonnull
    private JPasswordField password, confirm;
    @Nonnull
    private JPanel panel;
    @Nonnull
    private JCheckBox checkBox;
    @Nonnull
    private JTextField hint, username;
    @Nonnull
    private JButton back, registerButton;
    @Nonnull
    private JLabel usernameIcon, passwordIcon, confirmIcon, hintIcon;

    public Registration() {
        usernameIcon.setIcon(TreeIconRenderer.Logo.USER.getIcon(Config.APP));
        passwordIcon.setIcon(TreeIconRenderer.Logo.PASSWORD.getIcon(Config.APP));
        confirmIcon.setIcon(TreeIconRenderer.Logo.PASSWORD.getIcon(Config.APP));
        hintIcon.setIcon(TreeIconRenderer.Logo.HINT.getIcon(Config.APP));
        password.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(@Nonnull KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_DOWN) confirm.requestFocus();
                else if (event.getKeyCode() == KeyEvent.VK_UP) username.requestFocus();
                if (event.getKeyChar() != '\n') return;
                String password = String.valueOf(Registration.this.password.getPassword());
                if (password.length() >= 8) confirm.requestFocus();
                else if (password.isEmpty()) PassProtect.showErrorDialog("Enter a password");
                else PassProtect.showErrorDialog("Password is too short");
            }
        });
        confirm.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(@Nonnull KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_UP) password.requestFocus();
                else if (event.getKeyCode() == KeyEvent.VK_DOWN) hint.requestFocus();
                if (event.getKeyChar() != '\n') return;
                if (!Arrays.equals(password.getPassword(), confirm.getPassword())) {
                    PassProtect.showErrorDialog("Passwords doesn't match");
                    confirm.setText(null);
                    confirm.setForeground(Color.GRAY);
                } else hint.requestFocus();
            }
        });
        hint.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(@Nonnull KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_UP) confirm.requestFocus();
                else if (event.getKeyChar() == '\n') handleRegistration();
            }
        });
        username.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(@Nonnull KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_DOWN) password.requestFocus();
                if (event.getKeyChar() != '\n') return;
                String username = Registration.this.username.getText() == null ? "" : Registration.this.username.getText();
                if (username.isEmpty()) PassProtect.showErrorDialog("Enter a username");
                else if (!isLegal(username)) PassProtect.showErrorDialog("Username contains illegal characters");
                else if (username.length() < 4) PassProtect.showErrorDialog("Username is too short");
                else password.requestFocus();
            }
        });
        checkBox.addActionListener(actionEvent -> {
            ((PasswordField) password).setPasswordVisible(checkBox.isSelected());
            ((PasswordField) confirm).setPasswordVisible(checkBox.isSelected());
        });
        registerButton.addActionListener(actionEvent -> handleRegistration());
        back.addActionListener(actionEvent -> PassProtect.getInstance().setLoggedIn(false));
        back.setEnabled(!PassProtect.retrieveUsers().isEmpty());
    }

    private boolean isLegal(@Nonnull String username) {
        return username.matches("^[a-zA-Z0-9]+$");
    }

    private void handleRegistration() {
        String password = String.valueOf(Registration.this.password.getPassword());
        String confirm = String.valueOf(Registration.this.confirm.getPassword());
        String username = this.username.getText() == null ? "" : this.username.getText();
        File saves = new File(username, "saves.pp");
        if (!saves.exists() && username.length() >= 4 && password.equals(confirm) && password.length() >= 8 && isLegal(username)) {
            try {
                Storage.setInstance(new Storage(username, password.getBytes(StandardCharsets.UTF_8)));
                Config config = new Config(username);
                config.setHint(hint.getText().isEmpty() ? null : hint.getText());
                Config.setInstance(config);
                PassProtect.getInstance().setLoggedIn(true);
            } catch (Exception e) {
                PassProtect.showErrorDialog("An unexpected error occurred", e);
            }
        } else if (saves.exists()) {
            PassProtect.showErrorDialog("This username is already taken");
            Registration.this.username.requestFocus();
        } else if (username.isEmpty()) {
            PassProtect.showErrorDialog("Enter a username");
            Registration.this.username.requestFocus();
        } else if (username.length() < 4) {
            PassProtect.showErrorDialog("Username is too short");
            Registration.this.username.requestFocus();
        } else if (!password.equals(confirm)) {
            PassProtect.showErrorDialog("Passwords doesn't match");
            Registration.this.confirm.requestFocus();
        } else if (password.isEmpty()) {
            PassProtect.showErrorDialog("Enter a password");
            Registration.this.password.requestFocus();
        } else if (!isLegal(username)) {
            PassProtect.showErrorDialog("Username contains illegal characters");
            Registration.this.username.requestFocus();
        } else {
            PassProtect.showErrorDialog("Password is too short");
            Registration.this.password.requestFocus();
        }
    }

    private void createUIComponents() {
        this.username = new TextField().setPlaceholder("Enter a username");
        this.password = new PasswordField().setPlaceholder("Enter a password");
        this.confirm = new PasswordField().setPlaceholder("Confirm password");
        this.hint = new TextField().setPlaceholder("Enter a hint");
    }

    @Nonnull
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 340);
    }

    @Override
    public void onFocus() {
        Launcher.applyAppearance(Config.APP);
    }
}
