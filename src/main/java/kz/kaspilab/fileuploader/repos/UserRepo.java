package kz.kaspilab.fileuploader.repos;

import kz.kaspilab.fileuploader.domains.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface UserRepo extends ReactiveMongoRepository<User, String> {

    Mono<User> findByUsername(String username);
}
