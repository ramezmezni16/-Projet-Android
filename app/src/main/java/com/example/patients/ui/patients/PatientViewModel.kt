
package com.example.patients.ui.patients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.patients.PatientsApplication
import com.example.patients.data.model.Patient
import com.example.patients.data.model.PatientRequest
import com.example.patients.data.repository.PatientRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PatientViewModel(
    private val repository: PatientRepository
) : ViewModel() {

    // État de l'écran
    private val _uiState =
        MutableStateFlow<PatientsUiState>(PatientsUiState.Loading)

    val uiState: StateFlow<PatientsUiState> =
        _uiState.asStateFlow()

    // Messages ponctuels pour les Snackbars
    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    init {
        loadPatients()
    }

    // Charger la liste des patients
    fun loadPatients() {
        _uiState.value = PatientsUiState.Loading

        viewModelScope.launch {
            repository.getPatients()
                .onSuccess { patients ->
                    _uiState.value = PatientsUiState.Success(patients)
                }
                .onFailure { e ->
                    _uiState.value = PatientsUiState.Error(
                        e.message ?: "Erreur inconnue"
                    )
                }
        }
    }

    // Ajouter un patient
    fun addPatient(request: PatientRequest) {
        viewModelScope.launch {
            repository.createPatient(request)
                .onSuccess { patient ->
                    updateList { it + patient }

                    _messages.emit(
                        "${patient.prenom} ${patient.nom} ajouté"
                    )
                }
                .onFailure { e ->
                    _messages.emit(
                        e.message ?: "Ajout impossible"
                    )
                }
        }
    }

    // Supprimer un patient
    fun deletePatient(patient: Patient) {
        viewModelScope.launch {
            repository.deletePatient(patient.id)
                .onSuccess {
                    updateList { list ->
                        list.filterNot { it.id == patient.id }
                    }

                    _messages.emit(
                        "${patient.prenom} ${patient.nom} supprimé"
                    )
                }
                .onFailure { e ->
                    _messages.emit(
                        e.message ?: "Suppression impossible"
                    )
                }
        }
    }
    // Modifier un patient
    fun updatePatient(
        patient: Patient,
        request: PatientRequest
    ) {
        viewModelScope.launch {
            repository.updatePatient(patient.id, request)
                .onSuccess { updatedPatient ->
                    updateList { list ->
                        list.map {
                            if (it.id == updatedPatient.id) updatedPatient else it
                        }
                    }

                    _messages.emit(
                        "${updatedPatient.prenom} ${updatedPatient.nom} modifié"
                    )
                }
                .onFailure { e ->
                    _messages.emit(
                        e.message ?: "Modification impossible"
                    )
                }
        }
    }

    // Modifier la liste affichée
    private fun updateList(
        transform: (List<Patient>) -> List<Patient>
    ) {
        _uiState.update { state ->
            if (state is PatientsUiState.Success) {
                state.copy(
                    patients = transform(state.patients)
                )
            } else {
                state
            }
        }
    }

    // Factory du ViewModel
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as PatientsApplication

                PatientViewModel(
                    app.container.patientRepository
                )
            }
        }
    }
}
