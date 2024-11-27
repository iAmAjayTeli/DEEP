package com.example.deep.model

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class AdminCredentials(
    val username:String,
    val password:String
)

class Admin {
    companion object{
        private val adminRef = FirebaseFirestore.getInstance().collection("Admin")

        suspend fun getAdminCredentials(): AdminCredentials? {
           return try {
                val result= adminRef.document("Credentials").get().await()
                return result.toObject(AdminCredentials::class.java)
            }
            catch (e:Exception){
                e.printStackTrace()
                null
            }
        }
    }
}
