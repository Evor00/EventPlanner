package com.OR_CP.eventplanner.IU.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.OR_CP.eventplanner.data.model.Event

@Composable
fun EventListScreen(navController: NavController) {

    val sampleEvents = listOf(
        Event("1", "Viaje a Cusco", "Un viaje increíble", "20/12/2025"),
        Event("2", "Fiesta en la playa", "Mancora Baby!", "10/02/2026")
    )

    Column(modifier = Modifier.padding(20.dp)) {

        Text("Mis Eventos", style = MaterialTheme.typography.headlineLarge)

        sampleEvents.forEach { event ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = event.title, style = MaterialTheme.typography.titleLarge)
                    Text(event.date)
                    Text(event.description)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("event_form") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear Evento")
        }
    }
}
