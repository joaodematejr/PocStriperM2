package com.example.pocstriperm2

import com.stripe.stripeterminal.external.callable.ConnectionTokenCallback
import com.stripe.stripeterminal.external.callable.ConnectionTokenProvider
import com.stripe.stripeterminal.external.models.ConnectionTokenException
import org.json.JSONObject
import java.io.PrintWriter
import java.io.StringWriter
import java.net.HttpURLConnection
import java.net.URL

class TokenProvider : ConnectionTokenProvider {
    override fun fetchConnectionToken(callback: ConnectionTokenCallback) {
        var connection: HttpURLConnection? = null
        try {
            connection =
                URL("http://192.168.1.131:4242/connection_token").openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")

            val responseCode = connection.responseCode

            if (responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                val jsonResponse = JSONObject(response)
                val resToken = jsonResponse.getString("secret")
                callback.onSuccess(resToken)
            } else {
                connection.headerFields.entries.joinToString("\n") { "${it.key}: ${it.value}" }
                val errorResponse = connection.errorStream?.bufferedReader()?.readText()
                val errorMessage = errorResponse?.let {
                    try {
                        JSONObject(it).getJSONObject("error").getString("message")
                    } catch (e: Exception) {
                        it
                    }
                } ?: "Failed to fetch token (HTTP $responseCode)"
                callback.onFailure(ConnectionTokenException(errorMessage))
            }
        } catch (e: Exception) {
            val sw = StringWriter()
            e.printStackTrace(PrintWriter(sw))
            callback.onFailure(ConnectionTokenException("Network error: ${e.message}"))
        } finally {
            connection?.disconnect()
        }
    }

}
