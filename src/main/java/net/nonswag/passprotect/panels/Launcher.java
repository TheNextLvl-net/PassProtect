package net.nonswag.passprotect.panels;

import lombok.Getter;
import net.nonswag.passprotect.Installer;
import net.nonswag.passprotect.PassProtect;
import net.nonswag.passprotect.Uninstaller;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.FileNotFoundException;

@Getter
public class Launcher extends Panel {
    @Nonnull
    private JPanel panel;
    @Nonnull
    private JCheckBox installedCheckBox, deleteUserDataCheckBox;
    @Nonnull
    private JRadioButton startRadioButton, installRadioButton, uninstallRadioButton, updateRadioButton;
    @Nonnull
    private JButton nextButton;

    public Launcher() {
        boolean installed = new File(System.getProperty("user.home"), ".pass-protect").exists();
        if (installed) uninstallRadioButton.setEnabled(true);
        else uninstallRadioButton.setToolTipText("PassProtect is not installed on this user account");
        if (!installed) deleteUserDataCheckBox.setToolTipText("PassProtect is not installed on this user account");
        if (net.nonswag.passprotect.Launcher.getFile() == null) {
            updateRadioButton.setToolTipText("Installation file not found");
            installRadioButton.setToolTipText("Installation file not found");
        } else {
            updateRadioButton.setEnabled(true);
            installRadioButton.setEnabled(true);
        }
        ActionListener action = actionEvent -> {
            installedCheckBox.setEnabled(startRadioButton.isSelected());
            if (!nextButton.isEnabled()) nextButton.setEnabled(true);
            deleteUserDataCheckBox.setEnabled(uninstallRadioButton.isSelected() && installed);
        }, next = actionEvent -> {
            if (startRadioButton.isSelected()) {
                net.nonswag.passprotect.Launcher.start(installedCheckBox.isSelected());
            } else if (installRadioButton.isSelected()) {
                try {
                    Installer.init(false);
                } catch (FileNotFoundException e) {
                    PassProtect.showErrorDialog("Failed to install PassProtect", e);
                }
            } else if (uninstallRadioButton.isSelected()) {
                try {
                    if (installed) Uninstaller.init(deleteUserDataCheckBox.isSelected());
                } catch (FileNotFoundException e) {
                    PassProtect.showErrorDialog("Failed to uninstall PassProtect", e);
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
