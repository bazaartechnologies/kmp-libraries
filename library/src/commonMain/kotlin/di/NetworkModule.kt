import com.bazaartech.core_network.api.BaseUrls
import com.bazaartech.core_network.api.CertTransparencyFlagProvider
import com.bazaartech.core_network.api.SessionManager
import com.bazaartech.core_network.interceptor.HeadersInterceptor
import com.bazaartech.core_network.token.AuthTokenProvider
import com.bazaartech.core_network.token.TokenRefreshService
import com.bazaartech.core_network.utils.AppVersionDetailsProvider
import io.ktor.client.*
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpSend
import io.ktor.client.request.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.http.ContentDisposition.Companion.File
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

val networkModule = module {

    single {
        HttpClient {

            install(ContentNegotiation) {
                json(
                    json = Json {
                        ignoreUnknownKeys = true
                    }
                )
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 70_000
                connectTimeoutMillis = 70_000
                socketTimeoutMillis = 70_000
            }

            if (BuildConfig.DEBUG) {
                install(Logging) {
                    level = LogLevel.BODY
                    logger = Logger.DEFAULT
                }
            } else {
                install(Logging) {
                    level = LogLevel.NONE
                }
            }

            install(get(named("CertificateInterceptor"))) // Install custom Certificate Interceptor

            install(DefaultRequest) {
                HeadersInterceptor(get(), get())
            }


            engine {
                // Enable connection pooling, connection retries, etc.
                threadsCount = 4
                pipelining = true
            }
        }
    }

    single(named("CertificateInterceptor")) { provideCertificateInterceptor(get(), get()) }

    single<AuthTokenProvider> {
        object : AuthTokenProvider {
            override fun getAuthToken(): String {
                return get<SessionManager>().getAuthToken()
            }
        }
    }

    single {
        HeadersInterceptor(get(), get())
    }

    single {
        TokenRefreshService(get(), get())
    }

    single<TokenRefreshService> { KtorGatewayService(get()) }

    single(named("MainGateway")) {
        HttpClient(CIO) {
            install(JsonFeature) {
                serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }

            defaultRequest {
                url(get<BaseUrls>().getMainGatewayUrl())
            }
        }
    }

    single(named("SecureGateway")) {
        HttpClient(CIO) {
            install(JsonFeature) {
                serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }

            defaultRequest {
                url(get<BaseUrls>().getSecureGatewayUrl())
            }
        }
    }
}

private fun createCache(context: Context): Cache {
    return Cache(context.cacheDir, CACHE_SIZE)
}

fun provideCertificateInterceptor(
    certTransparencyFlagProvider: CertTransparencyFlagProvider,
    baseUrls: BaseUrls
) = createClientPlugin("CertificateInterceptor") {
    if (certTransparencyFlagProvider.isFlagEnable().not()) {
        // No-op interceptor if the certificate transparency flag is disabled
        onRequest { request, _ -> }
    } else {
        onRequest { request, _ ->
            val mainGatewayUrl = baseUrls.getMainGatewayUrl()
            val secureGatewayUrl = baseUrls.getSecureGatewayUrl()

            val logListServiceUrl = "https://www.gstatic.com/ct/log_list/v3/"

            if (request.url.toString() in listOf(mainGatewayUrl, secureGatewayUrl)) {
                // Add logic to integrate with CT logListService
                // Implement CT verification
                val result = performCertificateTransparencyVerification(request, logListServiceUrl)
                if (result is VerificationResult.Failure) {
                    // Log the failure here or take necessary actions
                    logger.log(request.url.host, result)
                }
            }
        }
    }
}

fun performCertificateTransparencyVerification(request: HttpRequestBuilder, logListServiceUrl: String): VerificationResult {
    // Placeholder logic for actual verification
    return VerificationResult.Success
}

sealed class VerificationResult {
    object Success : VerificationResult()
    data class Failure(val reason: String) : VerificationResult()
}

interface CTLogger {
    fun log(host: String, result: VerificationResult)
}

object logger : CTLogger {
    override fun log(host: String, result: VerificationResult) {
        if (result is VerificationResult.Failure) {
            println("Certificate transparency check failed for $host with reason: ${result.toString()}")
        }
    }
}