package com.example.patients.data.model

import kotlinx.serialization.Serializable

// Patient reçu depuis l'API
@Serializable
data class Patient(
    val id: String,
    val nom: String,
    val prenom: String,
    val age: Int? = null,
    val telephone: String? = null,
    val medecin: String? = null,
    val statut: String = "Actif"
)

// Données envoyées pour créer un patient
@Serializable
data class PatientRequest(
    val nom: String,
    val prenom: String,
    val age: Int? = null,
    val telephone: String? = null,
    val medecin: String? = null
)

// Réponse d'erreur de l'API
@Serializable
data class ApiError(
    val message: String,
    val erreurs: List<String> = emptyList()
)