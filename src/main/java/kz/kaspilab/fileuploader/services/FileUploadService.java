package kz.kaspilab.fileuploader.services;

import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

public interface FileUploadService {

    Mono<Void> upload(FilePart filePart, String uploadedBy);
}
