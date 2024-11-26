package com.example.deep.model

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await



data class Pledges(
    val personalDetails: PersonalDetails,
    val emergencyContactDetails: EmergencyContactDetails,
    val eyeBankDetails: EyeBankDetails,
    val pledgeConfirmation: Boolean
)

data class PersonalDetails(
    val fullName:String,
    val email:String,
    val bloodGroup: String,
    val dateOfBirth:String,
    val mobileNumber: String,
    val district: String,
    val pinCode: String,
    val age:String,
    val address: String

)

data class EmergencyContactDetails(
    val emergencyContactName: String,
    val emergencyContactNumber: String,
    val emergencyAddress: String

)

data class EyeBankDetails(
    val eyeBankState: String,
    val eyeBankName: String,
    val eyeBankContact: String,
    val eyeBankDistrict: String
)


class PledgeCrud {

    companion object {
        private val pledgeRef = FirebaseFirestore.getInstance().collection("Pledge")

        // Add Pledge
        suspend fun addPledge(pledge: Pledges) {
            try {
                val emailId = pledge.personalDetails.email // Get email from PersonalDetails
                val pledgeData = mapOf(
                    "personalDetails" to pledge.personalDetails.toMap(),
                    "emergencyContactDetails" to pledge.emergencyContactDetails.toMap(),
                    "eyeBankDetails" to pledge.eyeBankDetails.toMap(),
                    "pledgeConfirmation" to pledge.pledgeConfirmation
                )
                pledgeRef.document(emailId).set(pledgeData).await()
                println("Pledge successfully added")
            } catch (e: Exception) {
                println("Error adding pledge: ${e.message}")
            }
        }

        // Get All Pledges
        suspend fun getPledges(): List<Pledges> {
            return try {
                val result = pledgeRef.get().await()
                result.documents.mapNotNull { doc ->
                    doc.toPledges()
                }
            } catch (e: Exception) {
                println("Error retrieving pledges: ${e.message}")
                emptyList()
            }
        }

        // Get Pledge by Email
        suspend fun getPledgeByEmail(emailId: String): Pledges? {
            return try {
                val document = pledgeRef.document(emailId).get().await()
                if (document.exists()) {
                    document.toPledges()
                } else {
                    println("No pledge found for the email ID: $emailId")
                    null
                }
            } catch (e: Exception) {
                println("Error retrieving pledge: ${e.message}")
                null
            }
        }

        // Check if Pledge Exists
        suspend fun pledgeExists(emailId: String): Boolean {
            return try {
                val document = pledgeRef.document(emailId).get().await()
                document.exists()
            } catch (e: Exception) {
                println("Error checking pledge existence: ${e.message}")
                false
            }
        }
    }
}

// Extensions to Map Data Classes
fun PersonalDetails.toMap(): Map<String, String> {
    return mapOf(
        "fullName" to fullName,
        "email" to email,
        "bloodGroup" to bloodGroup,
        "dateOfBirth" to dateOfBirth,
        "mobileNumber" to mobileNumber,
        "district" to district,
        "pinCode" to pinCode,
        "age" to age,
        "address" to address
    )
}

fun EmergencyContactDetails.toMap(): Map<String, String> {
    return mapOf(
        "emergencyContactName" to emergencyContactName,
        "emergencyContactNumber" to emergencyContactNumber,
        "emergencyAddress" to emergencyAddress
    )
}

fun EyeBankDetails.toMap(): Map<String, String> {
    return mapOf(
        "eyeBankState" to eyeBankState,
        "eyeBankName" to eyeBankName,
        "eyeBankContact" to eyeBankContact,
        "eyeBankDistrict" to eyeBankDistrict
    )
}

// Extensions to Convert Firestore Documents to Objects
fun DocumentSnapshot.toPledges(): Pledges? {
    val personalDetails = (get("personalDetails") as? Map<*, *>)?.let {
        PersonalDetails(
            fullName = it["fullName"] as? String ?: "",
            email = it["email"] as? String ?: "",
            bloodGroup = it["bloodGroup"] as? String ?: "",
            dateOfBirth = it["dateOfBirth"] as? String ?: "",
            mobileNumber = it["mobileNumber"] as? String ?: "",
            district = it["district"] as? String ?: "",
            pinCode = it["pinCode"] as? String ?: "",
            age = it["age"] as? String ?: "",
            address = it["address"] as? String ?: ""
        )
    }

    val emergencyContactDetails = (get("emergencyContactDetails") as? Map<*, *>)?.let {
        EmergencyContactDetails(
            emergencyContactName = it["emergencyContactName"] as? String ?: "",
            emergencyContactNumber = it["emergencyContactNumber"] as? String ?: "",
            emergencyAddress = it["emergencyAddress"] as? String ?: ""
        )
    }

    val eyeBankDetails = (get("eyeBankDetails") as? Map<*, *>)?.let {
        EyeBankDetails(
            eyeBankState = it["eyeBankState"] as? String ?: "",
            eyeBankName = it["eyeBankName"] as? String ?: "",
            eyeBankContact = it["eyeBankContact"] as? String ?: "",
            eyeBankDistrict = it["eyeBankDistrict"] as? String ?: ""
        )
    }

    val pledgeConfirmation = getBoolean("pledgeConfirmation") ?: false

    return if (personalDetails != null && emergencyContactDetails != null && eyeBankDetails != null) {
        Pledges(personalDetails, emergencyContactDetails, eyeBankDetails, pledgeConfirmation)
    } else null
}
