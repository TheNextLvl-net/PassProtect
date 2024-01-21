package net.thenextlvl.passprotect.server.model;

import com.google.gson.annotations.SerializedName;
import core.util.StringUtil;

public record Account(
        @SerializedName("email") String email,
        @SerializedName("password") String password,
        @SerializedName("salt") String salt
) {
    public Account(String email, String password) {
        this(email, password, StringUtil.random(16));
    }
}
