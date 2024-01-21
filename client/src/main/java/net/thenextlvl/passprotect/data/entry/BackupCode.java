package net.thenextlvl.passprotect.data.entry;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(fluent = true)
public class BackupCode extends Entry {
    private @SerializedName("codes") String[] codes;
}
