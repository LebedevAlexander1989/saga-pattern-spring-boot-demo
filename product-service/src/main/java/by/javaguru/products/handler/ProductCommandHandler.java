package by.javaguru.products.handler;

import by.javaguru.core.dto.Product;
import by.javaguru.core.dto.command.CancelProductReservationCommand;
import by.javaguru.core.dto.command.ReserveProductCommand;
import by.javaguru.core.dto.event.ProductCancelReservationEvent;
import by.javaguru.core.dto.event.ProductReservedEvent;
import by.javaguru.core.dto.event.ProductReservedFailedEvent;
import by.javaguru.products.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = {"${products.commands.topic.name}"})
public class ProductCommandHandler {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final ProductService productService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String productsEventsTopicName;

    public ProductCommandHandler(ProductService productService,
                                 KafkaTemplate<String, Object> kafkaTemplate,
                                 @Value("${products.events.topic.name}") String productsEventsTopicName) {
        this.productService = productService;
        this.kafkaTemplate = kafkaTemplate;
        this.productsEventsTopicName = productsEventsTopicName;
    }

    @KafkaHandler
    public void handleCommand(@Payload ReserveProductCommand command) {
        try {
            Product desiredProduct = new Product(command.getProductId(), command.getProductQuantity());
            Product reserve = productService.reserve(desiredProduct, command.getOrderId());

            ProductReservedEvent reservedEvent = new ProductReservedEvent(
                    command.getOrderId(),
                    command.getProductId(),
                    reserve.getPrice(),
                    reserve.getQuantity()
            );

            kafkaTemplate.send(productsEventsTopicName, reservedEvent);

        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);

            ProductReservedFailedEvent failedEvent = new ProductReservedFailedEvent(
                    command.getOrderId(),
                    command.getProductId(),
                    command.getProductQuantity()
            );

            kafkaTemplate.send(productsEventsTopicName, failedEvent);
        }
    }

    @KafkaHandler
    public void handleCommand(@Payload CancelProductReservationCommand cancelCommand) {
        Product productToCancel = new Product(cancelCommand.getProductId(), cancelCommand.getProductQuantity());
        productService.cancelReservation(productToCancel, cancelCommand.getOrderId());

        ProductCancelReservationEvent cancelEvent = new ProductCancelReservationEvent(
                cancelCommand.getOrderId()
        );
        kafkaTemplate.send(productsEventsTopicName, cancelEvent);
    }
}
