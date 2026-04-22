package com.sge.platforme_etude.service.user;


import com.sge.platforme_etude.dto.UserDto;
import com.sge.platforme_etude.dto.authentification.AuthRequest;
import com.sge.platforme_etude.dto.authentification.ValidationCodeRequest;
import com.sge.platforme_etude.entite.User;
import com.sge.platforme_etude.helper.security.jwt.JwtUtils;
import com.sge.platforme_etude.mapper.UserMapper;
import com.sge.platforme_etude.repository.UserRepo;
import com.sge.platforme_etude.service.EmailService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;

@Service
public class UserService {

    private final UserRepo userRepo;

    private final UserMapper userMapper;

    private final EmailService emailService;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    private final JwtUtils jwtUtils;

    private final RefreshTokenService refreshTokenService;

    public UserService(UserRepo userRepo, UserMapper userMapper,
                       EmailService emailService, JwtUtils jwtUtils,
                       RefreshTokenService refreshTokenService) {
        this.userRepo = userRepo;
        this.userMapper = userMapper;
        this.emailService = emailService;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
    }

    private String generateValidationCode() {
        Random rand = new Random();
        return String.format("%06d", rand.nextInt(999999));
    }

    @Transactional
    public UserDto createUser(UserDto userDto , AuthRequest authRequest){
        userRepo.findUserByEmail(authRequest.getEmail()).ifPresent(u -> {
            throw new RuntimeException("Email already exists");
        });

        User user = userMapper.toEntity(userDto);
        user.setEmail(authRequest.getEmail());
        user.setMotDePasse(encoder.encode(authRequest.getPassword()));
        User saved = userRepo.save(user);

        return userMapper.toDto(saved);
    }

    @Transactional
    public void loginProcess(AuthRequest authRequest) throws Exception {
        User user = userRepo.findUserByEmail(authRequest.getEmail())
                .orElseThrow(()-> new RuntimeException("User Not Found!"));

        if(!user.getActif()){
            throw new Exception("Votre compte est désactivé, contactez l'administrateur");
        }
        if(!encoder.matches(authRequest.getPassword() ,user.getMotDePasse() ) ){
            throw new Exception("Email ou Mot de passe incorrect");
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
    public String[] validateCode(ValidationCodeRequest validationCodeRequest) throws Exception {

        User user = userRepo.findUserByEmail(validationCodeRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User Not Found!"));

        if (user.getValidationCode() == null)
            throw new Exception("Aucun code de validation trouvé. Veuillez vous reconnecter.");

        if (user.getValidationCodeExpiration() == null)
            throw new Exception("Code de validation expiré. Veuillez vous reconnecter.");

        LocalDateTime expiration = user.getValidationCodeExpiration().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        if (now.isAfter(expiration)) {
            long secondesEcoulees = Duration.between(expiration, now).getSeconds();
            throw new Exception("Le code a expiré il y a " + secondesEcoulees + " secondes.");
        }

        if (!user.getValidationCode().trim().equals(validationCodeRequest.getValidationCode().trim()))
            throw new Exception("Code de validation incorrect");

        user.setValidationCode(null);
        user.setValidationCodeExpiration(null);
        userRepo.save(user);

        refreshTokenService.revokeAllUserTokens(user);
        String accessToken = jwtUtils.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = refreshTokenService.createRefreshToken(user.getId()).getToken();

        return new String[]{accessToken, refreshToken};
    }

    public UserDto findUserById(Long id){
        return userRepo.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(()-> new RuntimeException("User Not Found!"));
    }

    public List<UserDto> findAllUsers(){

        return userRepo.findAll()
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    public UserDto findUserByEmail(String email){
        return userRepo.findUserByEmail(email)
                .map(userMapper::toDto)
                .orElseThrow(()-> new RuntimeException("User Not Found"));
    }
    @Transactional
    public UserDto updateUserById(UserDto userDto , Long id){

        User user = userRepo.findById(id)
                .orElseThrow(()-> new RuntimeException("User Not Found"));
        userMapper.updateEntity(user,userDto);
        User updated = userRepo.save(user);

        return userMapper.toDto(updated);

    }

    @Transactional
    public void deleteUserById(Long id){
        User user = userRepo.findById(id)
                .orElseThrow(()-> new RuntimeException("User Not Found"));
        userRepo.delete(user);
    }
}
