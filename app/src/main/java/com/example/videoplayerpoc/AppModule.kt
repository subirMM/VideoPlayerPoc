package com.example.videoplayerpoc

import android.app.Application
import com.example.videoplayerpoc.util.CustomVideoRecorder
import com.example.videoplayerpoc.util.FileManagerUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
object AppModule {

    @Provides
    @ActivityScoped
    fun providesFilesManager(app: Application): FileManagerUtil {
        return FileManagerUtil(app)
    }

    @Provides
    @ActivityScoped
    fun providesCustomVideoRecorder(): CustomVideoRecorder {
        return CustomVideoRecorder()
    }
}