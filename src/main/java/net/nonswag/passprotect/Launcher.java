package net.nonswag.passprotect;

import com.github.weisj.darklaf.theme.laf.*;
import lombok.Getter;
import net.nonswag.core.Core;
import net.nonswag.core.utils.SystemUtil;
import net.nonswag.passprotect.api.files.Config;
import net.nonswag.passprotect.api.files.Storage;

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
        } catch (Exception ignored) {
        } finally {
            Enumeration<Object> keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = UIManager.get(key);
                if (value instanceof Font) UIManager.put(key, config.getFont());
            }
            updateLAF(PassProtect.getInstance().getWindow());
        }
    }

    private static void updateLAF(@Nullable Window window) {
        if (window == null) return;
        for (Window childWindow : window.getOwnedWindows()) updateLAF(childWindow);
        SwingUtilities.updateComponentTreeUI(window);
    }

    public static void main(String[] args) throws Exception {
        if (!SystemUtil.TYPE.isWindows()) Core.init();
        List<String> arguments = Arrays.asList(args);
        if (args.length == 0) net.nonswag.passprotect.panels.Launcher.init();
        else if (args[0].equalsIgnoreCase("start")) start(args.length >= 2 && args[1].equalsIgnoreCase("installed"));
        else if (args[0].equalsIgnoreCase("uninstall")) Uninstaller.init(null);
        else if (args[0].equalsIgnoreCase("install")) Installer.init(false);
        else if (args[0].equalsIgnoreCase("update")) Installer.init(true);
        else net.nonswag.passprotect.panels.Launcher.init();
    }

    public static void start(boolean installed) {
        if (!Config.APP.isSessionLocked()) {
            Config.APP.setSessionLocked(true);
            PassProtect.main(installed);
            registerShutdownHook();
            Config.APP.save();
        } else PassProtect.showErrorDialog("PassProtect is already running");
    }

    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (Storage.getInstance() != null) Storage.getInstance().save();
                if (Config.getInstance() != null) Config.getInstance().save();
                Config.APP.setSessionLocked(false);
                Config.APP.save();
            } catch (Exception e) {
                PassProtect.showErrorDialog("Something went wrong while shutting down", e);
            }
        }, "Shutdown Thread"));
    }
}
