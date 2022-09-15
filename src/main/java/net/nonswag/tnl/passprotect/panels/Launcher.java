package net.nonswag.tnl.passprotect.panels;

import lombok.Getter;
import net.nonswag.tnl.passprotect.Installer;
import net.nonswag.tnl.passprotect.PassProtect;
import net.nonswag.tnl.passprotect.Shortcut;
import net.nonswag.tnl.passprotect.Uninstaller;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

@Getter
public class Launcher extends Panel {
    @Nonnull
    private JPanel panel;
    @Nonnull
    private JCheckBox installedCheckBox, deleteUserDataCheckBox, commandLineShortcutCheckBox, deepEraseCheckBox;
    @Nonnull
    private JRadioButton startRadioButton, installRadioButton, uninstallRadioButton, updateRadioButton;
    @Nonnull
    private JButton nextButton;

    @SuppressWarnings({"ReturnInsideFinallyBlock"})
    public Launcher() {
        boolean root = "root".equals(System.getenv("USER"));
        if (!root) {
            deepEraseCheckBox.setToolTipText("This option requires root access");
            commandLineShortcutCheckBox.setToolTipText("This option requires root access");
        }
        boolean installed = new File(System.getProperty("user.home"), ".pass-protect").exists();
        if (installed || root) uninstallRadioButton.setEnabled(true);
        else uninstallRadioButton.setToolTipText("PassProtect is not installed on this user account");
        if (!installed) deleteUserDataCheckBox.setToolTipText("PassProtect is not installed on this user account");
        if (net.nonswag.tnl.passprotect.Launcher.getFile() == null) {
            updateRadioButton.setToolTipText("Installation file not found");
            installRadioButton.setToolTipText("Installation file not found");
        } else {
            updateRadioButton.setEnabled(true);
            installRadioButton.setEnabled(true);
        }
        ActionListener action = actionEvent -> {
            installedCheckBox.setEnabled(startRadioButton.isSelected());
            commandLineShortcutCheckBox.setEnabled(installRadioButton.isSelected() && root);
            if (!nextButton.isEnabled()) nextButton.setEnabled(true);
            deleteUserDataCheckBox.setEnabled(uninstallRadioButton.isSelected() && installed);
            deepEraseCheckBox.setEnabled(uninstallRadioButton.isSelected() && root);
        }, next = actionEvent -> {
            if (startRadioButton.isSelected()) {
                net.nonswag.tnl.passprotect.Launcher.start(installedCheckBox.isSelected());
            } else if (installRadioButton.isSelected()) {
                try {
                    try {
                        if (!commandLineShortcutCheckBox.isSelected()) return;
                        if (Shortcut.init() == 0) {
                            JOptionPane.showMessageDialog(PassProtect.getInstance().getWindow(), "Successfully registered the command shortcut", "Installer", JOptionPane.INFORMATION_MESSAGE);
                        } else PassProtect.showErrorDialog("Failed to register the command shortcut properly");
                    } finally {
                        Installer.init(false);
                    }
                } catch (FileNotFoundException e) {
                    PassProtect.showErrorDialog("Failed to install PassProtect", e);
                } catch (IOException | InterruptedException e) {
                    PassProtect.showErrorDialog("Failed to register the command shortcut", e);
                }
            } else if (uninstallRadioButton.isSelected()) {
                try {
                    if (installed) Uninstaller.init(deleteUserDataCheckBox.isSelected());
                } catch (FileNotFoundException e) {
                    PassProtect.showErrorDialog("Failed to uninstall PassProtect", e);
                } finally {
                    if (!deepEraseCheckBox.isSelected() || (Uninstaller.deepErase() && installed)) return;
                    if (!installed) {
                        JOptionPane.showMessageDialog(PassProtect.getInstance().getWindow(), "Successfully uninstalled PassProtect deeply", "Uninstaller", JOptionPane.INFORMATION_MESSAGE);
                    } else PassProtect.showErrorDialog("Failed to deeply erase PassProtect's data");
                }
            } else if (updateRadioButton.isSelected()) {
                try {
                    Installer.init(true);
                } catch (FileNotFoundException e) {
                    PassProtect.showErrorDialog("Failed to update PassProtect", e);
                }
            }
        };
        startRadioButton.addActionListener(action);
        installRadioButton.addActionListener(action);
        uninstallRadioButton.addActionListener(action);
        updateRadioButton.addActionListener(action);
        nextButton.addActionListener(next);
    }

    @Nonnull
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 340);
    }

    public static void init() {
        Panel panel = new Launcher();
        JFrame window = new JFrame(panel.getName());
        PassProtect.getInstance().setWindow(window);
        window.setContentPane(panel.getPanel());
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(panel.isResizable());
        window.setPreferredSize(panel.getPreferredSize());
        window.setMinimumSize(window.getPreferredSize());
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        if (PassProtect.getInstance().getIcon() != null) window.setIconImage(PassProtect.getInstance().getIcon());
        window.requestFocus(FocusEvent.Cause.ACTIVATION);
        panel.onFocus();
    }
}
