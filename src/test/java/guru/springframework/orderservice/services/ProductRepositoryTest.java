package guru.springframework.orderservice.services;

import guru.springframework.orderservice.domain.ProductStatus;
import guru.springframework.orderservice.domain.Product;
import guru.springframework.orderservice.repositories.ProductRepository;
import guru.springframework.orderservice.services.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
@ActiveProfiles("local")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ComponentScan(basePackageClasses = {ProductService.class})
class ProductRepositoryTest {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductService productService;


    @Test
    void testGetCategory() {
        Product product = productRepository.findByDescription("PRODUCT1").get();
        assertNotNull(product);
        assertNotNull(product.getCategories());
    }

    @Test
    void testSaveProduct() {
        Product product = new Product();
        product.setDescription("My Product");
        product.setProductStatus(ProductStatus.NEW);
        Product savedProduct = productRepository.save(product);
        Product fetchedProduct = productRepository.getById(savedProduct.getId());
        assertNotNull(fetchedProduct);
        assertNotNull(fetchedProduct.getDescription());
        assertNotNull(fetchedProduct.getCreatedDate());
        assertNotNull(fetchedProduct.getLastModifiedDate());
    }

    /**
     * @see at readme: Pessimistic Locking.
     **/
    @Test
    void testSaveAndUpdateProduct() {
        Product product = new Product();
        product.setDescription("My Product");
        product.setProductStatus(ProductStatus.NEW);
        Product savedProduct = productService.saveProduct(product);
        Product updatedProduct = productService.updateQOH(savedProduct.getId(), 25);
        assertNotNull(updatedProduct);
        assertNotNull(updatedProduct.getDescription());
    }
}














