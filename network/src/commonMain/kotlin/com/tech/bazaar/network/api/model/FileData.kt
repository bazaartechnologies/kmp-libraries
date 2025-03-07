package com.tech.bazaar.network.api.model

class FileData(
    val name: String,
    val bytes: ByteArray,
    val contentType: String,
    val key: String = ""
)