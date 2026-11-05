package fu.fruitstore.auth.services;

import fu.fruitstore.auth.dto.RegisterDto;
import fu.fruitstore.auth.dto.UserProfileDto;
import fu.fruitstore.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    User register(RegisterDto dto);
    User findByEmail(String email);
    User findById(Long id);
    User updateProfile(String email, UserProfileDto dto);
    Page<User> getAllUsers(String keyword, Pageable pageable);
    void toggleUserStatus(Long id);
    void deleteUser(Long id);
}

