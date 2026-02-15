package com.deep.eyedonation.model

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class OurWork(
    val id: String? = null,
    val mediaUrl: String = "",
    val mediaType: String = "image" // "image" or "video"
)

class OurWorkCrud {
    companion object {
        private val workRef = FirebaseFirestore.getInstance().collection("OurWorks")

        suspend fun addWork(work: OurWork) {
            try {
                val workData = mapOf(
                    "mediaUrl" to work.mediaUrl,
                    "mediaType" to work.mediaType
                )
                val newWorkRef = workRef.add(workData).await()
                val workWithId = work.copy(id = newWorkRef.id)
                newWorkRef.set(workWithId).await()
                Log.d("OurWorkCrud", "Work added successfully with ID: ${newWorkRef.id}")
            } catch (e: Exception) {
                Log.e("OurWorkCrud", "Error adding work: ${e.message}")
            }
        }

        suspend fun getWorks(): List<OurWork> {
            return try {
                val snapshot = workRef.get().await()
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(OurWork::class.java)?.copy(id = doc.id)
                }
            } catch (e: Exception) {
                Log.e("OurWorkCrud", "Error getting works: ${e.message}")
                emptyList()
            }
        }

        suspend fun deleteWork(workId: String) {
            try {
                workRef.document(workId).delete().await()
                Log.d("OurWorkCrud", "Work deleted successfully")
            } catch (e: Exception) {
                Log.e("OurWorkCrud", "Error deleting work: ${e.message}")
            }
        }
    }
}
