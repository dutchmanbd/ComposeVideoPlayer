package com.dutch.composevideoplayer.di

import android.app.Application
import android.content.Context
import com.dutch.composevideoplayer.util.MetaDataReader
import com.dutch.composevideoplayer.util.MetaDataReaderImpl
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object VideoPlayerModule {

    @Provides
    @ViewModelScoped
    fun provideVideoPlayer(
        @ApplicationContext context: Context
    ): Player {
        return ExoPlayer.Builder(context)
            .build()
    }

    @Provides
    @ViewModelScoped
    fun provideMetaDataReader(
        app: Application
    ): MetaDataReader = MetaDataReaderImpl(app)
}