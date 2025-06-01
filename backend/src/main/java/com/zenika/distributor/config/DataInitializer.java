package com.zenika.distributor.config;


import com.zenika.distributor.model.Product;
import com.zenika.distributor.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

  private final ProductRepository productRepository;

  public DataInitializer(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  public void run(String... args) throws Exception {
    List<Product> products = Arrays.asList(
      new Product(null, "Soda Classique", new BigDecimal("3.50")),
      new Product(null, "Jus d'Orange Frais", new BigDecimal("4.00")),
      new Product(null, "Eau Minérale Naturelle", new BigDecimal("1.50")),
      new Product(null, "Chips Croustillantes Sel & Vinaigre", new BigDecimal("2.75")),
      new Product(null, "Barre Chocolatée aux Noisettes", new BigDecimal("5.00")),
      new Product(null, "Bonbons Gélifiés Fruités", new BigDecimal("2.00")),
      new Product(null, "Sandwich Poulet Crudités", new BigDecimal("7.50")),
      new Product(null, "Café Express Chaud", new BigDecimal("3.00"))
    );

    productRepository.saveAll(products);
  }
}
