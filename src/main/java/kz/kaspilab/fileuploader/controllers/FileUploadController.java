package kz.kaspilab.fileuploader.controllers;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kz.kaspilab.fileuploader.services.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Validated
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<Void>> uploadFile(
            @RequestPart("file") @NotNull FilePart filePart,
            @RequestHeader("X-Client-Id") @NotBlank String uploadedBy
    ) {
        return fileUploadService.upload(filePart, uploadedBy)
                .then(Mono.just(ResponseEntity.accepted().build()));
    }
}
