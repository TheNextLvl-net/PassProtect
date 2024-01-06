package net.thenextlvl.passprotect.api.code;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class QRCode {

    @Nonnull
    public static File generate(String text) throws WriterException, IOException {
        BitMatrix bitMatrix = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 350, 350);
        File file = File.createTempFile(UUID.nameUUIDFromBytes(text.getBytes(StandardCharsets.UTF_8)).toString(), ".png");
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", file.toPath());
        return file;
    }

    @Nonnull
    public static String read(File qrCode) throws IOException, NotFoundException {
        LuminanceSource source = new BufferedImageLuminanceSource(ImageIO.read(qrCode));
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Result result = new MultiFormatReader().decode(bitmap);
        return result.getText();
    }
}
