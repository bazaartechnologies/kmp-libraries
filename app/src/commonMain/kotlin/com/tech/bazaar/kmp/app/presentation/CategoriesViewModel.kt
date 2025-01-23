package com.tech.bazaar.kmp.app.presentation

import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.stateIn
import com.tech.bazaar.kmp.app.data.CategoriesResponseModel
import com.tech.bazaar.kmp.app.data.repository.CategoryRepository
import com.tech.bazaar.network.api.PlatformContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

class CategoriesViewModel(platformContext: PlatformContext) : ViewModel() {
    private val categoryRepository = CategoryRepository(platformContext)

    @NativeCoroutinesState
    val uiState: StateFlow<CategoriesUiState> =
        categoryRepository.getCategories().map { category ->
            if (category.isEmpty()) CategoriesUiState.Error("No categories found")
            else {
                CategoriesUiState.Success(category)
            }
        }
            .stateIn(
                viewModelScope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = CategoriesUiState.Loading
            )


}

sealed interface CategoriesUiState {
    object Loading : CategoriesUiState
    data class Success(val categories: List<CategoriesResponseModel.CategoryDto>) :
        CategoriesUiState

    data class Error(val message: String) : CategoriesUiState
}


