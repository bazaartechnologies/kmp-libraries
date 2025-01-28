package com.tech.bazaar.network.api.exception

class NoInternetException(override val message: String = "No internet connection detected") : NetworkClientException(message)