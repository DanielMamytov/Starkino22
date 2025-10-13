package co.nisari.katisnar.presentation.data.di

import android.content.Context
import androidx.room.Room
import co.nisari.katisnar.presentation.data.local.MIGRATION_1_2
import co.nisari.katisnar.presentation.data.local.StarDatabase
import co.nisari.katisnar.presentation.data.local.StarLocationDao
import co.nisari.katisnar.presentation.data.local.StarRouteDao
import co.nisari.katisnar.presentation.data.repository.StarRouteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): StarDatabase =
        Room.databaseBuilder(
            context,
            StarDatabase::class.java,
            "starkino_db"
        ).addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideStarLocationDao(db: StarDatabase): StarLocationDao = db.starLocationDao()

    @Provides
    fun provideStarRouteDao(db: StarDatabase): StarRouteDao = db.starRouteDao()

    @Provides
    @Singleton
    fun provideStarRouteRepository(dao: StarRouteDao): StarRouteRepository =
        StarRouteRepository(dao)
}
