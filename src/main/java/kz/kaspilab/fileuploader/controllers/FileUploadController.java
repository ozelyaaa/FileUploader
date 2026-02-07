package kz.kaspilab.fileuploader.controllers;

import jakarta.validation.constraints.NotNull;
import kz.kaspilab.fileuploader.services.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
            @AuthenticationPrincipal Jwt jwt
            ) {
        String userId = jwt.getSubject();
        return fileUploadService.upload(filePart, userId)
                .thenReturn(ResponseEntity.accepted().build());
    }
}
