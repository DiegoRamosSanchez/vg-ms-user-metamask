package pe.edu.vallegrande.metamask.users.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.metamask.users.dto.UserLoginDto;
import pe.edu.vallegrande.metamask.users.dto.UserRegistrationDto;
import pe.edu.vallegrande.metamask.users.dto.UserResponseDto;
import pe.edu.vallegrande.metamask.users.dto.UserUpdateDto;
import pe.edu.vallegrande.metamask.users.service.UserService;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public Mono<ResponseEntity<UserResponseDto>> register(@Valid @RequestBody UserRegistrationDto registrationDto) {
        return userService.registerUser(registrationDto)
                .map(user -> ResponseEntity.status(HttpStatus.CREATED).body(user))
                .onErrorResume(RuntimeException.class, ex -> {
                    System.err.println("Registration error: " + ex.getMessage());
                    return Mono.just(ResponseEntity.badRequest().build());
                })
                .onErrorResume(Exception.class, ex -> {
                    System.err.println("Unexpected error: " + ex.getMessage());
                    ex.printStackTrace();
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<UserResponseDto>> login(@Valid @RequestBody UserLoginDto loginDto) {
        return userService.loginUser(loginDto)
                .map(user -> ResponseEntity.ok(user))
                .onErrorResume(throwable ->
                        Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build())
                );
    }

    @GetMapping("/user/{id}")
    public Mono<ResponseEntity<UserResponseDto>> getUser(@PathVariable UUID id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(user))
                .onErrorResume(throwable ->
                        Mono.just(ResponseEntity.notFound().build())
                );
    }

    @PutMapping("/user/{id}")
    public Mono<ResponseEntity<UserResponseDto>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateDto updateDto) {
        return userService.updateUser(id, updateDto)
                .map(user -> ResponseEntity.ok(user))
                .onErrorResume(throwable ->
                        Mono.just(ResponseEntity.badRequest().build())
                );
    }
}