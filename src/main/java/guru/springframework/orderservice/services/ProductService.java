package guru.springframework.orderservice.services;


import guru.springframework.orderservice.domain.Product;

public interface ProductService {


    Product saveProduct(Product product);

    Product updateQOH(Long id, Integer quantity);
}
