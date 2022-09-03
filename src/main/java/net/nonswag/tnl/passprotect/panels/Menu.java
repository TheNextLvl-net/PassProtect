package net.nonswag.tnl.passprotect.panels;

import lombok.Getter;
import net.nonswag.tnl.core.api.file.helper.FileHelper;
import net.nonswag.tnl.core.api.object.TriFunction;
import net.nonswag.tnl.core.utils.LinuxUtil;
import net.nonswag.tnl.passprotect.Launcher;
import net.nonswag.tnl.passprotect.PassProtect;
import net.nonswag.tnl.passprotect.Uninstaller;
import net.nonswag.tnl.passprotect.api.entry.*;
import net.nonswag.tnl.passprotect.api.fields.TextField;
import net.nonswag.tnl.passprotect.api.files.Config;
import net.nonswag.tnl.passprotect.api.files.Storage;
import net.nonswag.tnl.passprotect.api.nodes.*;
import net.nonswag.tnl.passprotect.api.renderer.TreeIconRenderer;
import net.nonswag.tnl.passprotect.dialogs.ActiveDialogs;
import net.nonswag.tnl.passprotect.dialogs.ChangePassword;
import net.nonswag.tnl.passprotect.dialogs.entries.BackupCodeEntry;
import net.nonswag.tnl.passprotect.dialogs.entries.FileEntry;
import net.nonswag.tnl.passprotect.dialogs.entries.PasswordEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Function;

@Getter
public class Menu extends Panel {
    @Nonnull
    private JPanel panel;
    @Nonnull
    private JTree categories;
    @Nonnull
    private JMenuBar menu;
    @Nonnull
    private JPopupMenu actionMenu;
    @Nonnull
    private JButton newCategory;
    @Nonnull
    private JTextField searchBar;

    @Nonnull
    private final Storage storage;
    @Nonnull
    private final Config config;

    @Nonnull
    private final Runnable deleteAction = () -> {
        EntryTreeNode<?> node = getSelectedEntry();
        if (node == null) Toolkit.getDefaultToolkit().beep();
        else if (JOptionPane.showConfirmDialog(null, "Do you really want to delete this " + node.type() + "?", "Delete " + node.type(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == 0) {
            if (node.getParent() instanceof CategoryTreeNode parent) parent.getEntry().remove(node.getEntry());
            else if (node.getEntry() instanceof Category child) getStorage().getCategories().remove(child);
            update();
        }
    };
    @Nonnull
    @SuppressWarnings({"rawtypes", "unchecked"})
    private final Runnable renameAction = () -> {
        CategoryTreeNode node = getSelectedCategory();
        if (node != null) {
            String name = JOptionPane.showInputDialog("Enter a new name", node.getEntry().getName());
            if (name == null || name.isEmpty()) return;
            TreeSet categories = node.getParent() instanceof CategoryTreeNode parent ? parent.getEntry() : getStorage().getCategories();
            Category renamed = new Category(name, node.getEntry());
            if (!categories.contains(renamed)) {
                categories.remove(node.getEntry());
                categories.add(renamed);
                update();
            } else PassProtect.showErrorDialog("A category with this name does already exist");
        } else {
            EntryTreeNode entry = getSelectedEntry();
            if (entry != null) this.categories.expandPath(new TreePath(entry));
            else Toolkit.getDefaultToolkit().beep();
        }
    };

    public Menu() {
        Storage storage = Storage.getInstance();
        Config config = Config.getInstance();
        if (storage == null) throw new IllegalStateException("storage not initialized");
        if (config == null) throw new IllegalStateException("config not initialized");
        this.storage = storage;
        this.config = config;
        categories.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        newCategory.addActionListener(actionEvent -> newCategory(Category::new));
        categories.setCellRenderer(new TreeIconRenderer(config));
        categories.setUI(new BasicTreeUI());
        registerListeners();
        constructActionMenu();
        constructSearchBar();
        constructMenu();
        update();
    }

    private void constructSearchBar() {
        searchBar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(@Nonnull KeyEvent event) {
                update();
            }
        });
    }

    private void registerListeners() {
        panel.registerKeyboardAction(actionEvent -> this.searchBar.requestFocus(),
                KeyStroke.getKeyStroke("control F"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        categories.addTreeExpansionListener(new TreeExpansionListener() {

            @Override
            public void treeExpanded(@Nonnull TreeExpansionEvent event) {
                Object component = event.getPath().getLastPathComponent();
                if (!(component instanceof EntryTreeNode<?> node)) return;
                if (ActiveDialogs.getInstance().containsKey(node.path())) treeCollapsed(event, true);
                else if (component instanceof PasswordTreeNode) expandPasswordTreeNode(event);
                else if (component instanceof BackupCodeTreeNode) expandBackupCodeTreeNode(event);
                else if (component instanceof FileTreeNode) expandFileTreeNode(event);
            }

            private void expandPasswordTreeNode(@Nonnull TreeExpansionEvent event) {
                if (!(event.getPath().getLastPathComponent() instanceof PasswordTreeNode node)) return;
                ActiveDialogs.getInstance().put(node.path(), new PasswordEntry(node.getEntry(), config, (password, type) -> {
                    if (!type.isConfirm() || password.equals(node.getEntry())) return true;
                    if (!(node.getParent() instanceof CategoryTreeNode parent)) return true;
                    if (!password.getName().isEmpty() && password.getPassword().length > 0) {
                        parent.getEntry().remove(node.getEntry());
                        parent.getEntry().add(password);
                        update();
                        return true;
                    } else if (password.getName().isEmpty()) PassProtect.showErrorDialog("Login name cannot be blank");
                    else PassProtect.showErrorDialog("Password cannot be blank");
                    return false;
                }).onSuccess(() -> collapseTreeNode(node, event.getPath())));
            }

            private void expandBackupCodeTreeNode(@Nonnull TreeExpansionEvent event) {
                if (!(event.getPath().getLastPathComponent() instanceof BackupCodeTreeNode node)) return;
                ActiveDialogs.getInstance().put(node.path(), new BackupCodeEntry(node.getEntry(), config, (backupCode, type) -> {
                    if (!type.isConfirm()) return true;
                    BackupCode code = backupCode.get();
                    if (code.equals(node.getEntry())) return true;
                    if (!(node.getParent() instanceof CategoryTreeNode parent)) return true;
                    if (!code.getName().isEmpty() && code.getCodes().length > 0) {
                        if (code.getCodes().length <= 32) {
                            parent.getEntry().remove(node.getEntry());
                            parent.getEntry().add(code);
                            update();
                            return true;
                        } else PassProtect.showErrorDialog("You passed the maximum of 32 backup codes");
                    } else if (code.getName().isEmpty()) PassProtect.showErrorDialog("Login name cannot be blank");
                    else PassProtect.showErrorDialog("Codes cannot be blank");
                    return false;
                }).onSuccess(() -> collapseTreeNode(node, event.getPath())));
            }

            private void expandFileTreeNode(@Nonnull TreeExpansionEvent event) {
                if (!(event.getPath().getLastPathComponent() instanceof FileTreeNode node)) return;
                ActiveDialogs.getInstance().put(node.path(), new FileEntry(node.getEntry(), config, (file, type) -> {
                    if (!type.isConfirm() || file.equals(node.getEntry())) return true;
                    if (!(node.getParent() instanceof CategoryTreeNode parent)) return true;
                    if (!file.getName().isEmpty()) {
                        parent.getEntry().remove(node.getEntry());
                        parent.getEntry().add(file);
                        update();
                        return true;
                    } else PassProtect.showErrorDialog("File name cannot be blank");
                    return false;
                }).onSuccess(() -> collapseTreeNode(node, event.getPath())));
            }

            private void collapseTreeNode(@Nonnull EntryTreeNode<?> node, @Nonnull TreePath path) {
                ActiveDialogs.getInstance().remove(node.path());
                categories.collapsePath(path);
            }

            public void treeCollapsed(@Nonnull TreeExpansionEvent event, boolean outdated) {
                JDialog jDialog = ActiveDialogs.getInstance().get(event.getPath().toString());
                if (jDialog instanceof PasswordEntry dialog) dialog.close(PasswordEntry.CloseEvent.Type.CLOSE);
                else if (jDialog instanceof BackupCodeEntry dialog) dialog.close(BackupCodeEntry.CloseEvent.Type.CLOSE);
                else if (jDialog instanceof FileEntry dialog) dialog.close(FileEntry.CloseEvent.Type.CLOSE);
                if (outdated) categories.collapsePath(event.getPath());
            }

            @Override
            public void treeCollapsed(@Nonnull TreeExpansionEvent event) {
                treeCollapsed(event, false);
            }
        });
    }

    private void constructActionMenu() {
        JMenu aNew = new JMenu("New");

        JMenuItem rename = new JMenuItem("Rename", KeyEvent.VK_R);
        JMenuItem delete = new JMenuItem("Delete", KeyEvent.VK_D);
        JMenuItem category = new JMenuItem("Category", KeyEvent.VK_C);
        JMenuItem password = new JMenuItem("Password", KeyEvent.VK_P);
        JMenuItem backupCodes = new JMenuItem("Backup code", KeyEvent.VK_B);
        JMenuItem newFile = new JMenuItem("File", KeyEvent.VK_F);

        aNew.setMnemonic(KeyEvent.VK_N);

        category.addActionListener(actionEvent -> newCategory(Category::new));
        password.addActionListener(actionEvent -> newPassword(Password::new));
        backupCodes.addActionListener(actionEvent -> newBackupCode(BackupCode::new));
        newFile.addActionListener(actionEvent -> newFile(File::new));
        actionMenu.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(@Nonnull FocusEvent event) {
                if (actionMenu.isVisible()) actionMenu.setVisible(false);
            }
        });
        delete.addActionListener(actionEvent -> {
            actionMenu.setVisible(false);
            deleteAction.run();
        });
        rename.addActionListener(actionEvent -> {
            actionMenu.setVisible(false);
            renameAction.run();
        });

        aNew.add(category);
        aNew.add(password);
        aNew.add(backupCodes);
        aNew.add(newFile);

        actionMenu.add(aNew);
        actionMenu.add(rename);
        actionMenu.add(delete);
    }

    private void constructMenu() {
        JMenu file = new JMenu("File");
        JMenu aNew = new JMenu("New");
        JMenu selection = new JMenu("Selection");
        JMenu settings = new JMenu("Settings");
        JMenu appearance = new JMenu("Appearance");

        JMenuItem logout = new JMenuItem("Logout", KeyEvent.VK_L);
        JMenuItem exit = new JMenuItem("Exit", KeyEvent.VK_X);
        JMenuItem saveAll = new JMenuItem("Save", KeyEvent.VK_S);
        JMenuItem reloadAll = new JMenuItem("Reload", KeyEvent.VK_R);
        JMenuItem category = new JMenuItem("Category", KeyEvent.VK_C);
        JMenuItem password = new JMenuItem("Password", KeyEvent.VK_P);
        JMenuItem backupCodes = new JMenuItem("Backup code", KeyEvent.VK_B);
        JMenuItem newFile = new JMenuItem("File", KeyEvent.VK_F);
        JMenuItem delete = new JMenuItem("Delete", KeyEvent.VK_D);
        JMenuItem rename = new JMenuItem("Rename", KeyEvent.VK_R);
        JMenuItem clear = new JMenuItem("Clear", KeyEvent.VK_C);
        JMenuItem changePassword = new JMenuItem("Change Password", KeyEvent.VK_C);
        JMenuItem deleteUser = new JMenuItem("Delete User", KeyEvent.VK_D);
        JMenuItem install = new JMenuItem("Install PassProtect", KeyEvent.VK_I);
        JMenuItem uninstall = new JMenuItem("Uninstall PassProtect", KeyEvent.VK_U);

        install.setEnabled(!PassProtect.isInstalled() && Launcher.getFile() != null);
        uninstall.setEnabled(PassProtect.isInstalled());

        file.setMnemonic(KeyEvent.VK_F);
        selection.setMnemonic(KeyEvent.VK_E);
        settings.setMnemonic(KeyEvent.VK_S);
        appearance.setMnemonic(KeyEvent.VK_A);
        aNew.setMnemonic(KeyEvent.VK_N);

        saveAll.setAccelerator(KeyStroke.getKeyStroke("control S"));
        reloadAll.setAccelerator(KeyStroke.getKeyStroke("control R"));
        clear.setAccelerator(KeyStroke.getKeyStroke("control alt C"));
        category.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
        password.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
        backupCodes.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
        logout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
        delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        rename.setAccelerator(KeyStroke.getKeyStroke("control shift R"));

        category.addActionListener(actionEvent -> newCategory(Category::new));
        password.addActionListener(actionEvent -> newPassword(Password::new));
        backupCodes.addActionListener(actionEvent -> newBackupCode(BackupCode::new));
        newFile.addActionListener(actionEvent -> newFile(File::new));
        clear.addActionListener(actionEvent -> {
            if (categories.getSelectionPath() != null) categories.clearSelection();
            else Toolkit.getDefaultToolkit().beep();
        });
        rename.addActionListener(actionEvent -> renameAction.run());
        for (UIManager.LookAndFeelInfo theme : Launcher.getLookAndFeels()) {
            JMenuItem item = new JMenuItem(theme.getName());
            item.setEnabled(!UIManager.getLookAndFeel().getClass().getName().equals(theme.getClassName()));
            item.addActionListener(actionEvent -> {
                config.setAppearance(theme);
                Launcher.applyAppearance(config);
                PassProtect.getInstance().setLoggedIn(true);
            });
            appearance.add(item);
        }
        saveAll.addActionListener(actionEvent -> {
            try {
                storage.save();
                config.save();
                JOptionPane.showInternalMessageDialog(null, "Saved all changes", "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                failedToSave(e);
            }
        });
        reloadAll.addActionListener(actionEvent -> {
            if (JOptionPane.showConfirmDialog(null, "Changes you made may not be saved", "Reload All from Disk", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == 0) {
                try {
                    config.load();
                    storage.init(true);
                    update();
                } catch (Exception e) {
                    Storage.setInstance(null);
                    Config.setInstance(null);
                    PassProtect.getInstance().setLoggedIn(false);
                }
            }
        });
        exit.addActionListener(actionEvent -> System.exit(0));
        logout.addActionListener(actionEvent -> {
            if (JOptionPane.showConfirmDialog(null, "Do you really want to close this session?", "Close session", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == 0) {
                try {
                    storage.save();
                    config.save();
                } catch (Exception e) {
                    failedToSave(e);
                } finally {
                    if (actionMenu.isVisible()) actionMenu.setVisible(false);
                    Config.setInstance(null);
                    Storage.setInstance(null);
                    PassProtect.getInstance().setLoggedIn(false);
                }
            }
        });
        delete.addActionListener(actionEvent -> deleteAction.run());
        changePassword.addActionListener(actionEvent -> new ChangePassword(storage, config));
        deleteUser.addActionListener(actionEvent -> {
            if (JOptionPane.showConfirmDialog(null, "Do you really want to delete this user\nThis cannot be undone", "Delete user", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (FileHelper.delete(getStorage().getFile().getAbsoluteFile().getParentFile())) {
                    PassProtect.getInstance().setLoggedIn(false);
                } else PassProtect.showErrorDialog("Failed to delete user '%s'".formatted(getStorage().getUser()));
            }
        });
        install.addActionListener(actionEvent -> {
            java.io.File installer = Launcher.getFile();
            if (installer != null) {
                try {
                    PassProtect.setInstalled(LinuxUtil.runShellCommand("java -jar " + installer.getAbsolutePath() + " install") == 0);
                    install.setEnabled(!PassProtect.isInstalled());
                } catch (IOException | InterruptedException e) {
                    PassProtect.showErrorDialog("Failed to install PassProtect", e);
                }
            } else PassProtect.showErrorDialog("Found no installation file");
        });
        uninstall.addActionListener(actionEvent -> {
            try {
                if (JOptionPane.showConfirmDialog(null, "Do you really want to uninstall PassProtect?", null, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    Uninstaller.init(null);
                }
            } catch (FileNotFoundException e) {
                PassProtect.showErrorDialog("Failed to uninstall PassProtect", e);
            }
        });

        aNew.add(category);
        aNew.add(password);
        aNew.add(backupCodes);
        aNew.add(newFile);

        file.add(aNew);
        file.add(saveAll);
        file.add(reloadAll);
        file.add(logout);
        file.add(exit);

        selection.add(delete);
        selection.add(rename);
        selection.add(clear);

        settings.add(changePassword);
        settings.add(deleteUser);
        settings.add(install);
        settings.add(uninstall);
        settings.add(appearance);

        menu.add(file);
        menu.add(selection);
        menu.add(settings);
    }

    private void update() {
        if (!PassProtect.getInstance().isLoggedIn()) return;
        newCategory.setVisible(storage.getCategories().isEmpty());
        List<Integer> rows = new ArrayList<>();
        for (int i = 0; i < categories.getRowCount(); i++) if (categories.isExpanded(i)) rows.add(i);
        DefaultMutableTreeNode categories = new DefaultMutableTreeNode();
        for (Category category : storage.getCategories()) {
            String filter = searchBar.getText();
            if (filter != null && !filter.isEmpty() && !category.getName().contains(filter)) continue;
            categories.add(category.tree());
        }
        this.categories.setModel(new DefaultTreeModel(categories, true));
        for (int row : rows) {
            TreePath path = this.categories.getPathForRow(row);
            if (path == null) continue;
            Object component = path.getLastPathComponent();
            if (component instanceof EntryTreeNode<?> && !(component instanceof CategoryTreeNode)) continue;
            this.categories.expandRow(row);
        }
        this.categories.addMouseListener(new MouseAdapter() {

            private void hide() {
                if (actionMenu.isVisible()) actionMenu.setVisible(false);
            }

            @Override
            public void mouseReleased(@Nonnull MouseEvent event) {
                if (event.getButton() != MouseEvent.BUTTON3) return;
                int row = Menu.this.categories.getRowForLocation(event.getX(), event.getY());
                TreePath selPath = Menu.this.categories.getPathForLocation(event.getX(), event.getY());
                if (selPath != null && row > -1) {
                    Menu.this.categories.setSelectionPath(selPath);
                    Menu.this.categories.setSelectionRow(row);
                    actionMenu.show(event.getComponent(), event.getX(), event.getY());
                } else hide();
            }

            @Override
            public void mousePressed(@Nonnull MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON1) hide();
            }
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void newCategory(@Nonnull Function<String, Category> function) {
        String name = JOptionPane.showInputDialog("Enter a name");
        if (name == null || name.isEmpty()) return;
        CategoryTreeNode node = getSelectedCategory();
        AbstractCollection list = node == null ? storage.getCategories() : node.getEntry();
        Category category = function.apply(name);
        if (!list.contains(category)) {
            list.add(category);
            update();
        } else PassProtect.showErrorDialog("A category with this name does already exist");
    }

    private void newPassword(@Nonnull TriFunction<String, String, byte[], Password> function) {
        CategoryTreeNode node = getSelectedCategory();
        if (node != null) new PasswordEntry(new Password("", new byte[0]), config, (password, type) -> {
            if (!type.isConfirm()) return true;
            if (!password.getName().isEmpty() && password.getPassword().length > 0) {
                if (!exists(password, node.getEntry())) {
                    node.getEntry().add(function.apply(password.getName(), password.getDescription(), password.getPassword()));
                    update();
                    return true;
                } else PassProtect.showErrorDialog("A password with this name does already exist in this category");
            } else if (password.getName().isEmpty()) PassProtect.showErrorDialog("Login name cannot be blank");
            else PassProtect.showErrorDialog("Password cannot be blank");
            return false;
        });
        else PassProtect.showErrorDialog("You have to select a category first");
    }

    private void newBackupCode(@Nonnull BiFunction<String, String[], BackupCode> function) {
        CategoryTreeNode node = getSelectedCategory();
        if (node != null) new BackupCodeEntry(new BackupCode(""), config, (backupCode, type) -> {
            if (!type.isConfirm()) return true;
            BackupCode code = backupCode.get();
            if (!code.getName().isEmpty() && code.getCodes().length > 0) {
                if (!exists(code, node.getEntry())) {
                    if (code.getCodes().length <= 32) {
                        node.getEntry().add(function.apply(code.getName(), code.getCodes()));
                        update();
                        return true;
                    } else PassProtect.showErrorDialog("You passed the maximum of 32 backup codes");
                } else PassProtect.showErrorDialog("A backup code with this name does already exist in this category");
            } else if (code.getName().isEmpty()) PassProtect.showErrorDialog("Login name cannot be blank");
            else PassProtect.showErrorDialog("Codes cannot be blank");
            return false;
        });
        else PassProtect.showErrorDialog("You have to select a category first");
    }

    private void newFile(@Nonnull BiFunction<String, String[], File> function) {
        CategoryTreeNode node = getSelectedCategory();
        if (node != null) new FileEntry(new File(""), config, (file, type) -> {
            if (!type.isConfirm()) return true;
            if (!file.getName().isEmpty()) {
                if (!exists(file, node.getEntry())) {
                    node.getEntry().add(function.apply(file.getName(), file.getContent()));
                    update();
                    return true;
                } else PassProtect.showErrorDialog("A file with this name does already exist in this category");
            } else PassProtect.showErrorDialog("File name cannot be blank");
            return false;
        });
        else PassProtect.showErrorDialog("You have to select a category first");
    }

    private static boolean exists(@Nonnull Password password, @Nonnull Category category) {
        for (Entry e : category) if (e instanceof Password && e.getName().equals(password.getName())) return true;
        return false;
    }

    private static boolean exists(@Nonnull BackupCode backupCode, @Nonnull Category category) {
        for (Entry e : category) if (e instanceof BackupCode && e.getName().equals(backupCode.getName())) return true;
        return false;
    }

    private static boolean exists(@Nonnull File file, @Nonnull Category category) {
        for (Entry e : category) if (e instanceof File && e.getName().equals(file.getName())) return true;
        return false;
    }

    private void failedToSave(@Nonnull Exception e) {
        PassProtect.showErrorDialog("An error occurred while saving your changes", e);
    }

    @Nullable
    private EntryTreeNode<?> getSelectedEntry() {
        TreePath path = categories.getSelectionPath();
        if (path == null) return null;
        return path.getLastPathComponent() instanceof EntryTreeNode<?> node ? node : null;
    }

    @Nullable
    private CategoryTreeNode getSelectedCategory() {
        return getSelectedEntry() instanceof CategoryTreeNode node ? node : null;
    }

    @Nullable
    private PasswordTreeNode getSelectedPassword() {
        return getSelectedEntry() instanceof PasswordTreeNode node ? node : null;
    }

    @Nullable
    private BackupCodeTreeNode getSelectedBackupCode() {
        return getSelectedEntry() instanceof BackupCodeTreeNode node ? node : null;
    }

    @Nullable
    private FileTreeNode getSelectedFile() {
        return getSelectedEntry() instanceof FileTreeNode node ? node : null;
    }

    @Nonnull
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(720, 480);
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    private void createUIComponents() {
        this.searchBar = new TextField().setPlaceholder("Enter search query");
    }
}