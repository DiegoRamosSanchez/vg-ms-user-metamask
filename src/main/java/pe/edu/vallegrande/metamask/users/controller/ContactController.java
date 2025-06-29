package pe.edu.vallegrande.metamask.users.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.metamask.users.dto.ContactCreateDto;
import pe.edu.vallegrande.metamask.users.dto.ContactResponseDto;
import pe.edu.vallegrande.metamask.users.dto.ContactUpdateDto;
import pe.edu.vallegrande.metamask.users.service.ContactService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping("/user/{userId}")
    public Mono<ResponseEntity<ContactResponseDto>> createContact(
            @PathVariable UUID userId,
            @Valid @RequestBody ContactCreateDto createDto) {
        return contactService.createContact(userId, createDto)
                .map(contact -> ResponseEntity.status(HttpStatus.CREATED).body(contact))
                .onErrorResume(throwable ->
                        Mono.just(ResponseEntity.badRequest().build())
                );
    }

    @GetMapping("/user/{userId}")
    public Flux<ContactResponseDto> getUserContacts(@PathVariable UUID userId) {
        return contactService.getUserContacts(userId);
    }

    @GetMapping("/{contactId}/user/{userId}")
    public Mono<ResponseEntity<ContactResponseDto>> getContact(
            @PathVariable UUID contactId,
            @PathVariable UUID userId) {
        return contactService.getContactById(userId, contactId)
                .map(contact -> ResponseEntity.ok(contact))
                .onErrorResume(throwable ->
                        Mono.just(ResponseEntity.notFound().build())
                );
    }

    @PutMapping("/{contactId}/user/{userId}")
    public Mono<ResponseEntity<ContactResponseDto>> updateContact(
            @PathVariable UUID contactId,
            @PathVariable UUID userId,
            @Valid @RequestBody ContactUpdateDto updateDto) {
        return contactService.updateContact(userId, contactId, updateDto)
                .map(contact -> ResponseEntity.ok(contact))
                .onErrorResume(throwable ->
                        Mono.just(ResponseEntity.notFound().build())
                );
    }

    @DeleteMapping("/{contactId}/user/{userId}")
    public Mono<ResponseEntity<Void>> deleteContact(
            @PathVariable UUID contactId,
            @PathVariable UUID userId) {
        return contactService.deleteContact(userId, contactId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(throwable ->
                        Mono.just(ResponseEntity.notFound().build())
                );
    }
}