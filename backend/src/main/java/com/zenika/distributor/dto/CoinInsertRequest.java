package com.zenika.distributor.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CoinInsertRequest {
  @NotNull(message = "Coin value cannot be null")
  private BigDecimal value;
}
