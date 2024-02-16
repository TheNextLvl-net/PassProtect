package passprotect.client.util;

public class OSUtil {
    private static final String system = System.getProperty("os.name").toLowerCase();
    public static final boolean WINDOWS = system.contains("win");
    public static final boolean MAC_OS = system.contains("mac");
    public static final boolean SOLARIS = system.contains("sunos");
    public static final boolean LINUX = system.contains("linux");
    public static final boolean UNIX = system.contains("nix")
                                       || system.contains("nux")
                                       || system.contains("aix");
}
