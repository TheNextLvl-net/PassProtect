package net.thenextlvl.passprotect.data.entry.otp;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.thenextlvl.passprotect.data.entry.Entry;

@Getter
@Setter
@Accessors(fluent = true)
public abstract class OTP extends Entry {
    private @SerializedName("secret-key") String secretKey;
}
