package com.deep.eyedonation.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.deep.eyedonation.model.DonorRegistration
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Service to handle notifications to eye banks when a donor is registered
 */
object NotificationService {

    private const val TAG = "NotificationService"
    private val firestore = FirebaseFirestore.getInstance()
    private val notificationsRef = firestore.collection("EyeBankNotifications")

    /**
     * Data class for storing notification in Firestore
     */
    data class EyeBankNotification(
        val id: String = "",
        val donorRegistrationId: String = "",
        val eyeBankName: String = "",
        val eyeBankAddress: String = "",
        val eyeBankLatitude: Double = 0.0,
        val eyeBankLongitude: Double = 0.0,
        val hospitalName: String = "",
        val hospitalLocation: String = "",
        val hospitalContact: String = "",
        val donorName: String = "",
        val donorAge: String = "",
        val deathDateTime: String = "",
        val causeOfDeath: String = "",
        val urgencyLevel: String = "high", // Eye donation is time-sensitive (6 hours)
        val status: String = "pending", // pending, seen, accepted, rejected
        val createdAt: Long = System.currentTimeMillis(),
        val message: String = ""
    )

    /**
     * Creates a notification entry in Firestore for the eye bank
     * This can be fetched by the eye bank's app/dashboard
     */
    suspend fun createEyeBankNotification(
        donorRegistration: DonorRegistration,
        donorRegistrationId: String
    ): String {
        return try {
            val message = buildNotificationMessage(donorRegistration)
            
            val notification = EyeBankNotification(
                donorRegistrationId = donorRegistrationId,
                eyeBankName = donorRegistration.eyeBankName,
                eyeBankAddress = donorRegistration.eyeBankAddress,
                eyeBankLatitude = donorRegistration.eyeBankLatitude,
                eyeBankLongitude = donorRegistration.eyeBankLongitude,
                hospitalName = donorRegistration.hospitalName,
                hospitalLocation = donorRegistration.hospitalLocation,
                hospitalContact = donorRegistration.staffContact,
                donorName = donorRegistration.donorName,
                donorAge = donorRegistration.donorAge,
                deathDateTime = donorRegistration.deathDateTime,
                causeOfDeath = donorRegistration.causeOfDeath,
                message = message
            )

            val docRef = notificationsRef.document()
            val notificationWithId = notification.copy(id = docRef.id)
            docRef.set(notificationWithId).await()
            
            Log.d(TAG, "Notification created with ID: ${docRef.id}")
            docRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error creating notification: ${e.message}")
            throw e
        }
    }

    /**
     * Builds a formatted notification message
     */
    private fun buildNotificationMessage(donor: DonorRegistration): String {
        return """
            🚨 URGENT: Eye Donation Available
            
            Donor: ${donor.donorName} (Age: ${donor.donorAge})
            Time of Death: ${donor.deathDateTime}
            Cause: ${donor.causeOfDeath}
            
            Hospital: ${donor.hospitalName}
            Department: ${donor.department}
            Location: ${donor.hospitalLocation}
            
            Contact: ${donor.staffName}
            Phone: +91${donor.staffContact}
            
            ⏰ Time is critical - Eyes must be retrieved within 6 hours.
            
            Please respond immediately.
        """.trimIndent()
    }

    /**
     * Opens SMS app with pre-filled message to notify eye bank
     * This is a fallback/immediate notification method
     */
    fun sendSMSNotification(
        context: Context,
        phoneNumber: String,
        donorRegistration: DonorRegistration
    ) {
        val message = """
URGENT: Eye Donation at ${donorRegistration.hospitalName}

Donor: ${donorRegistration.donorName}, Age ${donorRegistration.donorAge}
Death: ${donorRegistration.deathDateTime}
Location: ${donorRegistration.hospitalLocation}

Contact: +91${donorRegistration.staffContact}

Please respond ASAP - 6hr window.
        """.trimIndent()

        val smsIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("sms:$phoneNumber")
            putExtra("sms_body", message)
        }
        
        try {
            context.startActivity(smsIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening SMS: ${e.message}")
        }
    }

    /**
     * Opens phone dialer to call the eye bank
     */
    fun callEyeBank(context: Context, phoneNumber: String) {
        val callIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        
        try {
            context.startActivity(callIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening dialer: ${e.message}")
        }
    }

    /**
     * Opens WhatsApp with pre-filled message (if available)
     */
    fun sendWhatsAppNotification(
        context: Context,
        phoneNumber: String,
        donorRegistration: DonorRegistration
    ) {
        val message = """
🚨 *URGENT: Eye Donation Available*

*Donor Details:*
• Name: ${donorRegistration.donorName}
• Age: ${donorRegistration.donorAge}
• Time of Death: ${donorRegistration.deathDateTime}
• Cause: ${donorRegistration.causeOfDeath}

*Hospital Details:*
• Hospital: ${donorRegistration.hospitalName}
• Department: ${donorRegistration.department}
• Location: ${donorRegistration.hospitalLocation}

*Contact Person:*
• Name: ${donorRegistration.staffName}
• Phone: +91${donorRegistration.staffContact}

⏰ *Time is critical - Eyes must be retrieved within 6 hours.*

Please respond immediately.
        """.trimIndent()

        // Format phone number for WhatsApp (remove + and spaces)
        val formattedNumber = "91$phoneNumber".replace("+", "").replace(" ", "")
        
        val whatsappIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://wa.me/$formattedNumber?text=${Uri.encode(message)}")
        }
        
        try {
            context.startActivity(whatsappIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening WhatsApp: ${e.message}")
            // Fallback to SMS if WhatsApp is not available
            sendSMSNotification(context, phoneNumber, donorRegistration)
        }
    }

    /**
     * Get all pending notifications for an eye bank (by name/location)
     */
    suspend fun getPendingNotificationsForEyeBank(eyeBankName: String): List<EyeBankNotification> {
        return try {
            val result = notificationsRef
                .whereEqualTo("eyeBankName", eyeBankName)
                .whereEqualTo("status", "pending")
                .get()
                .await()
            
            result.toObjects(EyeBankNotification::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching notifications: ${e.message}")
            emptyList()
        }
    }

    /**
     * Update notification status (when eye bank responds)
     */
    suspend fun updateNotificationStatus(notificationId: String, status: String) {
        try {
            notificationsRef.document(notificationId)
                .update("status", status)
                .await()
            Log.d(TAG, "Notification status updated to: $status")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating notification status: ${e.message}")
        }
    }
}
