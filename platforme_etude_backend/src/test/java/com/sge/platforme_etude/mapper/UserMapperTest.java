package com.sge.platforme_etude.mapper;

import com.sge.platforme_etude.dto.UserDto;
import com.sge.platforme_etude.entite.User;
import com.sge.platforme_etude.helper.enums.Role;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    void toDto_returnsNull_whenUserIsNull() {
        assertNull(mapper.toDto(null));
    }

    @Test
    void toDto_mapsFields() {
        User user = new User();
        user.setId(42L);
        user.setNom("Doe");
        user.setPrenom("Jane");
        user.setEmail("jane.doe@example.com");
        user.setRole(Role.ROLE_USER);
        user.setActif(false);

        UserDto dto = mapper.toDto(user);

        assertNotNull(dto);
        assertEquals(42L, dto.getId());
        assertEquals("Doe", dto.getNom());
        assertEquals("Jane", dto.getPrenom());
        assertEquals("jane.doe@example.com", dto.getEmail());
        assertEquals(Role.ROLE_USER, dto.getRole());
        assertFalse(dto.getActif());
    }

    @Test
    void toEntity_defaultsActifToTrue_whenDtoActifIsNull() {
        UserDto dto = new UserDto();
        dto.setId(7L);
        dto.setNom("Smith");
        dto.setPrenom("John");
        dto.setRole(Role.ROLE_ADMIN);
        dto.setActif(null);

        User user = mapper.toEntity(dto);

        assertNotNull(user);
        assertEquals(7L, user.getId());
        assertEquals("Smith", user.getNom());
        assertEquals("John", user.getPrenom());
        assertEquals(Role.ROLE_ADMIN, user.getRole());
        assertTrue(user.getActif());
    }

    @Test
    void toDtoList_returnsEmpty_whenInputIsNull() {
        assertTrue(mapper.toDtoList(null).isEmpty());
    }

    @Test
    void updateEntity_updatesProvidedFields_andPreservesMissingFields() {
        User user = new User();
        user.setEmail("old@example.com");
        user.setActif(false);
        user.setRole(Role.ROLE_ADMIN);

        UserDto dto = new UserDto();
        dto.setNom("NewNom");
        dto.setPrenom("NewPrenom");

        mapper.updateEntity(user, dto);

        assertEquals("NewNom", user.getNom());
        assertEquals("NewPrenom", user.getPrenom());
        assertEquals("old@example.com", user.getEmail());
        assertEquals(Role.ROLE_ADMIN, user.getRole());
        assertFalse(user.getActif());
    }

    @Test
    void updateEntity_ignoresNullArguments() {
        User user = new User();
        mapper.updateEntity(user, null);
        mapper.updateEntity(null, new UserDto());
        assertNotNull(user);
    }
}

