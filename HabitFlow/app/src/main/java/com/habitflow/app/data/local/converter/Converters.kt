package com.habitflow.app.data.local.converter

import androidx.room.TypeConverter
import com.habitflow.app.domain.model.FrequencyType
import com.habitflow.app.domain.model.HabitDifficulty
import com.habitflow.app.domain.model.HabitValueType
import java.time.LocalDate

class Converters {

    @TypeConverter
    fun fromEpochDay(epochDay: Long?): LocalDate? = epochDay?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun toEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun fromFrequencyType(value: String?): FrequencyType =
        value?.let { FrequencyType.valueOf(it) } ?: FrequencyType.DAILY

    @TypeConverter
    fun toFrequencyType(value: FrequencyType?): String? = value?.name

    @TypeConverter
    fun fromHabitValueType(value: String?): HabitValueType =
        value?.let { HabitValueType.valueOf(it) } ?: HabitValueType.BOOLEAN

    @TypeConverter
    fun toHabitValueType(value: HabitValueType?): String? = value?.name

    @TypeConverter
    fun fromHabitDifficulty(value: String?): HabitDifficulty =
        value?.let { HabitDifficulty.valueOf(it) } ?: HabitDifficulty.MEDIUM

    @TypeConverter
    fun toHabitDifficulty(value: HabitDifficulty?): String? = value?.name
}
