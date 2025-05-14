package by.javaguru.core.dto.command;

import java.math.BigDecimal;
import java.util.UUID;

public class ProcessPaymentCommand {

    private UUID orderId;
    private UUID productId;
    private BigDecimal price;
    private Integer productQuantity;

    public ProcessPaymentCommand() {
    }

    public ProcessPaymentCommand(UUID orderId, UUID productId, BigDecimal price, Integer productQuantity) {
        this.orderId = orderId;
        this.productId = productId;
        this.price = price;
        this.productQuantity = productQuantity;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(Integer productQuantity) {
        this.productQuantity = productQuantity;
    }
}
