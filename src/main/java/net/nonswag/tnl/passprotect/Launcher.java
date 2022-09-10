package net.nonswag.tnl.passprotect;

import com.github.weisj.darklaf.theme.laf.*;
import lombok.Getter;
import net.nonswag.tnl.core.Core;
import net.nonswag.tnl.core.api.logger.Logger;
import net.nonswag.tnl.passprotect.api.files.Config;
import net.nonswag.tnl.passprotect.api.files.Storage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

public class Launcher {
    @Getter
    @Nullable
    private static File file;
    @Getter
    @Nonnull
    @SuppressWarnings("deprecation")
    private static final List<UIManager.LookAndFeelInfo> lookAndFeels = new ArrayList<>() {
        {
            add(new UIManager.LookAndFeelInfo("One Dark", OneDarkThemeDarklafLookAndFeel.class.getName()));
            add(new UIManager.LookAndFeelInfo("Darcula", DarculaThemeDarklafLookAndFeel.class.getName()));
            add(new UIManager.LookAndFeelInfo("High Contrast Dark", HighContrastDarkThemeDarklafLookAndFeel.class.getName()));
            add(new UIManager.LookAndFeelInfo("High Contrast Light", HighContrastLightThemeDarklafLookAndFeel.class.getName()));
            add(new UIManager.LookAndFeelInfo("Solarized Dark", SolarizedDarkThemeDarklafLookAndFeel.class.getName()));
            add(new UIManager.LookAndFeelInfo("Solarized Light", SolarizedLightThemeDarklafLookAndFeel.class.getName()));
            add(new UIManager.LookAndFeelInfo("IntelliJ", IntelliJThemeDarklafLookAndFeel.class.getName()));
            for (UIManager.LookAndFeelInfo feel : UIManager.getInstalledLookAndFeels()) if (!contains(feel)) add(feel);
        }

        @Override
        public boolean contains(@Nonnull Object object) {
            if (!(object instanceof UIManager.LookAndFeelInfo feel)) return false;
            for (UIManager.LookAndFeelInfo laf : this) if (laf.getClassName().equals(feel.getClassName())) return true;
            return false;
        }
    };

    static {
        try {
            applyAppearance(Config.APP);
            file = new File(Launcher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getAbsoluteFile();
            if (!file.getName().endsWith(".jar")) file = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void applyAppearance(@Nonnull Config config) {
        try {
            UIManager.setLookAndFeel(config.getAppearance().getClassName());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Enumeration<Object> keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = UIManager.get(key);
                if (value instanceof Font) UIManager.put(key, config.getFont());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Core.main(args);
        List<String> arguments = Arrays.asList(args);
        boolean shortcut = !arguments.isEmpty() && arguments.get(arguments.size() - 1).equalsIgnoreCase("sc");
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("start")) start(args.length >= 2 && args[1].equalsIgnoreCase("installed"));
            else if (args[0].equalsIgnoreCase("uninstall")) Uninstaller.init(null);
            else if (!shortcut && args[0].equalsIgnoreCase("install")) Installer.init(false);
            else if (!shortcut && args[0].equalsIgnoreCase("update")) Installer.init(true);
            else if (!shortcut && args[0].equalsIgnoreCase("shortcut")) Shortcut.init();
            else help(args[0], shortcut);
        } else net.nonswag.tnl.passprotect.panels.Launcher.init();
    }

    public static void start(boolean installed) {
        PassProtect.main(installed);
        registerShutdownHook();
    }

    private static void help(@Nonnull String command, boolean shortcut) {
        Logger.error.printf("Unknown command: %s", command).println();
        Logger.info.println("Possible arguments:");
        Logger.info.println("\sstart (installed)");
        Logger.info.println("\s\s(starts pass-protect (as installed version))");
        Logger.info.println("\suninstall");
        Logger.info.println("\s\s(starts the pass-protect uninstaller)");
        if (shortcut) return;
        Logger.info.println("\sinstall");
        Logger.info.println("\s\s(starts the pass-protect installer)");
        Logger.info.println("\supdate");
        Logger.info.println("\s\s(updates the existing pass-protect installation)");
        Logger.info.println("\sshortcut");
        Logger.info.println("\s\s(register the pass-protect bash command)");
    }

    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (Storage.getInstance() != null) Storage.getInstance().save();
                if (Config.getInstance() != null) Config.getInstance().save();
                Config.APP.save();
            } catch (Exception e) {
                PassProtect.showErrorDialog("Something went wrong while shutting down", e);
            }
        }, "Shutdown Thread"));
    }
}
