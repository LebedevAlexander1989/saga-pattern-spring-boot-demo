package by.javaguru.core.dto.event;

import java.util.UUID;

public class OrderApproveEvent {

    private UUID orderId;

    public OrderApproveEvent() {
    }

    public OrderApproveEvent(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }
}
