package net.thenextlvl.passprotect.form;

import ch.bailu.gtk.adw.Application;
import ch.bailu.gtk.adw.ApplicationWindow;
import ch.bailu.gtk.adw.HeaderBar;
import ch.bailu.gtk.adw.*;
import ch.bailu.gtk.gtk.MessageDialog;
import ch.bailu.gtk.gtk.*;
import core.file.format.TextFile;
import core.io.IO;
import net.thenextlvl.passprotect.PassProtect;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class InstallationForm {

    private final ApplicationWindow window;

    private final ListBox entries = createListBox();
    private final ListBox installation = createListBox();
    private final ListBox importing = createListBox();

    private final ActionRow desktopEntryRow = new ActionRow();
    private final ActionRow activitiesEntryRow = new ActionRow();

    private final ActionRow installRow = new ActionRow();
    private final ActionRow updateRow = new ActionRow();
    private final ActionRow uninstallRow = new ActionRow();

    private final ActionRow importingRow = new ActionRow();

    private final Switch desktopEntrySwitch = new Switch();
    private final Switch activitiesEntrySwitch = new Switch();

    public InstallationForm(Application application) {
        this.window = new ApplicationWindow(application);

        var content = new Box(Orientation.VERTICAL, 0);
        var headerBar = new HeaderBar();

        updateUI();

        entries.setMarginBottom(0);
        entries.append(desktopEntryRow);
        entries.append(activitiesEntryRow);

        installation.setMarginBottom(0);
        installation.append(installRow);
        installation.append(updateRow);
        installation.append(uninstallRow);

        initDesktopEntry();
        initActivitiesEntry();

        importing.append(importingRow);

        headerBar.setTitleWidget(new WindowTitle("PassProtect", "Installer"));

        content.append(headerBar);
        content.append(entries);
        content.append(installation);
        content.append(importing);

        window.setDefaultSize(400, 500);
        window.setResizable(false);
        window.setContent(content);
    }

    public void present() {
        window.present();
    }

    private void updateUI() {
        entries.setSensitive(PassProtect.isInstalled());

        updateDesktopEntry();
        updateActivitiesEntry();

        updateInstallRow();
        updateUpdateRow();
        updateUninstallRow();

        updateImportRow();
    }

    private ListBox createListBox() {
        var list = new ListBox();
        list.setMarginTop(32);
        list.setMarginEnd(32);
        list.setMarginBottom(32);
        list.setMarginStart(32);
        list.addCssClass("boxed-list");
        return list;
    }

    private void updateInstallRow() {
        installRow.setTitle("Install PassProtect");
        installRow.setSubtitle(PassProtect.DATA_FOLDER.getAbsolutePath());
        installRow.setSensitive(!PassProtect.isInstalled() && PassProtect.hasJarFile());
        if (PassProtect.isInstalled()) installRow.setTooltipText("PassProtect is already installed");
        else if (PassProtect.hasJarFile()) installRow.setTooltipText("Click to install PassProtect");
        else installRow.setTooltipText("The installer file could not be found");
        installRow.onActivated(this::install);
        installRow.setActivatable(true);
        installRow.setSelectable(false);
    }

    private void updateUpdateRow() {
        updateRow.setTitle("Update PassProtect");
        var installed = PassProtect.resolveInstalledVersion();
        var comparator = PassProtect.VERSION.comparator(installed);
        var updatable = PassProtect.VERSION.isNewerThen(installed);
        updateRow.setSubtitle(PassProtect.VERSION + " " + comparator + " " + installed);
        updateRow.setSensitive(PassProtect.isInstalled() && updatable);
        if (PassProtect.isInstalled() && PassProtect.hasJarFile() && updatable)
            updateRow.setTooltipText("Click to update PassProtect");
        else if (PassProtect.isInstalled() && !PassProtect.hasJarFile())
            updateRow.setTooltipText("The updater file could not be found");
        else if (!PassProtect.isInstalled())
            updateRow.setTooltipText("PassProtect is not yet installed");
        else if (PassProtect.VERSION.equals(installed))
            updateRow.setTooltipText("PassProtect is already up to date");
        else updateRow.setTooltipText("The installed version is more recent");
        updateRow.onActivated(this::update);
        updateRow.setSelectable(false);
        updateRow.setActivatable(true);
    }

    private void updateUninstallRow() {
        uninstallRow.setTitle("Uninstall PassProtect");
        uninstallRow.setSensitive(PassProtect.DATA_FOLDER.exists());
        if (uninstallRow.isSensitive()) uninstallRow.setTooltipText("Click to uninstall PassProtect");
        else uninstallRow.setTooltipText("PassProtect is not installed");
        uninstallRow.onActivated(this::uninstallDialog);
        uninstallRow.setSelectable(false);
        uninstallRow.setActivatable(true);
    }

    private void updateImportRow() {
        importingRow.setTitle("Import existing userdata");
        importingRow.setSensitive(PassProtect.isInstalled());
        if (PassProtect.isInstalled()) importingRow.setTooltipText("Click to import existing userdata");
        else importingRow.setTooltipText("PassProtect is not yet installed");
        importingRow.onActivated(this::importDialog);
        importingRow.setSelectable(false);
        importingRow.setActivatable(true);
    }

    private void initDesktopEntry() {
        desktopEntryRow.onActivated(this::selectDesktop);
        desktopEntrySwitch.onStateSet(active -> {
            if (!active && PassProtect.resolveDesktopEntry().exists())
                PassProtect.resolveDesktopEntry().delete();
            else if (active && !PassProtect.resolveDesktopEntry().exists())
                createDesktopEntry();
            return false;
        });
    }

    private void initActivitiesEntry() {
        activitiesEntryRow.onActivated(this::selectActivitiesMenu);
        activitiesEntrySwitch.onStateSet(active -> {
            if (!active && PassProtect.resolveActivitiesEntry().exists())
                PassProtect.resolveActivitiesEntry().delete();
            else if (active && !PassProtect.resolveActivitiesEntry().exists())
                createActivitiesEntry();
            return false;
        });
    }

    private void updateDesktopEntry() {
        desktopEntrySwitch.setValign(Align.CENTER);
        desktopEntrySwitch.setSensitive(PassProtect.getAppData().getDesktop().exists());
        desktopEntrySwitch.setActive(PassProtect.resolveDesktopEntry().exists());
        desktopEntrySwitch.setTooltipText("Click to toggle the desktop entry");
        desktopEntryRow.setTitle("Create a desktop entry");
        desktopEntryRow.setSubtitle(PassProtect.getAppData().getDesktop().getAbsolutePath());
        desktopEntryRow.setSelectable(false);
        desktopEntryRow.setActivatable(true);
        desktopEntryRow.setTooltipText("Click to select your Desktop");
        desktopEntryRow.addSuffix(desktopEntrySwitch);
    }

    private void updateActivitiesEntry() {
        activitiesEntrySwitch.setValign(Align.CENTER);
        activitiesEntrySwitch.setSensitive(PassProtect.getAppData().getActivities().exists());
        activitiesEntrySwitch.setActive(PassProtect.resolveActivitiesEntry().exists());
        activitiesEntrySwitch.setTooltipText("Click to toggle the activities menu entry");
        activitiesEntryRow.setTitle("Create an activities entry");
        activitiesEntryRow.setSubtitle(PassProtect.getAppData().getActivities().getAbsolutePath());
        activitiesEntryRow.setSelectable(false);
        activitiesEntryRow.setActivatable(true);
        activitiesEntryRow.setTooltipText("Click to select your Activities Menu");
        activitiesEntryRow.addSuffix(activitiesEntrySwitch);
    }

    private void selectDesktop() {
        desktopEntrySwitch.setActive(PassProtect.resolveDesktopEntry().exists());
        var dialog = new FileChooserDialogExtended("Select your Desktop", window, 2,
                new FileChooserDialogExtended.DialogButton("Select", 0));
        dialog.onResponse(id -> {
            if (id != 0) {
                if (!PassProtect.getAppData().getDesktop().exists()) desktopEntrySwitch.setActive(false);
                return;
            }
            PassProtect.getAppData().setDesktop(new File(dialog.getPath()));
            System.out.println(PassProtect.getAppData().getDesktop());
            desktopEntryRow.setSubtitle(PassProtect.getAppData().getDesktop().getAbsolutePath());
            dialog.close();
            updateUI();
        });
        dialog.setPath(PassProtect.getAppData().getDesktop().getAbsolutePath());
        dialog.setDefaultSize(1000, 800);
        dialog.setModal(true);
        dialog.present();
    }

    private void selectActivitiesMenu() {
        activitiesEntrySwitch.setActive(PassProtect.resolveActivitiesEntry().exists());
        var dialog = new FileChooserDialogExtended("Select your Activities Menu", window, 2,
                new FileChooserDialogExtended.DialogButton("Select", 0));
        dialog.onResponse(id -> {
            if (id != 0) {
                if (!PassProtect.getAppData().getActivities().exists()) activitiesEntrySwitch.setActive(false);
                return;
            }
            PassProtect.getAppData().setActivities(new File(dialog.getPath()));
            System.out.println(PassProtect.getAppData().getActivities());
            activitiesEntryRow.setSubtitle(PassProtect.getAppData().getActivities().getAbsolutePath());
            dialog.close();
            updateUI();
        });
        dialog.setPath(PassProtect.getAppData().getActivities().getAbsolutePath());
        dialog.setModal(true);
        dialog.present();
        dialog.setDefaultSize(1000, 800);
    }

    private void createDesktopEntry() {
        createEntry(PassProtect.resolveDesktopEntry());
    }

    private void createActivitiesEntry() {
        createEntry(PassProtect.resolveActivitiesEntry());
    }

    private void createEntry(File entry) {
        new TextFile(IO.of(entry)).setRoot(List.of("""
                [Desktop Entry]
                Name=PassProtect
                Version=3.0.0
                Path=%s
                Exec=%s -jar PassProtect.jar
                Icon=%s
                Type=Application
                Terminal=false
                Categories=Security;
                MimeType=application/x-pass-protect;
                """
                .formatted(PassProtect.DATA_FOLDER.getAbsolutePath(),
                        PassProtect.JAVA.getAbsolutePath(),
                        PassProtect.ICON_FILE.getAbsolutePath())
                .split("\n"))).save();
    }

    private void install() {
        try {
            PassProtect.DATA_FOLDER.mkdirs();
            Files.copy(PassProtect.FILE.toPath(), PassProtect.INSTALLATION.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            var icon = PassProtect.class.getClassLoader().getResourceAsStream("icon.png");
            if (icon != null) Files.copy(icon, PassProtect.ICON_FILE.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            PassProtect.sendNotification("Successfully installed PassProtect");
            updateUI();
            // cleanup
        } catch (IOException e) {
            PassProtect.sendNotification("Something went wrong during the installation");
            e.printStackTrace();
        }
    }

    private void update() {
        try {
            Files.copy(PassProtect.FILE.toPath(), PassProtect.INSTALLATION.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            PassProtect.sendNotification("Successfully updated PassProtect");
            updateUI();
        } catch (IOException e) {
            PassProtect.sendNotification("Something went wrong during the update");
            e.printStackTrace();
        }
    }

    private void uninstallDialog() {
        var dialog = new MessageDialog(window, 0, 0, 4,
                "Do you want to delete all userdata?\n" +
                        "This cannot be undone");
        dialog.onResponse(id -> {
            if (id == -8 || id == -9) confirmationDialog(id == -8);
            dialog.close();
        });
        dialog.setModal(true);
        dialog.present();
    }

    private void importDialog() {
        // todo: implement
    }

    private void confirmationDialog(boolean removeData) {
        var extra = removeData ? "\nYour userdata will be gone for ever" : "";
        var dialog = new MessageDialog(window, 0, 0, 4,
                "Are you sure you want to uninstall PassProtect?" + extra);
        dialog.onResponse(id -> {
            dialog.close();
            if (id != -8) return;
            PassProtect.resolveDesktopEntry().delete();
            PassProtect.resolveActivitiesEntry().delete();
            if (removeData) delete(PassProtect.DATA_FOLDER);
            else delete(PassProtect.INSTALLATION);
            delete(PassProtect.ICON_FILE);
            PassProtect.sendNotification("Successfully uninstalled PassProtect");
            window.close();
        });
        dialog.setModal(true);
        dialog.present();
    }

    private void delete(File file) {
        if (file.isDirectory()) {
            var files = file.listFiles();
            if (files == null) return;
            for (var all : files) delete(all);
        }
        file.delete();
    }
}
