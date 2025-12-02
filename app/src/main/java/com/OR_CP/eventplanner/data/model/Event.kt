package com.OR_CP.eventplanner.data.model

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val location: String = "",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val participants: List<String> = emptyList()
){
    fun isValid(): Boolean {
        return title.isNotBlank() &&
                date.isNotBlank() &&
                location.isNotBlank()
    }

    fun addParticipant(userId: String): Event {
        return if (userId !in participants) {
            copy(participants = participants + userId)
        } else {
            this
        }
    }

    fun removeParticipant(userId: String): Event {
        return copy(participants = participants.filter { it != userId })
    }
}