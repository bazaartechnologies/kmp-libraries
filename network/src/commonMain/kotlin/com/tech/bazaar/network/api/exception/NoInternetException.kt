package com.tech.bazaar.network.api.exception

class NoInternetException(message: String = "No internet connection detected") :
    NetworkClientException(message)