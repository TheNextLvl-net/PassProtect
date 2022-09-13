package net.nonswag.tnl.passprotect.dialogs;

import net.nonswag.tnl.core.api.file.helper.FileHelper;
import net.nonswag.tnl.passprotect.Launcher;
import net.nonswag.tnl.passprotect.PassProtect;
import net.nonswag.tnl.passprotect.api.files.Config;
import net.nonswag.tnl.passprotect.api.renderer.TreeIconRenderer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class ResetPassword extends JDialog {
    @Nonnull
    private JPanel panel;
    @Nonnull
    private JButton reset, cancel;
    @Nonnull
    private JLabel hint;
    @Nonnull
    private JCheckBox checkBox;
    @Nonnull
    private JComboBox<String> username;
    @Nonnull
    private JLabel usernameIcon, hintIcon;

    @Nullable
    private final CloseEvent closeEvent = type -> {
        if (!type.isConfirm()) return true;
        File dir = new File(username.getSelectedItem() == null ? "" : (String) username.getSelectedItem());
        File saves = new File(dir, "saves.pp");
        if (saves.exists() && saves.isFile() && checkBox.isVisible() && checkBox.isSelected()) {
            String text = JOptionPane.showInputDialog(PassProtect.getInstance().getWindow(), "To reset enter: CONFIRM");
            if (!"CONFIRM".equals(text)) return false;
            try {
                if (!saves.exists() || !saves.isFile()) return true;
                File reset = new File(".trashed", dir.getName());
                FileHelper.createDirectory(reset);
                Files.move(dir.toPath(), reset.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return true;
            } catch (Exception e) {
                PassProtect.showErrorDialog("Something went wrong while resetting the users files", e);
                return false;
            } finally {
                Launcher.applyAppearance(Config.APP);
                PassProtect.getInstance().setLoggedIn(false);
            }
        } else if (!saves.exists() || !saves.isFile()) {
            PassProtect.invalidUser((String) username.getSelectedItem());
            username.setSelectedItem(null);
            username.requestFocus();
        } else if (checkBox.isVisible()) Toolkit.getDefaultToolkit().beep();
        else if (!hint.isVisible()) {
            String hint = new Config((String) username.getSelectedItem()).getHint();
            if (hint != null && !hint.isEmpty()) this.hint.setText(hint);
            this.hint.setVisible(true);
            this.hintIcon.setVisible(true);
            this.reset.setText("Reset");
            this.username.setEnabled(false);
        } else checkBox.setVisible(true);
        if (checkBox.isVisible() && !checkBox.isSelected()) checkBox.requestFocus();
        return false;
    };

    public ResetPassword(@Nullable String username) {
        super((Frame) null, "Reset password");
        this.username.setSelectedItem(username == null || username.isEmpty() ? null : username);
        List<String> users = PassProtect.retrieveUsers();
        if (!users.isEmpty()) for (String user : users) this.username.addItem(user);
        usernameIcon.setIcon(TreeIconRenderer.Logo.USER.getIcon(Config.APP));
        hintIcon.setIcon(TreeIconRenderer.Logo.HINT.getIcon(Config.APP));
        setModal(true);
        setContentPane(panel);
        getRootPane().setDefaultButton(reset);
        reset.addActionListener(e -> close(CloseEvent.Type.CONFIRM));
        cancel.addActionListener(e -> close(CloseEvent.Type.CANCEL));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(@Nonnull WindowEvent event) {
                close(CloseEvent.Type.CLOSE);
            }
        });
        panel.registerKeyboardAction(e -> close(CloseEvent.Type.CLOSE), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        setResizable(false);
        setPreferredSize(new Dimension(360, 180));
        pack();
        setLocationRelativeTo(PassProtect.getInstance().getWindow());
        setVisible(true);
        setAlwaysOnTop(true);
    }

    private void close(@Nonnull CloseEvent.Type type) {
        boolean close = true;
        if (closeEvent != null) close = closeEvent.close(type);
        if (close) dispose();
    }

    private interface CloseEvent {
        boolean close(@Nonnull CloseEvent.Type type);

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
