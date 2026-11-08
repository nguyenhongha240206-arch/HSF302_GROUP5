package fu.fruitstore.dashboard.controller;

import fu.fruitstore.dashboard.dto.RevenueReportDto;
import fu.fruitstore.dashboard.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class AdminReportController {

    private final ReportService reportService;

    @GetMapping
    public String showRevenueReport(
            @RequestParam(name = "period", defaultValue = "month") String period,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model
    ) {
        RevenueReportDto report = reportService.getRevenueReport(period, startDate, endDate);
        model.addAttribute("report", report);
        model.addAttribute("currentPeriod", period);
        model.addAttribute("startDate", report.getStartDate());
        model.addAttribute("endDate", report.getEndDate());

        return "admin/reports/revenue";
    }
}

