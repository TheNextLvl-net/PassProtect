package net.thenextlvl.passprotect.api.files;

import com.github.weisj.darklaf.theme.OneDarkTheme;
import com.google.gson.JsonObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.thenextlvl.core.api.errors.file.FileLoadException;
import net.thenextlvl.core.api.errors.file.FileSaveException;
import net.thenextlvl.core.api.file.formats.JsonFile;
import net.thenextlvl.core.api.math.MathUtil;
import net.thenextlvl.core.utils.SystemUtil;
import net.thenextlvl.passprotect.Launcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = false)
public class Config extends JsonFile {

    @Getter
    @Setter
    @Nullable
    private static Config instance = null;
    @Nonnull
    public static Config APP = new Config();

    @Nonnull
    private Font font;
    @Nonnull
    private UIManager.LookAndFeelInfo appearance;
    @Nullable
    private String hint, lastUser;
    @Nonnull
    private final String username;
    @Nullable
    private final TrustedDevices trustedDevices;
    @Nonnull
    private Image profilePicture;
    private int passwordChangeReminder;
    private final boolean profilePictureSet;
    private long lastPasswordChange;
    private boolean sessionLocked;

    public Config(String username) {
        super(username, "config.json");
        this.trustedDevices = new TrustedDevices(this.username = username);
        Image profilePicture;
        boolean profilePictureSet;
        try {
            profilePicture = new ImageIcon(ImageIO.read(new File(username, "profile-picture.png"))).getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
            profilePictureSet = true;
        } catch (IOException ignored) {
            BufferedImage image = new BufferedImage(48, 48, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setClip(new Ellipse2D.Float(0, 0, 48, 48));
            graphics.setColor(new Color(MathUtil.randomInteger(0, 255), MathUtil.randomInteger(0, 255), MathUtil.randomInteger(0, 255)));
            graphics.fill(graphics.getClip());
            graphics.setColor(Color.BLACK);
            graphics.drawString(String.valueOf(username.charAt(0)), 23, 28);
            profilePicture = image;
            profilePictureSet = false;
        }
        this.profilePicture = profilePicture;
        this.profilePictureSet = profilePictureSet;
    }

    private Config() {
        super("config.json");
        trustedDevices = null;
        username = "APP";
        profilePictureSet = false;
    }

    @Nonnull
    public TrustedDevices getTrustedDevices() {
        assert trustedDevices != null;
        return trustedDevices;
    }

    @Nonnull
    @Override
    public Config load() throws FileLoadException {
        super.load();
        JsonObject root = getJsonElement().getAsJsonObject();
        if (!root.has("font") || !root.get("font").isJsonObject()) root.add("font", new JsonObject());
        if (!root.has("appearance")) root.addProperty("appearance", OneDarkTheme.class.getName());
        JsonObject font = root.getAsJsonObject("font");
        if (!font.has("type")) font.addProperty("type", OSUtil.isLinux() ? "Ubuntu" : "Arial");
        if (!font.has("size")) font.addProperty("size", 13);
        if (root.has("hint")) hint = new String(Base64.getDecoder().decode(root.get("hint").getAsString()));
        this.lastPasswordChange = root.has("last-password-change") ? root.get("last-password-change").getAsLong() : System.currentTimeMillis();
        this.passwordChangeReminder = root.has("password-change-reminder") ? root.get("password-change-reminder").getAsInt() : Reminder.MONTHLY;
        this.font = new Font(font.get("type").getAsString(), Font.PLAIN, font.get("size").getAsInt());
        this.lastUser = root.has("last-user") ? root.get("last-user").getAsString() : null;
        this.appearance = parse(root.get("appearance").getAsString());
        this.sessionLocked = root.has("session-locked") && root.get("session-locked").getAsBoolean();
        return this;
    }

    @Override
    public void save() throws FileSaveException {
        JsonObject root = new JsonObject();
        JsonObject font = new JsonObject();
        if (!equals(APP)) {
            font.addProperty("type", this.font.getName());
            font.addProperty("size", this.font.getSize());
            root.add("font", font);
            root.addProperty("appearance", this.appearance.getClassName());
            if (hint != null) root.addProperty("hint", Base64.getEncoder().encodeToString(hint.getBytes(getCharset())));
            root.addProperty("last-password-change", lastPasswordChange);
            root.addProperty("password-change-reminder", passwordChangeReminder);
        } else if (equals(APP) && lastUser != null) {
            root.addProperty("last-user", lastUser);
            if (sessionLocked) root.addProperty("session-locked", true);
            else root.remove("session-locked");
        }
        setJsonElement(root);
        super.save();
    }

    @Nonnull
    private UIManager.LookAndFeelInfo parse(String name) {
        for (UIManager.LookAndFeelInfo theme : Launcher.getLookAndFeels()) {
            if (theme.getClassName().equals(name)) return theme;
        }
        return Launcher.getLookAndFeels().get(0);
    }

    public static class Reminder {
        private static final int NEVER = 0;
        private static final int MONTHLY = 1;
        private static final int YEARLY = 2;
    }
}
