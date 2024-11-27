package com.example.deep.model

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class Donor(
    val donorName: String = "",
    val age: Int = 0,
    val gender: String = "",
    val familyMemberName: String = "",
    val fullAddress: String = "",
    val dateTimeOfDeath: String = "",
    val contactNumber: String = ""
)


class DonorCrud {

    companion object {
        private val donorref = FirebaseFirestore.getInstance().collection("Donors")

        suspend fun addDonor(donor: Donor) {
            try {

                donorref.document(donor.contactNumber).set(donor).await()
            } catch (e: Exception) {
                println("Error adding donor registration: ${e.message}")
            }
        }

        // Retrieve a pledge by contact number
        suspend fun getPledgeByContactNumber(contactNumber: String): Donor? {
            return try {
                val document = donorref.document(contactNumber).get().await()
                if (document.exists()) {
                    document.toObject(Donor::class.java)
                } else {
                    println("No donor found for the contact number: $contactNumber")
                    null
                }
            } catch (e: Exception) {
                println("Error retrieving donor: ${e.message}")
                null
            }
        }

        // Retrieve all pledges from Firestore
        suspend fun getAllDonors(): List<Donor> {
            return try {
                val result = donorref.get().await()
                result.toObjects(Donor::class.java)
            } catch (e: Exception) {
                println("Error retrieving all donors: ${e.message}")
                emptyList()
            }
        }

        suspend fun pledgeExists(contactNumber: String): Boolean {

            return try {
                val document = donorref.document(contactNumber).get().await()
                document.exists()
            } catch (e: Exception) {
                println("Error checking pledge existence: ${e.message}")
                false
            }
        }


    }
}