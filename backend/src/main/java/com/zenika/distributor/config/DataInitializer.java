package com.zenika.distributor.config;


import com.zenika.distributor.model.Product;
import com.zenika.distributor.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

  private final ProductRepository productRepository;

  public DataInitializer(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  public void run(String... args) throws Exception {

  }
}
