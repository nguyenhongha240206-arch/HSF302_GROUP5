package com.example.identity_service.controller;

import com.example.identity_service.dto.request.UserCreationRequest;
import com.example.identity_service.dto.request.UserUpdateRequest;
import com.example.identity_service.dto.response.ApiResponse;
import com.example.identity_service.dto.response.UserResponse;
import com.example.identity_service.entity.User;
import com.example.identity_service.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
  private final UserService userService;

  @PostMapping
  public ResponseEntity<ApiResponse> createUser(
      @Valid @RequestBody UserCreationRequest userCreationRequest) {
    ApiResponse response = new ApiResponse<>();
    response.setCode(1001);
    response.setResult(this.userService.createUser(userCreationRequest));
    return ResponseEntity.ok(response);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<List<User>> getAllUsers() {
    return ResponseEntity.ok(userService.getAllUsers());
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserResponse> getUserById(@PathVariable("id") String id) {
    return ResponseEntity.ok(userService.getUserById(id));
  }

  @GetMapping("/myInfo")
  public ApiResponse<UserResponse> getMyInfo() {
    return ApiResponse.<UserResponse>builder().result(userService.getMyInfo()).build();
  }

  @PutMapping("/{id}")
  public ResponseEntity<User> updateUser(
      @PathVariable("id") String id, @RequestBody UserUpdateRequest request) {
    return ResponseEntity.ok(userService.updateUser(request, id));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable("id") String id) {
    userService.deleteUser(id);
    return ResponseEntity.ok().build();
  }
}
