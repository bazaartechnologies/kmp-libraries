package com.bazaartech.core_network.http

import org.json.JSONObject
import org.json.simple.parser.JSONParser

open class CustomHttpException(private val response: retrofit2.Response<*>) : RuntimeException() {
    private val customCode: Int
    private val customMessage: String
    private val errorJson: String? = response.errorBody()?.string()

    override val message: String?
        get() = "HTTP $customCode $customMessage"

    init {
        customCode = getErrorCode(errorJson)
        customMessage = getErrorMessage(errorJson)
    }

    open fun code(): Int {
        return customCode
    }

    /** HTTP status message.  */
    open fun message(): String? {
        return customMessage
    }

    private fun getErrorCode(rawJson: String?): Int {
        return try {
            JSONObject(rawJson).getInt("code")
        } catch (exception: Exception) {
            response.code()
        }
    }

    private fun getErrorMessage(rawJson: String?): String {
        return try {
            val obj = JSONParser().parse(rawJson) as org.json.simple.JSONObject

            var message = obj["message"] as String?

            if (message.isNullOrBlank()) {
                message = (obj["errors"] as org.json.simple.JSONArray)[0] as String
            }

            return message
        } catch (ex: Exception) {
            response.message()
        }
    }
}
