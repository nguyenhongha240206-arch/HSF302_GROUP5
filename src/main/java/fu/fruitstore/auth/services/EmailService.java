package fu.fruitstore.auth.services;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otpCode) {
        log.info("\n======================================================\n" +
                 "[OTP SERVICE] Ma OTP xac thuc cho email: {}\n" +
                 "MA OTP: {}\n" +
                 "Ma co hieu luc trong 5 phut.\n" +
                 "======================================================", toEmail, otpCode);

        if (mailSender == null || fromEmail == null || fromEmail.isBlank() || fromEmail.contains("YOUR_GMAIL")) {
            log.warn("Chua cau hinh thong tin Gmail SMTP trong application.properties. Ma OTP chi duoc ghi nhan trong log console.");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Fruit Store");
            helper.setTo(toEmail);
            helper.setSubject("Mã xác thực đặt lại mật khẩu - Fruit Store");

            String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;'>"
                    + "<div style='text-align: center; margin-bottom: 20px;'>"
                    + "<h2 style='color: #28a745; margin: 0;'>🍎 Fruit Store Management</h2>"
                    + "<p style='color: #666; font-size: 14px;'>Yêu cầu đặt lại mật khẩu tài khoản</p>"
                    + "</div>"
                    + "<p>Xin chào,</p>"
                    + "<p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản liên kết với email này. Dưới đây là mã OTP xác thực của bạn:</p>"
                    + "<div style='text-align: center; margin: 30px 0;'>"
                    + "<span style='display: inline-block; background-color: #f1f8e9; color: #2e7d32; font-size: 32px; font-weight: bold; letter-spacing: 8px; padding: 12px 24px; border-radius: 8px; border: 1px dashed #4caf50;'>"
                    + otpCode + "</span>"
                    + "</div>"
                    + "<p style='color: #e53935; font-size: 13px;'>* Lưu ý: Mã OTP này có hiệu lực trong vòng <strong>5 phút</strong>. Tuyệt đối không chia sẻ mã này cho bất kỳ ai.</p>"
                    + "<p>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email hoặc liên hệ với bộ phận hỗ trợ của chúng tôi.</p>"
                    + "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'/>"
                    + "<p style='font-size: 12px; color: #999; text-align: center;'>© 2026 Fruit Store - HSF302 Group 5</p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Email chua ma OTP da duoc gui thanh cong toi: {}", toEmail);
        } catch (Exception e) {
            log.error("Khong the gui email toi {}: {}", toEmail, e.getMessage());
        }
    }
}
