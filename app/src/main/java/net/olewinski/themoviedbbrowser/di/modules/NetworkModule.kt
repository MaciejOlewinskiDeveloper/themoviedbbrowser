package net.olewinski.themoviedbbrowser.di.modules

import android.os.Build
import android.util.Log
import net.olewinski.themoviedbbrowser.BuildConfig
import net.olewinski.themoviedbbrowser.cloud.service.TmdbService
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

val networkModule = module {
    single { getTmdbService() }
}

fun getTmdbService(): TmdbService {
    val okHttpClientBuilder = OkHttpClient.Builder()

    // Logging full body of network traffic in debug builds.
    if (BuildConfig.DEBUG) {
        val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        okHttpClientBuilder.addInterceptor(httpLoggingInterceptor)
    }

    // Ugly solution for problems in handling TLS version used by TMDB API on Android 4.4. It is
    // possible to be solved by, e.g. using this: https://developer.android.com/training/articles/security-gms-provider
    //
    // In commercial application this should be carefully investigated and proper solution
    // should be chosen, especially in e.g. banking app. I would not be comfortable to use below
    // solution in any application that handles sensitive date. Fortunately, TMDB API in this
    // application doesn't handle any user's sensitive data.
    if (Build.VERSION.SDK_INT in 16..21) {
        try {
            val sc: SSLContext = SSLContext.getInstance("TLSv1.2")

            sc.init(null, null, null)

            okHttpClientBuilder.sslSocketFactory(Tls12SocketFactory(sc.socketFactory))

            val cs = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2).build()

            val specs: MutableList<ConnectionSpec> = ArrayList()

            specs.add(cs)
            specs.add(ConnectionSpec.COMPATIBLE_TLS)
            specs.add(ConnectionSpec.CLEARTEXT)

            okHttpClientBuilder.connectionSpecs(specs)
        } catch (exc: Exception) {
            Log.e("OkHttpTLSCompat", "Error while setting TLS 1.2", exc)
        }
    }

    return Retrofit.Builder()
        .baseUrl(TmdbService.TMDB_ENDPOINT)
        .client(okHttpClientBuilder.build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TmdbService::class.java)
}

class Tls12SocketFactory(val delegate: SSLSocketFactory) : SSLSocketFactory() {
    override fun getDefaultCipherSuites(): Array<String> {
        return delegate.defaultCipherSuites
    }

    override fun getSupportedCipherSuites(): Array<String> {
        return delegate.supportedCipherSuites
    }

    @Throws(IOException::class)
    override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket {
        return patch(delegate.createSocket(s, host, port, autoClose))
    }

    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(host: String, port: Int): Socket {
        return patch(delegate.createSocket(host, port))
    }

    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(
        host: String,
        port: Int,
        localHost: InetAddress,
        localPort: Int
    ): Socket {
        return patch(delegate.createSocket(host, port, localHost, localPort))
    }

    @Throws(IOException::class)
    override fun createSocket(host: InetAddress, port: Int): Socket {
        return patch(delegate.createSocket(host, port))
    }

    @Throws(IOException::class)
    override fun createSocket(
        address: InetAddress,
        port: Int,
        localAddress: InetAddress,
        localPort: Int
    ): Socket {
        return patch(delegate.createSocket(address, port, localAddress, localPort))
    }

    private fun patch(s: Socket): Socket {
        if (s is SSLSocket) {
            s.enabledProtocols = TLS_V12_ONLY
        }
        return s
    }

    companion object {
        private val TLS_V12_ONLY = arrayOf("TLSv1.2")
    }
}
