import lombok.Getter;
import net.nonswag.tnl.core.api.math.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.List;

@Getter
public class Device {

    @Nonnull
    private final String serialNumber, model;
    @Nullable
    private String macAddress, version;

    public Device(@Nonnull String serialNumber, @Nonnull String model) {
        this.serialNumber = serialNumber;
        this.model = model;
    }

    @Nullable
    public String getMacAddress() throws AdbException {
        if (macAddress != null) return macAddress;
        List<String> callback = ADB.execute("adb shell ip addr show wlan0 | grep 'link/ether '| cut -d' ' -f6");
        return callback.isEmpty() ? null : (macAddress = callback.get(0));
    }

    @Nullable
    public String getVersion() throws AdbException {
        if (version != null) return version;
        List<String> callback = ADB.execute("adb shell getprop ro.build.version.release");
        return callback.isEmpty() ? null : (version = callback.get(0));
    }

    @Nonnull
    public List<String> listFiles(@Nonnull File workingDirectory) throws AdbException {
        return ADB.execute("adb shell ls %s".formatted(workingDirectory.getPath()));
    }

    @Nonnull
    public List<String> listFiles() throws AdbException {
        return listFiles(new File(""));
    }

    public void mkdir(@Nonnull File file) throws AdbException {
        ADB.execute("adb shell mkdir %s".formatted(file.getPath()));
    }

    public void rm(@Nonnull File file) throws AdbException {
        ADB.execute("adb shell rm %s".formatted(file.getPath()));
    }

    public void rmDir(@Nonnull File file) throws AdbException {
        ADB.execute("adb shell rm -r %s".formatted(file.getPath()));
    }

    public void push(@Nonnull File source, @Nonnull File destination) throws AdbException {
        ADB.execute("adb push %s %s".formatted(source.getAbsolutePath(), destination.getPath()));
    }

    public void pull(@Nonnull File source, @Nonnull File destination) throws AdbException {
        ADB.execute("adb pull %s %s".formatted(source.getAbsolutePath(), destination.getPath()));
    }

    public void setBatteryLevel(@Range(from = 0, to = 100) int battery) throws AdbException {
        ADB.execute("adb shell dumpsys battery set level %s".formatted(battery));
    }

    @Range(from = 0, to = 100)
    public int getBatteryLevel() throws AdbException {
        String[] level = ADB.execute("adb shell dumpsys battery | grep level").get(0).split(" ");
        return Integer.parseInt(level[level.length - 1]);
    }

    @Nonnull
    public Status getBatteryStatus() throws AdbException {
        String[] level = ADB.execute("adb shell dumpsys battery | grep status").get(0).split(" ");
        return Status.valueOf(Integer.parseInt(level[level.length - 1]));
    }

    public void setBatteryStatus(@Nonnull Status status) throws AdbException {
        ADB.execute("adb shell dumpsys battery set status %s".formatted(status.getId()));
    }

    public void resetBattery() throws AdbException {
        ADB.execute("adb shell dumpsys battery reset");
    }

    public void gallery() throws AdbException {
        ADB.execute("adb shell am start -t image/* -a android.intent.action.VIEW");
    }

    public void browse(@Nonnull String url) throws AdbException {
        ADB.execute("adb shell am start -a android.intent.action.VIEW -d %s".formatted(url));
    }

    public void call(@Nonnull String number) throws AdbException {
        ADB.execute("adb shell am start -a android.intent.action.CALL -d tel:%s".formatted(number));
    }

    public void sms(@Nonnull String number) throws AdbException {
        ADB.execute("adb shell am start -a android.intent.action.SENDTO -d sms:%s".formatted(number));
    }

    public void setResolution(int width, int height) throws AdbException {
        ADB.execute("adb shell wm size %sx%s".formatted(width, height));
    }

    public void resetResolution() throws AdbException {
        ADB.execute("adb shell wm size reset");
    }

    @Nonnull
    public String getResolution() throws AdbException {
        String[] resolution = ADB.execute("adb shell wm size").get(0).split(" ");
        return resolution[resolution.length - 1];
    }

    public void setDensity(int density) throws AdbException {
        ADB.execute("adb shell wm density %s".formatted(density));
    }

    public void resetDensity() throws AdbException {
        ADB.execute("adb shell wm density reset");
    }

    public int getDensity() throws AdbException {
        String[] density = ADB.execute("adb shell wm density").get(0).split(" ");
        return Integer.parseInt(density[density.length - 1]);
    }

    public void inputText(@Nonnull String text) throws AdbException {
        ADB.execute("adb shell input text '%s'".formatted(text));
    }

    public void keyEvent(@Nonnull KeyCode keyCode) throws AdbException {
        ADB.execute("adb shell input keyevent %s".formatted(keyCode.getId()));
    }

    public void reboot(@Nonnull State state) throws AdbException {
        List<String> callback = ADB.execute("adb %s".formatted(state.getCommand()));
        if (callback.isEmpty()) return;
        throw new AdbException("Failed to reboot (mode %s): %s".formatted(state, callback.get(0)));
    }

    @Getter
    public enum Status {
        UNKNOWN("unknown", 1),
        CHARGING("charging", 2),
        DISCHARGING("discharging", 3),
        NOT_CHARGING("not charging", 4),
        FULL("full", 5);

        @Nonnull
        private final String name;
        private final int id;

        Status(@Nonnull String name, int id) {
            this.name = name;
            this.id = id;
        }

        @Nonnull
        public static Status valueOf(int id) {
            for (Status status : values()) if (status.getId() == id) return status;
            return UNKNOWN;
        }
    }

    @Getter
    public enum KeyCode {
        KEYCODE_SOFT_LEFT(1),
        KEYCODE_SOFT_RIGHT(2),
        KEYCODE_HOME(3),
        KEYCODE_BACK(4),
        KEYCODE_CALL(5),
        KEYCODE_ENDCALL(6),
        KEYCODE_0(7),
        KEYCODE_1(8),
        KEYCODE_2(9),
        KEYCODE_3(10),
        KEYCODE_4(11),
        KEYCODE_5(12),
        KEYCODE_6(13),
        KEYCODE_7(14),
        KEYCODE_8(15),
        KEYCODE_9(16),
        KEYCODE_STAR(17),
        KEYCODE_POUND(18),
        KEYCODE_DPAD_UP(19),
        KEYCODE_DPAD_DOWN(20),
        KEYCODE_DPAD_LEFT(21),
        KEYCODE_DPAD_RIGHT(22),
        KEYCODE_DPAD_CENTER(23),
        KEYCODE_VOLUME_UP(24),
        KEYCODE_VOLUME_DOWN(25),
        KEYCODE_POWER(26),
        KEYCODE_CAMERA(27),
        KEYCODE_CLEAR(28),
        KEYCODE_A(29),
        KEYCODE_B(30),
        KEYCODE_C(31),
        KEYCODE_D(32),
        KEYCODE_E(33),
        KEYCODE_F(34),
        KEYCODE_G(35),
        KEYCODE_H(36),
        KEYCODE_I(37),
        KEYCODE_J(38),
        KEYCODE_K(39),
        KEYCODE_L(40),
        KEYCODE_M(41),
        KEYCODE_N(42),
        KEYCODE_O(43),
        KEYCODE_P(44),
        KEYCODE_Q(45),
        KEYCODE_R(46),
        KEYCODE_S(47),
        KEYCODE_T(48),
        KEYCODE_U(49),
        KEYCODE_V(50),
        KEYCODE_W(51),
        KEYCODE_X(52),
        KEYCODE_Y(53),
        KEYCODE_Z(54),
        KEYCODE_COMMA(55),
        KEYCODE_PERIOD(56),
        KEYCODE_ALT_LEFT(57),
        KEYCODE_ALT_RIGHT(58),
        KEYCODE_SHIFT_LEFT(59),
        KEYCODE_SHIFT_RIGHT(60),
        KEYCODE_TAB(61),
        KEYCODE_SPACE(62),
        KEYCODE_SYM(63),
        KEYCODE_EXPLORER(64),
        KEYCODE_ENVELOPE(65),
        KEYCODE_ENTER(66),
        KEYCODE_DEL(67),
        KEYCODE_GRAVE(68),
        KEYCODE_MINUS(69),
        KEYCODE_EQUALS(70),
        KEYCODE_LEFT_BRACKET(71),
        KEYCODE_RIGHT_BRACKET(72),
        KEYCODE_BACKSLASH(73),
        KEYCODE_SEMICOLON(74),
        KEYCODE_APOSTROPHE(75),
        KEYCODE_SLASH(76),
        KEYCODE_AT(77),
        KEYCODE_NUM(78),
        KEYCODE_HEADSETHOOK(79),
        KEYCODE_FOCUS(80),
        KEYCODE_PLUS(81),
        KEYCODE_MENU(82),
        KEYCODE_NOTIFICATION(83),
        KEYCODE_SEARCH(84),
        KEYCODE_MEDIA_PLAY_PAUSE(85),
        KEYCODE_MEDIA_STOP(86),
        KEYCODE_MEDIA_NEXT(87),
        KEYCODE_MEDIA_PREVIOUS(88),
        KEYCODE_MEDIA_REWIND(89),
        KEYCODE_MEDIA_FAST_FORWARD(90),
        KEYCODE_MUTE(91),
        KEYCODE_PAGE_UP(92),
        KEYCODE_PAGE_DOWN(93),
        KEYCODE_PICTSYMBOLS(94),
        KEYCODE_MOVE_HOME(122),
        KEYCODE_MOVE_END(123);

        private final int id;

        KeyCode(int id) {
            this.id = id;
        }
    }

    @Getter
    public enum State {
        DEFAULT("reboot"),
        RECOVERY("reboot recovery"),
        BOOTLOADER("reboot-bootloader"),
        ROOT("root");

        @Nonnull
        private final String command;

        State(@Nonnull String command) {
            this.command = command;
        }
    }
}
