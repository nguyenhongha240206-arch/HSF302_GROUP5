package fu.fruitstore.inventory.entity;

public enum LossReason {
    SPOILED("Dập nát / Úng thối"),
    EXPIRED("Hết hạn sử dụng / Quá chín"),
    WEIGHT_LOSS("Hao hụt tự nhiên khi bảo quản lạnh"),
    DAMAGED_IN_TRANSIT("Hư hỏng trong quá trình vận chuyển");

    private final String displayName;

    LossReason(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

