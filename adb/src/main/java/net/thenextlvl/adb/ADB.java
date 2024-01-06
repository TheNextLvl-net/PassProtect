package net.thenextlvl.adb;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ADB {

    private static final boolean linux = System.getProperty("os.name").toLowerCase().contains("linux");

    public static void main(String[] args) throws AdbException {
        for (Device device : getDevices()) {
            System.out.println(device.getSerialNumber());
            System.out.println(device.getModel());
            System.out.println(device.getMacAddress());
        }
    }

    public static List<Device> getDevices() throws AdbException {
        var devices = new ArrayList<Device>();
        try {
            execute("devices -l").stream().skip(1).forEach(s -> {
                try {
                    if (s.isEmpty()) return;
                    var split = Arrays.stream(s.split(" ")).filter(s1 -> !s1.isEmpty()).toList();
                    if (split.isEmpty()) return;
                    var status = split.size() >= 2 ? split.get(1) : null;
                    if ("unauthorized".equalsIgnoreCase(status)) throw new AdbException("device not authorized");
                    devices.add(new Device(split.get(0), status));
                } catch (AdbException e) {
                    System.err.println("Failed to read device information: " + s);
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            throw new AdbException(e.getMessage(), e);
        }
        return devices;
    }

    public static List<String> execute(String command) throws AdbException {
        try {
            List<String> callback = new ArrayList<>();
            Process process = linux ? Runtime.getRuntime().exec(("adb " + command).split(" ")) :
                    Runtime.getRuntime().exec(("cmd.exe /C adb.exe " + command).split(" "),
                            null, new File("platform-tools"));
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String string;
            while ((string = br.readLine()) != null) callback.add(string);
            process.waitFor();
            process.destroy();
            br.close();
            return callback;
        } catch (Exception e) {
            throw new AdbException(e.getMessage(), e);
        }
    }
}
