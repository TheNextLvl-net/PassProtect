package net.nonswag.tnl.passprotect.dialogs;

import net.nonswag.tnl.core.utils.StringUtil;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Randomizer extends JDialog {
    @Nonnull
    private JPanel panel;
    @Nonnull
    private JButton buttonOK, buttonCancel;
    @Nonnull
    private JCheckBox symbols, numbers, ambiguous;
    @Nonnull
    private JSpinner spinner;
    @Nonnull
    private JComboBox<?> comboBox;

    @Nonnull
    private final CloseEvent closeEvent;

    public Randomizer(@Nonnull Dialog owner, @Nonnull CloseEvent closeEvent) {
        super(owner, "Password Generator");
        this.closeEvent = closeEvent;
        setModal(true);
        spinner.setValue(16);
        registerListeners();
        setContentPane(panel);
        getRootPane().setDefaultButton(buttonOK);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(false);
        setPreferredSize(new Dimension(360, 180));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        setAlwaysOnTop(true);
        panel.requestFocus(FocusEvent.Cause.ACTIVATION);
    }

    private void registerListeners() {
        buttonOK.addActionListener(e -> close(getPassword(), CloseEvent.Type.CONFIRM));
        buttonCancel.addActionListener(e -> close(getPassword(), CloseEvent.Type.CANCEL));
        panel.registerKeyboardAction(e -> close(getPassword(), CloseEvent.Type.CLOSE), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(@Nonnull WindowEvent event) {
                close(getPassword(), CloseEvent.Type.CLOSE);
            }
        });
        spinner.addChangeListener(changeEvent -> {
            if (((int) spinner.getValue()) < 6) spinner.setValue(6);
            else if (((int) spinner.getValue()) > 2048) spinner.setValue(2048);
        });
    }

    @Nonnull
    private String getPassword() {
        int length = (int) spinner.getValue();
        StringBuilder chars = new StringBuilder();
        if (comboBox.getSelectedIndex() == 0) chars.append("aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTuUvVwWxXyYzZ");
        else if (comboBox.getSelectedIndex() == 1) chars.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        else if (comboBox.getSelectedIndex() == 2) chars.append("abcdefghijklmnopqrstuvwxyz");
        if (numbers.isSelected()) chars.append("0123456789");
        if (ambiguous.isSelected()) chars.append("^°{}[]()/|\\'\"`´~,;:.<>µ");
        if (symbols.isSelected()) chars.append("@#€$%§!?*=-+_");
        return StringUtil.random(chars.toString().toCharArray(), length);
    }

    private void close(@Nonnull String password, @Nonnull CloseEvent.Type type) {
        closeEvent.close(password, type);
        dispose();
    }

    public interface CloseEvent {
        void close(@Nonnull String password, @Nonnull CloseEvent.Type type);

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
