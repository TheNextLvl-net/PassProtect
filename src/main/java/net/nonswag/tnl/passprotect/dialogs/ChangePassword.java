package net.nonswag.tnl.passprotect.dialogs;

import com.google.common.hash.Hashing;
import net.nonswag.tnl.passprotect.PassProtect;
import net.nonswag.tnl.passprotect.api.fields.PasswordField;
import net.nonswag.tnl.passprotect.api.fields.TextField;
import net.nonswag.tnl.passprotect.api.files.Config;
import net.nonswag.tnl.passprotect.api.files.Storage;
import net.nonswag.tnl.passprotect.api.renderer.TreeIconRenderer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ChangePassword extends JDialog {
    @Nonnull
    private JPanel panel;
    @Nonnull
    private JButton buttonOK, buttonCancel;
    @Nonnull
    private JPasswordField oldPassword, newPassword, confirmPassword;
    @Nonnull
    private JCheckBox checkBox;
    @Nonnull
    private JTextField hint;
    @Nonnull
    private JLabel oldPasswordIcon, newPasswordIcon, confirmPasswordIcon, hintIcon;

    private void createUIComponents() {
        Config config = Config.getInstance();
        oldPassword = new PasswordField().setPlaceholder("Current password");
        newPassword = new PasswordField().setPlaceholder("New password");
        confirmPassword = new PasswordField().setPlaceholder("Repeat password");
        hint = new TextField(config == null ? null : config.getHint()).setPlaceholder("Enter a hint");
    }

    @Nullable
    private final CloseEvent closeEvent = (current, newPassword, confirm, hint, type) -> {
        Storage storage = Storage.getInstance();
        if (storage == null || !type.isConfirm()) return true;
        if (current.length > 0) {
            if (Arrays.equals(storage.getSecurityKey(), Hashing.sha256().hashBytes(String.valueOf(current).getBytes(StandardCharsets.UTF_8)).asBytes())) {
                if (newPassword.length > 0) {
                    if (Arrays.equals(newPassword, confirm)) {
                        if (newPassword.length >= 8) {
                            try {
                                storage.setSecurityKey(String.valueOf(newPassword).getBytes(StandardCharsets.UTF_8));
                                Config config = Config.getInstance();
                                if (config != null) {
                                    Storage.setInstance(null);
                                    config.setLastPasswordChange(System.currentTimeMillis());
                                    config.setHint(hint.isEmpty() ? null : hint);
                                    config.save();
                                }
                                Config.setInstance(null);
                                PassProtect.getInstance().setLoggedIn(false);
                                JOptionPane.showInternalMessageDialog(PassProtect.getInstance().getWindow(), "Changed your password", "Success", JOptionPane.INFORMATION_MESSAGE);
                                return true;
                            } catch (Exception e) {
                                PassProtect.showErrorDialog("Failed to change your password", e);
                            }
                        } else PassProtect.showErrorDialog("Password is too short");
                    } else PassProtect.showErrorDialog("Passwords do not match, try again");
                } else PassProtect.showErrorDialog("Enter a password");
            } else PassProtect.showErrorDialog("Wrong password");
        } else PassProtect.showErrorDialog("Enter your current password");
        return false;
    };

    public ChangePassword(@Nonnull Storage storage, @Nonnull Config config) {
        super((Frame) null, "Change password");
        oldPasswordIcon.setIcon(TreeIconRenderer.Logo.PASSWORD.getIcon(config));
        newPasswordIcon.setIcon(TreeIconRenderer.Logo.PASSWORD.getIcon(config));
        confirmPasswordIcon.setIcon(TreeIconRenderer.Logo.PASSWORD.getIcon(config));
        hintIcon.setIcon(TreeIconRenderer.Logo.HINT.getIcon(config));
        setContentPane(panel);
        getRootPane().setDefaultButton(buttonOK);
        buttonOK.addActionListener(e -> close(oldPassword.getPassword(), newPassword.getPassword(), confirmPassword.getPassword(), hint.getText(), CloseEvent.Type.CONFIRM));
        buttonCancel.addActionListener(e -> close(oldPassword.getPassword(), newPassword.getPassword(), confirmPassword.getPassword(), hint.getText(), CloseEvent.Type.CANCEL));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(@Nonnull WindowEvent event) {
                close(oldPassword.getPassword(), newPassword.getPassword(), confirmPassword.getPassword(), hint.getText(), CloseEvent.Type.CLOSE);
            }
        });
        checkBox.addActionListener(actionEvent -> {
            ((PasswordField) oldPassword).setPasswordVisible(checkBox.isSelected());
            ((PasswordField) newPassword).setPasswordVisible(checkBox.isSelected());
            ((PasswordField) confirmPassword).setPasswordVisible(checkBox.isSelected());
        });
        panel.registerKeyboardAction(e -> close(oldPassword.getPassword(), newPassword.getPassword(), confirmPassword.getPassword(),
                hint.getText(), CloseEvent.Type.CLOSE), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        setResizable(false);
        setPreferredSize(new Dimension(360, 180));
        pack();
        setLocationRelativeTo(PassProtect.getInstance().getWindow());
        setVisible(true);
        setAlwaysOnTop(true);
        panel.requestFocus(FocusEvent.Cause.ACTIVATION);
    }

    private void close(@Nonnull char[] current, @Nonnull char[] newPassword, @Nonnull char[] confirm, @Nonnull String hint, @Nonnull CloseEvent.Type type) {
        boolean close = true;
        if (closeEvent != null) close = closeEvent.close(current, newPassword, confirm, hint, type);
        if (close) dispose();
    }

    private interface CloseEvent {
        boolean close(@Nonnull char[] current, @Nonnull char[] newPassword, @Nonnull char[] confirm, @Nonnull String hint, @Nonnull CloseEvent.Type type);

        enum Type {
            CLOSE, CANCEL, CONFIRM;

            public boolean isClose() {
                return equals(CLOSE);
            }

            public boolean isCancel() {
                return equals(CANCEL);
            }

            public boolean isConfirm() {
                return equals(CONFIRM);
            }
        }
    }
}
