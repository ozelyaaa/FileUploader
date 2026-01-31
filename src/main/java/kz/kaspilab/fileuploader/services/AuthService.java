package kz.kaspilab.fileuploader.services;

import kz.kaspilab.fileuploader.domains.User;
import kz.kaspilab.fileuploader.models.AuthResponse;
import kz.kaspilab.fileuploader.models.LoginRequest;
import kz.kaspilab.fileuploader.models.RegisterRequest;
import kz.kaspilab.fileuploader.repos.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    @Value("${security.jwt.issuer}")
    private String issuer;

    @Value("${security.jwt.ttl-minutes}")
    private long ttlMinutes;

    public Mono<User> register(RegisterRequest request) {
        return userRepo.findByUsername(request.username())
                .hasElement()
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalStateException("Username is already in use"));
                    }
                    return Mono.fromCallable(() -> passwordEncoder.encode(request.password()))
                            .subscribeOn(Schedulers.boundedElastic())
                            .flatMap(hash -> userRepo.save(
                                    User.builder()
                                            .username(request.username())
                                            .passwordHash(hash)
                                            .createdAt(Instant.now())
                                            .build()
                            ));
                });
    }

    public Mono<AuthResponse> login(LoginRequest request) {
        return userRepo.findByUsername(request.username())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Invalid username")))
                .flatMap(user ->
                        Mono.fromCallable(() ->
                                passwordEncoder.matches(request.password(), user.getPasswordHash()))
                                .subscribeOn(Schedulers.boundedElastic())
                                .flatMap(matches -> {
                                    if (!matches) {
                                        return  Mono.error(new IllegalArgumentException("Invalid password"));
                                    }
                                    return Mono.just(new AuthResponse(issueToken(user)));
                                })
                );
    }

    private String issueToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(Duration.ofMinutes(ttlMinutes));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(expiry)
                .subject(user.getId())
                .claim("username", user.getUsername())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder
                .encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();
    }
}
