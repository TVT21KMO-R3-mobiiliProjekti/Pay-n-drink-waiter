package com.example.payndrink

import com.example.payndrinkwaiter.database.DatabaseAccess
import junit.framework.TestCase.*
import org.junit.Test

class DatabaseUnitTest {
    @Test
    fun connectionTest(){
        val dbAccess = DatabaseAccess()
        assertNotNull(dbAccess.connectToDatabase())
    }
}