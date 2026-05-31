package com.sge.platforme_etude.service.user;


import com.sge.platforme_etude.dto.UserDto;
import com.sge.platforme_etude.dto.authentification.AuthRequest;
import com.sge.platforme_etude.dto.authentification.ForgotPasswordRequest;
import com.sge.platforme_etude.dto.authentification.ResetPasswordRequest;
import com.sge.platforme_etude.dto.authentification.ValidationCodeRequest;
import com.sge.platforme_etude.entite.PasswordResetToken;
import com.sge.platforme_etude.entite.User;
import com.sge.platforme_etude.helper.enums.Role;
import com.sge.platforme_etude.helper.exceptions.BadRequestException;
import com.sge.platforme_etude.helper.exceptions.ConflictException;
import com.sge.platforme_etude.helper.exceptions.ForbiddenException;
import com.sge.platforme_etude.helper.exceptions.NotFoundException;
import com.sge.platforme_etude.helper.exceptions.UnauthorizedException;
import com.sge.platforme_etude.helper.security.jwt.JwtUtils;
import com.sge.platforme_etude.mapper.UserMapper;
import com.sge.platforme_etude.repository.PasswordResetTokenRepo;
import com.sge.platforme_etude.repository.UserRepo;
import com.sge.platforme_etude.service.EmailService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

@Service
public class UserService {

    private static final Duration PASSWORD_RESET_EXPIRATION = Duration.ofMinutes(10);
    private static final Duration PASSWORD_RESET_RESEND_COOLDOWN = Duration.ofSeconds(60);
    private static final int PASSWORD_RESET_MAX_ATTEMPTS = 5;

    private final UserRepo userRepo;

    private final UserMapper userMapper;

    private final EmailService emailService;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    private final JwtUtils jwtUtils;

    private final RefreshTokenService refreshTokenService;

    private final PasswordResetTokenRepo passwordResetTokenRepo;

    private final SecureRandom secureRandom = new SecureRandom();

    public UserService(UserRepo userRepo, UserMapper userMapper,
                       EmailService emailService, JwtUtils jwtUtils,
                       RefreshTokenService refreshTokenService,
                       PasswordResetTokenRepo passwordResetTokenRepo) {
        this.userRepo = userRepo;
        this.userMapper = userMapper;
        this.emailService = emailService;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
        this.passwordResetTokenRepo = passwordResetTokenRepo;
    }

    private String generateValidationCode() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    @Transactional
    public UserDto createUser(UserDto userDto , AuthRequest authRequest){
        String email = normalizeEmail(authRequest.getEmail());
        userRepo.findByEmailIgnoreCase(email).ifPresent(u -> {
            throw new ConflictException("Email already exists");
        });

        User user = userMapper.toEntity(userDto);
        user.setEmail(email);
        user.setMotDePasse(encoder.encode(authRequest.getPassword()));
        user.setRole(Role.ROLE_USER);
        User saved = userRepo.save(user);

        return userMapper.toDto(saved);
    }

    @Transactional
    public void loginProcess(AuthRequest authRequest) {
        User user = userRepo.findByEmailIgnoreCase(normalizeEmail(authRequest.getEmail()))
                .orElseThrow(() -> new NotFoundException("User Not Found!"));

        if(!user.getActif()){
            throw new ForbiddenException("Votre compte est désactivé, contactez l'administrateur");
        }
        if(!encoder.matches(authRequest.getPassword() ,user.getMotDePasse() ) ){
            throw new UnauthorizedException("Email ou Mot de passe incorrect");
        }

        String validationCode = generateValidationCode();

        LocalDateTime expiration = LocalDateTime.now()
                .plusSeconds(180)
                .truncatedTo(ChronoUnit.SECONDS);

        user.setValidationCode(validationCode);
        user.setValidationCodeExpiration(expiration);

        userRepo.save(user);

        emailService.sendEmail(
                user.getEmail(),
                "Code de validation",
                "Votre code de validation est : " + validationCode +
                        "\nCe code expirera après 3 minutes."
        );

    }

    @Transactional
    public String[] validateCode(ValidationCodeRequest validationCodeRequest) {

        User user = userRepo.findByEmailIgnoreCase(normalizeEmail(validationCodeRequest.getEmail()))
                .orElseThrow(() -> new NotFoundException("User Not Found!"));

        if (user.getValidationCode() == null)
            throw new UnauthorizedException("Aucun code de validation trouvé. Veuillez vous reconnecter.");

        if (user.getValidationCodeExpiration() == null)
            throw new UnauthorizedException("Code de validation expiré. Veuillez vous reconnecter.");

        LocalDateTime expiration = user.getValidationCodeExpiration().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        if (now.isAfter(expiration)) {
            long secondesEcoulees = Duration.between(expiration, now).getSeconds();
            throw new UnauthorizedException("Le code a expiré il y a " + secondesEcoulees + " secondes.");
        }

        if (!user.getValidationCode().trim().equals(validationCodeRequest.getValidationCode().trim()))
            throw new UnauthorizedException("Code de validation incorrect");

        user.setValidationCode(null);
        user.setValidationCodeExpiration(null);
        userRepo.save(user);

        refreshTokenService.revokeAllUserTokens(user);
        String accessToken = jwtUtils.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = refreshTokenService.createRefreshToken(user.getId()).getToken();

        return new String[]{accessToken, refreshToken};
    }

    @Transactional
    public void requestPasswordReset(ForgotPasswordRequest request) {
        String email = normalizeEmail(request.getEmail());
        userRepo.findByEmailIgnoreCase(email).ifPresent(user -> {
            if (!Boolean.TRUE.equals(user.getActif())) {
                return;
            }

            LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
            boolean recentCodeAlreadySent = passwordResetTokenRepo.findFirstByUserAndUsedAtIsNullOrderByCreatedAtDesc(user)
                    .filter(token -> !token.isExpired())
                    .filter(token -> Duration.between(token.getCreatedAt(), now).compareTo(PASSWORD_RESET_RESEND_COOLDOWN) < 0)
                    .isPresent();
            if (recentCodeAlreadySent) {
                return;
            }

            passwordResetTokenRepo.findAllByUserAndUsedAtIsNull(user).forEach(token -> token.setUsedAt(now));

            String code = generateValidationCode();
            PasswordResetToken token = new PasswordResetToken();
            token.setUser(user);
            token.setCodeHash(encoder.encode(code));
            token.setCreatedAt(now);
            token.setExpiresAt(now.plus(PASSWORD_RESET_EXPIRATION).truncatedTo(ChronoUnit.SECONDS));
            passwordResetTokenRepo.save(token);

            emailService.sendEmail(
                    user.getEmail(),
                    "Reinitialisation du mot de passe",
                    "Votre code de reinitialisation est : " + code +
                            "\nCe code expirera apres 10 minutes." +
                            "\nSi vous n'avez pas demande cette operation, ignorez cet email."
            );
        });
    }

    @Transactional
    public void resetForgottenPassword(ResetPasswordRequest request) {
        User user = userRepo.findByEmailIgnoreCase(normalizeEmail(request.getEmail()))
                .orElseThrow(() -> new UnauthorizedException("Code de reinitialisation invalide ou expire"));

        if (!Boolean.TRUE.equals(user.getActif())) {
            throw new UnauthorizedException("Code de reinitialisation invalide ou expire");
        }

        PasswordResetToken token = passwordResetTokenRepo.findFirstByUserAndUsedAtIsNullOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new UnauthorizedException("Code de reinitialisation invalide ou expire"));

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        if (token.isExpired()) {
            token.setUsedAt(now);
            passwordResetTokenRepo.save(token);
            throw new UnauthorizedException("Code de reinitialisation invalide ou expire");
        }

        if (token.getAttempts() >= PASSWORD_RESET_MAX_ATTEMPTS) {
            token.setUsedAt(now);
            passwordResetTokenRepo.save(token);
            throw new UnauthorizedException("Nombre de tentatives depasse. Demandez un nouveau code");
        }

        if (!encoder.matches(request.getCode().trim(), token.getCodeHash())) {
            token.setAttempts(token.getAttempts() + 1);
            passwordResetTokenRepo.save(token);
            throw new UnauthorizedException("Code de reinitialisation incorrect");
        }

        if (encoder.matches(request.getNewPassword(), user.getMotDePasse())) {
            throw new BadRequestException("Le nouveau mot de passe doit etre different de l'ancien");
        }

        user.setMotDePasse(encoder.encode(request.getNewPassword()));
        token.setUsedAt(now);
        passwordResetTokenRepo.save(token);
        userRepo.save(user);
        refreshTokenService.revokeAllUserTokens(user);
    }

    public UserDto findUserById(Long id){
        return userRepo.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new NotFoundException("User Not Found!"));
    }

    public List<UserDto> findAllUsers(){

        return userRepo.findAll()
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    public UserDto findUserByEmail(String email){
        return userRepo.findByEmailIgnoreCase(normalizeEmail(email))
                .map(userMapper::toDto)
                .orElseThrow(() -> new NotFoundException("User Not Found"));
    }
    @Transactional
    public UserDto updateUserById(UserDto userDto , Long id){

        User user = userRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("User Not Found"));
        userMapper.updateEntity(user,userDto);
        User updated = userRepo.save(user);

        return userMapper.toDto(updated);

    }

    @Transactional
    public void deleteUserById(Long id){
        deleteUserById(id, null);
    }

    @Transactional
    public void deleteUserById(Long id, Long currentUserId){
        User user = userRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("User Not Found"));
        if (currentUserId != null && user.getId().equals(currentUserId)) {
            throw new BadRequestException("Vous ne pouvez pas supprimer votre propre compte administrateur");
        }
        ensureNotLastActiveAdmin(user);
        userRepo.delete(user);
    }

    @Transactional
    public UserDto toggleUserStatus(Long id, Long currentUserId) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("User Not Found"));
        if (currentUserId != null && user.getId().equals(currentUserId)) {
            throw new BadRequestException("Vous ne pouvez pas desactiver votre propre compte administrateur");
        }
        if (Boolean.TRUE.equals(user.getActif())) {
            ensureNotLastActiveAdmin(user);
        }
        user.setActif(!Boolean.TRUE.equals(user.getActif()));
        return userMapper.toDto(userRepo.save(user));
    }

    @Transactional
    public UserDto updateUserRole(Long id, Role role, Long currentUserId) {
        if (role == null) {
            throw new BadRequestException("Le role est obligatoire");
        }
        User user = userRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("User Not Found"));
        if (currentUserId != null && user.getId().equals(currentUserId) && role != Role.ROLE_ADMIN) {
            throw new BadRequestException("Vous ne pouvez pas retirer votre propre role administrateur");
        }
        if (user.getRole() == Role.ROLE_ADMIN && role != Role.ROLE_ADMIN) {
            ensureNotLastActiveAdmin(user);
        }
        user.setRole(role);
        return userMapper.toDto(userRepo.save(user));
    }

    private void ensureNotLastActiveAdmin(User user) {
        if (user.getRole() == Role.ROLE_ADMIN && Boolean.TRUE.equals(user.getActif())
                && userRepo.countByRoleAndActifTrue(Role.ROLE_ADMIN) <= 1) {
            throw new ConflictException("Impossible de modifier le dernier administrateur actif");
        }
    }
}
