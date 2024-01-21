package net.thenextlvl.passprotect.data.entry;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Accessors(fluent = true)
public abstract class Entry implements Comparable<Entry> {
    private @SerializedName("name") String name;

    @Override
    public int compareTo(Entry entry) {
        return name().compareTo(entry.name());
    }
}
