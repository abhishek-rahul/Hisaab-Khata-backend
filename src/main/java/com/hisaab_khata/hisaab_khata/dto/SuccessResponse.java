package com.hisaab_khata.hisaab_khata.dto;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuccessResponse<T> {
    private boolean success;   // true for success, false for errors
    private int status;        // HTTP status code
    private String message;    // Success message
    private T data;            // Response payload
}

