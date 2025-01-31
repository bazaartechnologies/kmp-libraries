package com.tech.bazaar.kmp.app.data.repository

import com.tech.bazaar.kmp.app.data.CategoriesResponseModel
import com.tech.bazaar.kmp.app.data.GatewayService
import com.tech.bazaar.network.api.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CategoryRepository(private val gatewayService: GatewayService) {
    fun getCategories(): Flow<List<CategoriesResponseModel.CategoryDto>> = flow {
        when (val categoriesResponse = gatewayService.getCategories()) {
            is ResultState.Error -> emit(listOf())
            is ResultState.Success -> emit(categoriesResponse.data.categories)
        }
    }
}