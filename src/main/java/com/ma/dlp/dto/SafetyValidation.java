package com.ma.dlp.dto;
import lombok.Data;

@Data
public class SafetyValidation {
    private Boolean isValid;
    private String[] warnings;
    private String[] errors;
    private Boolean requiresConfirmation;
    private String confirmationMessage;
}
