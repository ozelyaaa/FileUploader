package kz.kaspilab.fileuploader.utils;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
public class FileHashCalculator {

    public Mono<String> calculateHashSHA256(Path filePath) {
        return Mono.fromCallable(() -> {
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");

                    try (InputStream inputStream = Files.newInputStream(filePath)) {
                        byte[] buffer = new byte[8_192]; // 8 KB chunks
                        int bytesRead;

                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            digest.update(buffer, 0, bytesRead);
                        }
                    }

                    return HexFormat.of().formatHex(digest.digest());
                })
                .subscribeOn(Schedulers.boundedElastic());
    }
}
