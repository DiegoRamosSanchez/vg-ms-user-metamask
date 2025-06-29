package pe.edu.vallegrande.metamask.users.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactUpdateDto {
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Size(max = 42, message = "Address must not exceed 42 characters")
    private String address;

    private String notes;
}