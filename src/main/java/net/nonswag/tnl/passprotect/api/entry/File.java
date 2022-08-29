package net.nonswag.tnl.passprotect.api.entry;

import com.google.gson.JsonObject;
import lombok.Getter;
import net.nonswag.tnl.passprotect.api.nodes.FileTreeNode;
import net.nonswag.tnl.passprotect.utils.Compressor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

@Getter
public class File implements Entry {

    @Nonnull
    private final String name;
    @Nonnull
    private final String[] content;

    public File(@Nonnull String name, @Nonnull String... content) {
        this.name = name;
        this.content = content;
    }

    @Nonnull
    @Override
    public JsonObject parse() {
        JsonObject object = new JsonObject();
        object.addProperty("name", name);
        try {
            object.addProperty("content", Compressor.compress(content));
        } catch (IOException e) {
            object.addProperty("error", Base64.getEncoder().encodeToString(String.join("\n", content).getBytes(StandardCharsets.UTF_8)));
            e.printStackTrace();
        }
        return object;
    }

    @Nonnull
    @Override
    public FileTreeNode tree() {
        return new FileTreeNode(this);
    }

    @Override
    public int getType() {
        return Entry.FILE;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof File file && name.equals(file.name) && Arrays.equals(content, file.content);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name);
        result = 31 * result + Arrays.hashCode(content);
        return result;
    }

    @Override
    public int compareTo(@Nonnull Entry entry) {
        return equals(entry) ? 0 : entry instanceof File file ? name.compareTo(file.name) : 1;
    }
}
