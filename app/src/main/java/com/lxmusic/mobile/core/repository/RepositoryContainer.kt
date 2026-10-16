package com.lxmusic.mobile.core.repository

import android.content.Context
import com.lxmusic.mobile.core.db.AppDatabase

/** 应用级仓库容器 */
class RepositoryContainer private constructor(context: Context) {
    val database: AppDatabase = AppDatabase.build(context)
    val apiSourceRepository: ApiSourceRepository = ApiSourceRepository(database)
    val settingsRepository: SettingsRepository = SettingsRepository(context)

    companion object {
        @Volatile
        private var instance: RepositoryContainer? = null

        fun get(context: Context): RepositoryContainer =
            instance ?: synchronized(this) {
                instance ?: RepositoryContainer(context.applicationContext).also { instance = it }
            }
    }
}
