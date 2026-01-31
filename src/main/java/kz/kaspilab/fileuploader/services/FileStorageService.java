package kz.kaspilab.fileuploader.services;

import reactor.core.publisher.Mono;

import java.nio.file.Path;

public interface FileStorageService {

    Mono<String> upload(Path source, String storageKey);

    Mono<Void> delete(String storageKey);
}
