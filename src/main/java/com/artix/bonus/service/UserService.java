package com.artix.bonus.service;

import com.artix.bonus.dto.LoginRequest;
import com.artix.bonus.dto.ProfileUpdateRequest;
import com.artix.bonus.dto.RegisterRequest;
import com.artix.bonus.model.User;
import com.artix.bonus.repository.RefreshTokenRepository;
import com.artix.bonus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User register(RegisterRequest request) {
        Optional<User> existingByEmail = userRepository.findByEmail(request.getEmail());
        if (existingByEmail.isPresent() && !existingByEmail.get().isDeleted()) {
            throw new RuntimeException("Email уже зарегистрирован");
        }

        Optional<User> existingByPhone = userRepository.findByPhone(request.getPhone());
        if (existingByPhone.isPresent() && !existingByPhone.get().isDeleted()) {
            throw new RuntimeException("Телефон уже зарегистрирован");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setBirthDate(request.getBirthDate());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setDeleted(false);

        return userRepository.save(user);
    }

    public User login(LoginRequest request) {
        String login = request.getLogin();
        User user = null;

        boolean isEmail = login.contains("@");

        if (isEmail) {
            user = userRepository.findByEmail(login)
                    .filter(u -> !u.isDeleted())
                    .orElse(null);
        } else {
            user = userRepository.findByPhone(login)
                    .filter(u -> !u.isDeleted())
                    .orElse(null);
        }

        if (user == null) {
            throw new RuntimeException("Пользователь не найден");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Неверный пароль");
        }

        return user;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .filter(user -> !user.isDeleted())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .filter(user -> !user.isDeleted())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    public User updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = findById(userId);

        if (request.getFullName() != null && !request.getFullName().isEmpty()) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            user.setPhone(request.getPhone());
        }
        if (request.getBirthDate() != null && !request.getBirthDate().isEmpty()) {
            user.setBirthDate(request.getBirthDate());
        }

        return userRepository.save(user);
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = findById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Неверный текущий пароль");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(Long userId, String password) {
        User user = findById(userId);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Неверный пароль");
        }
        user.setDeleted(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        refreshTokenRepository.deleteByUserId(userId);
    }
}