package kz.kaspilab.fileuploader.services;

import kz.kaspilab.fileuploader.domains.FileRecord;
import kz.kaspilab.fileuploader.enums.FileStatus;
import kz.kaspilab.fileuploader.repos.FileRecordRepo;
import kz.kaspilab.fileuploader.utils.FileHashCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {

    private final FileRecordRepo fileRecordRepo;
    private final FileHashCalculator fileHashCalculator;
    private final FileStorageService fileStorageService;

    @Override
    public Mono<Void> upload(FilePart filePart, String uploadedBy) {

        String filename = filePart.filename();

        return Mono.fromCallable(() ->
                Files.createTempFile("upload-", ".tmp")
        )
                .flatMap(tempPath ->
                        bufferToTempFile(filePart, tempPath)
                                .then(processTempFile(tempPath, filename, uploadedBy))
                                .doFinally(signal -> deleteTempFile(tempPath))
                )
                .doOnSubscribe(sub ->
                        log.info("Upload started: client={}, filename={}", uploadedBy, filename)
                )
                .doOnSuccess(v ->
                        log.info("Upload finished: client={}, filename={}", uploadedBy, filename)
                );
    }

    private Mono<Void> bufferToTempFile(FilePart filePart, Path tempPath) {
        return DataBufferUtils
                .write(filePart.content(), tempPath)
                .then();
    }

    private void deleteTempFile(Path tempPath) {
        try {
            Files.deleteIfExists(tempPath);
            log.debug("Temp file deleted: {}", tempPath);
        } catch (IOException e) {
            log.warn("Failed to delete temp file: {}", tempPath, e);
        }
    }

    private Mono<Void> processTempFile(Path tempPath, String filename, String uploadedBy) {
        return fileHashCalculator.calculateHashSHA256(tempPath)
                .flatMap(hash ->
                        fileRecordRepo.findByUploadedByAndIdempotencyKey(uploadedBy, hash)
                                .hasElement()
                                .flatMap(exists -> {
                                    if (exists) {
                                        log.info(
                                                "Duplicate upload: client={}, hash={}",
                                                uploadedBy, hash
                                        );
                                        return Mono.empty();
                                    }
                                    return processNewUpload(tempPath, filename, hash, uploadedBy);
                                })
                );
    }

    private Mono<Void> processNewUpload(Path tempPath, String filename, String hash, String uploadedBy) {
        return createPendingRecord(filename, hash, uploadedBy)
                .flatMap(record ->
                        fileStorageService
                                .upload(tempPath, record.getId())
                                .flatMap(path -> setUploaded(record.getId(), path))
                                .onErrorResume(e ->
                                        compensateFailure(record.getId(), record.getId(), e)
                                )
                );
    }

    private Mono<FileRecord> createPendingRecord(String filename, String hash, String uploadedBy) {
        FileRecord fileRecord = FileRecord
                .builder()
                .fileName(filename)
                .idempotencyKey(hash)
                .uploadedBy(uploadedBy)
                .uploadedAt(Instant.now())
                .status(FileStatus.PENDING)
                .build();
        return fileRecordRepo.save(fileRecord)
                .doOnSuccess(savedFile -> log.info(
                        "PENDING record created: id={}, hash={}",
                        savedFile.getId(), hash)
                );
    }

    private Mono<Void> setUploaded(String id, String path) {
        return fileRecordRepo.findById(id)
                .flatMap(fileRecord -> {
                    fileRecord.setStatus(FileStatus.UPLOADED);
                    fileRecord.setStoragePath(path);
                    fileRecord.setUploadedAt(Instant.now());
                    return fileRecordRepo.save(fileRecord);
                })
                .doOnSuccess(fileRecord ->
                        log.info("Upload COMPLETED: id={}", fileRecord.getId()))
                .then();
    }

    private Mono<Void> compensateFailure(String id, String storageKey, Throwable error) {
        log.error("Upload failed for recordId={}", id, error);

        return fileStorageService.delete(storageKey)
                .onErrorResume(e -> {
                    log.warn("Failed to delete file", e);
                    return Mono.empty();
                })
                .then(fileRecordRepo.deleteById(id));
    }
}
