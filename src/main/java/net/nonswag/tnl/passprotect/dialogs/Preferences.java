package net.nonswag.tnl.passprotect.dialogs;

import net.nonswag.tnl.core.api.file.helper.FileHelper;
import net.nonswag.tnl.core.utils.LinuxUtil;
import net.nonswag.tnl.passprotect.Launcher;
import net.nonswag.tnl.passprotect.PassProtect;
import net.nonswag.tnl.passprotect.Uninstaller;
import net.nonswag.tnl.passprotect.api.files.Config;
import net.nonswag.tnl.passprotect.api.files.Storage;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Preferences extends JDialog {

    @Nonnull
    private JPanel panel, general, appearance;
    @Nonnull
    private JScrollPane appearanceScrollBar;
    @Nonnull
    private JButton changePassword, deleteUser, install, uninstall;

    public Preferences(@Nonnull Config config, @Nonnull Storage storage) {
        super(PassProtect.getInstance().getWindow(), "Preferences");
        setModal(true);
        setContentPane(panel);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(@Nonnull WindowEvent event) {
                dispose();
            }
        });
        panel.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        setResizable(false);
        setPreferredSize(new Dimension(360, 180));
        pack();
        setLocationRelativeTo(PassProtect.getInstance().getWindow());
        setupWindow(config, storage);
        setVisible(true);
        setAlwaysOnTop(true);
    }

    private void setupWindow(@Nonnull Config config, @Nonnull Storage storage) {
        install.setEnabled(!PassProtect.isInstalled() && Launcher.getFile() != null);
        uninstall.setEnabled(PassProtect.isInstalled());
        install.setToolTipText(install.isEnabled() ? null : PassProtect.isInstalled() ? "PassProtect is already installed" : "Installation file not found");
        uninstall.setToolTipText(uninstall.isEnabled() ? null : "PassProtect is not installed");
        install.addActionListener(actionEvent -> {
            java.io.File installer = Launcher.getFile();
            if (installer != null) {
                try {
                    PassProtect.setInstalled(LinuxUtil.runShellCommand("java -jar " + installer.getAbsolutePath() + " install") == 0);
                    install.setEnabled(!PassProtect.isInstalled());
                } catch (IOException | InterruptedException e) {
                    PassProtect.showErrorDialog("Failed to install PassProtect", e);
                }
            } else PassProtect.showErrorDialog("Found no installation file");
        });
        uninstall.addActionListener(actionEvent -> {
            try {
                if (JOptionPane.showConfirmDialog(PassProtect.getInstance().getWindow(), "Do you really want to uninstall PassProtect?", null, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    Uninstaller.init(null);
                }
            } catch (FileNotFoundException e) {
                PassProtect.showErrorDialog("Failed to uninstall PassProtect", e);
            }
        });
        deleteUser.addActionListener(actionEvent -> {
            if (JOptionPane.showConfirmDialog(PassProtect.getInstance().getWindow(), "Do you really want to delete this user\nThis cannot be undone", "Delete user", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (FileHelper.delete(storage.getFile().getAbsoluteFile().getParentFile())) {
                    Config.setInstance(null);
                    Storage.setInstance(null);
                    PassProtect.getInstance().setLoggedIn(false);
                } else PassProtect.showErrorDialog("Failed to delete user '%s'".formatted(storage.getUser()));
            }
        });
        appearanceScrollBar.getVerticalScrollBar().setUnitIncrement(15);
        for (int index = 0; index < Launcher.getLookAndFeels().size(); index++) {
            UIManager.LookAndFeelInfo theme = Launcher.getLookAndFeels().get(index);
            JButton button = new JButton(theme.getName());
            button.setEnabled(!UIManager.getLookAndFeel().getClass().getName().equals(theme.getClassName()));
            button.addActionListener(actionEvent -> {
                config.setAppearance(theme);
                Launcher.applyAppearance(config);
                PassProtect.getInstance().setLoggedIn(true);
                new Preferences(config, storage);
            });
            appearance.add(button);
        }
        changePassword.addActionListener(actionEvent -> new ChangePassword(storage, config));
    }

    private void createUIComponents() {
        appearance = new JPanel(new GridLayout(Launcher.getLookAndFeels().size(), 1));
    }
}
