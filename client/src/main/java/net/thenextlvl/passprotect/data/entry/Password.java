package net.thenextlvl.passprotect.data.entry;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(fluent = true)
public class Password extends Entry {
    private @SerializedName("description") String description;
    private @SerializedName("password") byte[] password;
}
