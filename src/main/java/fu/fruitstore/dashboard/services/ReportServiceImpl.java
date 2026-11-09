package fu.fruitstore.dashboard.services;

import fu.fruitstore.dashboard.dto.DailyRevenueDto;
import fu.fruitstore.dashboard.dto.RevenueReportDto;
import fu.fruitstore.dashboard.dto.TopSellingFruitDto;
import fu.fruitstore.inventory.entity.LossSlip;
import fu.fruitstore.inventory.repository.LossSlipRepository;
import fu.fruitstore.order.entity.Order;
import fu.fruitstore.order.entity.OrderDetail;
import fu.fruitstore.order.entity.OrderStatus;
import fu.fruitstore.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final OrderRepository orderRepository;
    private final LossSlipRepository lossSlipRepository;

    @Override
    @Transactional(readOnly = true)
    public RevenueReportDto getRevenueReport(String period, LocalDate customStartDate, LocalDate customEndDate) {
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate;

        String selectedPeriod = (period != null && !period.isBlank()) ? period.toLowerCase() : "month";

        switch (selectedPeriod) {
            case "today":
                startDate = today;
                endDate = today;
                break;
            case "week":
                startDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                endDate = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                break;
            case "all":
                startDate = LocalDate.of(2020, 1, 1);
                endDate = today.plusYears(1);
                break;
            case "custom":
                startDate = customStartDate != null ? customStartDate : today.withDayOfMonth(1);
                endDate = customEndDate != null ? customEndDate : today;
                break;
            case "month":
            default:
                selectedPeriod = "month";
                startDate = today.withDayOfMonth(1);
                endDate = today.with(TemporalAdjusters.lastDayOfMonth());
                break;
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // Fetch Orders in date range
        List<Order> orders = orderRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDateTime, endDateTime);

        long totalOrders = orders.size();
        long deliveredCount = 0;
        long pendingCount = 0;
        long shippingCount = 0;
        long cancelledCount = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        List<Order> deliveredOrders = new ArrayList<>();

        for (Order o : orders) {
            if (o.getOrderStatus() == OrderStatus.DELIVERED) {
                deliveredCount++;
                totalRevenue = totalRevenue.add(o.getFinalAmount());
                deliveredOrders.add(o);
            } else if (o.getOrderStatus() == OrderStatus.PENDING) {
                pendingCount++;
            } else if (o.getOrderStatus() == OrderStatus.SHIPPING || o.getOrderStatus() == OrderStatus.CONFIRMED) {
                shippingCount++;
            } else if (o.getOrderStatus() == OrderStatus.CANCELLED) {
                cancelledCount++;
            }
        }

        // Fetch Loss Slips in date range
        List<LossSlip> lossSlips = lossSlipRepository.findBySlipDateBetween(startDateTime, endDateTime);
        BigDecimal totalLossCost = lossSlips.stream()
                .map(LossSlip::getTotalLossAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netProfit = totalRevenue.subtract(totalLossCost);

        // Calculate Top Selling Fruits (UC-23)
        // Group by product from delivered (or valid) orders
        Map<String, TopSellingFruitDto> productSalesMap = new HashMap<>();

        for (Order order : deliveredOrders) {
            for (OrderDetail detail : order.getOrderDetails()) {
                String key = detail.getProduct() != null 
                        ? "P_" + detail.getProduct().getId() 
                        : "CUSTOM_" + detail.getProductName();

                TopSellingFruitDto dto = productSalesMap.computeIfAbsent(key, k -> {
                    TopSellingFruitDto newDto = new TopSellingFruitDto();
                    if (detail.getProduct() != null) {
                        newDto.setProductId(detail.getProduct().getId());
                        newDto.setProductName(detail.getProduct().getName());
                        newDto.setProductCode(detail.getProduct().getCode());
                        newDto.setCategoryName(detail.getProduct().getCategory() != null ? detail.getProduct().getCategory().getName() : "");
                        newDto.setUnit(detail.getProduct().getUnit());
                        newDto.setImageUrl(detail.getProduct().getImageUrl());
                    } else {
                        newDto.setProductName(detail.getProductName());
                        newDto.setProductCode("CUSTOM_GIFT");
                        newDto.setCategoryName("Giỏ Quà Tùy Biến");
                        newDto.setUnit("giỏ");
                        newDto.setImageUrl("https://images.unsplash.com/photo-1543083477-4f785aeafaa9?auto=format&fit=crop&w=150&q=80");
                    }
                    newDto.setTotalQuantitySold(0);
                    newDto.setTotalRevenue(BigDecimal.ZERO);
                    return newDto;
                });

                dto.setTotalQuantitySold(dto.getTotalQuantitySold() + detail.getQuantity());
                dto.setTotalRevenue(dto.getTotalRevenue().add(detail.getSubTotal()));
            }
        }

        List<TopSellingFruitDto> topSellingList = productSalesMap.values().stream()
                .sorted((a, b) -> Long.compare(b.getTotalQuantitySold(), a.getTotalQuantitySold()))
                .limit(10)
                .collect(Collectors.toList());

        for (int i = 0; i < topSellingList.size(); i++) {
            topSellingList.get(i).setRank(i + 1);
        }

        // Daily Revenue Breakdown
        Map<LocalDate, DailyRevenueDto> dailyMap = new TreeMap<>(Comparator.reverseOrder());
        for (Order order : deliveredOrders) {
            LocalDate d = order.getCreatedAt().toLocalDate();
            DailyRevenueDto daily = dailyMap.computeIfAbsent(d, date -> DailyRevenueDto.builder()
                    .date(date)
                    .orderCount(0)
                    .revenue(BigDecimal.ZERO)
                    .build());
            daily.setOrderCount(daily.getOrderCount() + 1);
            daily.setRevenue(daily.getRevenue().add(order.getFinalAmount()));
        }

        return RevenueReportDto.builder()
                .period(selectedPeriod)
                .startDate(startDate)
                .endDate(endDate)
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .deliveredOrders(deliveredCount)
                .pendingOrders(pendingCount)
                .shippingOrders(shippingCount)
                .cancelledOrders(cancelledCount)
                .totalLossCost(totalLossCost)
                .netProfit(netProfit)
                .topSellingFruits(topSellingList)
                .dailyRevenues(new ArrayList<>(dailyMap.values()))
                .deliveredOrderList(deliveredOrders)
                .build();
    }
}

