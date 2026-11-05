package com.example.identity_service.controller;

import com.example.identity_service.dto.request.RoleRequest;
import com.example.identity_service.dto.response.ApiResponse;
import com.example.identity_service.dto.response.RoleResponse;
import com.example.identity_service.service.RoleService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/roles")
public class RoleController {

  private final RoleService roleService;

  @PostMapping
  public ApiResponse<RoleResponse> createRole(@RequestBody RoleRequest roleRequest) {
    return ApiResponse.<RoleResponse>builder().result(roleService.createRole(roleRequest)).build();
  }

  @GetMapping
  public ApiResponse<List<RoleResponse>> getRoles() {
    return ApiResponse.<List<RoleResponse>>builder().result(roleService.getRoles()).build();
  }

  @DeleteMapping
  public ApiResponse<Void> deleteRole(@RequestBody String roleName) {
    roleService.deleteRole(roleName);
    return ApiResponse.<Void>builder().build();
  }
}
