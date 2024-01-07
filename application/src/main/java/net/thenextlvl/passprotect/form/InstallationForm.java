package net.thenextlvl.passprotect.form;

import ch.bailu.gtk.adw.Application;
import ch.bailu.gtk.adw.ApplicationWindow;
import ch.bailu.gtk.adw.HeaderBar;
import ch.bailu.gtk.adw.*;
import ch.bailu.gtk.gtk.MessageDialog;
import ch.bailu.gtk.gtk.*;
import com.google.common.hash.Hashing;
import core.file.format.TextFile;
import core.io.IO;
import net.thenextlvl.crypto.aes.AES;
import net.thenextlvl.passprotect.PassProtect;
import net.thenextlvl.passprotect.dialog.PasswordDialog;
import net.thenextlvl.passprotect.util.GTK;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.List;

public class InstallationForm {

    private final ApplicationWindow window;

    private final ListBox entries = GTK.createListBox();
    private final ListBox installation = GTK.createListBox();
    private final ListBox importing = GTK.createListBox();

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

        window.setTitle("PassProtect");

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
        importingRow.onActivated(this::selectImportFile);
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
        activitiesEntrySwitch.onStateSet(active -> {
            if (!active && PassProtect.ACTIVITIES_ENTRY.exists())
                PassProtect.ACTIVITIES_ENTRY.delete();
            else if (active && !PassProtect.ACTIVITIES_ENTRY.exists())
                createActivitiesEntry();
            updateDesktopDatabase();
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
        activitiesEntrySwitch.setActive(PassProtect.ACTIVITIES_ENTRY.exists());
        activitiesEntrySwitch.setSensitive(!PassProtect.ACTIVITIES_ENTRY.exists());
        // activitiesEntrySwitch.setTooltipText("Click to toggle the activities menu entry");
        activitiesEntryRow.setTitle("Create an activities entry");
        activitiesEntryRow.setSubtitle(PassProtect.ACTIVITIES.getAbsolutePath());
        activitiesEntryRow.setSelectable(false);
        activitiesEntryRow.setTooltipText("This is required to handle *.pp files");
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

    private void selectImportFile() {
        var dialog = new FileChooserDialogExtended("Select the file you want to import", window, 0,
                new FileChooserDialogExtended.DialogButton("Import", 0));
        dialog.onResponse(id -> {
            if (id != 0) {
                dialog.close();
                return;
            }
            var file = new File(dialog.getPath());
            if (file.isFile()) try {
                var content = String.join("", Files.readAllLines(file.toPath(), StandardCharsets.UTF_8));
                Base64.getDecoder().decode(content);
                importFile(file, content);
                dialog.close();
            } catch (Exception e) {
                System.err.println("Tried to load invalid file: " + e.getMessage());
            }
        });
        var filter = new FileFilter();
        filter.setName("PassProtect files");
        filter.addPattern("*.pp");
        dialog.asFileChooser().addFilter(filter);
        dialog.asFileChooser().setCreateFolders(false);
        dialog.setModal(true);
        dialog.present();
        dialog.setDefaultSize(1000, 800);
    }

    private void createDesktopEntry() {
        createEntry(PassProtect.resolveDesktopEntry());
    }

    private void createActivitiesEntry() {
        activitiesEntrySwitch.setSensitive(false);
        createEntry(PassProtect.ACTIVITIES_ENTRY);
    }

    public void updateMimeDatabase() {
        try {
            new ProcessBuilder("update-mime-database", PassProtect.MIME_FOLDER.getAbsolutePath())
                    .inheritIO()
                    .start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateDesktopDatabase() {
        try {
            new ProcessBuilder("update-desktop-database", PassProtect.ACTIVITIES.getAbsolutePath())
                    .inheritIO()
                    .start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createEntry(File entry) {
        new TextFile(IO.of(entry)).setRoot(List.of("""
                [Desktop Entry]
                Name=PassProtect
                Version=3.0.0
                Path=%s
                Description=The best password manager out there
                Exec=%s -jar pass-protect.jar %%U
                Icon=%s
                Type=Application
                Terminal=false
                Categories=Security;
                MimeType=application/pass-protect;
                """
                .formatted(PassProtect.DATA_FOLDER.getAbsolutePath(),
                        PassProtect.JAVA.getAbsolutePath(),
                        PassProtect.ICON_FILE.getAbsolutePath())
                .split("\n"))).save();
    }

    private void install() {
        try (var icon = PassProtect.class.getClassLoader().getResourceAsStream("icon.png");
             var mime = PassProtect.class.getClassLoader().getResourceAsStream("pass-protect.xml")) {

            PassProtect.DATA_FOLDER.mkdirs();
            PassProtect.MIME_PACKAGES.mkdirs();

            Files.copy(PassProtect.FILE.toPath(), PassProtect.INSTALLATION.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            if (icon != null) Files.copy(icon, PassProtect.ICON_FILE.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            if (mime != null) Files.copy(mime, PassProtect.MIME_TYPE_FILE.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            activitiesEntrySwitch.setActive(true);
            updateMimeDatabase();
            PassProtect.sendNotification("Successfully installed PassProtect");
            // todo: cleanup
        } catch (IOException e) {
            PassProtect.sendNotification("Something went wrong during the installation");
            e.printStackTrace();
        } finally {
            updateUI();
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

    private void confirmationDialog(boolean removeData) {
        var extra = removeData ? "\nYour userdata will be gone for ever" : "";
        var dialog = new MessageDialog(window, 0, 0, 4,
                "Are you sure you want to uninstall PassProtect?" + extra);
        dialog.onResponse(id -> {
            dialog.close();
            if (id != -8) return;
            PassProtect.sendNotification("Successfully uninstalled PassProtect");
            PassProtect.resolveDesktopEntry().delete();
            PassProtect.ACTIVITIES_ENTRY.delete();
            if (removeData) delete(PassProtect.DATA_FOLDER);
            else delete(PassProtect.INSTALLATION);
            delete(PassProtect.MIME_TYPE_FILE);
            delete(PassProtect.DATA_FILE);
            delete(PassProtect.ICON_FILE);
            PassProtect.DATA_FOLDER.delete();
            updateDesktopDatabase();
            updateMimeDatabase();
            window.close();
        });
        dialog.setModal(true);
        dialog.present();
    }

    private void importFile(File file, String content) {
        var dialog = new PasswordDialog(window);

        var headerBar = new HeaderBar();
        headerBar.setTitleWidget(new WindowTitle("Import userdata", "Decrypt file"));
        dialog.setTitlebar(headerBar);

        dialog.passwordEntryRow().setTitle("Enter the password for this file");
        Runnable onClicked = () -> {
            var password = dialog.passwordEntryRow().asEditable().getText().toString();
            try {
                var bytes = password.getBytes(StandardCharsets.UTF_8);
                var aes = new AES(Hashing.sha256().hashBytes(bytes).asBytes());
                importContent(aes, content);
                dialog.close();
            } catch (Exception e) {
                if (password.isEmpty())
                    System.err.println("you have to enter a password");
                else System.err.println("you entered the wrong password");
            }
        };
        dialog.passwordEntryRow().onEntryActivated(onClicked::run);
        dialog.confirmButton().onClicked(onClicked::run);

        dialog.setTitle("Import userdata");
        dialog.setModal(true);
        dialog.present();
    }

    private void importContent(AES aes, String content) throws Exception {
        var decode = aes.decode(content);
        // todo: if existing data -> merge
        // todo: otherwise -> use imported data
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
