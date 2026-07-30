package expensetracker.authservice.service;

import expensetracker.authservice.entities.RefreshToken;
import expensetracker.authservice.entities.UserInfo;
import expensetracker.authservice.repository.RefreshTokenRepository;
import expensetracker.authservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository  userRepository;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository
            , UserRepository userRepository
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public RefreshToken createRefreshToken(String userId) {
        Optional<UserInfo> userInfoOptional = userRepository.findById(userId);

        if (userInfoOptional.isPresent()) {
            UserInfo userInfoExtracted = userInfoOptional.get();
            RefreshToken refreshToken = RefreshToken
                .builder()
                .userInfo(userInfoExtracted)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(1000 * 60 * 60))
                .build();

        return refreshTokenRepository.save(refreshToken);
        }

        return null;
    }

    public RefreshToken verifyExpiration(RefreshToken refreshToken) {
        if(refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException(refreshToken.getToken() + " Refresh token is expired. Please make a new login.");
        }

        return refreshToken;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public boolean deleteByToken(String token) {
       Optional<RefreshToken> tokenOptional = refreshTokenRepository.findByToken(token);
       if(tokenOptional.isPresent()) {
           refreshTokenRepository.delete(tokenOptional.get());
           return true;
       }
       return false;
    }
}
