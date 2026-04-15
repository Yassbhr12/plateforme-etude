package com.sge.platforme_etude.mapper;

import com.sge.platforme_etude.dto.GroupeEtudeDto;
import com.sge.platforme_etude.entite.GroupeEtude;
import com.sge.platforme_etude.entite.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GroupeEtudeMapper {

    private final UserMapper userMapper;

    public GroupeEtudeMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public GroupeEtudeDto toDto(GroupeEtude groupeEtude) {
        if (groupeEtude == null) {
            return null;
        }

        GroupeEtudeDto dto = new GroupeEtudeDto();
        dto.setId(groupeEtude.getId());
        dto.setNom(groupeEtude.getNom());
        dto.setDescription(groupeEtude.getDescription());

        if (groupeEtude.getAdmin() != null) {
            dto.setAdminId(groupeEtude.getAdmin().getId());
            dto.setAdminNom(groupeEtude.getAdmin().getNom());
            dto.setAdminEmail(groupeEtude.getAdmin().getEmail());
        }

        if (groupeEtude.getUsers() != null) {
            dto.setUsers(userMapper.toDtoList(groupeEtude.getUsers()));
        }

        return dto;
    }

    public GroupeEtude toEntity(GroupeEtudeDto dto, User admin, List<User> users) {
        if (dto == null) {
            return null;
        }

        GroupeEtude groupeEtude = new GroupeEtude();
        groupeEtude.setId(dto.getId());
        groupeEtude.setNom(dto.getNom());
        groupeEtude.setDescription(dto.getDescription());
        groupeEtude.setAdmin(admin);
        groupeEtude.setUsers(users);

        return groupeEtude;
    }

    public void updateEntity(GroupeEtude groupeEtude, GroupeEtudeDto dto, User admin, List<User> users) {
        if (groupeEtude == null || dto == null) {
            return;
        }

        groupeEtude.setNom(dto.getNom());
        groupeEtude.setDescription(dto.getDescription());
        groupeEtude.setAdmin(admin == null ? groupeEtude.getAdmin() : admin);
        groupeEtude.setUsers(users == null ? groupeEtude.getUsers() : users);
    }

    public List<GroupeEtudeDto> toDtoList(List<GroupeEtude> groupeEtudes) {
        if (groupeEtudes == null) {
            return List.of();
        }
        return groupeEtudes.stream()
                .map(this::toDto)
                .toList();
    }
}
