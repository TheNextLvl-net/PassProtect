package net.thenextlvl.passprotect.data.entry.otp;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(fluent = true)
public class TOTP extends OTP {
    private @SerializedName("issuer") String issuer;
    private @SerializedName("account-name") String accountName;
}
