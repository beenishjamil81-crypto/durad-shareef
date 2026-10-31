package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class DuroodRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun addRecitation_authenticatedUser_succeedsAndCreatesDocument(): Unit = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repository = DuroodRepository(firestore)

    val result = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.addRecitation(
        userId = uid,
        userName = "Alice",
        count = 100L,
        duroodType = "Durood Ibrahimi",
        note = "For Friday blessings"
      )
    }

    assertTrue(result.isSuccess)
    val id = result.getOrThrow()
    assertTrue(id.isNotEmpty())
  }

  @Test
  fun observePublicRecitations_authenticatedUser_emitsRecitations(): Unit = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repository = DuroodRepository(firestore)

    val addResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.addRecitation(
        userId = uid,
        userName = "Alice",
        count = 33L,
        duroodType = "Sallallahu Alayhi Wasallam",
        note = ""
      )
    }
    assertTrue(addResult.isSuccess)
    val id = addResult.getOrThrow()

    val list = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observePublicRecitations().first { recs ->
        recs.any { it.id == id }
      }
    }
    assertTrue(list.any { it.id == id && it.count == 33L })
  }

  @Test
  fun observePublicRecitations_unauthenticatedUser_failsWithPermissionDenied(): Unit = runBlocking {
    auth.signOut()
    val repository = DuroodRepository(firestore)

    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        repository.observePublicRecitations().first()
      }
      fail("Expected exception due to missing permissions")
    } catch (e: Exception) {
      val firestoreException = checkNotNull(e.unwrapFirestoreException()) {
        "Expected nested FirebaseFirestoreException, got $e"
      }
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
    }
  }

  private companion object {
    const val ALICE_EMAIL = "alice@test.com"
    const val DEFAULT_TIMEOUT_MS = 6000L
    const val FLOW_TIMEOUT_MS = 6000L
  }
}
