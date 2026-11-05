package fu.fruitstore.order.entity;

public enum PaymentMethod {
    COD("Thanh toán khi nhận hàng (COD)"),
    BANK_TRANSFER("Chuyển khoản ngân hàng (QR Code / Banking)");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

