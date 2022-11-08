package net.nonswag.passprotect.utils;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Compressor {

    @Nonnull
    public static String compress(@Nonnull String[] strings) throws IOException {
        return compress(String.join("\n", strings));
    }

    @Nonnull
    public static String compress(@Nonnull String string) throws IOException {
        if (string.length() == 0) return string;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(); GZIPOutputStream gzip = new GZIPOutputStream(out)) {
            gzip.write(string.getBytes(StandardCharsets.UTF_8));
            gzip.close();
            return Base64.getEncoder().encodeToString(out.toByteArray());
        }
    }

    @Nonnull
    public static String decompress(@Nonnull String string) throws IOException {
        if (string.length() == 0) return string;
        byte[] decode = Base64.getDecoder().decode(string.getBytes(StandardCharsets.UTF_8));
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(); GZIPInputStream unzip = new GZIPInputStream(new ByteArrayInputStream(decode))) {
            byte[] buffer = new byte[256];
            int n;
            while ((n = unzip.read(buffer)) >= 0) out.write(buffer, 0, n);
            return out.toString(StandardCharsets.UTF_8);
        }
    }
}
