package by.javaguru.orders.service;

import by.javaguru.core.dto.Order;
import by.javaguru.core.dto.event.OrderApproveEvent;
import by.javaguru.core.dto.event.OrderCreatedEvent;
import by.javaguru.core.types.OrderStatus;
import by.javaguru.orders.dao.jpa.entity.OrderEntity;
import by.javaguru.orders.dao.jpa.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String ordersEventsTopicName;

    public OrderServiceImpl(OrderRepository orderRepository,
                            KafkaTemplate<String, Object> kafkaTemplate,
                            @Value("${orders.events.topic.name}") String ordersEventsTopicName) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.ordersEventsTopicName = ordersEventsTopicName;
    }

    @Override
    public Order placeOrder(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setCustomerId(order.getCustomerId());
        entity.setProductId(order.getProductId());
        entity.setProductQuantity(order.getProductQuantity());
        entity.setStatus(OrderStatus.CREATED);
        orderRepository.save(entity);

        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(
                entity.getId(),
                entity.getCustomerId(),
                entity.getProductId(),
                entity.getProductQuantity()
        );

        kafkaTemplate.send(ordersEventsTopicName, orderCreatedEvent);

        return new Order(
                entity.getId(),
                entity.getCustomerId(),
                entity.getProductId(),
                entity.getProductQuantity(),
                entity.getStatus());
    }

    @Override
    public void approveOrder(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId).orElse(null);
        Assert.notNull(order, "Order with orderId: " + orderId + " not found in database");
        order.setStatus(OrderStatus.APPROVED);
        orderRepository.save(order);

        OrderApproveEvent orderApproveEvent = new OrderApproveEvent(orderId);
        kafkaTemplate.send(ordersEventsTopicName, orderApproveEvent);

    }

    @Override
    public void rejectedOrder(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId).orElse(null);
        Assert.notNull(order, "Order with orderId: " + orderId + " not found in database");
        order.setStatus(OrderStatus.REJECTED);
        orderRepository.save(order);
    }
}
