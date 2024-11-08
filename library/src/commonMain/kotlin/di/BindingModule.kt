package com.bazaartech.core_network.di

import com.bazaartech.core_network.event.EventHelperImp
import com.bazaartech.core_network.event.EventsHelper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingModule {

    @Binds
    internal abstract fun bindEventHelper(eventHelperImp: EventHelperImp): EventsHelper
}
