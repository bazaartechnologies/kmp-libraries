package com.tech.bazaar.network.api.model

data class FileData(
    val name: String,
    val bytes: ByteArray,
    val contentType: String,
    val key: String = ""
)