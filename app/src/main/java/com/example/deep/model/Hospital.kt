package com.example.deep.model

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


data class Hospital(
    val hospitalName: String = "",
    val hospitalAddress: String = "",
    val district: String = "",
    val state: String = "",
    val contactPerson: String = "",
    val contactNumber: String = "",
    val emailAddress: String = "",
    val eyeBanksNearby: String = "" // Store details or IDs of nearby eye banks
)


class HospitalCrud {

    companion object {
        private val hospitalRef = FirebaseFirestore.getInstance().collection("Hospitals")

        // Add a hospital to Firestore using emailAddress as the unique document ID
        suspend fun addHospital(hospital: Hospital) {
            try {
                hospitalRef.document(hospital.emailAddress).set(hospital).await()
                println("Hospital successfully registered")
            } catch (e: Exception) {
                println("Error registering hospital: ${e.message}")
            }
        }

        // Retrieve a hospital by email address
        suspend fun getHospitalByEmail(emailAddress: String): Hospital? {
            return try {
                val document = hospitalRef.document(emailAddress).get().await()
                if (document.exists()) {
                    document.toObject(Hospital::class.java)
                } else {
                    println("No hospital found for the email: $emailAddress")
                    null
                }
            } catch (e: Exception) {
                println("Error retrieving hospital: ${e.message}")
                null
            }
        }

        // Retrieve all hospitals from Firestore
        suspend fun getAllHospitals(): List<Hospital> {
            return try {
                val result = hospitalRef.get().await()
                result.toObjects(Hospital::class.java)
            } catch (e: Exception) {
                println("Error retrieving all hospitals: ${e.message}")
                emptyList()
            }
        }

        suspend fun hospitalExists(emailAddress: String): Boolean {
            return try {
                val document = hospitalRef.document(emailAddress).get().await()
                document.exists() // Returns true if the document exists, false otherwise
            } catch (e: Exception) {
                println("Error checking hospital existence: ${e.message}")
                false
            }
        }







    }
}
