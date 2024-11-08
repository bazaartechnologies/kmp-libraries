package com.bazaartech.core_network.di

import com.bazaartech.core_network.event.EventHelperImp
import com.bazaartech.core_network.event.EventsHelper
import org.koin.dsl.module

val bindingModule = module {
    single<EventsHelper> { EventHelperImp(get()) }
}