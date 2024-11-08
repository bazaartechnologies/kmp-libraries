package com.bazaartech.core_network.qualifiers

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
internal annotation class MainGateway

@Qualifier
@Retention(AnnotationRetention.BINARY)
internal annotation class SecureGateway
