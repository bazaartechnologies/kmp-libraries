import com.bazaartech.core_network.api.BaseUrls
import com.bazaartech.core_network.api.CertTransparencyFlagProvider
import com.bazaartech.core_network.api.NetworkApiExceptionLogger
import com.bazaartech.core_network.api.NetworkEventLogger
import com.bazaartech.core_network.api.NetworkExternalDependencies
import com.bazaartech.core_network.api.SessionManager
import org.koin.core.module.Module
import org.koin.dsl.module

val ExternalDependenciesModule: Module = module {

    single<BaseUrls> { get<NetworkExternalDependencies>().getBaseUrls() }

    factory<SessionManager> { get<NetworkExternalDependencies>().getSessionManager() }

    single<NetworkEventLogger> { get<NetworkExternalDependencies>().getNetworkEventLogger() }

    single<NetworkApiExceptionLogger> { get<NetworkExternalDependencies>().getNetworkExceptionLogger() }

    single<CertTransparencyFlagProvider> { get<NetworkExternalDependencies>().getCertTransparencyFlagProvider() }
}
