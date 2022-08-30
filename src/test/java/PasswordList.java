import net.nonswag.tnl.core.api.file.formats.TextFile;
import net.nonswag.tnl.passprotect.utils.Compressor;

import java.io.IOException;

public class PasswordList {

    public static void main(String[] args) throws IOException {
        decompress();
    }

    private static void compress() throws IOException {
        TextFile decompressed = new TextFile("passwords.txt");
        TextFile compressed = new TextFile("compressed.txt");
        compressed.setContent(Compressor.compress(String.join("\n", decompressed.getContent())));
        compressed.save();
    }

    private static void decompress() throws IOException {
        TextFile decompressed = new TextFile("passwords.txt");
        TextFile compressed = new TextFile("compressed.txt");
        decompressed.setContent(Compressor.decompress(String.join("\n", compressed.getContent())));
        decompressed.save();
    }
}
