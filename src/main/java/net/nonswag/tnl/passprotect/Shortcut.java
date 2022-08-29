package net.nonswag.tnl.passprotect;

import net.nonswag.tnl.core.api.errors.file.FileException;
import net.nonswag.tnl.core.api.file.formats.ShellFile;
import net.nonswag.tnl.core.utils.LinuxUtil;

import java.io.IOException;

public class Shortcut {

    public static int init() throws FileException, IOException, InterruptedException {
        FileException fe = null;
        ShellFile file = new ShellFile("/usr/bin/", "pass-protect");
        file.setContent(new String[]{"cd $HOME/.pass-protect/", "java -jar PassProtect.jar ${@} sc"}).save();
        if (!file.getFile().setExecutable(true, false)) fe = new FileException("Failed to mark file as executable");
        (file = new ShellFile("/etc/bash_completion.d/", "pass-protect")).setContent(new String[]{"complete -W \"start install uninstall update shortcut\" pass-protect"}).save();
        try {
            return LinuxUtil.runShellScript(file);
        } finally {
            if (fe != null) throw fe;
        }
    }
}
