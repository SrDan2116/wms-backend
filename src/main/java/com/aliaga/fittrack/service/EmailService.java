package com.aliaga.fittrack.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    // private final JavaMailSender javaMailSender; // (Descomentar en el futuro)

    public void sendResetToken(String to, String token) {
        // Link que llevará al Frontend
        String link = "https://tu-frontend.com/reset-password?token=" + token;
        
        // --- SIMULACIÓN (PARA QUE VEAS EL LINK EN CONSOLA) ---
        System.out.println("==================================================");
        System.out.println("📧 SIMULANDO ENVÍO DE EMAIL A: " + to);
        System.out.println("🔑 TOKEN: " + token);
        System.out.println("🔗 LINK DE RECUPERACIÓN: " + link);
        System.out.println("==================================================");

        /* CÓDIGO REAL PARA EL FUTURO:
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Recuperar Contraseña - Fittrack");
        message.setText("Haz clic aquí para restablecer tu contraseña: " + link);
        javaMailSender.send(message);
        */
    }
}