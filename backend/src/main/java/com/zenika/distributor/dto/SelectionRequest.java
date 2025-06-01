package com.zenika.distributor.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SelectionRequest {
  @NotNull(message = "Product ID cannot be null")
  private Long productId;
}
