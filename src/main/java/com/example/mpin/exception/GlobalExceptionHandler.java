//package com.example.mpin.exception;
//
////import com.example.mpin.dto.ApiResponse;
//
//import com.example.mpin.config.ApiConstants;
//import com.example.mpin.constants.ValidationMessages;
//import com.example.mpin.dto.ApiResponse;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.dao.DataIntegrityViolationException;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//import java.nio.file.AccessDeniedException;
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@ControllerAdvice
//public class GlobalExceptionHandler {
//
////    // Handle validation errors
////    @ExceptionHandler(MethodArgumentNotValidException.class)
////    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
////        String errors = ex.getBindingResult()
////                .getFieldErrors()
////                .stream()
////                .map(err -> err.getField() + ": " + err.getDefaultMessage())
////                .collect(Collectors.joining(", "));
////
////        ErrorResponse error = new ErrorResponse(
////                LocalDateTime.now(),
////                HttpStatus.BAD_REQUEST.value(),
////                "VALIDATION_FAILED",
////                errors
////        );
////        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
////    }
////
////    // Handle IllegalArgumentException separately
////    @ExceptionHandler(IllegalArgumentException.class)
////    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
////        ErrorResponse error = new ErrorResponse(
////                LocalDateTime.now(),
////                HttpStatus.BAD_REQUEST.value(),
////                "INVALID_ARGUMENT",
////                ex.getMessage()
////        );
////        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
////    }
//
//    // Optional: catch all other exceptions
//    @ExceptionHandler(IllegalArgumentException.class)
//    public ResponseEntity<ApiResponse<Object>> handleBadRequest(IllegalArgumentException ex) {
//        ApiResponse<Object> response = new ApiResponse<>("FAILED", "400", ex.getMessage());
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//    }
//
//    @ExceptionHandler(AccessDeniedException.class)
//    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException ex) {
//        ApiResponse<Object> response = new ApiResponse<>("FAILED", "403", ex.getMessage());
//        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiResponse<Object>> handleInternalError(Exception ex) {
//        ApiResponse<Object> response = new ApiResponse<>("FAILED", "500", ValidationMessages.INTERNAL_ERROR);
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//    }
////    @ExceptionHandler(ResourceNotFoundException.class)
////    public ResponseEntity<ApiResponse<?>> handleNotFound(ResourceNotFoundException ex) {
////        return ResponseEntity.status(HttpStatus.NOT_FOUND)
////                .body(new ApiResponse<>(ApiConstants.FAILED, ex.getMessage(), null, "404"));
////    }
////
////    @ExceptionHandler(BadRequestException.class)
////    public ResponseEntity<ApiResponse<?>> handleBadRequest(BadRequestException ex) {
////        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
////                .body(new ApiResponse<>(ApiConstants.FAILED, ex.getMessage(), null, "400"));
////    }
////
////    @ExceptionHandler(MethodArgumentNotValidException.class)
////    public ResponseEntity<ApiResponse<?>> handleValidation(MethodArgumentNotValidException ex) {
////        String message = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
////        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
////                .body(new ApiResponse<>(ApiConstants.FAILED, message, null, "400"));
////    }
////
////    @ExceptionHandler(Exception.class)
////    public ResponseEntity<ApiResponse<?>> handleInternal(Exception ex) {
////        ex.printStackTrace();
////        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
////                .body(new ApiResponse<>(ApiConstants.FAILED, ApiConstants.INTERNAL_ERROR, null, "500"));
////    }
//
//
//    // Error response payload
//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class ErrorResponse {
//        private LocalDateTime timestamp;
//        private int status;
//        private String errorCode;
//        private String message;
//    }
//}
