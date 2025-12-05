package com.deep.eyedonation.model

import android.os.Parcelable
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.parcelize.Parcelize

@Parcelize
data class Hospital(
    val hospitalName: String = "",
    val emailAddress: String = "",
    val hospitalAddress: String = "",
    val district: String = "",
    val state: String = "",
    val contactPerson: String = "",
    val contactNumber: String = "",
    val eyeBankName: String = "",
    val isDetailsCorrect: Boolean = true
) : Parcelable


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
                Log.e("HospitalCrud", "Error retrieving hospitals: ${e.message}")
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

        suspend fun deleteHospital(emailAddress: String) {
            try {
                hospitalRef.document(emailAddress).delete().await()
                println("Hospital successfully deleted")
            } catch (e: Exception) {
                println("Error deleting hospital: ${e.message}")
            }
        }


    }
}
