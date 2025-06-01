package com.zenika.distributor.enums;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public enum Coin {
  HALF_MAD(new BigDecimal("0.50")),
  ONE_MAD(new BigDecimal("1.00")),
  TWO_MAD(new BigDecimal("2.00")),
  FIVE_MAD(new BigDecimal("5.00")),
  TEN_MAD(new BigDecimal("10.00"));

  private final BigDecimal value;

  Coin(BigDecimal value) {
    this.value = value;
  }

  public BigDecimal getValue() {
    return value;
  }

  public static boolean isValid(BigDecimal value) {
    return Arrays.stream(values()).anyMatch(coin -> coin.getValue().compareTo(value) == 0);
  }

  public static Optional<Coin> fromValue(BigDecimal value) {
    return Arrays.stream(values())
      .filter(coin -> coin.getValue().compareTo(value) == 0)
      .findFirst();
  }

  public static List<Coin> getSortedCoinsDesc() {
    return Arrays.stream(values())
      .sorted(Comparator.comparing(Coin::getValue).reversed())
      .collect(Collectors.toList());
  }
}
