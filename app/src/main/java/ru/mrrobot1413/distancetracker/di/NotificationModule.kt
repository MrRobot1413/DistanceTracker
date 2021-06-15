package ru.mrrobot1413.distancetracker.di

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import ru.mrrobot1413.distancetracker.R
import ru.mrrobot1413.distancetracker.misc.Constants
import ru.mrrobot1413.distancetracker.ui.MainActivity

@Module
@InstallIn(ServiceComponent::class)
object NotificationModule {

    @ServiceScoped
    @Provides
    fun providesPendingIntent(
        @ApplicationContext context: Context
    ): PendingIntent {
        return PendingIntent.getActivity(
            context,
            Constants.PENDING_INTENT_REQUEST_CODE,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT)
    }

    @ServiceScoped
    @Provides
    fun providesNotificationBuilder(
        @ApplicationContext context: Context,
        pendingIntent: PendingIntent
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_baseline_directions_run_24)
            .setContentIntent(pendingIntent)
    }

    @ServiceScoped
    @Provides
    fun providesNotificationManager(
        @ApplicationContext context: Context
    ): NotificationManager{
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}