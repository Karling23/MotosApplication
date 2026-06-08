package com.motosapp.di

import com.motosapp.data.repository.*
import com.motosapp.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository



    @Binds @Singleton
    abstract fun bindOrderRepository(impl: OrderRepositoryImpl): OrderRepository

    @Binds @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds @Singleton
    abstract fun bindMotocicletaRepository(impl: MotocicletaRepositoryImpl): MotocicletaRepository

    @Binds @Singleton
    abstract fun bindCascoRepository(impl: CascoRepositoryImpl): CascoRepository

    @Binds @Singleton
    abstract fun bindAccesorioRepository(impl: AccesorioRepositoryImpl): AccesorioRepository

    @Binds @Singleton
    abstract fun bindMarcaRepository(impl: MarcaRepositoryImpl): MarcaRepository

    @Binds @Singleton
    abstract fun bindCategoriaRepository(impl: CategoriaRepositoryImpl): CategoriaRepository

    @Binds @Singleton
    abstract fun bindTestingRepository(impl: TestingRepositoryImpl): TestingRepository
}