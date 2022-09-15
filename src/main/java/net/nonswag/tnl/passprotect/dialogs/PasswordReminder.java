package net.nonswag.tnl.passprotect.dialogs;

import net.nonswag.tnl.passprotect.PassProtect;
import net.nonswag.tnl.passprotect.api.files.Config;
import net.nonswag.tnl.passprotect.api.files.Storage;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.TimeUnit;

public class PasswordReminder extends JDialog {
    @Nonnull
    private JPanel panel;
    @Nonnull
    private JButton yesButton, notNow;
    @Nonnull
    private JLabel line1, line2, line3, line4;

    public PasswordReminder(@Nonnull Storage storage, @Nonnull Config config, long l) {
        super(PassProtect.getInstance().getWindow(), "Security reminder");
        setContentPane(panel);
        getRootPane().setDefaultButton(yesButton);
        yesButton.addActionListener(e -> close(CloseEvent.Type.CONFIRM));
        notNow.addActionListener(e -> close(CloseEvent.Type.CANCEL));
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
        setModal(true);
        setLocationRelativeTo(PassProtect.getInstance().getWindow());
        line1.setText(line1.getText().formatted(TimeUnit.MILLISECONDS.toDays(l)));
        setVisible(true);
        setAlwaysOnTop(true);
        panel.requestFocus(FocusEvent.Cause.ACTIVATION);
    }

    private void close(@Nonnull CloseEvent.Type type) {
        dispose();
        if (!type.isConfirm()) return;
        Storage storage = Storage.getInstance();
        Config config = Config.getInstance();
        if (storage == null || config == null) return;
        new Thread(() -> new ChangePassword(storage, config), "change-password").start();
    }

    private interface CloseEvent {

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
