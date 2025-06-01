package com.zenika.distributor.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponse {
  private List<BigDecimal> refundedCoins;
  private String message;
}
