package fu.fruitstore.product.entity;

public enum ProductType {
    FRUIT("Trái cây tươi"),
    GIFT_BASKET("Giỏ quà trái cây"),
    DRIED_FRUIT("Trái cây sấy khô & chế biến");

    private final String displayName;

    ProductType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

