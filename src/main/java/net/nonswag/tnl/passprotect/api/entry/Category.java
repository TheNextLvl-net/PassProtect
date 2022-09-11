package net.nonswag.tnl.passprotect.api.entry;

import com.google.gson.JsonObject;
import lombok.Getter;
import net.nonswag.tnl.passprotect.api.nodes.CategoryTreeNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.TreeSet;

@Getter
public class Category extends TreeSet<Entry> implements Entry {

    @Nonnull
    private final String name;

    public Category(@Nonnull String name, @Nonnull Collection<? extends Entry> collection) {
        super(collection);
        this.name = name;
    }

    public Category(@Nonnull String name) {
        this.name = name;
    }

    @Override
    public boolean remove(@Nullable Object object) {
        return removeIf(entry -> Objects.equals(entry, object));
    }

    @Nonnull
    @Override
    public JsonObject parse() {
        JsonObject object = new JsonObject();
        object.addProperty("name", getName());
        for (Entry entry : this) object.add("%s;%s".formatted(entry.getType(), entry.hashCode()), entry.parse());
        return object;
    }

    @Nonnull
    @Override
    public CategoryTreeNode tree() {
        CategoryTreeNode node = new CategoryTreeNode(this);
        for (Entry entry : this) node.add(entry.tree());
        return node;
    }

    @Override
    public int getType() {
        return Entry.CATEGORY;
    }

    @Override
    public String toString() {
        return "Category{" +
                "name='" + name + '\'' +
                "entries='" + super.toString() + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        return o instanceof Category c && name.equals(c.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name);
    }

    @Override
    public int compareTo(@Nonnull Entry entry) {
        return equals(entry) ? 0 : entry instanceof Category category ? name.compareTo(category.name) : -1;
    }
}
