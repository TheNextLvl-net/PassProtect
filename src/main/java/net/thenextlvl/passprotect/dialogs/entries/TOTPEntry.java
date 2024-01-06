package net.thenextlvl.passprotect.dialogs.entries;

import net.thenextlvl.core.utils.StringUtil;
import net.thenextlvl.passprotect.PassProtect;
import net.thenextlvl.passprotect.api.authentication.Authenticator;
import net.thenextlvl.passprotect.api.code.InvalidQRCodeException;
import net.thenextlvl.passprotect.api.code.QRCode;
import net.thenextlvl.passprotect.api.entry.TOTP;
import net.thenextlvl.passprotect.api.fields.PasswordField;
import net.thenextlvl.passprotect.api.fields.TextField;
import net.thenextlvl.passprotect.api.files.Config;
import net.thenextlvl.passprotect.util.Clipboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.net.URI;
import java.util.HashMap;

public class TOTPEntry extends JDialog {
    @Nonnull
    private JPanel panel;
    @Nonnull
    private JButton buttonOK, buttonCancel, qrCode, code;
    @Nonnull
    private JTextField issuer, accountName;
    @Nonnull
    private JPasswordField secretKey;
    @Nonnull
    private JCheckBox checkBox;

    @Nonnull
    private final CloseEvent closeEvent;
    @Nullable
    private Runnable onSuccess;

    @SuppressWarnings("BusyWait")
    public TOTPEntry(TOTP totp, Config config, CloseEvent closeEvent) {
        super(PassProtect.getInstance().getWindow(), totp.getName().isEmpty() ? null : totp.getName());
        if (!totp.getIssuer().isEmpty()) this.issuer.setText(totp.getIssuer());
        if (!totp.getAccountName().isEmpty()) this.accountName.setText(totp.getAccountName());
        if (!totp.getSecretKey().isEmpty()) this.secretKey.setText(totp.getSecretKey());
        this.closeEvent = closeEvent;
        registerListeners();
        setContentPane(panel);
        getRootPane().setDefaultButton(buttonOK);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(true);
        setPreferredSize(new Dimension(360, 210));
        setMinimumSize(getPreferredSize());
        pack();
        setLocationRelativeTo(PassProtect.getInstance().getWindow());
        setVisible(true);
        setAlwaysOnTop(true);
        panel.requestFocus(FocusEvent.Cause.ACTIVATION);
        new Thread(() -> {
            try {
                while (isVisible()) {
                    update();
                    Thread.sleep(1000);
                }
            } catch (InterruptedException ignored) {
            }
        }, "totp-code-generator").start();
    }

    private boolean isEmpty() {
        return secretKey.getPassword().length == 0 && (accountName.getText() == null || accountName.getText().isEmpty()) && (issuer.getText() == null || issuer.getText().isEmpty());
    }

    private void update() {
        try {
            if (isEmpty()) qrCode.setText("Import from QR-Code");
            else qrCode.setText("Show QR-Code");
            String secret = secretKey.getPassword() == null ? "" : String.valueOf(secretKey.getPassword());
            String text = StringUtil.format("0".repeat(6), Authenticator.INSTANCE.getTotpPassword(secret));
            qrCode.setEnabled(!accountName.getText().isEmpty() || isEmpty());
            code.setVisible(true);
            if (code.getText().equals(text)) return;
            code.setText(text);
            code.setEnabled(true);
        } catch (IllegalArgumentException e) {
            qrCode.setEnabled(isEmpty());
            code.setVisible(false);
        }
    }

    @Nonnull
    public TOTPEntry onSuccess(@Nullable Runnable onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }

    private void registerListeners() {
        code.addActionListener(actionEvent -> {
            StringSelection contents = new StringSelection(code.getText());
            Clipboard.setContent(code.getText(), (clipboard, transferable) -> {
                if (!code.getText().equals(Clipboard.getContent())) code.setEnabled(true);
            });
            code.setEnabled(false);
        });
        buttonOK.addActionListener(e -> close(CloseEvent.Type.CONFIRM));
        buttonCancel.addActionListener(e -> close(CloseEvent.Type.CANCEL));
        panel.registerKeyboardAction(e -> close(CloseEvent.Type.CLOSE), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyAdapter updater = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent event) {
                update();
            }
        };
        secretKey.addKeyListener(updater);
        accountName.addKeyListener(updater);
        qrCode.addActionListener(actionEvent -> {
            try {
                if (isEmpty()) {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setDialogTitle("Import an image file...");
                    chooser.setApproveButtonText("Import");
                    chooser.setApproveButtonToolTipText("Import selected file");
                    if (chooser.showOpenDialog(TOTPEntry.this) != JFileChooser.APPROVE_OPTION) return;
                    java.io.File file = chooser.getSelectedFile();
                    if (!file.exists()) return;
                    URI uri = URI.create(QRCode.read(file));
                    if (!uri.getHost().equals("totp")) throw new InvalidQRCodeException("Cannot interpret QR-Code");
                    if (!uri.getScheme().equals("otpauth")) throw new InvalidQRCodeException("scheme: " + uri.getScheme());
                    if (!uri.getPath().startsWith("/")) throw new InvalidQRCodeException("path: " + uri.getPath());
                    HashMap<String, String> parameters = new HashMap<>();
                    for (String query : uri.getQuery().split("&")) {
                        int index = query.indexOf("=");
                        parameters.put(query.substring(0, index), query.substring(index + 1));
                    }
                    String issuer = null, secret, path = uri.getPath().substring(1);
                    if (!parameters.containsKey("secret")) throw new InvalidQRCodeException("The QR-Code is invalid");
                    if (parameters.containsKey("issuer")) issuer = parameters.get("issuer");
                    secret = parameters.get("secret");
                    String[] paths = path.split(":");
                    this.issuer.setText(issuer);
                    this.accountName.setText(paths.length == 1 ? paths[0] : paths[1]);
                    this.secretKey.setText(secret);
                    update();
                } else {
                    String issuer = this.issuer.getText() == null ? "" : this.issuer.getText();
                    String accountName = this.accountName.getText() == null ? "" : this.accountName.getText();
                    String secretKey = this.secretKey.getPassword() == null ? "" : String.valueOf(this.secretKey.getPassword());
                    TOTP.showQRCode(issuer, accountName, secretKey);
                }
            } catch (Exception e) {
                if (isEmpty()) PassProtect.showErrorDialog("Failed to import QR-Code", e);
                else PassProtect.showErrorDialog("Failed to generate the QR-Code", e);
            }
        });
        checkBox.addActionListener(actionEvent -> ((PasswordField) secretKey).setPasswordVisible(checkBox.isSelected()));
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                close(CloseEvent.Type.CLOSE);
            }
        });
    }

    public void close(CloseEvent.Type type) {
        if (!closeEvent.close(getTotp(), type)) return;
        if (onSuccess != null) onSuccess.run();
        dispose();
    }

    @Nonnull
    private TOTP getTotp() {
        return new TOTP(issuer.getText(), accountName.getText(), String.valueOf(secretKey.getPassword()));
    }

    private void createUIComponents() {
        this.issuer = new net.thenextlvl.passprotect.api.fields.TextField().setPlaceholder("Issuer");
        this.accountName = new TextField().setPlaceholder("Account name");
        this.secretKey = new PasswordField().setPlaceholder("Secret key");
    }

    public interface CloseEvent {
        boolean close(TOTP totp, CloseEvent.Type type);

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
