package com.example.deep.model

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


data class Pledge(
    val name: String,
    val fathersName: String,
    val dateOfBirth: String,
    val age: Int,
    val mobileNumber: String,
    val emailId: String,
    val bloodGroup: String,
    val formFillingDate: String,
    val district: String,
    val address: String,
    val pinCode: String,
    val emergencyContactName: String,
    val emergencyContactNumber: String,
    val emergencyAddress: String,
    val eyeBankstate: String,
    val eyeBankName: String,
    val eyeBankContact: String,
    val eyeBankDistrict: String,
    val pledgeConfirmation: Boolean,
    val certificationConfirmation: Boolean
)


class PledgeCrud {

    companion object{
        private val pledgeref= FirebaseFirestore.getInstance().collection("Pledge")


        suspend fun addPledge(pledge: Pledge) {
            try {
                pledgeref.document(pledge.emailId).set(pledge).await()
                println("Pledge successfully added")
            } catch (e: Exception) {
                println("Error adding pledge: ${e.message}")
            }
        }

        suspend fun getPledges(): List<Pledge> {

            return try {
                val result = pledgeref.get().await() // Await the Firestore operation
                result.toObjects(Pledge::class.java) // Convert Firestore documents to Pledge objects
            } catch (e: Exception) {
                println("Error retrieving pledges: ${e.message}")
                emptyList() // Return an empty list in case of an error
            }

        }


        suspend fun getPledgeByEmail(emailId: String): Pledge? {
            return try {
                val document = pledgeref.document(emailId).get().await() // Get the document by emailId
                if (document.exists()) {
                    document.toObject(Pledge::class.java) // Convert Firestore document to Pledge object
                } else {
                    println("No pledge found for the email ID: $emailId")
                    null
                }
            } catch (e: Exception) {
                println("Error retrieving pledge: ${e.message}")
                null
            }
        }

        suspend fun pledgeExists(emailId: String): Boolean {
            return try {
                val document = pledgeref.document(emailId).get().await()
                document.exists()
            } catch (e: Exception) {
                println("Error checking pledge existence: ${e.message}")
                false
            }
        }


    }
}