package kz.kaspilab.fileuploader.services;

import kz.kaspilab.fileuploader.domains.FileRecord;
import kz.kaspilab.fileuploader.enums.FileStatus;
import kz.kaspilab.fileuploader.repos.FileRecordRepo;
import kz.kaspilab.fileuploader.utils.FileHashCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {

    private final FileRecordRepo fileRecordRepo;
    private final FileHashCalculator fileHashCalculator;

    @Override
    public Mono<Void> upload(FilePart filePart, String uploadedBy) {
        return fileHashCalculator.calculateHashSHA256(filePart)
                .flatMap(hash -> handleUpload(filePart, hash, uploadedBy))
                .doOnSubscribe(sub -> log.info(
                        "Upload started: client={}, filename={}",
                        uploadedBy, filePart.filename())
                )
                .doOnSuccess(result -> log.info(
                        "Upload finished: client={}, filename={}", uploadedBy, filePart.filename())
                );
    }

    private Mono<Void> handleUpload(FilePart filePart, String hash, String uploadedBy) {
        return fileRecordRepo
                .findByUploadedByAndIdempotencyKey(uploadedBy, hash)
                .hasElement()
                .flatMap(exists -> {
                    if (exists) {
                        log.info(
                                "Duplicate upload: client={}, hash={}",
                                uploadedBy, hash
                        );
                        return Mono.empty();
                    }
                    return createPendingRecord(filePart, hash, uploadedBy);
                });
    }

    private Mono<Void> createPendingRecord(FilePart filePart, String hash, String uploadedBy) {
        FileRecord fileRecord = FileRecord
                .builder()
                .fileName(filePart.filename())
                .idempotencyKey(hash)
                .uploadedBy(uploadedBy)
                .uploadedAt(Instant.now())
                .status(FileStatus.PENDING)
                .build();
        return fileRecordRepo.save(fileRecord)
                .doOnSuccess(savedFile -> log.info(
                        "PENDING record created: id={}, hash={}",
                        savedFile.getId(), hash)
                ).then();
    }
}
