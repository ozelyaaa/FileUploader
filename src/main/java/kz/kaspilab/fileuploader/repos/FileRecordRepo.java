package kz.kaspilab.fileuploader.repos;

import kz.kaspilab.fileuploader.domains.FileRecord;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface FileRecordRepo extends ReactiveMongoRepository<FileRecord,String> {

    Mono<FileRecord> findByUploadedByAndIdempotencyKey(String uploadedBy, String idempotencyKey);
}
