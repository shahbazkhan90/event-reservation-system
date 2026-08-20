package com.project.eventreservation.controller;

import com.project.eventreservation.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtService jwtService;

    public AuthController(JwtService service){
        this.jwtService = service;
    }

    public record LoginRequest(String username,String password){}
    public record AuthResponse(String token){}

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody LoginRequest request){
        if("admin".equals(request.username) && "password123".equals(request.password)){
            String token = jwtService.generateToken("42");
            return ResponseEntity.ok(new AuthResponse(token));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Credentials");
    }

}
