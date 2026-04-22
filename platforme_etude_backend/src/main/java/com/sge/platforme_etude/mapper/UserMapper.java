package com.sge.platforme_etude.mapper;


import com.sge.platforme_etude.dto.UserDto;
import com.sge.platforme_etude.entite.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        UserDto userDto = new UserDto();

        userDto.setId(user.getId());
        userDto.setNom(user.getNom());
        userDto.setPrenom(user.getPrenom());
        userDto.setEmail(user.getEmail());
        userDto.setRole(user.getRole());
        userDto.setActif(user.getActif());

        return userDto;
    }

    public User toEntity(UserDto userDto) {
        if (userDto == null) {
            return null;
        }

        User user = new User();

        user.setId(userDto.getId());
        user.setNom(userDto.getNom());
        user.setPrenom(userDto.getPrenom());

//        user.setEmail(authRequest.getEmail());
//        user.setMotDePasse(authRequest.getPassword());

        user.setRole(userDto.getRole());
        user.setActif(userDto.getActif() == null ? true : userDto.getActif());

        return user;

    }

    public List<UserDto> toDtoList(List<User> users) {
        if (users == null) {
            return List.of();
        }
        return users.stream()
                .map(this::toDto)
                .toList();
    }

    public void updateEntity(User user, UserDto userDto) {
        if (user == null || userDto == null) {
            return;
        }

        user.setNom(userDto.getNom());
        user.setPrenom(userDto.getPrenom());
        user.setEmail(userDto.getEmail());
        user.setRole(userDto.getRole());
        user.setActif(userDto.getActif() == null ? true : userDto.getActif());


    }
}
