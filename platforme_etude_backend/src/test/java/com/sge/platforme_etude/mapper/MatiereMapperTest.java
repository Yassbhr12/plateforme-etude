package com.sge.platforme_etude.mapper;

import com.sge.platforme_etude.dto.MatiereDto;
import com.sge.platforme_etude.entite.Matiere;
import com.sge.platforme_etude.entite.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatiereMapperTest {

    private final MatiereMapper mapper = new MatiereMapper();

    @Test
    void toDto_returnsNull_whenMatiereIsNull() {
        assertNull(mapper.toDto(null));
    }

    @Test
    void toDto_mapsFieldsAndUserInfo() {
        User user = new User();
        user.setId(3L);
        user.setNom("Sam");
        user.setEmail("sam@example.com");

        Matiere matiere = new Matiere();
        matiere.setId(10L);
        matiere.setNom("Maths");
        matiere.setPriorite(5);
        matiere.setUser(user);

        MatiereDto dto = mapper.toDto(matiere);

        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals("Maths", dto.getNom());
        assertEquals(5, dto.getPriorite());
        assertEquals(3L, dto.getUserId());
        assertEquals("Sam", dto.getUserNom());
        assertEquals("sam@example.com", dto.getUserEmail());
    }

    @Test
    void toEntity_mapsFieldsAndUsesProvidedUser() {
        User user = new User();
        user.setId(2L);

        MatiereDto dto = new MatiereDto();
        dto.setId(15L);
        dto.setNom("Physique");
        dto.setPriorite(2);

        Matiere matiere = mapper.toEntity(dto, user);

        assertNotNull(matiere);
        assertEquals(15L, matiere.getId());
        assertEquals("Physique", matiere.getNom());
        assertEquals(2, matiere.getPriorite());
        assertSame(user, matiere.getUser());
    }

    @Test
    void toDtoList_returnsEmpty_whenInputIsNull() {
        assertTrue(mapper.toDtoList(null).isEmpty());
    }

    @Test
    void updateEntity_keepsExistingUser_whenProvidedUserIsNull() {
        User existingUser = new User();
        existingUser.setId(8L);

        Matiere matiere = new Matiere();
        matiere.setUser(existingUser);

        MatiereDto dto = new MatiereDto();
        dto.setNom("Chimie");
        dto.setPriorite(4);

        mapper.updateEntity(matiere, dto, null);

        assertEquals("Chimie", matiere.getNom());
        assertEquals(4, matiere.getPriorite());
        assertSame(existingUser, matiere.getUser());
    }
}

