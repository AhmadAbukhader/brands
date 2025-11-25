package com.system.brands.Service.UserManagement;


import com.system.brands.Dto.LoginDto;
import com.system.brands.Dto.LoginResponse;
import com.system.brands.Dto.SignUpDto;
import com.system.brands.Dto.SignUpResponse;
import com.system.brands.Exception.DuplicateResourceException;
import com.system.brands.Exception.InvalidCredentialsException;
import com.system.brands.Model.User;
import com.system.brands.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    public SignUpResponse signUp(SignUpDto inputUser) {
        // Check if username already exists
        if (userRepository.findByUsername(inputUser.getUsername()).isPresent()) {
            throw new DuplicateResourceException("User", "username", inputUser.getUsername());
        }

        try {
            User user = User.builder()
                    .username(inputUser.getUsername())
                    .password(passwordEncoder.encode(inputUser.getPassword()))
                    .name(inputUser.getName())
                    .build();

            userRepository.save(user);

            String token = jwtService.generateToken(user);

            return SignUpResponse.builder().token(token).username(user.getUsername()).build();
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("User", "username", inputUser.getUsername());
        }
    }

    public LoginResponse authenticate(LoginDto input) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            input.getUsername(),
                            input.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException();
        }

        User user = userRepository.findByUsername(input.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException());
        
        String token = jwtService.generateToken(user);

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .build();
    }
}

