package pe.edu.vallegrande.metamask.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.metamask.users.dto.ContactCreateDto;
import pe.edu.vallegrande.metamask.users.dto.ContactResponseDto;
import pe.edu.vallegrande.metamask.users.dto.ContactUpdateDto;
import pe.edu.vallegrande.metamask.users.entity.Contact;
import pe.edu.vallegrande.metamask.users.repository.ContactRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;

    public Mono<ContactResponseDto> createContact(UUID userId, ContactCreateDto createDto) {
        return contactRepository.existsByUserIdAndName(userId, createDto.getName())
                .flatMap(nameExists -> {
                    if (nameExists) {
                        return Mono.error(new RuntimeException("Contact name already exists"));
                    }
                    return contactRepository.existsByUserIdAndAddress(userId, createDto.getAddress());
                })
                .flatMap(addressExists -> {
                    if (addressExists) {
                        return Mono.error(new RuntimeException("Contact address already exists"));
                    }

                    Contact contact = Contact.builder()
                            .userId(userId)
                            .name(createDto.getName())
                            .address(createDto.getAddress())
                            .notes(createDto.getNotes())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();

                    return contactRepository.save(contact);
                })
                .map(this::convertToResponseDto);
    }

    public Flux<ContactResponseDto> getUserContacts(UUID userId) {
        return contactRepository.findByUserId(userId)
                .map(this::convertToResponseDto);
    }

    public Mono<ContactResponseDto> getContactById(UUID userId, UUID contactId) {
        return contactRepository.findByIdAndUserId(contactId, userId)
                .map(this::convertToResponseDto)
                .switchIfEmpty(Mono.error(new RuntimeException("Contact not found")));
    }

    public Mono<ContactResponseDto> updateContact(UUID userId, UUID contactId, ContactUpdateDto updateDto) {
        return contactRepository.findByIdAndUserId(contactId, userId)
                .switchIfEmpty(Mono.error(new RuntimeException("Contact not found")))
                .flatMap(contact -> {
                    if (updateDto.getName() != null) {
                        contact.setName(updateDto.getName());
                    }
                    if (updateDto.getAddress() != null) {
                        contact.setAddress(updateDto.getAddress());
                    }
                    if (updateDto.getNotes() != null) {
                        contact.setNotes(updateDto.getNotes());
                    }
                    contact.setUpdatedAt(LocalDateTime.now());

                    return contactRepository.save(contact);
                })
                .map(this::convertToResponseDto);
    }

    public Mono<Void> deleteContact(UUID userId, UUID contactId) {
        return contactRepository.deleteByIdAndUserId(contactId, userId)
                .filter(deletedCount -> deletedCount > 0)
                .switchIfEmpty(Mono.error(new RuntimeException("Contact not found")))
                .then();
    }

    private ContactResponseDto convertToResponseDto(Contact contact) {
        return ContactResponseDto.builder()
                .id(contact.getId())
                .name(contact.getName())
                .address(contact.getAddress())
                .notes(contact.getNotes())
                .createdAt(contact.getCreatedAt())
                .updatedAt(contact.getUpdatedAt())
                .build();
    }
}