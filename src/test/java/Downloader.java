import net.nonswag.tnl.passprotect.utils.Compressor;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class Downloader {

    public static void main(String[] args) throws IOException {
        String compressed = readContent("https://raw.githubusercontent.com/NonSwag/PassProtect/main/compressed.txt");
        System.out.println(Compressor.decompress(compressed));
    }

    @Nonnull
    public static String readContent(@Nonnull String url) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
            String line;
            StringBuilder content = new StringBuilder();
            while ((line = reader.readLine()) != null) content.append(line).append("\n");
            return content.toString();
        }
    }
}
