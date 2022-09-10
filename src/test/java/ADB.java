import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ADB {

    public static void main(String[] args) throws AdbException {
        for (Device device : ADB.getDevices()) {
            System.out.println(device.getSerialNumber());
        }
    }

    @Nonnull
    public static List<Device> getDevices() throws AdbException {
        List<Device> devices = new ArrayList<>();
        try {
            List<String> list = execute("adb devices -l");
            list.subList(1, list.size()).forEach(s -> {
                if (s.isEmpty()) return;
                String[] split = s.split(" {13}");
                devices.add(new Device(split[0], split[1]));
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
            Process process = Runtime.getRuntime().exec(command);
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
