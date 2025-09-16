package com.example.mpin.service;

import com.example.mpin.model.AppUser;
import com.example.mpin.model.RefreshToken;
import com.example.mpin.repository.AppUserRepository;
import com.example.mpin.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repo;
    private final AppUserRepository userRepo;

    public RefreshTokenService(RefreshTokenRepository repo, AppUserRepository userRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
    }

    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        AppUser user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        repo.deleteByUser(user);
        repo.flush();

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(LocalDateTime.now().plusDays(7));

        return repo.saveAndFlush(token);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return repo.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            repo.delete(token);
            throw new RuntimeException("Refresh token expired");
        }
        return token;
    }
    @Transactional
    public void deleteByToken(String token) {
        repo.findByToken(token).ifPresent(repo::delete);
    }
}