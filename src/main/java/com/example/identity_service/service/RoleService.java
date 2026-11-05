package com.example.identity_service.service;

import com.example.identity_service.dto.request.RoleRequest;
import com.example.identity_service.dto.response.RoleResponse;
import com.example.identity_service.entity.Role;
import com.example.identity_service.mapper.RoleMapper;
import com.example.identity_service.repository.PermissionRepository;
import com.example.identity_service.repository.RoleRepository;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {

  private final RoleRepository roleRepository;
  private final RoleMapper roleMapper;
  private final PermissionRepository permissionRepository;

  // Create
  public RoleResponse createRole(RoleRequest request) {
    Role role = roleMapper.toRole(request);
    var permissions = permissionRepository.findAllById(request.getPermissions());
    role.setPermissions(new HashSet<>(permissions));
    roleRepository.save(role);

    return roleMapper.toRoleResponse(role);
  }

  // Get list
  public List<RoleResponse> getRoles() {
    List<Role> roles = roleRepository.findAll();
    return roles.stream().map(roleMapper::toRoleResponse).toList();
  }

  // Delete
  public void deleteRole(String roleName) {
    roleRepository.deleteById(roleName);
  }
}
