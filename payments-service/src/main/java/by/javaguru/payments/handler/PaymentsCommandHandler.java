package by.javaguru.payments.handler;

import by.javaguru.core.dto.Payment;
import by.javaguru.core.dto.command.ProcessPaymentCommand;
import by.javaguru.core.dto.event.PaymentProcessedEvent;
import by.javaguru.core.dto.event.PaymentProcessedFailedEvent;
import by.javaguru.core.exceptions.CreditCardProcessorUnavailableException;
import by.javaguru.payments.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@KafkaListener(topics = {
        "${payments.commands.topic.name}"
})
@Component
public class PaymentsCommandHandler {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private final PaymentService paymentService;
    private final String paymentsEventsTopicName;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentsCommandHandler(PaymentService paymentService,
                                  @Value("${payments.events.topic.name}") String paymentsEventsTopicName,
                                  KafkaTemplate<String, Object> kafkaTemplate) {
        this.paymentService = paymentService;
        this.paymentsEventsTopicName = paymentsEventsTopicName;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaHandler
    public void handleCommand(@Payload ProcessPaymentCommand paymentCommand) {

        try {
            Payment payment = new Payment(
                    paymentCommand.getOrderId(),
                    paymentCommand.getProductId(),
                    paymentCommand.getPrice(),
                    paymentCommand.getProductQuantity()
            );
            Payment processPayment = paymentService.process(payment);
            PaymentProcessedEvent paymentProcessedEvent = new PaymentProcessedEvent(processPayment.getOrderId(), processPayment.getId());
            kafkaTemplate.send(paymentsEventsTopicName, paymentProcessedEvent);
        } catch (CreditCardProcessorUnavailableException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            PaymentProcessedFailedEvent paymentProcessedFailedEvent = new PaymentProcessedFailedEvent(
                    paymentCommand.getOrderId(),
                    paymentCommand.getProductId(),
                    paymentCommand.getProductQuantity()
            );

            kafkaTemplate.send(paymentsEventsTopicName, paymentProcessedFailedEvent);
        }
    }
}
