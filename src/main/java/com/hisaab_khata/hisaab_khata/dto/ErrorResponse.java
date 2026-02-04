package com.hisaab_khata.hisaab_khata.dto;



import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private LocalDateTime timestamp;    // auto-filled
    private int status;                 // HTTP status code
    private String error;               // SHORT error name e.g. BAD_REQUEST
    private String message;             // developer-friendly message
    private String path;                // URI path (auto-filled)
    private String code;                // app-specific error code e.g. PRODUCT_NOT_FOUND
}

