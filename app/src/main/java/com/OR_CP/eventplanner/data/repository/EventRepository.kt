package com.OR_CP.eventplanner.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.snapshots
import com.OR_CP.eventplanner.data.model.Event
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class EventRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val eventsCollection = firestore.collection("events")

    suspend fun createEvent(event: Event): Result<String>{
        return try {
            if (!event.isValid()) {
                return Result.failure(IllegalArgumentException("Datos del evento inválidos"))
            }

            val docRef = eventsCollection.document()
            val eventWithId = event.copy(id = docRef.id)

            docRef.set(eventWithId).await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getEventsRealTime(): Flow<List<Event>> {
        return eventsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Event::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
    }

    suspend fun getEventById(eventId: String): Result<Event?> {
        return try {
            val document = eventsCollection.document(eventId).get().await()
            val event = document.toObject(Event::class.java)
            Result.success(event)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEventsByUser(userId: String): Result<List<Event>> {
        return try {
            val snapshot = eventsCollection
                .whereEqualTo("createdBy", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val events = snapshot.documents.mapNotNull {
                it.toObject(Event::class.java)
            }

            Result.success(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchEventsByTitle(query: String): Result<List<Event>> {
        return try {
            val snapshot = eventsCollection.get().await()
            val events = snapshot.documents.mapNotNull {
                it.toObject(Event::class.java)
            }.filter {
                it.title.contains(query, ignoreCase = true)
            }

            Result.success(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateEvent(event: Event): Result<Unit> {
        return try {
            if (event.id.isEmpty()) {
                return Result.failure(IllegalArgumentException("El evento debe tener un ID"))
            }

            if (!event.isValid()) {
                return Result.failure(IllegalArgumentException("Datos del evento inválidos"))
            }

            eventsCollection.document(event.id).set(event).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateEventFields(eventId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            eventsCollection.document(eventId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteEvent(eventId: String): Result<Unit> {
        return try {
            if (eventId.isEmpty()) {
                return Result.failure(IllegalArgumentException("ID de evento inválido"))
            }

            eventsCollection.document(eventId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addParticipant(eventId: String, userId: String): Result<Unit> {
        return try {
            val eventResult = getEventById(eventId)
            eventResult.getOrNull()?.let { event ->
                val updatedEvent = event.addParticipant(userId)
                updateEvent(updatedEvent)
            } ?: Result.failure(Exception("Evento no encontrado"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeParticipant(eventId: String, userId: String): Result<Unit> {
        return try {
            val eventResult = getEventById(eventId)
            eventResult.getOrNull()?.let { event ->
                val updatedEvent = event.removeParticipant(userId)
                updateEvent(updatedEvent)
            } ?: Result.failure(Exception("Evento no encontrado"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}