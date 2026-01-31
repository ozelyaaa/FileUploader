package kz.kaspilab.fileuploader.domains;

import kz.kaspilab.fileuploader.enums.FileStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "files")
@CompoundIndex(
        name = "uploadedBy_idempotencyKey_index",
        def = "{'uploadedBy': 1, 'idempotencyKey': 1}",
        unique = true
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileRecord {

    @Id
    private String id;

    private String fileName;

    private String uploadedBy;

    private String idempotencyKey;

    private String storagePath;

    private Instant uploadedAt;

    private FileStatus status;
}
