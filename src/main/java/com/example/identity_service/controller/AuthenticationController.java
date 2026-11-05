package com.example.identity_service.controller;

import com.example.identity_service.dto.request.AuthRequest;
import com.example.identity_service.dto.request.IntrospectRequest;
import com.example.identity_service.dto.response.ApiResponse;
import com.example.identity_service.dto.response.AuthResponse;
import com.example.identity_service.dto.response.IntrospectResponse;
import com.example.identity_service.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import java.text.ParseException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthenticationController {

  private final AuthenticationService authenticationService;

  @PostMapping("/login")
  public ApiResponse<AuthResponse> authenticate(@RequestBody AuthRequest authRequest) {

    var result = authenticationService.authenticate(authRequest);
    return ApiResponse.<AuthResponse>builder().result(result).build();
  }

  @PostMapping("/introspect")
  public ApiResponse<IntrospectResponse> postMethodName(@RequestBody IntrospectRequest request)
      throws JOSEException, ParseException {

    var result = authenticationService.intrspect(request);
    return ApiResponse.<IntrospectResponse>builder().result(result).build();
  }
}
