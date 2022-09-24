package net.nonswag.tnl.adb;

import lombok.Getter;
import net.nonswag.tnl.core.api.logger.Logger;
import net.nonswag.tnl.core.api.math.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Getter
public class Device {

    @Nonnull
    private final String serialNumber;
    @Nonnull
    private final String macAddress, ipAddress, version, fingerprint, model;
    @Nullable
    private final String status;
    @Nonnull
    private final UUID uniqueId;

    public Device(@Nonnull String serialNumber) throws AdbException {
        this(serialNumber, null);
    }

    public Device(@Nonnull String serialNumber, @Nullable String status) throws AdbException {
        this.serialNumber = serialNumber;
        this.status = status;
        this.macAddress = retrieveMacAddress();
        this.ipAddress = retrieveIPAddress();
        this.fingerprint = retrieveFingerprint();
        this.version = retrieveVersion();
        this.model = retrieveModel();
        this.uniqueId = UUID.nameUUIDFromBytes(getMacAddress().getBytes(StandardCharsets.UTF_8));
    }

    @Nonnull
    private String retrieveMacAddress() throws AdbException {
        List<String> callback = runShellCommand("ip addr show wlan0 | grep 'link/ether '| cut -d' ' -f6");
        if (callback.isEmpty()) throw new AdbException("WLAN information not found");
        return callback.get(0);
    }

    @Nonnull
    private String retrieveIPAddress() throws AdbException {
        List<String> callback = runShellCommand("ip addr show wlan0 | grep 'inet '| cut -d' ' -f6");
        if (callback.isEmpty()) throw new AdbException("WLAN information not found");
        return callback.get(0).split("/")[0];
    }

    @Nonnull
    private String retrieveFingerprint() throws AdbException {
        List<String> callback = runShellCommand("getprop ro.build.fingerprint");
        if (callback.isEmpty()) throw new AdbException("Property information not found");
        return callback.get(0);
    }

    @Nonnull
    private String retrieveModel() throws AdbException {
        List<String> callback = runShellCommand("getprop ro.product.model");
        if (callback.isEmpty()) throw new AdbException("Property information not found");
        return callback.get(0);
    }

    @Nonnull
    private String retrieveVersion() throws AdbException {
        List<String> callback = runShellCommand("getprop ro.build.version.release");
        if (callback.isEmpty()) throw new AdbException("Property information not found");
        return callback.get(0);
    }

    public void connectTCP(int port, int code) throws AdbException {
        Logger.debug.println(runCommand("connect %s:%s %s".formatted(getIpAddress(), port, code)));
    }

    public void connectTCP(int port) throws AdbException {
        Logger.debug.println(runCommand("connect %s:%s".formatted(getIpAddress(), port)));
    }

    public void connectTCP(long code) throws AdbException {
        Logger.debug.println(runCommand("connect %s %s".formatted(getIpAddress(), code)));
    }

    public void connectTCP() throws AdbException {
        Logger.debug.println(runCommand("connect %s".formatted(getIpAddress())));
    }

    @Nonnull
    public List<String> listFiles(@Nonnull File workingDirectory) throws AdbException {
        return runShellCommand("ls %s".formatted(workingDirectory.getPath()));
    }

    @Nonnull
    public List<String> listFiles() throws AdbException {
        return listFiles(new File(""));
    }

    public void mkdir(@Nonnull File file) throws AdbException {
        runShellCommand("mkdir %s".formatted(file.getPath()));
    }

    public void rm(@Nonnull File file) throws AdbException {
        runShellCommand("rm %s".formatted(file.getPath()));
    }

    public void rmDir(@Nonnull File file) throws AdbException {
        runShellCommand("rm -r %s".formatted(file.getPath()));
    }

    public void push(@Nonnull File source, @Nonnull File destination) throws AdbException {
        runCommand("push %s %s".formatted(source.getAbsolutePath(), destination.getPath()));
    }

    public void pull(@Nonnull File source, @Nonnull File destination) throws AdbException {
        runCommand("pull %s %s".formatted(source.getAbsolutePath(), destination.getPath()));
    }

    public void setBatteryLevel(@Range(from = 0, to = 100) int battery) throws AdbException {
        runShellCommand("dumpsys battery set level %s".formatted(battery));
    }

    @Range(from = 0, to = 100)
    public int getBatteryLevel() throws AdbException {
        List<String> callback = runShellCommand("dumpsys battery | grep level");
        if (callback.isEmpty()) throw new AdbException("Battery information not found");
        String[] level = callback.get(0).split(" ");
        return Integer.parseInt(level[level.length - 1]);
    }

    @Nonnull
    public Status getBatteryStatus() throws AdbException {
        List<String> callback = runShellCommand("dumpsys battery | grep status");
        if (callback.isEmpty()) throw new AdbException("Battery information not found");
        String[] level = callback.get(0).split(" ");
        return Status.valueOf(Integer.parseInt(level[level.length - 1]));
    }

    public void setBatteryStatus(@Nonnull Status status) throws AdbException {
        runShellCommand("dumpsys battery set status %s".formatted(status.getId()));
    }

    public void resetBattery() throws AdbException {
        runShellCommand("dumpsys battery reset");
    }

    public void gallery() throws AdbException {
        runShellCommand("am start -t image/* -a android.intent.action.VIEW");
    }

    public void browse(@Nonnull String url) throws AdbException {
        runShellCommand("am start -a android.intent.action.VIEW -d %s".formatted(url));
    }

    public void call(@Nonnull String number) throws AdbException {
        runShellCommand("am start -a android.intent.action.CALL -d tel:%s".formatted(number));
    }

    public void sms(@Nonnull String number) throws AdbException {
        runShellCommand("am start -a android.intent.action.SENDTO -d sms:%s".formatted(number));
    }

    public void setResolution(int width, int height) throws AdbException {
        runShellCommand("wm size %sx%s".formatted(width, height));
    }

    public void resetResolution() throws AdbException {
        runShellCommand("wm size reset");
    }

    @Nonnull
    public String getResolution() throws AdbException {
        List<String> callback = runShellCommand("wm size");
        if (callback.isEmpty()) throw new AdbException("Resolution information not found");
        String[] resolution = callback.get(0).split(" ");
        return resolution[resolution.length - 1];
    }

    public void setDensity(int density) throws AdbException {
        runShellCommand("wm density %s".formatted(density));
    }

    public void resetDensity() throws AdbException {
        runShellCommand("wm density reset");
    }

    public int getDensity() throws AdbException {
        List<String> callback = runShellCommand("wm density");
        if (callback.isEmpty()) throw new AdbException("Density information not found");
        String[] density = callback.get(0).split(" ");
        return Integer.parseInt(density[density.length - 1]);
    }

    public void inputText(@Nonnull String text) throws AdbException {
        runShellCommand("input text '%s'".formatted(text));
    }

    public void keyEvent(@Nonnull KeyCode keyCode) throws AdbException {
        runShellCommand("input keyevent %s".formatted(keyCode.getId()));
    }

    public void reboot(@Nonnull State state) throws AdbException {
        List<String> callback = runCommand(state.getCommand());
        if (callback.isEmpty()) return;
        throw new AdbException("Failed to reboot (mode %s): %s".formatted(state, callback.get(0)));
    }

    @Nonnull
    public List<String> runCommand(@Nonnull String command) throws AdbException {
        return ADB.execute("-s %s %s".formatted(getSerialNumber(), command));
    }

    @Nonnull
    public List<String> runShellCommand(@Nonnull String command) throws AdbException {
        return runCommand("shell ".concat(command));
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
