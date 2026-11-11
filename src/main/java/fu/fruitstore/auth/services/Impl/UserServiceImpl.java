package fu.fruitstore.auth.services.Impl;

import fu.fruitstore.auth.dto.*;
import fu.fruitstore.auth.entity.PasswordResetOtp;
import fu.fruitstore.auth.entity.Role;
import fu.fruitstore.auth.entity.User;
import fu.fruitstore.auth.entity.UserAddress;
import fu.fruitstore.auth.repository.PasswordResetOtpRepository;
import fu.fruitstore.auth.repository.RoleRepository;
import fu.fruitstore.auth.repository.UserAddressRepository;
import fu.fruitstore.auth.repository.UserRepository;
import fu.fruitstore.auth.services.EmailService;
import fu.fruitstore.auth.services.UserService;
import fu.fruitstore.shared.exception.BadRequestException;
import fu.fruitstore.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserAddressRepository userAddressRepository;
    private final PasswordResetOtpRepository otpRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    // ==========================================
    // REGISTRATION WITH EMAIL OTP
    // ==========================================

    @Override
    @Transactional
    public void sendRegistrationOtp(RegisterStep1Dto dto) {
        String cleanEmail = dto.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(cleanEmail)) {
            throw new BadRequestException("Email này đã được sử dụng bởi một tài khoản khác!");
        }

        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().isBlank()) {
            if (userRepository.existsByPhoneNumber(dto.getPhoneNumber().trim())) {
                throw new BadRequestException("Số điện thoại này đã được đăng ký bởi tài khoản khác!");
            }
        }

        // Hủy các mã OTP cũ chưa dùng của email này
        otpRepository.invalidateAllByEmail(cleanEmail);

        // Sinh mã OTP 6 số ngẫu nhiên
        SecureRandom random = new SecureRandom();
        String otpCode = String.format("%06d", random.nextInt(1_000_000));

        PasswordResetOtp otp = PasswordResetOtp.builder()
                .email(cleanEmail)
                .otpCode(otpCode)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .isUsed(false)
                .build();
        otpRepository.save(otp);

        // Gửi OTP qua email
        emailService.sendOtpEmail(cleanEmail, otpCode);
    }

    @Override
    @Transactional
    public boolean verifyRegistrationOtp(String email, String otpCode) {
        String cleanEmail = email.toLowerCase().trim();
        String cleanOtp = otpCode.trim();

        PasswordResetOtp otp = otpRepository
                .findTopByEmailAndOtpCodeAndIsUsedFalseOrderByCreatedAtDesc(cleanEmail, cleanOtp)
                .orElse(null);

        if (otp == null || otp.isExpired()) {
            return false;
        }

        otp.setUsed(true);
        otpRepository.save(otp);
        return true;
    }

    @Override
    @Transactional
    public User completeRegistration(String fullName, String email, String phoneNumber, RegisterPasswordDto passwordDto) {
        if (!passwordDto.getPassword().equals(passwordDto.getConfirmPassword())) {
            throw new BadRequestException("Mật khẩu xác nhận không khớp!");
        }

        String cleanEmail = email.toLowerCase().trim();
        if (userRepository.existsByEmail(cleanEmail)) {
            throw new BadRequestException("Email này đã được sử dụng bởi một tài khoản khác!");
        }

        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name("ROLE_CUSTOMER")
                        .description("Khách hàng mua sắm")
                        .build()));

        User user = User.builder()
                .fullName(fullName.trim())
                .email(cleanEmail)
                .password(passwordEncoder.encode(passwordDto.getPassword()))
                .phoneNumber(phoneNumber != null && !phoneNumber.isBlank() ? phoneNumber.trim() : null)
                .enabled(true)
                .roles(new HashSet<>(Collections.singletonList(customerRole)))
                .build();

        return userRepository.save(user);
    }

    // ==========================================
    // USER QUERIES & PROFILE
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));
    }

    @Override
    @Transactional
    public User updateProfile(String email, UserProfileDto dto) {
        User user = findByEmail(email);
        user.setFullName(dto.getFullName().trim());

        if (dto.getSelectedAddressId() != null) {
            setDefaultAddress(user.getId(), dto.getSelectedAddressId());
            user = findById(user.getId());
        } else {
            if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().isBlank()) {
                user.setPhoneNumber(dto.getPhoneNumber().trim());
            }
            if (dto.getAddress() != null && !dto.getAddress().isBlank()) {
                user.setAddress(dto.getAddress().trim());
            }
        }
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(String keyword, Pageable pageable) {
        return userRepository.searchUsers(keyword, pageable);
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long id) {
        User user = findById(id);
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = findById(id);
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // ==========================================
    // PASSWORD MANAGEMENT
    // ==========================================

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordDto dto) {
        User user = findByEmail(email);

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Mật khẩu hiện tại không chính xác!");
        }

        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("Mật khẩu mới không được trùng với mật khẩu hiện tại!");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BadRequestException("Mật khẩu mới và xác nhận mật khẩu không khớp!");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void requestPasswordResetOtp(String email) {
        String cleanEmail = email.toLowerCase().trim();
        userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với email: " + cleanEmail));

        // Hủy các OTP cũ chưa dùng của email này
        otpRepository.invalidateAllByEmail(cleanEmail);

        // Sinh mã OTP 6 chữ số ngẫu nhiên
        SecureRandom random = new SecureRandom();
        String otpCode = String.format("%06d", random.nextInt(1_000_000));

        PasswordResetOtp otp = PasswordResetOtp.builder()
                .email(cleanEmail)
                .otpCode(otpCode)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .isUsed(false)
                .build();
        otpRepository.save(otp);

        // Gửi qua email
        emailService.sendOtpEmail(cleanEmail, otpCode);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verifyPasswordResetOtp(String email, String otpCode) {
        String cleanEmail = email.toLowerCase().trim();
        String cleanOtp = otpCode.trim();

        return otpRepository.findTopByEmailAndOtpCodeAndIsUsedFalseOrderByCreatedAtDesc(cleanEmail, cleanOtp)
                .map(otp -> !otp.isExpired())
                .orElse(false);
    }

    @Override
    @Transactional
    public void resetPasswordWithOtp(ResetPasswordDto dto) {
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BadRequestException("Mật khẩu mới và xác nhận mật khẩu không khớp!");
        }

        String cleanEmail = dto.getEmail().toLowerCase().trim();
        String cleanOtp = dto.getOtpCode().trim();

        PasswordResetOtp otp = otpRepository
                .findTopByEmailAndOtpCodeAndIsUsedFalseOrderByCreatedAtDesc(cleanEmail, cleanOtp)
                .orElseThrow(() -> new BadRequestException("Mã OTP không hợp lệ hoặc đã được sử dụng!"));

        if (otp.isExpired()) {
            throw new BadRequestException("Mã OTP đã hết hiệu lực (quá 5 phút). Vui lòng yêu cầu mã mới!");
        }

        User user = findByEmail(cleanEmail);
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        // Đánh dấu OTP đã dùng
        otp.setUsed(true);
        otpRepository.save(otp);
    }

    // ==========================================
    // ADDRESS BOOK MANAGEMENT
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<UserAddress> getUserAddresses(Long userId) {
        return userAddressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
    }

    @Override
    @Transactional
    public UserAddress addAddress(Long userId, AddressDto dto) {
        User user = findById(userId);
        List<UserAddress> existingAddresses = userAddressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);

        boolean makeDefault = dto.isDefault() || existingAddresses.isEmpty();

        if (makeDefault) {
            userAddressRepository.resetDefaultAddressByUserId(userId);
        }

        UserAddress address = UserAddress.builder()
                .user(user)
                .recipientName(dto.getRecipientName().trim())
                .phoneNumber(dto.getPhoneNumber().trim())
                .detailedAddress(dto.getDetailedAddress().trim())
                .isDefault(makeDefault)
                .build();

        UserAddress saved = userAddressRepository.save(address);

        if (makeDefault) {
            user.setAddress(saved.getDetailedAddress());
            user.setPhoneNumber(saved.getPhoneNumber());
            userRepository.save(user);
        }

        return saved;
    }

    @Override
    @Transactional
    public UserAddress updateAddress(Long userId, Long addressId, AddressDto dto) {
        UserAddress address = userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ!"));

        if (dto.isDefault()) {
            userAddressRepository.resetDefaultAddressByUserId(userId);
            address.setDefault(true);

            User user = address.getUser();
            user.setAddress(dto.getDetailedAddress().trim());
            user.setPhoneNumber(dto.getPhoneNumber().trim());
            userRepository.save(user);
        } else if (address.isDefault()) {
            User user = address.getUser();
            user.setAddress(dto.getDetailedAddress().trim());
            user.setPhoneNumber(dto.getPhoneNumber().trim());
            userRepository.save(user);
        }

        address.setRecipientName(dto.getRecipientName().trim());
        address.setPhoneNumber(dto.getPhoneNumber().trim());
        address.setDetailedAddress(dto.getDetailedAddress().trim());

        return userAddressRepository.save(address);
    }

    @Override
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        UserAddress address = userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ!"));

        boolean wasDefault = address.isDefault();
        userAddressRepository.delete(address);

        if (wasDefault) {
            List<UserAddress> remaining = userAddressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
            if (!remaining.isEmpty()) {
                UserAddress newDefault = remaining.get(0);
                newDefault.setDefault(true);
                userAddressRepository.save(newDefault);

                User user = newDefault.getUser();
                user.setAddress(newDefault.getDetailedAddress());
                user.setPhoneNumber(newDefault.getPhoneNumber());
                userRepository.save(user);
            }
        }
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long userId, Long addressId) {
        UserAddress address = userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ!"));

        userAddressRepository.resetDefaultAddressByUserId(userId);
        address.setDefault(true);
        userAddressRepository.save(address);

        User user = address.getUser();
        user.setAddress(address.getDetailedAddress());
        user.setPhoneNumber(address.getPhoneNumber());
        userRepository.save(user);
    }
}
