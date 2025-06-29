package pe.edu.vallegrande.metamask.users.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.metamask.users.entity.Contact;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface ContactRepository extends ReactiveCrudRepository<Contact, UUID> {

    Flux<Contact> findByUserId(UUID userId);

    Mono<Contact> findByIdAndUserId(UUID id, UUID userId);

    Mono<Long> deleteByIdAndUserId(UUID id, UUID userId);

    Mono<Boolean> existsByUserIdAndName(UUID userId, String name);

    Mono<Boolean> existsByUserIdAndAddress(UUID userId, String address);
}