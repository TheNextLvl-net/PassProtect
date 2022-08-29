package net.nonswag.tnl.passprotect.dialogs.entries;

import net.nonswag.tnl.core.api.object.Getter;
import net.nonswag.tnl.passprotect.api.entry.BackupCode;
import net.nonswag.tnl.passprotect.api.fields.TextField;
import net.nonswag.tnl.passprotect.api.files.Config;
import net.nonswag.tnl.passprotect.api.renderer.TreeIconRenderer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class BackupCodeEntry extends JDialog {
    @Nonnull
    private JPanel panel;
    @Nonnull
    private JButton buttonOK, buttonCancel;
    @Nonnull
    private JLabel nameIcon, codesIcon, limit;
    @Nonnull
    private JTextField name;
    @Nonnull
    private JTextArea codes;

    @Nonnull
    private final CloseEvent closeEvent;
    @Nullable
    private Runnable onSuccess;

    public BackupCodeEntry(@Nonnull BackupCode backupCode, @Nonnull Config config, @Nonnull CloseEvent closeEvent) {
        super((Frame) null, backupCode.getName().isEmpty() ? null : backupCode.getName());
        this.closeEvent = closeEvent;
        if (!backupCode.getName().isEmpty()) name.setText(backupCode.getName());
        if (backupCode.getCodes().length != 0) this.codes.setText(String.join("\n", backupCode.asStringArray()));
        registerListeners();
        limit.setText("Limit: %s/32".formatted(backupCode.getCodes().length));
        nameIcon.setIcon(TreeIconRenderer.Logo.USER.getIcon(config));
        codesIcon.setIcon(TreeIconRenderer.Logo.PASSWORD.getIcon(config));
        setContentPane(panel);
        getRootPane().setDefaultButton(buttonOK);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setPreferredSize(new Dimension(360, 210));
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        setAlwaysOnTop(true);
        panel.requestFocus(FocusEvent.Cause.ACTIVATION);
    }

    private void registerListeners() {
        buttonOK.addActionListener(e -> close(CloseEvent.Type.CONFIRM));
        buttonCancel.addActionListener(e -> close(CloseEvent.Type.CANCEL));
        panel.registerKeyboardAction(e -> close(CloseEvent.Type.CLOSE), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(@Nonnull WindowEvent event) {
                close(CloseEvent.Type.CLOSE);
            }
        });
        name.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(@Nonnull KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_DOWN) codes.requestFocus();
            }
        });
        codes.addFocusListener(new FocusAdapter() {

            @Nullable
            private Robot robot;
            private boolean previous = false;

            {
                try {
                    robot = new Robot();
                } catch (AWTException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void focusGained(@Nonnull FocusEvent event) {
                previous = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_NUM_LOCK);
                if (!previous) toggle(true);
            }

            @Override
            public void focusLost(@Nonnull FocusEvent event) {
                if (!previous) toggle(false);
            }

            private void toggle(boolean on) {
                if (robot == null) return;
                boolean current = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_NUM_LOCK);
                if ((current && previous && on) || (!current && !previous && !on)) return;
                robot.keyPress(KeyEvent.VK_NUM_LOCK);
                robot.keyRelease(KeyEvent.VK_NUM_LOCK);
                previous = current;
            }
        });
        codes.addKeyListener(new KeyAdapter() {

            @Override
            public void keyTyped(@Nonnull KeyEvent event) {
                key(event);
            }

            @Override
            public void keyPressed(@Nonnull KeyEvent event) {
                key(event);
            }

            @Override
            public void keyReleased(@Nonnull KeyEvent event) {
                key(event);
            }

            private void key(@Nonnull KeyEvent event) {
                int codes = getCodes().size();
                if (codes <= 32) limit.setText("Limit: %s/32".formatted(codes));
                else limit.setText("<html><span style='color:red;'>Limit: %s/32</span></html>".formatted(codes));
                if (event.getKeyChar() != ' ') return;
                event.setKeyCode(KeyEvent.VK_ENTER);
                event.setKeyChar((char) 0);
            }
        });
    }

    @Nonnull
    public BackupCodeEntry onSuccess(@Nullable Runnable onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }

    public void close(@Nonnull CloseEvent.Type type) {
        if (!closeEvent.close(this::getBackupCode, type)) return;
        if (onSuccess != null) onSuccess.run();
        dispose();
    }

    @Nonnull
    public BackupCode getBackupCode() {
        return new BackupCode(name.getText(), getCodes().toArray(new String[]{}));
    }

    @Nonnull
    private List<String> getCodes() {
        String[] split = this.codes.getText().replace(" ", "\n").split("\n");
        List<String> codes = new ArrayList<>();
        for (String code : split) if (!code.isEmpty()) codes.add(code);
        return codes;
    }

    private void createUIComponents() {
        this.name = new TextField().setPlaceholder("Login name");
    }

    public interface CloseEvent {
        boolean close(@Nonnull Getter<BackupCode> backupCode, @Nonnull CloseEvent.Type type);

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
