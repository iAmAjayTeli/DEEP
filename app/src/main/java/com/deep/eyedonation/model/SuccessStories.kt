package com.deep.eyedonation.model

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class Stories(
    val id: String? = null,
    val comments: String = "",
    val date: String = "",
    val rating: Float = 0.0f,
    val personName: String = ""
)


class SuccessStoriesCrud {
    companion object {
        private val storyRef = FirebaseFirestore.getInstance().collection("SuccessStories")

        suspend fun addStory(story: Stories) {
            try {
                val storyData = mapOf(
                    "comments" to story.comments,
                    "date" to story.date,
                    "rating" to story.rating,
                    "personName" to story.personName
                )
                val newStoryRef = storyRef.add(storyData).await()
                val storyWithId = story.copy(id = newStoryRef.id)
                newStoryRef.set(storyWithId).await()
                println("Story successfully added with ID: ${newStoryRef.id}")
            } catch (e: Exception) {
                println("Error adding story: ${e.message}")
            }
        }

        suspend fun getStories(): List<Stories> {
            return try {
                val result = storyRef.get().await()
                result.documents.mapNotNull { doc ->
                    doc.toObject(Stories::class.java)
                }

            } catch (e: Exception) {
                Log.e("SuccessStoriesCrud", "Error retrieving stories: ${e.message}")
                emptyList()
            }

        }

        suspend fun deleteStory(storyId: String) {
            try {
                storyRef.document(storyId).delete().await()
                println("Story successfully deleted")
            } catch (e: Exception) {
                println("Error deleting story: ${e.message}")
            }
        }

    }
}