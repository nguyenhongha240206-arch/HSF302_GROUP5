package com.example.identity_service.controller;

import com.example.identity_service.dto.request.PermissionRequest;
import com.example.identity_service.dto.response.ApiResponse;
import com.example.identity_service.dto.response.PermissionResponse;
import com.example.identity_service.service.PermissionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/permissions")
public class PermissionController {

  private final PermissionService permissionService;

  @PostMapping
  public ApiResponse<PermissionResponse> createPermission(
      @RequestBody PermissionRequest permissionRequest) {
    return ApiResponse.<PermissionResponse>builder()
        .result(permissionService.createPermission(permissionRequest))
        .build();
  }

  @GetMapping
  public ApiResponse<List<PermissionResponse>> getPermissions() {
    return ApiResponse.<List<PermissionResponse>>builder()
        .result(permissionService.getListPermissions())
        .build();
  }

  @DeleteMapping("/{permissionName}")
  public ApiResponse<Void> deletePermission(@PathVariable String permissionName) {
    permissionService.deletePermission(permissionName);
    return ApiResponse.<Void>builder().build();
  }
}
