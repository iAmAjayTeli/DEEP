package com.deep.eyedonation.model

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Model for registered eye banks with contact details
 * This collection stores eye banks that have registered in the system
 * with their verified contact information including email
 */
data class RegisteredEyeBank(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val city: String = "",
    val state: String = "",
    val isVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

class RegisteredEyeBankCrud {
    companion object {
        private val eyeBankRef = FirebaseFirestore.getInstance().collection("RegisteredEyeBanks")

        /**
         * Find eye bank by name (partial match)
         */
        suspend fun findEyeBankByName(name: String): RegisteredEyeBank? {
            return try {
                // First try exact match
                var result = eyeBankRef.whereEqualTo("name", name).get().await()
                if (!result.isEmpty) {
                    return result.documents.firstOrNull()?.toObject(RegisteredEyeBank::class.java)
                }

                // If no exact match, get all and do partial match
                result = eyeBankRef.get().await()
                val allBanks = result.toObjects(RegisteredEyeBank::class.java)
                
                // Find best match (contains name or name contains search term)
                allBanks.find { 
                    it.name.contains(name, ignoreCase = true) || 
                    name.contains(it.name, ignoreCase = true) 
                }
            } catch (e: Exception) {
                Log.e("RegisteredEyeBankCrud", "Error finding eye bank: ${e.message}")
                null
            }
        }

        /**
         * Find eye bank by location (nearest)
         */
        suspend fun findNearestEyeBank(latitude: Double, longitude: Double): RegisteredEyeBank? {
            return try {
                val result = eyeBankRef.get().await()
                val allBanks = result.toObjects(RegisteredEyeBank::class.java)
                
                // Find nearest bank
                allBanks.minByOrNull { bank ->
                    val latDiff = bank.latitude - latitude
                    val lngDiff = bank.longitude - longitude
                    latDiff * latDiff + lngDiff * lngDiff // Simple distance calculation
                }
            } catch (e: Exception) {
                Log.e("RegisteredEyeBankCrud", "Error finding nearest eye bank: ${e.message}")
                null
            }
        }

        /**
         * Get all registered eye banks
         */
        suspend fun getAllEyeBanks(): List<RegisteredEyeBank> {
            return try {
                val result = eyeBankRef.get().await()
                result.toObjects(RegisteredEyeBank::class.java)
            } catch (e: Exception) {
                Log.e("RegisteredEyeBankCrud", "Error getting eye banks: ${e.message}")
                emptyList()
            }
        }

        /**
         * Add a new eye bank (for admin use)
         */
        suspend fun addEyeBank(eyeBank: RegisteredEyeBank): String {
            return try {
                val docRef = eyeBankRef.document()
                val bankWithId = eyeBank.copy(id = docRef.id)
                docRef.set(bankWithId).await()
                docRef.id
            } catch (e: Exception) {
                Log.e("RegisteredEyeBankCrud", "Error adding eye bank: ${e.message}")
                throw e
            }
        }

        /**
         * Add sample eye banks for testing
         */
        suspend fun addSampleEyeBanks() {
            val sampleBanks = listOf(
                RegisteredEyeBank(
                    name = "SNM Medical and Eye Care Centre",
                    email = "snmeyecare@gmail.com",
                    phone = "9876543210",
                    address = "1st Floor, Bright complex, 17, Buddha vihar road, pulakeshinagar, Frazer Town",
                    city = "Bengaluru",
                    state = "Karnataka",
                    latitude = 12.9916,
                    longitude = 77.6117,
                    isVerified = true
                ),
                RegisteredEyeBank(
                    name = "Nethradhama Super Speciality Eye Hospital",
                    email = "info@nethradhama.org",
                    phone = "9988776655",
                    address = "Landmark: Madurai Idly Shop, 29, 80 Feet Rd, Hal, HAL 3rd Stage, Indiranagar",
                    city = "Bengaluru",
                    state = "Karnataka",
                    latitude = 12.9784,
                    longitude = 77.6408,
                    isVerified = true
                ),
                RegisteredEyeBank(
                    name = "Minto Eye Hospital",
                    email = "mintoeyebank@gmail.com",
                    phone = "9123456789",
                    address = "Chamrajpet, Bengaluru",
                    city = "Bengaluru",
                    state = "Karnataka",
                    latitude = 12.9592,
                    longitude = 77.5734,
                    isVerified = true
                )
            )

            for (bank in sampleBanks) {
                try {
                    // Check if already exists
                    val existing = findEyeBankByName(bank.name)
                    if (existing == null) {
                        addEyeBank(bank)
                        Log.d("RegisteredEyeBankCrud", "Added sample eye bank: ${bank.name}")
                    }
                } catch (e: Exception) {
                    Log.e("RegisteredEyeBankCrud", "Error adding sample bank: ${e.message}")
                }
            }
        }
    }
}
