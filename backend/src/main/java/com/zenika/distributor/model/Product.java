package com.zenika.distributor.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter; // Use Getter/Setter instead of Data for more control
import lombok.NoArgsConstructor;
import lombok.Setter; // Use Getter/Setter instead of Data

import java.math.BigDecimal;
import java.util.Objects; // Import Objects

@Entity
@Getter // Using individual annotations for more control
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Product name cannot be blank")
  private String name;

  @NotNull(message = "Product price cannot be null")
  @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
  private BigDecimal price;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Product product = (Product) o;
    // Only compare by ID if both IDs are not null.
    return id != null && Objects.equals(id, product.id);
  }

  @Override
  public int hashCode() {
    // Use ID for hashCode if not null, otherwise use superclass's hashCode.
    return id != null ? Objects.hash(id) : super.hashCode();
  }

  @Override
  public String toString() {
    return "Product{" +
      "id=" + id +
      ", name='" + name + '\'' +
      ", price=" + price +
      '}';
  }
}
