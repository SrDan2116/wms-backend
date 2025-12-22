package com.aliaga.fittrack.controller;

import com.aliaga.fittrack.dto.*;
import com.aliaga.fittrack.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    // --- NUEVO: OLVIDÉ CONTRASEÑA ---
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody ForgotPasswordRequest req) {
        service.forgotPassword(req.getEmail());
        return ResponseEntity.ok().build();
    }

    // --- NUEVO: RESTABLECER CONTRASEÑA ---
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest req) {
        service.resetPassword(req.getToken(), req.getNewPassword());
        return ResponseEntity.ok().build();
    }
}