package com.zenika.distributor.service;

import com.zenika.distributor.enums.Coin;
import com.zenika.distributor.exception.InsufficientFundsException;
import com.zenika.distributor.exception.InvalidCoinException;
import com.zenika.distributor.exception.NoItemSelectedException;
import com.zenika.distributor.exception.ProductNotFoundException;
import com.zenika.distributor.model.Product;
import com.zenika.distributor.repository.ProductRepository;
import com.zenika.distributor.dto.DispenseResponse;
import com.zenika.distributor.dto.ProductDTO;
import com.zenika.distributor.dto.RefundResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings; // <<<<<<<< IMPORT THIS
import org.mockito.quality.Strictness;          // <<<<<<<< IMPORT THIS

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // <<<<<<<< ADD THIS ANNOTATION
class VendingMachineServiceTest {

  @Mock
  private ProductRepository productRepository;

  @InjectMocks
  private VendingMachineService vendingMachineService;

  private Product soda;
  private Product chips;
  private Product water;

  @BeforeEach
  void setUp() {
    vendingMachineService = new VendingMachineService(productRepository);

    soda = new Product(1L, "Soda", new BigDecimal("3.50"));
    chips = new Product(2L, "Chips", new BigDecimal("4.00"));
    water = new Product(3L, "Water", new BigDecimal("1.50"));

    // With class-level lenient strictness, you don't strictly need lenient() here anymore,
    // but it doesn't hurt to keep them if you prefer being explicit.
    // For this solution, the class-level annotation is the main fix.
    when(productRepository.findAll()).thenReturn(Arrays.asList(soda, chips, water));
    when(productRepository.findById(soda.getId())).thenReturn(Optional.of(soda));
    when(productRepository.findById(chips.getId())).thenReturn(Optional.of(chips));
    when(productRepository.findById(water.getId())).thenReturn(Optional.of(water));
    when(productRepository.findById(99L)).thenReturn(Optional.empty());
  }

  // ... ALL YOUR TEST METHODS REMAIN THE SAME ...
  // (I've snipped them for brevity, but they should be here from your previous code)

  @Test
  void insertCoin_validCoin_shouldIncreaseBalance() {
    BigDecimal balance = vendingMachineService.insertCoin(Coin.FIVE_MAD.getValue());
    assertEquals(0, new BigDecimal("5.00").compareTo(balance));
    assertEquals(0, new BigDecimal("5.00").compareTo(vendingMachineService.getCurrentBalance()));

    balance = vendingMachineService.insertCoin(Coin.ONE_MAD.getValue());
    assertEquals(0, new BigDecimal("6.00").compareTo(balance));
    assertEquals(0, new BigDecimal("6.00").compareTo(vendingMachineService.getCurrentBalance()));
  }

  @Test
  void insertCoin_invalidCoin_shouldThrowException() {
    Exception exception = assertThrows(InvalidCoinException.class, () -> {
      vendingMachineService.insertCoin(new BigDecimal("0.75"));
    });
    assertTrue(exception.getMessage().contains("Invalid coin value: 0.75"));
  }

  @Test
  void listAvailableProducts_noBalance_allNotPurchasableExceptFree() { // Renamed for clarity
    List<ProductDTO> products = vendingMachineService.listAvailableProducts();
    assertFalse(products.get(0).isPurchasable()); // Soda 3.50
    assertFalse(products.get(1).isPurchasable()); // Chips 4.00
    assertFalse(products.get(2).isPurchasable()); // Water 1.50
  }

  @Test
  void listAvailableProducts_withSufficientBalanceForSome() {
    vendingMachineService.insertCoin(Coin.TWO_MAD.getValue()); // Balance 2.00
    List<ProductDTO> products = vendingMachineService.listAvailableProducts();

    assertFalse(products.stream().filter(p -> p.getId().equals(soda.getId())).findFirst().get().isPurchasable()); // Soda 3.50
    assertTrue(products.stream().filter(p -> p.getId().equals(water.getId())).findFirst().get().isPurchasable()); // Water 1.50
  }

  @Test
  void listAvailableProducts_balanceChangesPurchasabilityAfterSelection() {
    vendingMachineService.insertCoin(Coin.FIVE_MAD.getValue()); // Balance 5.00
    vendingMachineService.selectProduct(water.getId()); // Select Water (1.50), remaining spendable 3.50

    List<ProductDTO> products = vendingMachineService.listAvailableProducts();
    assertTrue(products.stream().filter(p -> p.getId().equals(soda.getId())).findFirst().get().isPurchasable());
    assertFalse(products.stream().filter(p -> p.getId().equals(chips.getId())).findFirst().get().isPurchasable());
  }


  @Test
  void selectProduct_sufficientFunds_shouldAddProductToSelection() {
    vendingMachineService.insertCoin(Coin.FIVE_MAD.getValue());
    Product selected = vendingMachineService.selectProduct(soda.getId());

    assertEquals(soda.getId(), selected.getId()); // Compare by ID due to equals/hashCode
    // Check if a product with the same ID is in the list
    assertTrue(vendingMachineService.getSelectedProducts().stream().anyMatch(p -> p.getId().equals(soda.getId())));
    assertEquals(1, vendingMachineService.getSelectedProducts().size());
  }

  @Test
  void selectProduct_multipleTimes_shouldAddMultipleInstances() {
    vendingMachineService.insertCoin(Coin.TEN_MAD.getValue());
    vendingMachineService.selectProduct(soda.getId());
    vendingMachineService.selectProduct(soda.getId());

    assertEquals(2, vendingMachineService.getSelectedProducts().size());
    assertEquals(2, vendingMachineService.getSelectedProducts().stream().filter(p -> p.getId().equals(soda.getId())).count());
  }


  @Test
  void selectProduct_insufficientFunds_shouldThrowException() {
    vendingMachineService.insertCoin(Coin.ONE_MAD.getValue()); // Balance 1.00

    Exception exception = assertThrows(InsufficientFundsException.class, () -> {
      vendingMachineService.selectProduct(soda.getId()); // Soda costs 3.50
    });
    assertTrue(exception.getMessage().contains("Insufficient funds to add 'Soda'"));
  }

  @Test
  void selectProduct_nonExistentProduct_shouldThrowException() {
    assertThrows(ProductNotFoundException.class, () -> {
      vendingMachineService.selectProduct(99L);
    });
  }

  @Test
  void deselectProduct_productInSelection_shouldRemoveOneInstance() {
    vendingMachineService.insertCoin(Coin.TEN_MAD.getValue());
    vendingMachineService.insertCoin(Coin.ONE_MAD.getValue()); // Total 11.00

    vendingMachineService.selectProduct(soda.getId());  // Cost 3.50
    vendingMachineService.selectProduct(soda.getId());  // Cost 3.50 (total 7.00)
    vendingMachineService.selectProduct(chips.getId()); // Cost 4.00 (total 11.00)

    assertEquals(3, vendingMachineService.getSelectedProducts().size());

    Product deselected = vendingMachineService.deselectProduct(soda.getId());
    assertNotNull(deselected);
    assertEquals(soda.getId(), deselected.getId());
    assertEquals(2, vendingMachineService.getSelectedProducts().size());
    assertEquals(1, vendingMachineService.getSelectedProducts().stream().filter(p -> p.getId().equals(soda.getId())).count());
    assertTrue(vendingMachineService.getSelectedProducts().stream().anyMatch(p -> p.getId().equals(chips.getId())));
  }

  @Test
  void deselectProduct_productNotInSelection_shouldReturnNull() {
    vendingMachineService.insertCoin(Coin.FIVE_MAD.getValue());
    vendingMachineService.selectProduct(chips.getId());

    Product deselected = vendingMachineService.deselectProduct(soda.getId());
    assertNull(deselected);
    assertEquals(1, vendingMachineService.getSelectedProducts().size());
  }

  @Test
  void dispenseProducts_sufficientFunds_shouldReturnProductsAndChangeAndResetState() {
    vendingMachineService.insertCoin(Coin.FIVE_MAD.getValue());
    vendingMachineService.insertCoin(Coin.ONE_MAD.getValue());
    vendingMachineService.selectProduct(soda.getId());
    vendingMachineService.selectProduct(water.getId());

    DispenseResponse response = vendingMachineService.dispenseProducts();

    assertEquals(2, response.getDispensedProducts().size());
    assertTrue(response.getDispensedProducts().stream().anyMatch(p -> p.getId().equals(soda.getId())));
    assertTrue(response.getDispensedProducts().stream().anyMatch(p -> p.getId().equals(water.getId())));
    assertEquals(1, response.getChangeCoins().size());
    assertEquals(0, Coin.ONE_MAD.getValue().compareTo(response.getChangeCoins().get(0)));
    assertEquals(0, BigDecimal.ZERO.compareTo(vendingMachineService.getCurrentBalance()));
    assertTrue(vendingMachineService.getSelectedProducts().isEmpty());
  }

  @Test
  void dispenseProducts_noItemsSelected_shouldThrowException() {
    vendingMachineService.insertCoin(Coin.FIVE_MAD.getValue());
    assertThrows(NoItemSelectedException.class, () -> {
      vendingMachineService.dispenseProducts();
    });
  }

  @Test
  void dispenseProducts_insufficientFundsForSelection_shouldBeCaughtBySelectProduct() {
    vendingMachineService.insertCoin(Coin.ONE_MAD.getValue());
    assertThrows(InsufficientFundsException.class, () -> {
      vendingMachineService.selectProduct(soda.getId());
    });
  }

  @Test
  void cancelTransaction_shouldRefundInsertedCoinsAndResetState() {
    vendingMachineService.insertCoin(Coin.FIVE_MAD.getValue());
    vendingMachineService.insertCoin(Coin.TWO_MAD.getValue());
    vendingMachineService.selectProduct(water.getId());

    RefundResponse response = vendingMachineService.cancelTransaction();

    assertEquals(2, response.getRefundedCoins().size());
    assertTrue(response.getRefundedCoins().contains(Coin.FIVE_MAD.getValue()));
    assertTrue(response.getRefundedCoins().contains(Coin.TWO_MAD.getValue()));
    assertEquals(0, BigDecimal.ZERO.compareTo(vendingMachineService.getCurrentBalance()));
    assertTrue(vendingMachineService.getSelectedProducts().isEmpty());
  }

  @Test
  void cancelTransaction_noCoinsInserted_shouldReturnEmptyRefund() {
    RefundResponse response = vendingMachineService.cancelTransaction();
    assertTrue(response.getRefundedCoins().isEmpty());
    assertTrue(response.getMessage().contains("No coins inserted to refund"));
  }
}
