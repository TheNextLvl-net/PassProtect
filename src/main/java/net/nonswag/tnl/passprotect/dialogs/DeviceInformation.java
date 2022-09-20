package net.nonswag.tnl.passprotect.dialogs;

import net.nonswag.tnl.adb.AdbException;
import net.nonswag.tnl.adb.Device;
import net.nonswag.tnl.adb.DeviceReference;
import net.nonswag.tnl.passprotect.api.files.Config;
import net.nonswag.tnl.passprotect.api.files.Storage;

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

    public DeviceInformation(@Nonnull Preferences preferences, @Nonnull DeviceReference reference, @Nonnull Config config, @Nonnull Storage storage) {
        super(preferences, reference.model());
        setModal(true);
        setContentPane(panel);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(@Nonnull WindowEvent event) {
                dispose();
            }
        });
        panel.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        buttonOK.addActionListener(actionEvent -> dispose());
        setResizable(false);
        setPreferredSize(new Dimension(360, 180));
        pack();
        setLocationRelativeTo(preferences);
        setupWindow(preferences, reference, config, storage);
        setVisible(true);
        setAlwaysOnTop(true);
    }

    private void setupWindow(@Nonnull Preferences preferences, @Nonnull DeviceReference reference, @Nonnull Config config, @Nonnull Storage storage) {
        try {
            Device device = new Device(reference.serialNumber(), reference.model());
            String battery = "Battery: %s%%".formatted(device.getBatteryLevel());
            if (device.getBatteryStatus().equals(Device.Status.CHARGING)) battery += " (Charging)";
            information.add(new JLabel("Status: Connected"));
            information.add(new JLabel("Model: ".concat(reference.model())));
            information.add(new JLabel("IP-Address: ".concat(device.getIpAddress())));
            information.add(new JLabel("Mac-Address: ".concat(device.getMacAddress())));
            information.add(new JLabel(battery));
        } catch (AdbException ignored) {
            information.add(new JLabel("Status: Disconnected"));
            information.add(new JLabel("Model: ".concat(reference.model())));
        } finally {
            untrust.addActionListener(actionEvent -> {
                dispose();
                preferences.dispose();
                storage.getTrustedDevices().remove(reference);
                new Preferences(config, storage, 2);
            });
        }
    }

    private void createUIComponents() {
        information = new JPanel(new GridLayout(5, 1));
    }
}
