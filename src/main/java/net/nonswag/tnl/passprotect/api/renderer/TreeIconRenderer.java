package net.nonswag.tnl.passprotect.api.renderer;

import net.nonswag.tnl.passprotect.Launcher;
import net.nonswag.tnl.passprotect.api.files.Config;
import net.nonswag.tnl.passprotect.api.nodes.BackupCodeTreeNode;
import net.nonswag.tnl.passprotect.api.nodes.EntryTreeNode;
import net.nonswag.tnl.passprotect.api.nodes.PasswordTreeNode;
import net.nonswag.tnl.passprotect.dialogs.ActiveDialogs;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.net.URL;
import java.util.HashMap;
import java.util.MissingResourceException;

public final class TreeIconRenderer extends DefaultTreeCellRenderer {

    @Nonnull
    private final JLabel label = new JLabel();
    @Nonnull
    private final Config config;

    public TreeIconRenderer(@Nonnull Config config) {
        this.config = config;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Component getTreeCellRendererComponent(@Nonnull JTree tree, @Nonnull Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean focus) {
        try {
            expanded = value instanceof PasswordTreeNode node ? ActiveDialogs.getInstance().containsKey(node.identity()) : expanded;
            expanded = value instanceof BackupCodeTreeNode node ? ActiveDialogs.getInstance().containsKey(node.identity()) : expanded;
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, false);
        } catch (Exception ignored) {
        } finally {
            if (value instanceof EntryTreeNode node) {
                this.setText("<html>" + node.style(selected).title() + "</html>");
                this.setFont(config.getFont());
                this.setIcon((expanded ? node.style(selected).expanded() : node.style(selected).collapsed()).getIcon(config));
            }
        }
        return this;
    }

    public enum Logo {
        DIRECTORY_CLOSED("images/directory/closed.png"),
        DIRECTORY_OPEN("images/directory/open.png"),
        LOCKED("images/password/locked.png"),
        UNLOCKED("images/password/unlocked.png"),
        BACKUP_OPEN("images/backup-codes/open.png"),
        BACKUP_CLOSED("images/backup-codes/closed.png"),
        FILE_CLOSED("images/file/closed.png"),
        FILE_OPEN("images/file/open.png"),
        TOTP_CLOSED("images/totp/closed.png"),
        TOTP_OPEN("images/totp/open.png"),
        PASSWORD("images/icons/password.png"),
        USER("images/icons/user.png"),
        HINT("images/icons/hint.png"),
        LOAD("images/icons/load.png"),
        SAVE("images/icons/save.png"),
        ;

        @Nonnull
        private final HashMap<Config, Icon> icons = new HashMap<>();
        @Nonnull
        private final String icon;

        Logo(@Nonnull String icon) {
            this.icon = icon;
        }

        @Nonnull
        public Icon getIcon(@Nonnull Config config) {
            if (icons.containsKey(config)) return icons.get(config);
            URL resource = Launcher.class.getClassLoader().getResource(icon);
            if (resource == null) throw new MissingResourceException(icon, Launcher.class.getName(), icon);
            int s = config.getFont().getSize();
            icons.put(config, new ImageIcon(new ImageIcon(resource).getImage().getScaledInstance(s, s, Image.SCALE_SMOOTH)));
            return icons.get(config);
        }
    }
}
