package by.javaguru.orders.handler;

import by.javaguru.core.dto.command.ApproveOrderCommand;
import by.javaguru.core.dto.command.RejectedOrderCommand;
import by.javaguru.orders.service.OrderService;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = {
        "${orders.commands.topic.name}"
})
public class OrderCommandsHandler {

    private final OrderService orderService;

    public OrderCommandsHandler(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaHandler
    public void handleCommand(@Payload ApproveOrderCommand orderCommand) {
        orderService.approveOrder(orderCommand.getOrderId());
    }

    @KafkaHandler
    public void handleCommand(@Payload RejectedOrderCommand rejectedOrderCommand) {
        orderService.rejectedOrder(rejectedOrderCommand.getOrderId());
    }
}
