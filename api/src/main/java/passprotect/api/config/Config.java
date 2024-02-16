package passprotect.api.config;

import com.google.gson.annotations.SerializedName;

public record Config(
        @SerializedName("port") int port,
        @SerializedName("allowed-origin") String allowedOrigin,
        @SerializedName("token-validity") long tokenValidity
) {
}
