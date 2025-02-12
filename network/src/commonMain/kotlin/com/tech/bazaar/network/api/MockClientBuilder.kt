package com.tech.bazaar.network.api

import com.tech.bazaar.network.builder.buildMockClient

class MockClientBuilder {
    private var requests: Requests = Requests()

    fun requests(requests: Requests) = apply { this.requests = requests }

    fun build() = buildMockClient(requests)

    data class Requests(
        val get: List<MockJsonApi> = emptyList(),
        val post: List<MockJsonApi> = emptyList(),
        val put: List<MockJsonApi> = emptyList()
    ) {
        data class MockJsonApi(
            val path: String,
            val response: String = "",
            val responseCode: Int = 200
        )
    }
}