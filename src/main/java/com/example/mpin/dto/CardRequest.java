package com.example.mpin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class CardRequest {
//    @NotBlank
//    private String holderName;
//
//    @NotBlank
//    private String cardNumber;
//
//    @NotBlank
//    private String validThru; // MM/YY
//
//    @NotBlank
//    private String mobile;
//
//    @NotBlank
//    private String type; // CREDIT or DEBIT
//}

@Data
public class CardRequest {

    @NotBlank(message = "Holder name is required")
    private String holderName;

    @NotBlank(message = "Card number is required")
    @Size(min = 16, max = 16, message = "Card number must be 16 digits")
    @Pattern(regexp = "^[0-9]{16}$", message = "Card number must be numeric")
    private String cardNumber;

    @NotBlank(message = "Valid Thru is required")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", message = "Valid Thru must be in MM/YY format")
    private String validThru;

    @NotBlank(message = "Type is required (CREDIT/DEBIT)")
    private String type;
}