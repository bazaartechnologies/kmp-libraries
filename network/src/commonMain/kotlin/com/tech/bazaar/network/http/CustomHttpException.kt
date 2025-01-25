package com.tech.bazaar.network.http


import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

open class CustomHttpException(private val response: HttpResponse) : RuntimeException() {
    private val customCode: Int
    private val customMessage: String
    private val errorJson: String =  runBlocking {  response.bodyAsText().getOrNull(0) }.toString()

    override val message: String?
        get() = "HTTP $customCode $customMessage"

    init {
        customCode = getErrorCode(errorJson)
        customMessage = getErrorMessage(errorJson)
    }

    val code: Int
        get() = customCode

    private fun getErrorCode(rawJson: String?): Int {
        return try {
            rawJson?.let {
                val json = Json.parseToJsonElement(it).jsonObject
                (json["code"] as? JsonPrimitive)?.int ?: response.status.value
            } ?: response.status.value
        } catch (exception: Exception) {
            response.status.value
        }
    }

    private fun getErrorMessage(rawJson: String?): String {
        return try {
            rawJson?.let {
                val json = Json.parseToJsonElement(it).jsonObject
                val message = (json["message"] as? JsonPrimitive)?.content
                message?.takeIf { it.isNotBlank() } ?: (json["errors"]?.jsonArray?.getOrNull(0) as? JsonPrimitive)?.content.orEmpty()
            } ?: response.status.description
        } catch (ex: Exception) {
            response.status.description
        }
    }
}
