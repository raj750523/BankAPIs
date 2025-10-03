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

    private String holderName;

    private String cardId;

    private String validThru;
    private String type;

}

