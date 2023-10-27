package guru.springframework.orderservice.bootstrap;

import guru.springframework.orderservice.domain.OrderHeader;
import guru.springframework.orderservice.repositories.OrderHeaderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class GetCategoryByOrderService {

    @Autowired
    OrderHeaderRepository orderHeaderRepository;


    @Transactional
    public void getCategoryByOrderService() {
        System.out.println("[*] Called at bootstrap...");
        // find will run inside an implicit transaction
        OrderHeader orderHeader = this.orderHeaderRepository.findById(44L).orElseGet(OrderHeader::new);
        orderHeader.getOrderLines().forEach(orderLine -> {
            System.out.println("Description of every o.line : " + orderLine.getProduct().getDescription());
            orderLine.getProduct().getCategories().forEach(category -> {
                System.out.println("Category : " + category.getDescription());
            });
        });
    }
}