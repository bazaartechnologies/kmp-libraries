package com.tech.bazaar.kmp.app.data

import com.tech.bazaar.network.api.SessionManager

class AppSessionManager : SessionManager {
    override suspend fun getAuthToken(): String? {
        return "eyJhbGciOiJSUzUxMiJ9.eyJzdWIiOiIiLCJyb2xlcyI6WyIiXSwiZmlsdGVycyI6W10sInVzZXJOYW1lIjoiZ3Vlc3QtdXNlciIsInVzZXJJZCI6Imd1ZXN0LWQxYTFjNmM3LTczOWMtNGE3Yi1hMzFmLTU0OWVhMjZiNTc4NiIsIm9yZ2FuaXphdGlvbklkIjoiNzkzMTY2MDI4MTAxOTEwODE4MzIxMCIsInVzZXJDaGFubmVsIjoiQ1VTVE9NRVJfQVBQIiwiY2xpZW50S2V5IjoiIiwibGVnYWN5VG9rZW4iOiIiLCJ1c2VyVHlwZSI6IkNVU1RPTUVSIiwic2NvcGVzIjpbInpvbmUuem9uZS5hbGwucmVhZCIsImNhdGFsb2cuY2F0ZWdvcnkuYWxsLnJlYWQiLCJjdXN0b21lci5yZWNvbW1lbmRhdGlvbi5hbGwucmVhZCIsImludmVudG9yeS53YXJlaG91c2UuYWxsLnJlYWQiLCJzYWxlcy5wcm9kdWN0LmFsbC5yZWFkIiwiY2F0YWxvZy5idW5kbGUuYWxsLnJlYWQiLCJjYXRhbG9nLnByb2R1Y3QuYWxsLnJlYWQiLCJpbnZlbnRvcnkucHJvZHVjdC5hbGwucmVhZCIsInByb21vLnByb21vLnNlbGYuYXZhaWwiLCJjb25maWd1cmF0aW9ucy5jb25maWd1cmF0aW9uLmFsbC5yZWFkIiwib3JkZXIuY29uZmlndXJhdGlvbi5hbGwucmVhZCIsImNhdGFsb2cuYmFubmVyLmFsbC5yZWFkIl0sImV4cCI6MTczNzkwNzk3MiwiaWF0IjoxNzM3NjQ4NzcyfQ.aE-6Im3nj1pwNIOCgPoPs8j33UjzQq_OCTk8somxP6rjTr610zx9w3OztUzysC3NyczOHhnnfdKn_pKuel7Ylnx1i94k-Pg94P90IdK6rnlH4n0g90L40iMa4q0mRmQCfG1iCPfDHDUsKxcvad2IqHOCyYoEULbx-34lzzL714Kqv5L4zyMNf9kawtj1PWLqkdhMIwpggXeh0PA9QlXFMUQjNY4Xv1KivYyBJuB6UcXoMMAR_YYH8b7E1mNtg8OIWKrWgtwV8ZkdCahY3h8PGCoRsifuyYnGGqNd1kEpg_V4oOTyWsHcnxln-xPaAc70xTB32igGlcwhTHG5vqj6ug"
    }

    override suspend fun getRefreshToken(): String? {
        return ""
    }

    override suspend fun getUsername(): String? {
        return "03312784998"
    }

    override suspend fun onTokenRefreshed(token: String, expiresAt: String, refreshToken: String) {

    }

    override suspend fun onTokenExpires() {
    }

}