package com.OR_CP.eventplanner.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Estados posibles de la autenticación
 *
 * Sealed class que representa todos los estados posibles durante
 * el proceso de autenticación (login/registro)
 */
sealed class AuthState {
    /** Estado inicial, sin operación en progreso */
    data object Idle : AuthState()

    /** Operación de autenticación en progreso */
    data object Loading : AuthState()

    /** Autenticación exitosa */
    data class Success(val userId: String, val email: String?) : AuthState()

    /** Error durante la autenticación */
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    // Instancia de Firebase Authentication
    private val auth = FirebaseAuth.getInstance()

    // Estado mutable privado (solo el ViewModel puede modificarlo)
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)

    // Estado público inmutable (la UI solo puede leerlo)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Usuario actualmente autenticado (null si no hay sesión)
    val currentUser: FirebaseUser? get() = auth.currentUser

    // Verificar si hay un usuario logueado
    val isUserLoggedIn: Boolean get() = currentUser != null

    init {
        // Verificar si hay una sesión activa al iniciar el ViewModel
        checkCurrentUser()
    }

    /**
     * Verifica si hay un usuario con sesión activa
     * Útil para decidir si mostrar Login o Home al iniciar la app
     */
    private fun checkCurrentUser() {
        currentUser?.let { user ->
            _authState.value = AuthState.Success(
                userId = user.uid,
                email = user.email
            )
        }
    }

    fun login(email: String, password: String) {
        // Validación de email
        if (!isValidEmail(email)) {
            _authState.value = AuthState.Error("El formato del email es inválido")
            return
        }

        // Validación de contraseña
        if (!isValidPassword(password)) {
            _authState.value = AuthState.Error("La contraseña debe tener al menos 6 caracteres")
            return
        }

        // Lanzar coroutine en el scope del ViewModel
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            try {
                // Intentar login con Firebase (suspende hasta completar)
                val result = auth.signInWithEmailAndPassword(email, password).await()

                // Obtener datos del usuario autenticado
                val user = result.user

                if (user != null) {
                    _authState.value = AuthState.Success(
                        userId = user.uid,
                        email = user.email
                    )
                } else {
                    _authState.value = AuthState.Error("Error al obtener datos del usuario")
                }

            } catch (e: FirebaseAuthException) {
                // Manejar errores específicos de Firebase Auth
                _authState.value = AuthState.Error(getAuthErrorMessage(e))

            } catch (e: Exception) {
                // Manejar otros errores (red, etc.)
                _authState.value = AuthState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    fun register(email: String, password: String, confirmPassword: String) {
        // Validación de email
        if (!isValidEmail(email)) {
            _authState.value = AuthState.Error("El formato del email es inválido")
            return
        }

        // Validación de contraseña
        if (!isValidPassword(password)) {
            _authState.value = AuthState.Error("La contraseña debe tener al menos 6 caracteres")
            return
        }

        // Validación de coincidencia de contraseñas
        if (password != confirmPassword) {
            _authState.value = AuthState.Error("Las contraseñas no coinciden")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading

            try {
                // Crear nuevo usuario en Firebase Auth
                val result = auth.createUserWithEmailAndPassword(email, password).await()

                val user = result.user

                if (user != null) {

                    _authState.value = AuthState.Success(
                        userId = user.uid,
                        email = user.email
                    )
                } else {
                    _authState.value = AuthState.Error("Error al crear la cuenta")
                }

            } catch (e: FirebaseAuthException) {
                _authState.value = AuthState.Error(getAuthErrorMessage(e))

            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    private fun getAuthErrorMessage(exception: FirebaseAuthException): String {
        return when (exception.errorCode) {
            "ERROR_INVALID_EMAIL" -> "El formato del email es inválido"
            "ERROR_WRONG_PASSWORD" -> "Contraseña incorrecta"
            "ERROR_USER_NOT_FOUND" -> "No existe una cuenta con este email"
            "ERROR_USER_DISABLED" -> "Esta cuenta ha sido deshabilitada"
            "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos fallidos. Intenta más tarde"
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Este email ya está registrado"
            "ERROR_WEAK_PASSWORD" -> "La contraseña es muy débil. Usa al menos 6 caracteres"
            "ERROR_INVALID_CREDENTIAL" -> "Credenciales inválidas"
            "ERROR_NETWORK_REQUEST_FAILED" -> "Error de conexión a internet"
            else -> "Error de autenticación: ${exception.message}"
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            if (!isValidEmail(email)) {
                return Result.failure(IllegalArgumentException("Email inválido"))
            }

            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateEmail(newEmail: String): Result<Unit> {
        return try {
            if (!isValidEmail(newEmail)) {
                return Result.failure(IllegalArgumentException("Email inválido"))
            }

            currentUser?.updateEmail(newEmail)?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePassword(newPassword: String): Result<Unit> {
        return try {
            if (!isValidPassword(newPassword)) {
                return Result.failure(IllegalArgumentException("Contraseña debe tener al menos 6 caracteres"))
            }

            currentUser?.updatePassword(newPassword)?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAccount(): Result<Unit> {
        return try {
            currentUser?.delete()?.await()
            _authState.value = AuthState.Idle
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}