package com.tech.bazaar.network.di

import com.tech.bazaar.network.event.EventHelperImp
import com.tech.bazaar.network.event.EventsHelper
import org.koin.dsl.module

val bindingModule = module {
    single<EventsHelper> {
        EventHelperImp(
            get()
        )
    }
}