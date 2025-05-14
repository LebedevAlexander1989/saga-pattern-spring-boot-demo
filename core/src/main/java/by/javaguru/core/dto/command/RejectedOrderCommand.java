package by.javaguru.core.dto.command;

import java.util.UUID;

public class RejectedOrderCommand {

    private UUID orderId;

    public RejectedOrderCommand() {
    }

    public RejectedOrderCommand(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }
}
