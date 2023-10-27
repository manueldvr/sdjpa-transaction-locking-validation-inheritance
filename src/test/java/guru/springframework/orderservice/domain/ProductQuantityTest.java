package guru.springframework.orderservice.domain;


import guru.springframework.orderservice.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("local")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ProductQuantityTest {


    @Autowired
    ProductRepository productRepository;



    @Test
    void testProductQuantityAddAndUpdate() {
        Product product = new Product();
        product.setDescription("My Product");
        product.setProductStatus(ProductStatus.NEW);
        Product savedProduct = productRepository.saveAndFlush(product);

        savedProduct.setQuantityOnHand(33);
//        Product fetchedProduct = productRepository.getById(savedProduct.getId());
        Product savedProduct2 = productRepository.saveAndFlush(savedProduct);

        System.out.println("Quantity On Hand : " + savedProduct2.getQuantityOnHand());

      //  assertNotNull(fetchedProduct);
    //    assertNotNull(fetchedProduct.getDescription());

    }
}
