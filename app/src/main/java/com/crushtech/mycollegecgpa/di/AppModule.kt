package com.crushtech.mycollegecgpa.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.crushtech.mycollegecgpa.data.local.SemesterDatabase
import com.crushtech.mycollegecgpa.data.remote.BasicAuthInterceptor
import com.crushtech.mycollegecgpa.data.remote.SemesterApi
import com.crushtech.mycollegecgpa.utils.Constants.BASE_URL
import com.crushtech.mycollegecgpa.utils.Constants.DATABASE_NAME
import com.crushtech.mycollegecgpa.utils.Constants.ENCRYPTED_SHARED_PREF_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideNoteDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        SemesterDatabase::class.java,
        DATABASE_NAME
    )
        .fallbackToDestructiveMigration()
        .build()

    @Singleton
    @Provides
    fun provideNoteDao(db: SemesterDatabase) = db.semesterDao()

    @Singleton
    @Provides
    fun provideBasicAuthInterceptor() = BasicAuthInterceptor()


    @Singleton
    @Provides
    fun provideSemesterApi(
        basicAuthInterceptor: BasicAuthInterceptor
    ): SemesterApi {
        val client = OkHttpClient.Builder()
            .addInterceptor(basicAuthInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(SemesterApi::class.java)
    }


    @Singleton
    @Provides
    fun provideEncryptedSharedPrefs(
        @ApplicationContext context: Context
    ): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_SHARED_PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}