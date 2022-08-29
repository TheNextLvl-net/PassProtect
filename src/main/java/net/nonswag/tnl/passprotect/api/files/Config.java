package net.nonswag.tnl.passprotect.api.files;

import com.github.weisj.darklaf.theme.OneDarkTheme;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import net.nonswag.tnl.core.api.errors.file.FileLoadException;
import net.nonswag.tnl.core.api.errors.file.FileSaveException;
import net.nonswag.tnl.core.api.file.formats.JsonFile;
import net.nonswag.tnl.core.utils.SystemUtil;
import net.nonswag.tnl.passprotect.Launcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.util.Base64;

@Getter
@Setter
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
    private String hint;
    @Nullable
    private String lastUser;

    public Config(@Nonnull String username) {
        super(username, "config.json");
    }

    private Config() {
        super("config.json");
    }

    @Nonnull
    @Override
    public Config load() throws FileLoadException {
        super.load();
        JsonObject root = getJsonElement().getAsJsonObject();
        if (!root.has("font") || !root.get("font").isJsonObject()) root.add("font", new JsonObject());
        if (!root.has("appearance")) root.addProperty("appearance", OneDarkTheme.class.getName());
        JsonObject font = root.getAsJsonObject("font");
        if (!font.has("type")) font.addProperty("type", SystemUtil.TYPE.isLinux() ? "Ubuntu" : "Arial");
        if (!font.has("size")) font.addProperty("size", 13);
        if (root.has("hint")) hint = new String(Base64.getDecoder().decode(root.get("hint").getAsString()));
        this.font = new Font(font.get("type").getAsString(), Font.PLAIN, font.get("size").getAsInt());
        this.lastUser = root.has("last-user") ? root.get("last-user").getAsString() : null;
        this.appearance = parse(root.get("appearance").getAsString());
        return this;
    }

    @Override
    public void save() throws FileSaveException {
        JsonObject root = new JsonObject();
        JsonObject font = new JsonObject();
        font.addProperty("type", this.font.getName());
        font.addProperty("size", this.font.getSize());
        root.add("font", font);
        root.addProperty("appearance", this.appearance.getClassName());
        if (hint != null) root.addProperty("hint", Base64.getEncoder().encodeToString(hint.getBytes(getCharset())));
        if (lastUser != null) root.addProperty("last-user", lastUser);
        setJsonElement(root);
        super.save();
    }

    @Nonnull
    private UIManager.LookAndFeelInfo parse(@Nonnull String name) {
        for (UIManager.LookAndFeelInfo theme : Launcher.getLookAndFeels()) {
            if (theme.getClassName().equals(name)) return theme;
        }
        return Launcher.getLookAndFeels().get(0);
    }

    @Override
    public String toString() {
        return "Config{" +
                "font=" + font +
                ", appearance=" + appearance +
                ", hint='" + hint + '\'' +
                '}';
    }
}
