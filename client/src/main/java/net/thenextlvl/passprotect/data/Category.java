package net.thenextlvl.passprotect.data;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.thenextlvl.passprotect.data.entry.Entry;

import java.util.TreeSet;

@Getter
@Setter
@ToString
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
public class Category extends Entry {
    private @SerializedName("entries") TreeSet<Entry> entries = new TreeSet<>();

    @Override
    public int compareTo(Entry entry) {
        if (!entry.getClass().equals(Category.class)) return -1;
        return super.compareTo(entry);
    }
}
