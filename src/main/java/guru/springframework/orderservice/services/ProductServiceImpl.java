package guru.springframework.orderservice.services;

import guru.springframework.orderservice.domain.Product;
import guru.springframework.orderservice.repositories.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class ProductServiceImpl implements ProductService{

    @Autowired
    private ProductRepository productRepository;



    @Override
    public Product saveProduct(Product product) {
        return this.productRepository.saveAndFlush(product);
    }

    @Transactional
    @Override
    public Product updateQOH(Long id, Integer quantity) {
        Product product =  this.productRepository.findById(id).orElseThrow();
        product.setQuantityOnHand(quantity);
        return productRepository.saveAndFlush(product);
    }
}
