package net.thenextlvl.passprotect.util;

public record Version(int major, int minor, int patch) {
    public static final Version UNKNOWN = new Version(-1, -1, -1);
    public static final Version ERROR = new Version(-2, -2, -2);
    public static final Version NONE = new Version(0, 0, 0);

    public static Version parse(String version) {
        try {
            if (version == null) return NONE;
            var split = version.split("\\.", 3);
            var major = Integer.parseInt(split[0]);
            var minor = Integer.parseInt(split[1]);
            var patch = Integer.parseInt(split[2]);
            return new Version(major, minor, patch);
        } catch (NumberFormatException e) {
            return ERROR;
        }
    }

    public boolean isNewerThen(Version version) {
        return major() > version.major()
                || minor() > version.minor()
                || patch() > version.patch();
    }

    public String comparator(Version version) {
        if (isNewerThen(version)) return "＞";
        else if (equals(version)) return "==";
        else return "＜";
    }

    public String name() {
        return major + "." + minor + "." + patch;
    }

    @Override
    public String toString() {
        if (equals(NONE)) return "none";
        if (equals(ERROR)) return "error";
        if (equals(UNKNOWN)) return "unknown";
        return "v" + name();
    }
}
