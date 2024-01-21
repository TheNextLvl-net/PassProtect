package net.thenextlvl.passprotect.data.entry.otp;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(fluent = true)
public class HOTP extends OTP {
    private @SerializedName("counter") int counter;
}
