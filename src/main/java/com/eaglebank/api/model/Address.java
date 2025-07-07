package com.eaglebank.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    
    @NotBlank
    @Column(name = "address_line1", nullable = false)
    private String line1;
    
    @Column(name = "address_line2")
    private String line2;
    
    @Column(name = "address_line3")
    private String line3;
    
    @NotBlank
    @Column(name = "address_town", nullable = false)
    private String town;
    
    @NotBlank
    @Column(name = "address_county", nullable = false)
    private String county;
    
    @NotBlank
    @Column(name = "address_postcode", nullable = false)
    private String postcode;
}

