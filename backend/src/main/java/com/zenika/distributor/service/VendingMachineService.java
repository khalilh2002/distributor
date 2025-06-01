package com.zenika.distributor.service;

import com.zenika.distributor.dto.DispenseResponse;
import com.zenika.distributor.dto.ProductDTO;
import com.zenika.distributor.dto.RefundResponse;
import com.zenika.distributor.enums.Coin; // Make sure your Coin enum is in this package
import com.zenika.distributor.exception.InsufficientFundsException;
import com.zenika.distributor.exception.InvalidCoinException;
import com.zenika.distributor.exception.NoItemSelectedException;
import com.zenika.distributor.exception.ProductNotFoundException;
import com.zenika.distributor.model.Product;
import com.zenika.distributor.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope; // Or default Singleton

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@ApplicationScope // This makes it a singleton for the application
public class VendingMachineService {

  private final ProductRepository productRepository;

  // Transactional state
  private BigDecimal currentBalance = BigDecimal.ZERO;
  private final List<Coin> insertedCoins = new ArrayList<>();
  private final List<Product> selectedProducts = new ArrayList<>(); // Can hold multiple instances

  @Autowired
  public VendingMachineService(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  public BigDecimal insertCoin(BigDecimal value) {
    // Assuming Coin.isValid and Coin.fromValue are static methods in your Coin enum
    if (!Coin.isValid(value)) {
      throw new InvalidCoinException("Invalid coin value: " + value +
        ". Accepted values: " + Coin.getSortedCoinsDesc().stream()
        .map(Coin::getValue).map(BigDecimal::toPlainString)
        .collect(Collectors.joining(", ")));
    }
    Coin coinEnum = Coin.fromValue(value)
      .orElseThrow(() -> new InvalidCoinException("Internal error: Coin enum mapping failed for value: " + value));

    insertedCoins.add(coinEnum);
    currentBalance = currentBalance.add(value);
    return currentBalance;
  }

  public List<ProductDTO> listAvailableProducts() {
    // Calculate the cost of items already selected
    BigDecimal totalSelectedCost = selectedProducts.stream()
      .map(Product::getPrice)
      .reduce(BigDecimal.ZERO, BigDecimal::add);
    // Available balance for *new* selections
    BigDecimal spendableForNewItems = currentBalance.subtract(totalSelectedCost);

    return productRepository.findAll().stream()
      .map(product -> new ProductDTO(
        product.getId(),
        product.getName(),
        product.getPrice(),
        // Can we afford one *more* of this product?
        spendableForNewItems.compareTo(product.getPrice()) >= 0
      ))
      .collect(Collectors.toList());
  }

  public Product selectProduct(Long productId) {
    Product product = productRepository.findById(productId)
      .orElseThrow(() -> new ProductNotFoundException("Product with ID " + productId + " not found."));

    // Calculate total cost if this product is added
    BigDecimal costOfAlreadySelected = selectedProducts.stream()
      .map(Product::getPrice)
      .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal costWithNewProduct = costOfAlreadySelected.add(product.getPrice());

    if (currentBalance.compareTo(costWithNewProduct) < 0) {
      throw new InsufficientFundsException(
        String.format("Insufficient funds to add '%s' (%.2f). Current selection cost: %.2f. Total needed: %.2f. Balance: %.2f",
          product.getName(), product.getPrice(), costOfAlreadySelected, costWithNewProduct, currentBalance)
      );
    }
    selectedProducts.add(product); // Add the product to the list
    return product;
  }

  /**
   * Deselects (removes) one instance of a product from the current selection.
   * @param productId The ID of the product to remove.
   * @return The Product object that was removed, or null if no such product was found in the selection.
   */
  public Product deselectProduct(Long productId) {
    // We don't need to check repository here if we only care about removing from selection.
    // However, returning the actual product details (name, price) is often useful for the response.
    // For simplicity, we'll iterate and remove.
    for (int i = 0; i < selectedProducts.size(); i++) {
      Product selected = selectedProducts.get(i);
      if (selected.getId().equals(productId)) {
        selectedProducts.remove(i);
        return selected; // Return the actual product instance that was removed
      }
    }
    return null; // Product with this ID was not in the selected list
  }


  public DispenseResponse dispenseProducts() {
    if (selectedProducts.isEmpty()) {
      throw new NoItemSelectedException("No products selected for dispensing.");
    }

    BigDecimal totalCost = selectedProducts.stream()
      .map(Product::getPrice)
      .reduce(BigDecimal.ZERO, BigDecimal::add);

    if (currentBalance.compareTo(totalCost) < 0) {
      throw new InsufficientFundsException("Insufficient funds to dispense. " +
        "Total cost: " + totalCost + ", Current balance: " + currentBalance);
    }

    BigDecimal changeAmount = currentBalance.subtract(totalCost);
    List<BigDecimal> changeCoins = calculateOptimizedChange(changeAmount);

    List<Product> dispensed = new ArrayList<>(selectedProducts);
    resetTransactionState();

    return new DispenseResponse(dispensed, changeCoins, "Products dispensed. Thank you!");
  }

  public RefundResponse cancelTransaction() {
    List<BigDecimal> coinsToRefund = insertedCoins.stream()
      .map(Coin::getValue)
      .collect(Collectors.toList());
    resetTransactionState();
    if (coinsToRefund.isEmpty()){
      return new RefundResponse(coinsToRefund, "No coins inserted to refund. Transaction cancelled.");
    }
    return new RefundResponse(coinsToRefund, "Transaction cancelled. Coins refunded.");
  }

  private List<BigDecimal> calculateOptimizedChange(BigDecimal amount) {
    List<BigDecimal> changeGiven = new ArrayList<>();
    BigDecimal remainingAmount = amount;
    for (Coin coin : Coin.getSortedCoinsDesc()) {
      while (remainingAmount.compareTo(coin.getValue()) >= 0) {
        changeGiven.add(coin.getValue());
        remainingAmount = remainingAmount.subtract(coin.getValue());
      }
    }
    if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
      System.err.println("Warning: Could not make exact change. Remaining: " + remainingAmount);
    }
    return changeGiven;
  }

  private void resetTransactionState() {
    currentBalance = BigDecimal.ZERO;
    insertedCoins.clear();
    selectedProducts.clear();
  }

  public Product addProduct(String name, BigDecimal price) {
    Product product = new Product(null, name, price);
    return productRepository.save(product);
  }

  public BigDecimal getCurrentBalance() {
    return currentBalance;
  }

  public List<Product> getSelectedProducts() {
    return new ArrayList<>(selectedProducts); // Return a copy
  }
}
