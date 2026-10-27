package com.utnGymGroup.gym_system.common.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL) // esto oculta los campos nulos en el JSON
public class ErrorResponse {
    private LocalDateTime timestamp;
    private String message;
    private String description;
    private Map<String, String> fieldErrors; // para guardar varios errores por campo (error en validacion de email y username por ejemplo)
}
