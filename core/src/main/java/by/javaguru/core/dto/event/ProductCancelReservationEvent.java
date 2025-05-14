package by.javaguru.core.dto.event;

import java.util.UUID;

public class ProductCancelReservationEvent {

    private UUID orderId;

    public ProductCancelReservationEvent() {
    }

    public ProductCancelReservationEvent(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }
}
