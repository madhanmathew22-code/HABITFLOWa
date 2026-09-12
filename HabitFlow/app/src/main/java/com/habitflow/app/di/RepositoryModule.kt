package com.habitflow.app.di

import com.habitflow.app.data.repository.HabitRepository
import com.habitflow.app.data.repository.HabitRepositoryImpl
import com.habitflow.app.data.repository.JournalRepository
import com.habitflow.app.data.repository.JournalRepositoryImpl
import com.habitflow.app.domain.streak.StreakEngine
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindHabitRepository(impl: HabitRepositoryImpl): HabitRepository

    @Binds
    @Singleton
    abstract fun bindJournalRepository(impl: JournalRepositoryImpl): JournalRepository

    companion object {
        @Provides
        @Singleton
        fun provideStreakEngine(): StreakEngine = StreakEngine()
    }
}
