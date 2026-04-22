package com.sge.platforme_etude.helper.security;


import com.sge.platforme_etude.entite.User;
import com.sge.platforme_etude.repository.UserRepo;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepo repo;

    public UserDetailsServiceImpl(UserRepo repo) {
        this.repo = repo;
    }


    @Override
    public @NotNull UserDetails loadUserByUsername(@NotNull String username) throws UsernameNotFoundException {
        User user = repo.findUserByEmail(username).orElseThrow(()->new UsernameNotFoundException("User Not Found"));
        if(!user.getActif()){
            throw new RuntimeException("Compte desactive");
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getMotDePasse())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name())))
                .accountExpired(false)
                .accountLocked(!user.getActif())
                .credentialsExpired(false)
                .build()
                ;
    }
}
