// presentation/viewmodel/UsersAdminViewModel.kt
package com.motosapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motosapp.domain.model.User
import com.motosapp.domain.model.UserPayload
import com.motosapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

enum class UserRoleFilter(val label: String) {
    ALL("Todos"),
    CLIENTS("Clientes"),
    STAFF("Staff"),
    ACTIVE("Activos"),
    INACTIVE("Inactivos"),
}

data class UsersAdminUiState(
    val users:      List<User>     = emptyList(),
    val isLoading:  Boolean        = false,
    val isPaginating: Boolean      = false,
    val error:      String?        = null,
    val total:      Int            = 0,
    val search:     String         = "",
    val roleFilter: UserRoleFilter = UserRoleFilter.ALL,
    val page:       Int            = 1,
    val hasMore:    Boolean        = false
)

sealed interface UserFormState {
    data object Idle                       : UserFormState
    data object Saving                     : UserFormState
    data class  Success(val msg: String)   : UserFormState
    data class  Error(val message: String) : UserFormState
}

@HiltViewModel
class UsersAdminViewModel @Inject constructor(
    private val repository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UsersAdminUiState())
    val state: StateFlow<UsersAdminUiState> = _state.asStateFlow()

    private val _formState = MutableStateFlow<UserFormState>(UserFormState.Idle)
    val formState: StateFlow<UserFormState> = _formState.asStateFlow()

    val filtered: StateFlow<List<User>> = _state
        .map { it.users }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private var searchJob: Job? = null

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, page = 1) }
            val s = _state.value
            
            val isStaff = when (s.roleFilter) {
                UserRoleFilter.STAFF -> true
                UserRoleFilter.CLIENTS -> false
                else -> null
            }
            val isActive = when (s.roleFilter) {
                UserRoleFilter.ACTIVE -> true
                UserRoleFilter.INACTIVE -> false
                else -> null
            }
            val currentSearch = s.search.ifBlank { null }
            
            repository.getUsers(search = currentSearch, isStaff = isStaff, isActive = isActive, page = 1)
                .onSuccess { (users, total) ->
                    _state.update { 
                        it.copy(
                            users = users, 
                            total = total, 
                            isLoading = false,
                            hasMore = users.size < total
                        ) 
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun loadMore() {
        val s = _state.value
        if (s.isLoading || s.isPaginating || !s.hasMore) return
        
        viewModelScope.launch {
            _state.update { it.copy(isPaginating = true, error = null) }
            val nextPage = s.page + 1
            
            val isStaff = when (s.roleFilter) {
                UserRoleFilter.STAFF -> true
                UserRoleFilter.CLIENTS -> false
                else -> null
            }
            val isActive = when (s.roleFilter) {
                UserRoleFilter.ACTIVE -> true
                UserRoleFilter.INACTIVE -> false
                else -> null
            }
            val currentSearch = s.search.ifBlank { null }
            
            repository.getUsers(search = currentSearch, isStaff = isStaff, isActive = isActive, page = nextPage)
                .onSuccess { (newItems, total) ->
                    _state.update { 
                        val updatedList = it.users + newItems
                        it.copy(
                            users = updatedList,
                            total = total,
                            page = nextPage,
                            isPaginating = false,
                            hasMore = updatedList.size < total
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isPaginating = false, error = e.message) }
                }
        }
    }

    fun setSearch(query: String) {
        _state.update { it.copy(search = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            load()
        }
    }

    fun setRoleFilter(filter: UserRoleFilter) {
        _state.update { it.copy(roleFilter = filter) }
        load()
    }

    fun toggleStaff(id: Int, isStaff: Boolean) {
        _state.update { s ->
            s.copy(users = s.users.map { u ->
                if (u.id == id) u.copy(isStaff = isStaff) else u
            })
        }
        viewModelScope.launch {
            val user = _state.value.users.first { it.id == id }
            repository.updateUser(id, UserPayload(
                username  = user.username,
                email     = user.email,
                firstName = user.firstName,
                lastName  = user.lastName,
                isStaff   = isStaff,
                isActive  = user.isActive,
            )).onFailure {
                _state.update { s ->
                    s.copy(users = s.users.map { u ->
                        if (u.id == id) u.copy(isStaff = !isStaff) else u
                    })
                }
            }
        }
    }

    fun toggleActive(id: Int) {
        val user = _state.value.users.find { it.id == id } ?: return
        val next = !user.isActive
        _state.update { s ->
            s.copy(users = s.users.map { u ->
                if (u.id == id) u.copy(isActive = next) else u
            })
        }
        viewModelScope.launch {
            repository.toggleActive(id)
                .onSuccess { serverActive ->
                    _state.update { s ->
                        s.copy(users = s.users.map { u ->
                            if (u.id == id) u.copy(isActive = serverActive) else u
                        })
                    }
                }
                .onFailure {
                    _state.update { s ->
                        s.copy(users = s.users.map { u ->
                            if (u.id == id) u.copy(isActive = !next) else u
                        })
                    }
                }
        }
    }

    fun createUser(payload: UserPayload) {
        _formState.value = UserFormState.Saving
        viewModelScope.launch {
            repository.createUser(payload)
                .onSuccess { created ->
                    _state.update { s ->
                        s.copy(users = listOf(created) + s.users, total = s.total + 1)
                    }
                    _formState.value = UserFormState.Success("Usuario creado")
                }
                .onFailure { e ->
                    _formState.value = UserFormState.Error(e.message ?: "Error al crear")
                }
        }
    }

    fun updateUser(id: Int, payload: UserPayload) {
        _formState.value = UserFormState.Saving
        viewModelScope.launch {
            repository.updateUser(id, payload)
                .onSuccess { updated ->
                    _state.update { s ->
                        s.copy(users = s.users.map { if (it.id == id) updated else it })
                    }
                    _formState.value = UserFormState.Success("Usuario actualizado")
                }
                .onFailure { e ->
                    _formState.value = UserFormState.Error(e.message ?: "Error al actualizar")
                }
        }
    }

    fun deleteUser(id: Int) {
        viewModelScope.launch {
            repository.deleteUser(id)
                .onSuccess {
                    _state.update { s ->
                        s.copy(users = s.users.filter { it.id != id }, total = s.total - 1)
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = e.message) }
                }
        }
    }

    fun resetFormState() { _formState.value = UserFormState.Idle }
}