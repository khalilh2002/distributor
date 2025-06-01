package com.zenika.distributor.dto;

import com.zenika.distributor.model.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DispenseResponse {
  private List<Product> dispensedProducts;
  private List<BigDecimal> changeCoins;
  private String message;
}
