package net.nonswag.tnl.passprotect.dialogs;

import net.nonswag.tnl.adb.ADB;
import net.nonswag.tnl.adb.Device;
import net.nonswag.tnl.core.api.file.helper.FileHelper;
import net.nonswag.tnl.core.utils.LinuxUtil;
import net.nonswag.tnl.core.utils.SystemUtil;
import net.nonswag.tnl.passprotect.Launcher;
import net.nonswag.tnl.passprotect.PassProtect;
import net.nonswag.tnl.passprotect.Uninstaller;
import net.nonswag.tnl.passprotect.api.files.Config;
import net.nonswag.tnl.passprotect.api.files.Storage;
import net.nonswag.tnl.passprotect.api.files.TrustedDevices;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Preferences extends JDialog {

    @Nonnull
    private JTabbedPane tab;
    @Nonnull
    private JPanel panel, general, appearance, trustedDevices;
    @Nonnull
    private JPanel trustedDevicesPanel;
    @Nonnull
    private JScrollPane appearanceScrollBar, trustedDevicesScrollBar;
    @Nonnull
    private JButton changePassword, deleteUser, install, uninstall;
    private JButton changeHint;

    public Preferences(@Nonnull Config config, @Nonnull Storage storage, int tab) {
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
        this.tab.setSelectedIndex(tab);
        setVisible(true);
        setAlwaysOnTop(true);
    }

    private void setupWindow(@Nonnull Config config, @Nonnull Storage storage) {
        install.setEnabled(!PassProtect.isInstalled() && Launcher.getFile() != null);
        uninstall.setEnabled(PassProtect.isInstalled());
        install.setToolTipText(install.isEnabled() ? null : PassProtect.isInstalled() ? "PassProtect is already installed" : "Installation file not found");
        uninstall.setToolTipText(uninstall.isEnabled() ? null : "PassProtect is not installed");
        if (install.getActionListeners().length == 0) install.addActionListener(actionEvent -> {
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
        if (uninstall.getActionListeners().length == 0) uninstall.addActionListener(actionEvent -> {
            try {
                if (JOptionPane.showConfirmDialog(PassProtect.getInstance().getWindow(), "Do you really want to uninstall PassProtect?", null, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    Uninstaller.init(null);
                }
            } catch (FileNotFoundException e) {
                PassProtect.showErrorDialog("Failed to uninstall PassProtect", e);
            }
        });
        if (deleteUser.getActionListeners().length == 0) deleteUser.addActionListener(actionEvent -> {
            if (JOptionPane.showConfirmDialog(PassProtect.getInstance().getWindow(), "Do you really want to delete this user\nThis cannot be undone", "Delete user", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (FileHelper.delete(storage.getFile().getAbsoluteFile().getParentFile())) {
                    Config.setInstance(null);
                    Storage.setInstance(null);
                    PassProtect.getInstance().setLoggedIn(false);
                } else PassProtect.showErrorDialog("Failed to delete user '%s'".formatted(storage.getUser()));
            }
        });
        trustedDevicesScrollBar.getVerticalScrollBar().setUnitIncrement(15);
        this.trustedDevices.removeAll();
        if (SystemUtil.TYPE.isLinux()) try {
            TrustedDevices trustedDevices = config.getTrustedDevices();
            trustedDevices.getDevices().forEach(device -> {
                JButton button = new JButton(device.getName());
                button.addActionListener(actionEvent -> new DeviceInformation(this, device, config, storage));
                this.trustedDevices.add(button);
            });
            List<Device> devices = new ArrayList<>();
            ADB.getDevices().forEach(device -> {
                if (!trustedDevices.isTrusted(device)) devices.add(device);
            });
            if (trustedDevices.hasTrustedDevices() && !devices.isEmpty()) this.trustedDevices.add(new JSeparator());
            devices.forEach(device -> {
                JButton button = new JButton("Trust ".concat(device.getModel()));
                button.addActionListener(actionEvent -> {
                    try {
                        trustedDevices.trust(device, new String(storage.getSecurityKey()));
                        setupWindow(config, storage);
                    } catch (Exception e) {
                        PassProtect.showErrorDialog("Failed to trust device", e);
                    }
                });
                this.trustedDevices.add(button);
            });
            if (devices.isEmpty() && !config.getTrustedDevices().hasTrustedDevices()) {
                JLabel label = new JLabel("Connect your phone via usb to your computer");
                label.setToolTipText("Android required");
                this.trustedDevices.add(label);
            }
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().startsWith("Cannot run program")) {
                trustedDevices.add(new JLabel("To use this feature you have to install ADB"));
            } else if (e.getMessage() != null) trustedDevices.add(new JLabel(e.getMessage()));
            else trustedDevices.add(new JLabel("Something went wrong"));
        }
        else tab.setEnabledAt(2, false);
        appearance.removeAll();
        appearanceScrollBar.getVerticalScrollBar().setUnitIncrement(15);
        for (int index = 0; index < Launcher.getLookAndFeels().size(); index++) {
            UIManager.LookAndFeelInfo theme = Launcher.getLookAndFeels().get(index);
            JButton button = new JButton(theme.getName());
            button.setEnabled(!UIManager.getLookAndFeel().getClass().getName().equals(theme.getClassName()));
            button.addActionListener(actionEvent -> {
                config.setAppearance(theme);
                Launcher.applyAppearance(config);
                setupWindow(config, storage);
            });
            appearance.add(button);
        }
        if (changePassword.getActionListeners().length == 0) {
            changePassword.addActionListener(actionEvent -> new ChangePassword(storage, config));
        }
        if (changeHint.getActionListeners().length == 0) changeHint.addActionListener(actionEvent -> {
            String hint = (String) JOptionPane.showInputDialog(this, "Enter a new hint", "Change hint", JOptionPane.INFORMATION_MESSAGE, null, null, config.getHint());
            if (hint != null) config.setHint(hint);
        });
    }

    private void createUIComponents() {
        trustedDevices = new JPanel(new GridLayout(5, 1));
        appearance = new JPanel(new GridLayout(Launcher.getLookAndFeels().size(), 1));
    }
}
