package net.thenextlvl.passprotect.dialogs.entries;

import net.thenextlvl.core.api.object.Pair;
import net.thenextlvl.passprotect.PassProtect;
import net.thenextlvl.passprotect.api.entry.Password;
import net.thenextlvl.passprotect.api.fields.PasswordField;
import net.thenextlvl.passprotect.api.fields.TextField;
import net.thenextlvl.passprotect.api.files.Config;
import net.thenextlvl.passprotect.api.renderer.TreeIconRenderer;
import net.thenextlvl.passprotect.dialogs.Randomizer;
import net.thenextlvl.passprotect.util.Clipboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class PasswordEntry extends JDialog {
    @Nonnull
    private JPanel panel;
    @Nonnull
    private JButton buttonOK, buttonCancel, randomizer, copy;
    @Nonnull
    private JPasswordField password;
    @Nonnull
    private JTextField name, description;
    @Nonnull
    private JCheckBox checkBox;
    @Nonnull
    private JLabel userIcon, descriptionIcon, passwordIcon;
    @Nonnull
    private JProgressBar safety;

    @Nonnull
    private final CloseEvent closeEvent;
    @Nullable
    private Runnable onSuccess;

    public PasswordEntry(Password password, Config config, CloseEvent closeEvent) {
        super(PassProtect.getInstance().getWindow(), password.getName().isEmpty() ? null : password.getName());
        if (!password.getName().isEmpty()) name.setText(password.getName());
        if (password.getPassword().length != 0) {
            this.password.setText(new String(password.getPassword()));
            copy.setVisible(true);
        } else randomizer.setVisible(true);
        if (password.getDescription() != null && !password.getDescription().isEmpty()) {
            description.setText(password.getDescription());
        }
        this.closeEvent = closeEvent;
        updateSafety();
        registerListeners();
        userIcon.setIcon(TreeIconRenderer.Logo.USER.getIcon(config));
        descriptionIcon.setIcon(TreeIconRenderer.Logo.HINT.getIcon(config));
        passwordIcon.setIcon(TreeIconRenderer.Logo.PASSWORD.getIcon(config));
        setContentPane(panel);
        getRootPane().setDefaultButton(buttonOK);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(false);
        setPreferredSize(new Dimension(360, 180));
        pack();
        setLocationRelativeTo(PassProtect.getInstance().getWindow());
        setVisible(true);
        setAlwaysOnTop(true);
        panel.requestFocus(FocusEvent.Cause.ACTIVATION);
    }

    private void updateSafety() {
        String password = String.valueOf(this.password.getPassword());
        Pair<Integer, String> pair = Password.Lookup.test(password);
        this.password.setToolTipText(pair.getValue());
        safety.setValue(Math.max(pair.getKey(), 0));
    }

    @Nullable
    private char[] lastPassword;

    private void registerListeners() {
        buttonOK.addActionListener(e -> close(CloseEvent.Type.CONFIRM));
        buttonCancel.addActionListener(e -> close(CloseEvent.Type.CANCEL));
        checkBox.addActionListener(actionEvent -> ((PasswordField) password).setPasswordVisible(checkBox.isSelected()));
        panel.registerKeyboardAction(e -> close(CloseEvent.Type.CLOSE), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        copy.addActionListener(actionEvent -> {
            Clipboard.setContent(String.valueOf(password.getPassword()), (clipboard, transferable) -> copy.setEnabled(true));
            lastPassword = password.getPassword();
            copy.setEnabled(false);
        });
        randomizer.addActionListener(actionEvent -> new Randomizer(this, (password, type) -> {
            if (!type.isConfirm() || password.isEmpty()) return;
            this.password.requestFocus();
            this.password.setText(password);
            this.randomizer.setVisible(false);
            this.copy.setVisible(true);
            updateSafety();
        }));
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                close(CloseEvent.Type.CLOSE);
            }
        });
        password.addKeyListener(new KeyAdapter() {

            @Override
            public void keyTyped(KeyEvent event) {
                update();
            }

            @Override
            public void keyPressed(KeyEvent event) {
                update();
            }

            @Override
            public void keyReleased(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_UP) description.requestFocus();
                update();
            }

            private void update() {
                char[] password = PasswordEntry.this.password.getPassword();
                if (!Arrays.equals(lastPassword, password)) copy.setEnabled(true);
                randomizer.setVisible(password.length == 0);
                copy.setVisible(!randomizer.isVisible());
                updateSafety();
            }
        });
        description.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_UP) name.requestFocus();
                else if (event.getKeyCode() == KeyEvent.VK_DOWN) password.requestFocus();
            }
        });
        name.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_DOWN) description.requestFocus();
            }
        });
    }

    @Nonnull
    public PasswordEntry onSuccess(@Nullable Runnable onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }

    public void close(CloseEvent.Type type) {
        if (!closeEvent.close(getPassword(), type)) return;
        if (onSuccess != null) onSuccess.run();
        dispose();
    }

    @Nonnull
    public Password getPassword() {
        return new Password(name.getText(), description.getText(), String.valueOf(this.password.getPassword()).getBytes(StandardCharsets.UTF_8));
    }

    private void createUIComponents() {
        name = new net.thenextlvl.passprotect.api.fields.TextField().setPlaceholder("Login name");
        description = new TextField().setPlaceholder("Description");
        password = new PasswordField().setPlaceholder("Password");
    }

    public interface CloseEvent {
        boolean close(Password password, Type type);

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
