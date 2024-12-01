package com.example.deep.model

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class Users(
    val role: String,
    val id: String = ""
)

class UsersCrud {
    companion object {
        private val userRef = FirebaseFirestore.getInstance().collection("Users")

        suspend fun addUser(users: Users) {
            try {
                val userData = mapOf(
                    "position" to users.role

                )
                val newUserRef = userRef.add(userData).await()
                val userWithId = users.copy(id = newUserRef.id)
                newUserRef.set(userWithId).await()
                println("Story successfully added with ID: ${newUserRef.id}")
            } catch (e: Exception) {
                println("Error adding story: ${e.message}")
            }
        }

    }
}