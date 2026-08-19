package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ContactItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<ContactItem>>

    @Query("SELECT * FROM contacts WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteContacts(): Flow<List<ContactItem>>

    @Query("SELECT * FROM contacts WHERE phoneNumber LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%'")
    fun searchContacts(query: String): Flow<List<ContactItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactItem>)

    @Update
    suspend fun updateContact(contact: ContactItem)

    @Delete
    suspend fun deleteContact(contact: ContactItem)

    @Query("SELECT COUNT(*) FROM contacts")
    suspend fun getContactCount(): Int
}
