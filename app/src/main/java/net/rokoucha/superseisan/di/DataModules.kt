package net.rokoucha.superseisan.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.rokoucha.superseisan.data.SuperSeisanDatabase
import net.rokoucha.superseisan.data.SuperSeisanRepository
import net.rokoucha.superseisan.data.SuperSeisanRepositoryImpl
import net.rokoucha.superseisan.data.dao.CurrencyDao
import net.rokoucha.superseisan.data.dao.ItemDao
import net.rokoucha.superseisan.data.dao.ParticipantDao
import net.rokoucha.superseisan.data.dao.SettlementDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Singleton
    @Binds
    abstract fun bindRepository(repository: SuperSeisanRepositoryImpl): SuperSeisanRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Singleton
    @Provides
    fun provideDataBase(@ApplicationContext context: Context): SuperSeisanDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            SuperSeisanDatabase::class.java,
            "SuperSeisan.db"
        ).build()
    }

    @Provides
    fun provideCurrencyDao(database: SuperSeisanDatabase): CurrencyDao = database.currencyDao()

    @Provides
    fun provideItemDao(database: SuperSeisanDatabase): ItemDao = database.itemDao()

    @Provides
    fun provideParticipantDao(database: SuperSeisanDatabase): ParticipantDao =
        database.participantDao()

    @Provides
    fun provideSettlementDao(database: SuperSeisanDatabase): SettlementDao =
        database.settlementDao()
}
