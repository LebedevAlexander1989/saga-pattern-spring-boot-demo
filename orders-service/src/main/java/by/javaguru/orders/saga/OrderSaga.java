package by.javaguru.orders.saga;

import by.javaguru.core.dto.command.ApproveOrderCommand;
import by.javaguru.core.dto.command.ProcessPaymentCommand;
import by.javaguru.core.dto.command.ReserveProductCommand;
import by.javaguru.core.dto.event.*;
import by.javaguru.orders.service.OrderHistoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static by.javaguru.core.types.OrderStatus.APPROVED;
import static by.javaguru.core.types.OrderStatus.CREATED;

@Component
@KafkaListener(topics = {
        "${orders.events.topic.name}",
        "${products.events.topic.name}",
        "${payments.events.topic.name}"
})
public class OrderSaga {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String productsCommandTopicName;
    private final String paymentsCommandsTopicName;

    private final String ordersCommandsTopicName;
    private final OrderHistoryService orderHistoryService;

    public OrderSaga(KafkaTemplate<String, Object> kafkaTemplate,
                     @Value("${products.commands.topic.name}") String productsCommandTopicName,
                     @Value("${payments.commands.topic.name}") String paymentsCommandsTopicName,
                     @Value("${orders.commands.topic.name}") String ordersCommandsTopicName,
                     OrderHistoryService orderHistoryService) {
        this.kafkaTemplate = kafkaTemplate;
        this.productsCommandTopicName = productsCommandTopicName;
        this.paymentsCommandsTopicName = paymentsCommandsTopicName;
        this.ordersCommandsTopicName = ordersCommandsTopicName;
        this.orderHistoryService = orderHistoryService;
    }

    @KafkaHandler
    public void handleEvent(@Payload OrderCreatedEvent orderCreatedEvent) {
        ReserveProductCommand command = new ReserveProductCommand(
                orderCreatedEvent.getOrderId(),
                orderCreatedEvent.getProductId(),
                orderCreatedEvent.getProductQuantity()
        );

        kafkaTemplate.send(productsCommandTopicName, command);

        orderHistoryService.add(orderCreatedEvent.getOrderId(), CREATED);
    }

    @KafkaHandler
    public void handleEvent(@Payload ProductReservedEvent reservedEvent) {
        ProcessPaymentCommand processPaymentCommand = new ProcessPaymentCommand(
                reservedEvent.getOrderId(),
                reservedEvent.getProductId(),
                reservedEvent.getPrice(),
                reservedEvent.getProductQuantity());

        kafkaTemplate.send(paymentsCommandsTopicName, processPaymentCommand);
    }

    @KafkaHandler
    public void handleEvent(@Payload PaymentProcessedEvent paymentProcessedEvent) {
        ApproveOrderCommand orderCommand = new ApproveOrderCommand(paymentProcessedEvent.getOrderId());

        kafkaTemplate.send(ordersCommandsTopicName, orderCommand);
    }

    @KafkaHandler
    public void handleEvent(@Payload OrderApproveEvent orderApproveEvent) {
        orderHistoryService.add(orderApproveEvent.getOrderId(), APPROVED);

    }
}
