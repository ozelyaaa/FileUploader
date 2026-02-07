package kz.kaspilab.fileuploader.controllers;

import kz.kaspilab.fileuploader.models.AuthResponse;
import kz.kaspilab.fileuploader.models.LoginRequest;
import kz.kaspilab.fileuploader.models.RegisterRequest;
import kz.kaspilab.fileuploader.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Mono<ResponseEntity<Void>> register(@RequestBody RegisterRequest registerRequest) {
        return authService.register(registerRequest)
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).build());
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest)
                .map(ResponseEntity::ok);
    }
}
