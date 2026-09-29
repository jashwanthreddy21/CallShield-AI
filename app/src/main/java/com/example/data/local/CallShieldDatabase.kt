package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.local.dao.AllowlistDao
import com.example.data.local.dao.CallDao
import com.example.data.local.dao.CallerMemoryDao
import com.example.data.local.dao.CommunityReportDao
import com.example.data.local.dao.RuleDao
import com.example.data.local.dao.TranscriptDao
import com.example.data.local.entity.AllowlistEntity
import com.example.data.local.entity.CallEntity
import com.example.data.local.entity.CallerMemoryEntity
import com.example.data.local.entity.CommunityReportEntity
import com.example.data.local.entity.RuleEntity
import com.example.data.local.entity.TranscriptEntity
import com.example.domain.model.CallAction
import com.example.domain.model.CallCategory
import com.example.domain.model.RiskLevel
import com.example.domain.model.RuleMatchType

class Converters {
    @TypeConverter
    fun fromCallAction(value: CallAction): String = value.name

    @TypeConverter
    fun toCallAction(value: String): CallAction = runCatching { CallAction.valueOf(value) }.getOrDefault(CallAction.ALLOW)

    @TypeConverter
    fun fromCallCategory(value: CallCategory?): String? = value?.name

    @TypeConverter
    fun toCallCategory(value: String?): CallCategory? = value?.let {
        runCatching { CallCategory.valueOf(it) }.getOrDefault(CallCategory.UNKNOWN)
    }

    @TypeConverter
    fun fromRiskLevel(value: RiskLevel): String = value.name

    @TypeConverter
    fun toRiskLevel(value: String): RiskLevel = runCatching { RiskLevel.valueOf(value) }.getOrDefault(RiskLevel.LOW)

    @TypeConverter
    fun fromRuleMatchType(value: RuleMatchType): String = value.name

    @TypeConverter
    fun toRuleMatchType(value: String): RuleMatchType = runCatching { RuleMatchType.valueOf(value) }.getOrDefault(RuleMatchType.EXACT_NUMBER)
}

@Database(
    entities = [
        CallEntity::class,
        RuleEntity::class,
        AllowlistEntity::class,
        TranscriptEntity::class,
        CallerMemoryEntity::class,
        CommunityReportEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CallShieldDatabase : RoomDatabase() {
    abstract fun callDao(): CallDao
    abstract fun ruleDao(): RuleDao
    abstract fun allowlistDao(): AllowlistDao
    abstract fun transcriptDao(): TranscriptDao
    abstract fun callerMemoryDao(): CallerMemoryDao
    abstract fun communityReportDao(): CommunityReportDao

    companion object {
        @Volatile
        private var INSTANCE: CallShieldDatabase? = null

        fun getDatabase(context: Context): CallShieldDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CallShieldDatabase::class.java,
                    "callshield_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
