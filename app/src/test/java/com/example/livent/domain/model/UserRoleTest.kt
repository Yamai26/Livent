package com.example.livent.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class UserRoleTest {

    @Test
    fun entries_containUserAndPublisher() {
        assertEquals(2, UserRole.entries.size)
        assertEquals(UserRole.USER, UserRole.valueOf("USER"))
        assertEquals(UserRole.PUBLISHER, UserRole.valueOf("PUBLISHER"))
    }
}
