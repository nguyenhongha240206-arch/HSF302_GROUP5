package fu.fruitstore.dashboard.dto;

import fu.fruitstore.order.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueReportDto {
    private String period; // today, week, month, all, custom
    private LocalDate startDate;
    private LocalDate endDate;

    @Builder.Default
    private BigDecimal totalRevenue = BigDecimal.ZERO; // Delivered orders revenue

    @Builder.Default
    private long totalOrders = 0;

    @Builder.Default
    private long deliveredOrders = 0;

    @Builder.Default
    private long pendingOrders = 0;

    @Builder.Default
    private long shippingOrders = 0;

    @Builder.Default
    private long cancelledOrders = 0;

    @Builder.Default
    private BigDecimal totalLossCost = BigDecimal.ZERO; // Loss slips in period

    @Builder.Default
    private BigDecimal netProfit = BigDecimal.ZERO; // totalRevenue - totalLossCost

    @Builder.Default
    private List<TopSellingFruitDto> topSellingFruits = new ArrayList<>();

    @Builder.Default
    private List<DailyRevenueDto> dailyRevenues = new ArrayList<>();

    @Builder.Default
    private List<Order> deliveredOrderList = new ArrayList<>();
}

