package com.skysam.hchirinos.mundialcatar.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.Preferences
import com.skysam.hchirinos.mundialcatar.common.Constants
import com.skysam.hchirinos.mundialcatar.common.Mundial
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Created by Hector Chirinos on 15/05/2022.
 */

object Preferences {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(Constants.PREFERENCES)

    private val PREFERENCE_NOTIFICATION = booleanPreferencesKey(Constants.PREFERENCES_NOTIFICATION)

    fun getNotificationStatus(): Flow<Boolean> {
        return Mundial.Mundial.getContext().dataStore.data
            .map {
                it[PREFERENCE_NOTIFICATION] ?: true
            }
    }

    suspend fun changeNotificationStatus(status: Boolean) {
        Mundial.Mundial.getContext().dataStore.edit {
            it[PREFERENCE_NOTIFICATION] = status
        }
    }
}