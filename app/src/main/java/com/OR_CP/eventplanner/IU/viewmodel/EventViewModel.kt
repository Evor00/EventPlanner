package com.OR_CP.eventplanner.IU.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.OR_CP.eventplanner.data.model.Event
import com.OR_CP.eventplanner.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EventViewModel(
    private val repository: EventRepository
) : ViewModel() {

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    init {
        loadEvents()
    }

    // -------------------------------
    //  LISTADO EN TIEMPO REAL
    // -------------------------------
    private fun loadEvents() {
        viewModelScope.launch {
            repository.getEventsRealTime()
                .catch { e -> _message.value = "Error: ${e.message}" }
                .collectLatest { list ->
                    _events.value = list
                }
        }
    }

    // -------------------------------
    //  CREAR EVENTO
    // -------------------------------
    fun createEvent(event: Event) {
        viewModelScope.launch {
            val result = repository.createEvent(event)
            _message.value = result.fold(
                onSuccess = { "Evento creado" },
                onFailure = { "Error al crear: ${it.message}" }
            )
        }
    }

    // -------------------------------
    //  ACTUALIZAR EVENTO
    // -------------------------------
    fun updateEvent(event: Event) {
        viewModelScope.launch {
            val result = repository.updateEvent(event)
            _message.value = result.fold(
                onSuccess = { "Evento actualizado" },
                onFailure = { "Error al actualizar: ${it.message}" }
            )
        }
    }

    // -------------------------------
    //  ELIMINAR EVENTO
    // -------------------------------
    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            val result = repository.deleteEvent(eventId)
            _message.value = result.fold(
                onSuccess = { "Evento eliminado" },
                onFailure = { "Error al eliminar: ${it.message}" }
            )
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
