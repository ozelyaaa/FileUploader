package kz.kaspilab.fileuploader.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
@Slf4j
public class LocalFileStorageServiceImpl implements FileStorageService {

    private static final Path BASE_PATH = Path.of("uploads");

    public LocalFileStorageServiceImpl() throws IOException {
        Files.createDirectories(BASE_PATH);
    }

    @Override
    public Mono<String> upload(Path source, String storageKey) {
        Path target = BASE_PATH.resolve(storageKey);

        return Mono.fromRunnable(() -> {
                    try {
                        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .thenReturn(target.toString());
    }

    @Override
    public Mono<Void> delete(String storageKey) {
        Path target = BASE_PATH.resolve(storageKey);

        return Mono.fromRunnable(() -> {
            try {
                Files.deleteIfExists(target);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
