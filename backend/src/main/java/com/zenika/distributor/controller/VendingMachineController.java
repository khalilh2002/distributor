package com.zenika.distributor.controller;

import com.zenika.distributor.dto.*;
import com.zenika.distributor.model.Product;
import com.zenika.distributor.service.VendingMachineService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/distributor") // Changed from /api/vending as per your provided controller
public class VendingMachineController {

  private final VendingMachineService vendingMachineService;

  @Autowired
  public VendingMachineController(VendingMachineService vendingMachineService) {
    this.vendingMachineService = vendingMachineService;
  }

  @PostMapping("/coin")
  public ResponseEntity<Map<String, BigDecimal>> insertCoin(@Valid @RequestBody CoinInsertRequest request) {
    BigDecimal newBalance = vendingMachineService.insertCoin(request.getValue());
    return ResponseEntity.ok(Map.of("currentBalance", newBalance));
  }

  @GetMapping("/products")
  public ResponseEntity<List<ProductDTO>> listProducts() {
    return ResponseEntity.ok(vendingMachineService.listAvailableProducts());
  }

  @PostMapping("/select")
  public ResponseEntity<Map<String, Object>> selectProduct(@Valid @RequestBody SelectionRequest request) {
    Product newlySelectedProduct = vendingMachineService.selectProduct(request.getProductId());

    List<Product> currentSelection = vendingMachineService.getSelectedProducts();
    BigDecimal currentSelectedItemsCost = currentSelection.stream()
      .map(Product::getPrice)
      .reduce(BigDecimal.ZERO, BigDecimal::add);
    long countOfThisProductSelected = currentSelection.stream()
      .filter(p -> p.getId().equals(newlySelectedProduct.getId()))
      .count();

    return ResponseEntity.ok(Map.of(
      "message", "Product '" + newlySelectedProduct.getName() + "' added to selection.",
      "action", "selected",
      "product", Map.of("id", newlySelectedProduct.getId(), "name", newlySelectedProduct.getName(), "price", newlySelectedProduct.getPrice()),
      "quantityOfThisProductInSelection", countOfThisProductSelected,
      "currentSelectedItemsCost", currentSelectedItemsCost,
      "currentBalance", vendingMachineService.getCurrentBalance()
    ));
  }

  @PostMapping("/deselect")
  public ResponseEntity<Map<String, Object>> deselectProduct(@Valid @RequestBody SelectionRequest request) {
    Product productThatWasDeselected = vendingMachineService.deselectProduct(request.getProductId());

    if (productThatWasDeselected == null) {
      // It's also good to check if the product ID is valid in the catalog at all,
      // but deselectProduct in service will return null if not in selection.
      // For a more specific error, the controller could call productRepository.existsById first.
      return ResponseEntity.badRequest().body(Map.of(
        "message", "Product with ID " + request.getProductId() + " not found in current selection or does not exist."
      ));
    }

    List<Product> currentSelection = vendingMachineService.getSelectedProducts();
    BigDecimal currentSelectedItemsCost = currentSelection.stream()
      .map(Product::getPrice)
      .reduce(BigDecimal.ZERO, BigDecimal::add);
    long countOfThisProductRemaining = currentSelection.stream()
      .filter(p -> p.getId().equals(productThatWasDeselected.getId()))
      .count();

    return ResponseEntity.ok(Map.of(
      "message", "One instance of product '" + productThatWasDeselected.getName() + "' removed from selection.",
      "action", "deselected",
      "product", Map.of("id", productThatWasDeselected.getId(), "name", productThatWasDeselected.getName(), "price", productThatWasDeselected.getPrice()),
      "quantityOfThisProductRemainingInSelection", countOfThisProductRemaining,
      "currentSelectedItemsCost", currentSelectedItemsCost,
      "currentBalance", vendingMachineService.getCurrentBalance()
    ));
  }

  @PostMapping("/dispense")
  public ResponseEntity<DispenseResponse> dispense() {
    return ResponseEntity.ok(vendingMachineService.dispenseProducts());
  }

  @PostMapping("/cancel")
  public ResponseEntity<RefundResponse> cancelTransaction() {
    return ResponseEntity.ok(vendingMachineService.cancelTransaction());
  }

  @PostMapping("/admin/product")
  public ResponseEntity<Product> addProduct(@Valid @RequestBody AddProductRequest request) {
    Product newProduct = vendingMachineService.addProduct(request.getName(), request.getPrice());
    return ResponseEntity.status(201).body(newProduct);
  }
  @GetMapping("/state")
  public ResponseEntity<Map<String, Object>> getCurrentState() {
    List<Product> currentSelectionRaw = vendingMachineService.getSelectedProducts();

    Map<Long, List<Product>> groupedById = currentSelectionRaw.stream()
      .collect(Collectors.groupingBy(Product::getId));

    List<Map<String, Object>> selectedItemsWithQuantity = groupedById.entrySet().stream()
      .map(entry -> {
        Product p = entry.getValue().get(0); // Get representative product for name/price
        // Using HashMap for inner map for clarity, Map.of is also fine here
        Map<String, Object> productMap = new HashMap<>();
        productMap.put("id", p.getId());
        productMap.put("name", p.getName());
        productMap.put("price", p.getPrice());
        productMap.put("quantity", (long) entry.getValue().size());
        return productMap;
      })
      .collect(Collectors.toList());

    // Use a HashMap for the outer map to avoid Map.of type inference issues
    Map<String, Object> responseBody = new HashMap<>();
    responseBody.put("currentBalance", vendingMachineService.getCurrentBalance());
    responseBody.put("selectedProducts", selectedItemsWithQuantity);
    responseBody.put("totalSelectedCost", currentSelectionRaw.stream().map(Product::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add));

    return ResponseEntity.ok(responseBody);
  }
}
