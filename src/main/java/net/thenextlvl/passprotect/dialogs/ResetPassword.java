package net.thenextlvl.passprotect.dialogs;

import net.thenextlvl.core.api.file.helper.FileHelper;
import net.thenextlvl.passprotect.PassProtect;
import net.thenextlvl.passprotect.api.files.Config;
import net.thenextlvl.passprotect.api.renderer.TreeIconRenderer;

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

public class ResetPassword extends JDialog {
    @Nonnull
    private JPanel panel;
    @Nonnull
    private JButton reset, cancel;
    @Nonnull
    private JLabel hint;
    @Nonnull
    private JCheckBox checkBox;
    @Nullable
    private final CloseEvent closeEvent;

    public ResetPassword(Config config) {
        super(PassProtect.getInstance().getWindow(), "Reset password");
        this.closeEvent = getCloseEvent(config);
        hint.setIcon(TreeIconRenderer.Logo.HINT.getIcon(Config.APP));
        setModal(true);
        setContentPane(panel);
        getRootPane().setDefaultButton(reset);
        reset.addActionListener(e -> close(CloseEvent.Type.CONFIRM));
        cancel.addActionListener(e -> close(CloseEvent.Type.CANCEL));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                close(CloseEvent.Type.CLOSE);
            }
        });
        panel.registerKeyboardAction(e -> close(CloseEvent.Type.CLOSE), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        setupWindow(config);
        setResizable(false);
        setPreferredSize(new Dimension(360, 180));
        pack();
        setLocationRelativeTo(PassProtect.getInstance().getWindow());
        setVisible(true);
        setAlwaysOnTop(true);
    }

    private void setupWindow(Config config) {
        String hint = config.getHint();
        if (hint != null && !hint.isEmpty()) this.hint.setText(hint);
    }

    @Nonnull
    private CloseEvent getCloseEvent(Config config) {
        return type -> {
            if (!type.isConfirm()) return true;
            File dir = new File(config.getUsername());
            File saves = new File(dir, "saves.pp");
            if (saves.exists() && saves.isFile() && checkBox.isVisible() && checkBox.isSelected()) {
                String text = JOptionPane.showInputDialog(this, "To reset enter: CONFIRM");
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
                    PassProtect.getInstance().setLoggedIn(false);
                }
            } else if (!saves.exists() || !saves.isFile()) {
                PassProtect.invalidUser(config.getUsername());
                dispose();
            } else if (checkBox.isVisible()) Toolkit.getDefaultToolkit().beep();
            else checkBox.setVisible(true);
            if (checkBox.isVisible() && !checkBox.isSelected()) checkBox.requestFocus();
            return false;
        };
    }

    private void close(CloseEvent.Type type) {
        boolean close = true;
        if (closeEvent != null) close = closeEvent.close(type);
        if (close) dispose();
    }

    private interface CloseEvent {
        boolean close(CloseEvent.Type type);

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
