package com.redmath.GymManagementApp.member;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MemberProfileCompletionDTOTest {

    @Test
    void testGettersAndSetters() {
        MemberProfileCompletionDTO dto = new MemberProfileCompletionDTO();
        dto.setPhoneNumber("1234567890");
        dto.setTrainerid(42L);
        dto.setGender("Male");

        assertEquals("1234567890", dto.getPhoneNumber());
        assertEquals(42L, dto.getTrainerid());
        assertEquals("Male", dto.getGender());
    }

    @Test
    void testDefaultValues() {
        MemberProfileCompletionDTO dto = new MemberProfileCompletionDTO();
        assertNull(dto.getPhoneNumber());
        assertNull(dto.getTrainerid());
        assertNull(dto.getGender());
    }
}