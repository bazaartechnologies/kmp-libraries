package com.tech.bazaar.kmp.app.data.repository

import com.tech.bazaar.kmp.app.data.CategoriesResponseModelV2
import com.tech.bazaar.kmp.app.data.GatewayService
import com.tech.bazaar.network.api.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CategoryRepository(val gatewayService: GatewayService = GatewayService()) {

      fun getCategories(): Flow<List<CategoriesResponseModelV2.CategoryDto>> = flow{
        val categoriesResponse = gatewayService.getCategories()
           when(categoriesResponse){
              is ResultState.Error -> emit(listOf())
              is ResultState.Success -> emit(categoriesResponse.data.categories)
          }
    }

}