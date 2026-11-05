package fu.fruitstore.order.entity;

public enum OrderStatus {
    PENDING("Chờ xác nhận", "badge bg-warning text-dark"),
    CONFIRMED("Đã xác nhận", "badge bg-info text-dark"),
    SHIPPING("Đang giao hàng", "badge bg-primary"),
    DELIVERED("Giao thành công", "badge bg-success"),
    CANCELLED("Đã hủy", "badge bg-danger");

    private final String displayName;
    private final String badgeClass;

    OrderStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}

