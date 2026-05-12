package com.sge.platforme_etude.service.user;

import com.sge.platforme_etude.entite.User;
import com.sge.platforme_etude.helper.exceptions.NotFoundException;
import com.sge.platforme_etude.helper.exceptions.UnauthorizedException;
import com.sge.platforme_etude.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService  {

    private final UserRepo userRepo;

    public Authentication getAuthentication(){
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public User getCurrentUser(){
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()){
            throw new UnauthorizedException("Aucun utilisateur connecté");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetails userDetails)) {
            throw new UnauthorizedException("Principal utilisateur invalide");
        }
        String email = userDetails.getUsername();
        return userRepo.findUserByEmail(email)
                .orElseThrow(() -> new NotFoundException("User Not Found"));
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }


}
