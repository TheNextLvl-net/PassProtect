package net.nonswag.tnl.passprotect.dialogs.entries;

import net.nonswag.tnl.core.api.file.formats.TextFile;
import net.nonswag.tnl.passprotect.api.entry.File;
import net.nonswag.tnl.passprotect.api.fields.TextField;
import net.nonswag.tnl.passprotect.api.files.Config;
import net.nonswag.tnl.passprotect.api.renderer.TreeIconRenderer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class FileEntry extends JDialog {
    @Nonnull
    private JPanel panel;
    @Nonnull
    private JButton buttonOK, buttonCancel;
    @Nonnull
    private JTextField name;
    @Nonnull
    private JTextArea content;
    @Nonnull
    private JButton importFile, exportFile;

    @Nonnull
    private final CloseEvent closeEvent;
    @Nullable
    private Runnable onSuccess;

    public FileEntry(@Nonnull File file, @Nonnull Config config, @Nonnull CloseEvent closeEvent) {
        super((Frame) null, file.getName().isEmpty() ? null : file.getName());
        if (!file.getName().isEmpty()) name.setText(file.getName());
        content.setText(String.join("\n", file.getContent()));
        importFile.setVisible(file.getContent().length == 0);
        exportFile.setVisible(!importFile.isVisible());
        exportFile.setHorizontalAlignment(SwingConstants.LEFT);
        importFile.setHorizontalAlignment(SwingConstants.LEFT);
        importFile.setIcon(TreeIconRenderer.Logo.LOAD.getIcon(config));
        exportFile.setIcon(TreeIconRenderer.Logo.SAVE.getIcon(config));
        this.closeEvent = closeEvent;
        registerListeners();
        setContentPane(panel);
        getRootPane().setDefaultButton(buttonOK);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(true);
        setPreferredSize(new Dimension(360, 360));
        setMinimumSize(getPreferredSize());
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        setAlwaysOnTop(true);
        panel.requestFocus(FocusEvent.Cause.ACTIVATION);
    }

    private void registerListeners() {
        buttonOK.addActionListener(e -> close(CloseEvent.Type.CONFIRM));
        buttonCancel.addActionListener(e -> close(CloseEvent.Type.CANCEL));
        panel.registerKeyboardAction(e -> close(CloseEvent.Type.CLOSE), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(@Nonnull WindowEvent event) {
                close(CloseEvent.Type.CLOSE);
            }
        });
        name.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(@Nonnull KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_DOWN) content.requestFocus();
            }
        });
        content.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(@Nonnull KeyEvent event) {
                key();
            }

            @Override
            public void keyPressed(@Nonnull KeyEvent event) {
                key();
            }

            @Override
            public void keyReleased(@Nonnull KeyEvent event) {
                key();
            }

            private void key() {
                importFile.setVisible(content.getText().isEmpty());
                exportFile.setVisible(!importFile.isVisible());
            }
        });
        importFile.addActionListener(actionEvent -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Import a file...");
            fileChooser.setApproveButtonText("Import");
            fileChooser.setApproveButtonToolTipText("Import selected file");
            if (fileChooser.showOpenDialog(FileEntry.this) != JFileChooser.APPROVE_OPTION) return;
            java.io.File file = fileChooser.getSelectedFile();
            if (!file.exists()) return;
            name.setText(file.getName());
            content.setText(String.join("\n", new TextFile(file).getContent()));
            if (!content.getText().isEmpty()) {
                importFile.setVisible(false);
                exportFile.setVisible(true);
            } else exportFile.setVisible(false);
        });
        exportFile.addActionListener(actionEvent -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new java.io.File(name.getText()));
            fileChooser.setDialogTitle("Export as...");
            fileChooser.setApproveButtonText("Export");
            fileChooser.setApproveButtonToolTipText("Export to selected file");
            if (fileChooser.showOpenDialog(FileEntry.this) != JFileChooser.APPROVE_OPTION) return;
            java.io.File file = fileChooser.getSelectedFile();
            new TextFile(file.getAbsoluteFile()).setContent(getContent()).save();
            JOptionPane.showInternalMessageDialog(null, "Successfully exported " + file.getName(), "Exported", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    @Nonnull
    public FileEntry onSuccess(@Nullable Runnable onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }

    public void close(@Nonnull CloseEvent.Type type) {
        if (!closeEvent.close(getFile(), type)) return;
        if (onSuccess != null) onSuccess.run();
        dispose();
    }

    @Nonnull
    public File getFile() {
        return new File(name.getText(), getContent());
    }

    @Nonnull
    private String[] getContent() {
        if (content.getText().isEmpty()) return new String[0];
        return content.getText().split("\n");
    }

    private void createUIComponents() {
        this.name = new TextField().setPlaceholder("File name");
    }

    public interface CloseEvent {
        boolean close(@Nonnull File file, @Nonnull CloseEvent.Type type);

        enum Type {
            CLOSE, CANCEL, CONFIRM;

            public boolean isClose() {
                return equals(CLOSE);
            }

            public boolean isCancel() {
                return equals(CANCEL);
            }

            public boolean isConfirm() {
                return equals(CONFIRM);
            }
        }
    }
}
