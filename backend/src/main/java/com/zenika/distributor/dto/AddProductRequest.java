package com.zenika.distributor.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AddProductRequest {
  @NotBlank(message = "Product name is required")
  private String name;

  @NotNull(message = "Product price is required")
  @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
  private BigDecimal price;
}
