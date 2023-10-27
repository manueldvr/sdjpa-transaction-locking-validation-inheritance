package guru.springframework.orderservice.bootstrap;

import guru.springframework.orderservice.domain.Customer;
import guru.springframework.orderservice.domain.Product;
import guru.springframework.orderservice.domain.ProductStatus;
import guru.springframework.orderservice.repositories.CustomerRepository;
import guru.springframework.orderservice.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
public class Bootstrap implements CommandLineRunner {


    @Autowired
    GetCategoryByOrderService getCategoryByOrderService;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    ProductService productService;


    /**
     * @see at readme: Fix Pessimistic Locking Error
     */
    private void updateProduct() {
        Product product = new Product();
        product.setDescription("My Product");
        product.setProductStatus(ProductStatus.NEW);
        Product savedProduct = productService.saveProduct(product);
        Product updatedProduct = productService.updateQOH(savedProduct.getId(), 25);
        System.out.println("Product qOH : " + updatedProduct.getQuantityOnHand());
    }

    @Override
    public void run(String... args) throws Exception {
        this.updateProduct();

        getCategoryByOrderService.getCategoryByOrderService();

        Customer customer1 = new Customer();
        customer1.setCustomerName("Testing Version 1 first");
        Customer savedCustomer2 = customerRepository.save(customer1);
        System.out.println("first Version is: " + savedCustomer2.getVersion());

        savedCustomer2.setCustomerName("Testing Version 2");
        Customer savedCustomer3 =customerRepository.save(savedCustomer2);
        System.out.println("2 - Version is: " + savedCustomer3.getVersion());

        savedCustomer3.setCustomerName("Testing Version 3");
        Customer savedCustomer4 = customerRepository.save(savedCustomer3);
        System.out.println("3 - Version is: " + savedCustomer4.getVersion());

        customerRepository.delete(savedCustomer4);
    }
}
