package com.tech.bazaar.kmp.app.presentation

import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.stateIn
import com.tech.bazaar.kmp.app.data.CategoriesResponseModel
import com.tech.bazaar.kmp.app.data.GatewayService
import com.tech.bazaar.kmp.app.data.repository.CategoryRepository
import com.tech.bazaar.network.api.PlatformContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.single

class CategoriesViewModel(platformContext: PlatformContext) : ViewModel() {
    private val gatewayService: GatewayService = GatewayService(platformContext)
    private val categoryRepository = CategoryRepository(gatewayService)

    @NativeCoroutinesState
    val uiState: StateFlow<CategoriesUiState> =
        getCategories()
            .stateIn(
                viewModelScope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = CategoriesUiState.Loading
            )

    private fun getCategories(): Flow<CategoriesUiState> = flow {
        while (true) {
            emit(CategoriesUiState.Loading)

            val categories = categoryRepository.getCategories()
                .map { category ->
                    if (category.isEmpty()) CategoriesUiState.Error("No categories found")
                    else CategoriesUiState.Success(category)
                }
                .single() // Collect single latest result

            emit(categories)
            delay(1 * 60 * 1000) // Wait for 1 minute before next fetch
        }
    }
}

sealed interface CategoriesUiState {
    data object Loading : CategoriesUiState
    data class Success(val categories: List<CategoriesResponseModel.CategoryDto>) :
        CategoriesUiState

    data class Error(val message: String) : CategoriesUiState
}


