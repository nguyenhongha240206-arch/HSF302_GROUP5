package fu.fruitstore.auth.services;

import fu.fruitstore.auth.dto.*;
import fu.fruitstore.auth.entity.User;
import fu.fruitstore.auth.entity.UserAddress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    User register(RegisterDto dto);
    User findByEmail(String email);
    User findById(Long id);
    User updateProfile(String email, UserProfileDto dto);
    Page<User> getAllUsers(String keyword, Pageable pageable);
    void toggleUserStatus(Long id);
    void deleteUser(Long id);

    // Password Management
    void changePassword(String email, ChangePasswordDto dto);
    void requestPasswordResetOtp(String email);
    boolean verifyPasswordResetOtp(String email, String otpCode);
    void resetPasswordWithOtp(ResetPasswordDto dto);

    // Address Book Management
    List<UserAddress> getUserAddresses(Long userId);
    UserAddress addAddress(Long userId, AddressDto dto);
    UserAddress updateAddress(Long userId, Long addressId, AddressDto dto);
    void deleteAddress(Long userId, Long addressId);
    void setDefaultAddress(Long userId, Long addressId);
}
