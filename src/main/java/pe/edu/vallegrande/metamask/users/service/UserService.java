package pe.edu.vallegrande.metamask.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.metamask.users.dto.UserLoginDto;
import pe.edu.vallegrande.metamask.users.dto.UserRegistrationDto;
import pe.edu.vallegrande.metamask.users.dto.UserResponseDto;
import pe.edu.vallegrande.metamask.users.dto.UserUpdateDto;
import pe.edu.vallegrande.metamask.users.entity.User;
import pe.edu.vallegrande.metamask.users.repository.UserRepository;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Mono<UserResponseDto> registerUser(UserRegistrationDto registrationDto) {
        return userRepository.existsByUsername(registrationDto.getUsername())
                .defaultIfEmpty(false)
                .flatMap(usernameExists -> {
                    if (usernameExists) {
                        return Mono.error(new RuntimeException("Username already exists"));
                    }
                    return userRepository.existsByEmail(registrationDto.getEmail())
                            .defaultIfEmpty(false);
                })
                .flatMap(emailExists -> {
                    if (emailExists) {
                        return Mono.error(new RuntimeException("Email already exists"));
                    }

                    User user = User.builder()
                            .username(registrationDto.getUsername())
                            .email(registrationDto.getEmail())
                            .password(passwordEncoder.encode(registrationDto.getPassword()))
                            .metamaskAddress(registrationDto.getMetamaskAddress())
                            .ethBalance(BigDecimal.ZERO)
                            .isActive(true)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();

                    return userRepository.save(user);
                })
                .map(this::convertToResponseDto);
    }

    public Mono<UserResponseDto> loginUser(UserLoginDto loginDto) {
        return userRepository.findByUsername(loginDto.getUsername())
                .filter(user -> passwordEncoder.matches(loginDto.getPassword(), user.getPassword()))
                .filter(User::getIsActive)
                .map(this::convertToResponseDto)
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid username or password")));
    }

    public Mono<UserResponseDto> getUserById(UUID id) {
        return userRepository.findById(id)
                .map(this::convertToResponseDto)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")));
    }

    public Mono<UserResponseDto> updateUser(UUID id, UserUpdateDto updateDto) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")))
                .flatMap(user -> {
                    // Validar username único si se está actualizando
                    Mono<Void> validations = Mono.empty();

                    if (updateDto.getUsername() != null && !updateDto.getUsername().equals(user.getUsername())) {
                        validations = userRepository.existsByUsername(updateDto.getUsername())
                                .defaultIfEmpty(false)
                                .flatMap(exists -> {
                                    if (exists) {
                                        return Mono.error(new RuntimeException("Username already exists"));
                                    }
                                    return Mono.empty();
                                });
                    }

                    // Validar email único si se está actualizando
                    if (updateDto.getEmail() != null && !updateDto.getEmail().equals(user.getEmail())) {
                        Mono<Void> emailValidation = userRepository.existsByEmail(updateDto.getEmail())
                                .defaultIfEmpty(false)
                                .flatMap(exists -> {
                                    if (exists) {
                                        return Mono.error(new RuntimeException("Email already exists"));
                                    }
                                    return Mono.empty();
                                });
                        validations = validations.then(emailValidation);
                    }

                    return validations.thenReturn(user);
                })
                .flatMap(user -> {
                    // Actualizar campos si están presentes
                    if (updateDto.getUsername() != null) {
                        user.setUsername(updateDto.getUsername());
                    }
                    if (updateDto.getEmail() != null) {
                        user.setEmail(updateDto.getEmail());
                    }
                    if (updateDto.getPassword() != null) {
                        user.setPassword(passwordEncoder.encode(updateDto.getPassword()));
                    }
                    if (updateDto.getMetamaskAddress() != null) {
                        user.setMetamaskAddress(updateDto.getMetamaskAddress());
                    }
                    if (updateDto.getEthBalance() != null) {
                        user.setEthBalance(updateDto.getEthBalance());
                    }
                    if (updateDto.getIsActive() != null) {
                        user.setIsActive(updateDto.getIsActive());
                    }
                    user.setUpdatedAt(LocalDateTime.now());

                    return userRepository.save(user);
                })
                .map(this::convertToResponseDto);
    }

    private UserResponseDto convertToResponseDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .metamaskAddress(user.getMetamaskAddress())
                .ethBalance(user.getEthBalance())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}