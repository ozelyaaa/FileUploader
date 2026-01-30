package kz.kaspilab.fileuploader.utils;

import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class FileHashCalculator {

    public Mono<String> calculateHashSHA256(FilePart filePart) {
        MessageDigest messageDigest;

        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException e) {
            return Mono.error(e);
        }

        return filePart.content()
                .doOnNext(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    messageDigest.update(bytes);
                })
                .then(Mono.fromSupplier(() -> HexFormat.of().formatHex(messageDigest.digest())));
    }
}
