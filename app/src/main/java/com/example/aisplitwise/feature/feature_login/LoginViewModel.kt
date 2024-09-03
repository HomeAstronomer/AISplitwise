package com.example.aisplitwise.feature.feature_login

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aisplitwise.DashBoardRoute
import com.example.aisplitwise.data.local.Member
import com.example.aisplitwise.data.local.toMap
import com.example.aisplitwise.data.repository.DataState
import com.example.aisplitwise.data.repository.LoginRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@Immutable
data class LoginScreenUiState(
    val showToast:Boolean=false,
    val toastMessage:String=""

)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val fireStoreDb: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
): ViewModel() {

    private val _uiState = MutableStateFlow(LoginScreenUiState())
    val uiState: StateFlow<LoginScreenUiState> = _uiState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LoginScreenUiState()
    )

    fun signIn(email:String, password:String,
               onSuccess:(DashBoardRoute)->Unit) {
        viewModelScope.launch {
            loginRepository.firebaseAuthSignIn(email,password).collect{dataState->
                when(dataState){
                    is DataState.Success->{

                        val fireBaseUser=dataState.data.user
                        val route=DashBoardRoute(uid=fireBaseUser?.uid?:"",
                            displayName = fireBaseUser?.displayName,
                            email = fireBaseUser?.email,
                            phoneNumber = fireBaseUser?.phoneNumber,
                            photoUrl = fireBaseUser?.photoUrl?.path)
                        onSuccess.invoke(route)
                    }
                    is DataState.Error->{
                        _uiState.update { it.copy(showToast = true, toastMessage = dataState.errorMessage) }
                    }
                }
            }


        }

    }

    fun resetToast() {
        _uiState.update { it.copy(showToast = false, toastMessage ="") }
    }


}