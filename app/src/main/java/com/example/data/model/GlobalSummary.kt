package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class GlobalSummary(
    @DocumentId val id: String = "",
    val totalCount: Long = 0L,
    val totalSubmissions: Long = 0L,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
