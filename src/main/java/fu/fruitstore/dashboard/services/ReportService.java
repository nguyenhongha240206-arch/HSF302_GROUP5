package fu.fruitstore.dashboard.services;

import fu.fruitstore.dashboard.dto.RevenueReportDto;

import java.time.LocalDate;

public interface ReportService {
    RevenueReportDto getRevenueReport(String period, LocalDate startDate, LocalDate endDate);
}

