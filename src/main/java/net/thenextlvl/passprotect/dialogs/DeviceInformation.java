package net.thenextlvl.passprotect.dialogs;

import net.thenextlvl.adb.Device;
import net.thenextlvl.passprotect.api.files.TrustedDevices;
import net.thenextlvl.adb.AdbException;
import net.thenextlvl.passprotect.api.files.Config;
import net.thenextlvl.passprotect.api.files.Storage;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class DeviceInformation extends JDialog {
    @Nonnull
    private JPanel panel, information;
    @Nonnull
    private JButton buttonOK, untrust;

    public DeviceInformation(Preferences preferences, TrustedDevices.Device device, Config config, Storage storage) {
        super(preferences, device.getName());
        setModal(true);
        setContentPane(panel);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                dispose();
            }
        });
        panel.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        buttonOK.addActionListener(actionEvent -> dispose());
        setResizable(false);
        setPreferredSize(new Dimension(360, 180));
        pack();
        setLocationRelativeTo(preferences);
        setupWindow(preferences, device, config, storage);
        setVisible(true);
        setAlwaysOnTop(true);
    }

    private void setupWindow(Preferences preferences, TrustedDevices.Device device, Config config, Storage storage) {
        try {
            Device physicalDevice = new Device(device.getSerialNumber());
            information.add(new JLabel("Status: Connected"));
            information.add(new JLabel("Name: ".concat(device.getName())));
            information.add(new JLabel("IP-Address: ".concat(physicalDevice.getIpAddress())));
            information.add(new JLabel("Mac-Address: ".concat(device.getMacAddress())));
            information.add(new JLabel("Battery: %s%%".formatted(physicalDevice.getBatteryLevel())));
        } catch (AdbException ignored) {
            information.add(new JLabel("Status: Disconnected"));
            information.add(new JLabel("Name: ".concat(device.getName())));
        } finally {
            untrust.addActionListener(actionEvent -> {
                dispose();
                preferences.dispose();
                config.getTrustedDevices().untrust(device);
                new Preferences(config, storage, 2);
            });
        }
    }

    private void createUIComponents() {
        information = new JPanel(new GridLayout(5, 1));
    }
}
