package net.nonswag.tnl.adb;

import net.nonswag.tnl.core.api.logger.Logger;
import net.nonswag.tnl.core.utils.SystemUtil;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ADB {

    public static void main(String[] args) throws AdbException {
        for (Device device : getDevices()) {
            System.out.println(device.getSerialNumber());
            System.out.println(device.getModel());
            System.out.println(device.getMacAddress());
        }
    }

    @Nonnull
    public static List<Device> getDevices() throws AdbException {
        List<Device> devices = new ArrayList<>();
        try {
            List<String> list = execute("devices -l");
            list.subList(1, list.size()).forEach(s -> {
                try {
                    if (s.isEmpty()) return;
                    List<String> split = Arrays.stream(s.split(" ")).filter(s1 -> !s1.isEmpty()).toList();
                    if (split.isEmpty()) return;
                    devices.add(new Device(split.get(0), split.size() >= 2 ? split.get(1) : null));
                } catch (AdbException e) {
                    Logger.error.println("Failed to read device information: " + s, e);
                }
            });
        } catch (Exception e) {
            throw new AdbException(e.getMessage(), e);
        }
        return devices;
    }

    @Nonnull
    public static List<String> execute(@Nonnull String command) throws AdbException {
        List<String> callback = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec("adb ".concat(command), null, SystemUtil.TYPE.isLinux() ? null : new File("platform-tools"));
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String string;
            while ((string = br.readLine()) != null) callback.add(string);
            process.waitFor();
            process.destroy();
            br.close();
        } catch (Exception e) {
            throw new AdbException(e.getMessage(), e);
        }
        return callback;
    }
}
