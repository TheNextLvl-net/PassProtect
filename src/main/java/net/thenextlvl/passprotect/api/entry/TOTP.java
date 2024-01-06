package net.thenextlvl.passprotect.api.entry;

import com.google.gson.JsonObject;
import lombok.Getter;
import net.thenextlvl.core.utils.LinuxUtil;
import net.thenextlvl.passprotect.api.code.QRCode;
import net.thenextlvl.passprotect.api.nodes.TOTPTreeNode;
import org.apache.http.client.utils.URIBuilder;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Getter
public class TOTP implements Entry {

    @Nonnull
    private final String issuer, secretKey, accountName;

    public TOTP(String issuer, String accountName, String secretKey) {
        this.issuer = issuer;
        this.accountName = accountName;
        this.secretKey = secretKey;
    }

    @Nonnull
    @Override
    public String getName() {
        if (issuer.isEmpty()) return accountName;
        return "%s (%s)".formatted(issuer, accountName);
    }

    @Nonnull
    @Override
    public JsonObject parse() {
        JsonObject object = new JsonObject();
        object.addProperty("issuer", issuer);
        object.addProperty("account-name", accountName);
        object.addProperty("secret-key", secretKey);
        return object;
    }

    @Nonnull
    @Override
    public TOTPTreeNode tree() {
        return new TOTPTreeNode(this);
    }

    @Override
    public int getType() {
        return TOTP;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        return o instanceof TOTP totp && issuer.equals(totp.issuer) && accountName.equals(totp.accountName) && secretKey.equals(totp.secretKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(issuer, accountName, secretKey);
    }

    @Override
    public int compareTo(Entry entry) {
        return equals(entry) ? 0 : entry instanceof TOTP file ? getName().compareTo(file.getName()) : 1;
    }

    public static void showQRCode(String issuer, String accountName, String secret) throws Exception {
        if (accountName.isEmpty()) throw new IllegalArgumentException("The account name cannot be empty");
        if (secret.isEmpty()) throw new IllegalArgumentException("The secret cannot be empty");
        File qrCode = QRCode.generate(generateQRCode(issuer, accountName, secret));
        try {
            Desktop.getDesktop().open(qrCode);
        } catch (IOException e) {
            LinuxUtil.Suppressed.runShellCommand("xdg-open %s".formatted(qrCode.getAbsolutePath()));
        }
    }

    @Nonnull
    private static String generateQRCode(String issuer, String accountName, String key) {
        URIBuilder uri = new URIBuilder().setScheme("otpauth").setHost("totp").setPath("/" + formatLabel(issuer, accountName));
        uri.setParameter("secret", key);
        if (!issuer.isEmpty()) uri.setParameter("issuer", issuer);
        return uri.toString();
    }

    @Nonnull
    private static String formatLabel(String issuer, String accountName) {
        if (accountName.isEmpty()) throw new IllegalArgumentException("Account name cannot be empty");
        if (issuer.contains(":")) throw new IllegalArgumentException("Issuer contains an invalid character <':'>");
        return issuer.isEmpty() ? accountName : issuer + ":" + accountName;
    }
}
