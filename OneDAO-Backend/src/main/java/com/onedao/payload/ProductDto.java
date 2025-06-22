package com.onedao.payload;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProductDto {
    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;
}

