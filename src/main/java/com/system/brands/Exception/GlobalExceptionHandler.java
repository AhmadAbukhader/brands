package com.system.brands.Exception;

import com.system.brands.Dto.ErrorResponseDto;
import com.system.brands.Dto.ValidationErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponseDto> handleResourceNotFoundException(
                        ResourceNotFoundException ex,
                        HttpServletRequest request) {

                ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                                .status(HttpStatus.NOT_FOUND.value())
                                .message("Resource Not Found")
                                .details(ex.getMessage())
                                .timestamp(LocalDateTime.now())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        @ExceptionHandler(DuplicateResourceException.class)
        public ResponseEntity<ErrorResponseDto> handleDuplicateResourceException(
                        DuplicateResourceException ex,
                        HttpServletRequest request) {

                ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                                .status(HttpStatus.CONFLICT.value())
                                .message("Duplicate Resource")
                                .details(ex.getMessage())
                                .timestamp(LocalDateTime.now())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }

        @ExceptionHandler(InvalidCredentialsException.class)
        public ResponseEntity<ErrorResponseDto> handleInvalidCredentialsException(
                        InvalidCredentialsException ex,
                        HttpServletRequest request) {

                ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .message("Authentication Failed")
                                .details(ex.getMessage())
                                .timestamp(LocalDateTime.now())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ErrorResponseDto> handleBadCredentialsException(
                        BadCredentialsException ex,
                        HttpServletRequest request) {

                ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .message("Authentication Failed")
                                .details("Invalid username or password")
                                .timestamp(LocalDateTime.now())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        @ExceptionHandler(UsernameNotFoundException.class)
        public ResponseEntity<ErrorResponseDto> handleUsernameNotFoundException(
                        UsernameNotFoundException ex,
                        HttpServletRequest request) {

                ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .message("Authentication Failed")
                                .details("User not found")
                                .timestamp(LocalDateTime.now())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<ErrorResponseDto> handleBadRequestException(
                        BadRequestException ex,
                        HttpServletRequest request) {

                ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message("Bad Request")
                                .details(ex.getMessage())
                                .timestamp(LocalDateTime.now())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ValidationErrorResponseDto> handleValidationException(
                        MethodArgumentNotValidException ex,
                        HttpServletRequest request) {

                Map<String, String> errors = new HashMap<>();
                ex.getBindingResult().getAllErrors().forEach((error) -> {
                        String fieldName = ((FieldError) error).getField();
                        String errorMessage = error.getDefaultMessage();
                        errors.put(fieldName, errorMessage);
                });

                ValidationErrorResponseDto errorResponse = ValidationErrorResponseDto.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message("Validation Failed")
                                .errors(errors)
                                .timestamp(LocalDateTime.now())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public ResponseEntity<ErrorResponseDto> handleMaxUploadSizeExceededException(
                        MaxUploadSizeExceededException ex,
                        HttpServletRequest request) {

                ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                                .status(HttpStatus.PAYLOAD_TOO_LARGE.value())
                                .message("File Too Large")
                                .details("The uploaded file exceeds the maximum allowed size of 50MB")
                                .timestamp(LocalDateTime.now())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorResponse);
        }

        @ExceptionHandler(MultipartException.class)
        public ResponseEntity<ErrorResponseDto> handleMultipartException(
                        MultipartException ex,
                        HttpServletRequest request) {

                ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message("File Upload Error")
                                .details("Error processing file upload: " + ex.getMessage())
                                .timestamp(LocalDateTime.now())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponseDto> handleGlobalException(
                        Exception ex,
                        HttpServletRequest request) {

                ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .message("Internal Server Error")
                                .details("An unexpected error occurred: " + ex.getMessage())
                                .timestamp(LocalDateTime.now())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
}
