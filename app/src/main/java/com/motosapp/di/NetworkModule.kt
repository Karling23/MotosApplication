package com.motosapp.di

import com.motosapp.BuildConfig
import com.motosapp.data.local.TokenDataStore
import com.motosapp.data.remote.api.*
import com.motosapp.data.remote.interceptor.AuthInterceptor
import com.motosapp.data.remote.interceptor.BearerTokenInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideLoggingInterceptor() = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    @Provides @Singleton
    fun provideOkHttpClient(
        tokenDataStore: TokenDataStore,
        authInterceptor: AuthInterceptor,
        logging: HttpLoggingInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .authenticator(authInterceptor)
        .addInterceptor(BearerTokenInterceptor(tokenDataStore))
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)



    @Provides @Singleton
    fun provideOrderApi(retrofit: Retrofit): OrderApi = retrofit.create(OrderApi::class.java)

    @Provides @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi = retrofit.create(UserApi::class.java)

    @Provides @Singleton
    fun provideMotocicletaApi(retrofit: Retrofit): MotocicletaApi = retrofit.create(MotocicletaApi::class.java)

    @Provides @Singleton
    fun provideCascoApi(retrofit: Retrofit): CascoApi = retrofit.create(CascoApi::class.java)

    @Provides @Singleton
    fun provideAccesorioApi(retrofit: Retrofit): AccesorioApi = retrofit.create(AccesorioApi::class.java)

    @Provides @Singleton
    fun provideMarcaApi(retrofit: Retrofit): MarcaApi = retrofit.create(MarcaApi::class.java)

    @Provides @Singleton
    fun provideCategoriaApi(retrofit: Retrofit): CategoriaApi = retrofit.create(CategoriaApi::class.java)

    @Provides @Singleton
    fun provideTestingApi(retrofit: Retrofit): TestingApi = retrofit.create(TestingApi::class.java)

    @Provides @Singleton
    fun provideHealthApi(retrofit: Retrofit): HealthApi = retrofit.create(HealthApi::class.java)
}
