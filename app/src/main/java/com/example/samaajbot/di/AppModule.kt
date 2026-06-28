package com.example.samaajbot.di

import android.content.Context
import androidx.room.Room
import com.example.samaajbot.data.api.*
import com.example.samaajbot.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides @Singleton
    fun provideApi(retrofit: Retrofit): SamaajBotApi =
        retrofit.create(SamaajBotApi::class.java)

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SamaajBotDatabase =
        Room.databaseBuilder(context, SamaajBotDatabase::class.java, "samaajbot.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides @Singleton
    fun provideChatDao(db: SamaajBotDatabase): ChatDao = db.chatDao()
}
