package com.deep.eyedonation.model

import android.os.Parcelable
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.parcelize.Parcelize

@Parcelize
data class DonorRegistration(
    val id: String = "",
    // Donor's Details
    val donorName: String = "",
    val donorAge: String = "",
    val donorGender: String = "",
    val deathDateTime: String = "",
    val causeOfDeath: String = "",
    // Hospital Details
    val hospitalName: String = "",
    val department: String = "",
    val staffName: String = "",
    val staffContact: String = "",
    // Location & Notification
    val hospitalLocation: String = "",
    val hospitalLatitude: Double = 0.0,
    val hospitalLongitude: Double = 0.0,
    val eyeBankName: String = "",
    val eyeBankAddress: String = "",
    val eyeBankLatitude: Double = 0.0,
    val eyeBankLongitude: Double = 0.0,
    // Meta
    val isDetailsCorrect: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "pending" // pending, notified, completed
) : Parcelable


class DonorRegistrationCrud {

    companion object {
        private val donorRef = FirebaseFirestore.getInstance().collection("DonorRegistrations")

        // Add a donor registration to Firestore
        suspend fun addDonorRegistration(donor: DonorRegistration): String {
            return try {
                val docRef = donorRef.document()
                val donorWithId = donor.copy(id = docRef.id)
                docRef.set(donorWithId).await()
                Log.d("DonorRegistrationCrud", "Donor registration successful with ID: ${docRef.id}")
                docRef.id
            } catch (e: Exception) {
                Log.e("DonorRegistrationCrud", "Error registering donor: ${e.message}")
                throw e
            }
        }

        // Retrieve a donor registration by ID
        suspend fun getDonorById(id: String): DonorRegistration? {
            return try {
                val document = donorRef.document(id).get().await()
                if (document.exists()) {
                    document.toObject(DonorRegistration::class.java)
                } else {
                    Log.d("DonorRegistrationCrud", "No donor found for ID: $id")
                    null
                }
            } catch (e: Exception) {
                Log.e("DonorRegistrationCrud", "Error retrieving donor: ${e.message}")
                null
            }
        }

        // Retrieve all donor registrations from Firestore
        suspend fun getAllDonorRegistrations(): List<DonorRegistration> {
            return try {
                val result = donorRef.get().await()
                result.toObjects(DonorRegistration::class.java)
            } catch (e: Exception) {
                Log.e("DonorRegistrationCrud", "Error retrieving donor registrations: ${e.message}")
                emptyList()
            }
        }

        // Get donor registrations by status
        suspend fun getDonorsByStatus(status: String): List<DonorRegistration> {
            return try {
                val result = donorRef.whereEqualTo("status", status).get().await()
                result.toObjects(DonorRegistration::class.java)
            } catch (e: Exception) {
                Log.e("DonorRegistrationCrud", "Error retrieving donors by status: ${e.message}")
                emptyList()
            }
        }

        // Update donor registration status
        suspend fun updateDonorStatus(id: String, status: String) {
            try {
                donorRef.document(id).update("status", status).await()
                Log.d("DonorRegistrationCrud", "Donor status updated to: $status")
            } catch (e: Exception) {
                Log.e("DonorRegistrationCrud", "Error updating donor status: ${e.message}")
            }
        }

        // Delete donor registration
        suspend fun deleteDonorRegistration(id: String) {
            try {
                donorRef.document(id).delete().await()
                Log.d("DonorRegistrationCrud", "Donor registration successfully deleted")
            } catch (e: Exception) {
                Log.e("DonorRegistrationCrud", "Error deleting donor registration: ${e.message}")
            }
        }
    }
}
