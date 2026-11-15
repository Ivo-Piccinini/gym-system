package com.utnGymGroup.gym_system.common.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private WebRequest webRequest;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    @DisplayName("handleDatabaseExceptions transforma violación de constraint users_dni_key en mensaje amigable")
    void shouldReturnFriendlyMessageForDniConstraintViolation() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "could not execute statement [ERROR: duplicate key value violates unique constraint \"users_dni_key\" Detail: Key (dni)=(10000001) already exists.]"
        );
        when(webRequest.getDescription(false)).thenReturn("uri=/api/users");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleDatabaseExceptions(ex, webRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Ya existe un usuario registrado con el DNI ingresado.", response.getBody().getMessage());
        assertFalse(response.getBody().getMessage().contains("could not execute statement"));
        assertFalse(response.getBody().getMessage().contains("users_dni_key"));
    }

    @Test
    @DisplayName("handleDatabaseExceptions transforma violación de constraint users_email_key en mensaje amigable")
    void shouldReturnFriendlyMessageForEmailConstraintViolation() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "could not execute statement [ERROR: duplicate key value violates unique constraint \"users_email_key\" Detail: Key (email)=(test@gym.com) already exists.]"
        );
        when(webRequest.getDescription(false)).thenReturn("uri=/api/users");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleDatabaseExceptions(ex, webRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Ya existe un usuario registrado con este email.", response.getBody().getMessage());
    }

    @Test
    @DisplayName("handleDatabaseExceptions transforma otra violación de unique constraint en mensaje amigable")
    void shouldReturnFriendlyMessageForGenericUniqueConstraint() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "duplicate key value violates unique constraint"
        );
        when(webRequest.getDescription(false)).thenReturn("uri=/api/something");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleDatabaseExceptions(ex, webRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Los datos ingresados ya se encuentran registrados en el sistema.", response.getBody().getMessage());
    }
}
