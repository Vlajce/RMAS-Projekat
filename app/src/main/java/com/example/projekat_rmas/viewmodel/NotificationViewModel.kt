package com.example.projekat_rmas.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NotificationViewModel(context: Context) : ViewModel() {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    private val _serviceRunningState = MutableLiveData<Boolean>().apply {
        value = sharedPreferences.getBoolean("is_notification_enabled", false)
    }
    val serviceRunningState: LiveData<Boolean> = _serviceRunningState


    fun setNotificationEnabled(enabled: Boolean) {
        _serviceRunningState.value = enabled
        sharedPreferences.edit().putBoolean("is_notification_enabled", enabled).apply()
    }
}