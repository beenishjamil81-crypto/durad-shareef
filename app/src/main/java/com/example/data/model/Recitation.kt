package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Recitation(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val count: Long = 0L,
    val duroodType: String = "",
    val note: String = "",
    val isPublic: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
