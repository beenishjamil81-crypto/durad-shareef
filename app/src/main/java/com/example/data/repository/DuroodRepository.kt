package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.GlobalSummary
import com.example.data.model.Recitation
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class DuroodRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    fun observePublicRecitations(): Flow<List<Recitation>> {
        val path = "recitations"
        return db.collection(path)
            .whereEqualTo("isPublic", true)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    Recitation::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    fun observeUserRecitations(userId: String): Flow<List<Recitation>> {
        val path = "recitations"
        return db.collection(path)
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    Recitation::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    fun observeGlobalSummary(): Flow<GlobalSummary?> {
        val docRef = db.collection("summary").document("global")
        return docRef.snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(
                        GlobalSummary::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                } else {
                    GlobalSummary(id = "global", totalCount = 0L, totalSubmissions = 0L)
                }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.GET, docRef.path)
                throw error
            }
    }

    suspend fun addRecitation(
        userId: String,
        userName: String,
        count: Long,
        duroodType: String,
        note: String
    ): Result<String> {
        require(userId.isNotBlank()) { "User must be authenticated" }
        require(userName.isNotBlank()) { "Name is required" }
        require(count in 1..10_000_000) { "Count must be between 1 and 10,000,000" }

        val recitationsCol = db.collection("recitations")
        val newDocRef = recitationsCol.document()
        val summaryDocRef = db.collection("summary").document("global")

        val recitationPayload = mapOf(
            "userId" to userId,
            "userName" to userName.trim().take(100),
            "count" to count,
            "duroodType" to duroodType.trim().ifBlank { "Durood Ibrahimi" }.take(100),
            "note" to note.trim().take(300),
            "isPublic" to true,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        val summaryPayload = mapOf(
            "totalCount" to FieldValue.increment(count),
            "totalSubmissions" to FieldValue.increment(1L),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        return try {
            val batch = db.batch()
            batch.set(newDocRef, recitationPayload)
            batch.set(summaryDocRef, summaryPayload, SetOptions.merge())
            batch.commit().await()
            Result.success(newDocRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, recitationsCol.path)
            Result.failure(e)
        }
    }

    suspend fun deleteRecitation(recitationId: String, countToDeduct: Long = 0L): Result<Unit> {
        val docRef = db.collection("recitations").document(recitationId)
        val summaryDocRef = db.collection("summary").document("global")

        return try {
            val batch = db.batch()
            batch.delete(docRef)
            if (countToDeduct > 0L) {
                batch.set(
                    summaryDocRef,
                    mapOf(
                        "totalCount" to FieldValue.increment(-countToDeduct),
                        "totalSubmissions" to FieldValue.increment(-1L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                )
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }
}
