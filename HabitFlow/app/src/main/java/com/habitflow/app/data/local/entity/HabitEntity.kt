package com.habitflow.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.habitflow.app.domain.model.FrequencyType
import com.habitflow.app.domain.model.HabitDifficulty
import com.habitflow.app.domain.model.HabitValueType
import java.time.LocalDate

/**
 * The central Habit table.
 *
 * Schedule fields are embedded directly on the habit rather than split into
 * a separate HabitSchedule table for this iteration: a habit has exactly
 * one active schedule at a time, so normalizing it out mainly adds a join
 * with no behavioral benefit yet. [HabitSchedule] history (for "the
 * schedule changed on this date") is the natural reason to split it out
 * later, and the streak engine already treats schedule resolution as an
 * injectable function so that split won't require touching it.
 *
 * [stackAfterHabitId] and [dependsOnHabitId] implement the simple forms of
 * habit stacking (section 9) and dependencies (section 10). Both are
 * nullable self-references; the repository layer is responsible for
 * rejecting cycles before insert/update (Room can't express that
 * constraint declaratively).
 */
@Entity(
    tableName = "habits",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["stackAfterHabitId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["dependsOnHabitId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("categoryId"),
        Index("stackAfterHabitId"),
        Index("dependsOnHabitId"),
    ],
)
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    val name: String,
    val description: String = "",
    val iconKey: String = "check_circle",
    val colorArgb: Int,

    val categoryId: Long? = null,

    // --- schedule ---
    val frequencyType: FrequencyType = FrequencyType.DAILY,
    /** Bitmask, bit 0 = Monday .. bit 6 = Sunday. Used when [frequencyType] == SPECIFIC_WEEKDAYS. */
    val weekdaysMask: Int = 0,
    /** Used when [frequencyType] == TIMES_PER_WEEK. */
    val timesPerWeek: Int? = null,
    /** Used when [frequencyType] == TIMES_PER_MONTH. */
    val timesPerMonth: Int? = null,
    /** Used when [frequencyType] == EVERY_N_DAYS. */
    val everyNDays: Int? = null,
    /** Acceptable +/- day window for FLEXIBLE schedules. */
    val flexibleWindowDays: Int? = null,

    // --- what a completion records ---
    val valueType: HabitValueType = HabitValueType.BOOLEAN,
    val targetQuantity: Double? = null,
    val unit: String? = null,

    val difficulty: HabitDifficulty = HabitDifficulty.MEDIUM,

    @ColumnInfo(name = "startDate") val startDate: LocalDate,
    @ColumnInfo(name = "endDate") val endDate: LocalDate? = null,

    val notes: String = "",

    val isArchived: Boolean = false,
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val sortOrder: Int = 0,

    /** Habit stacking: "after finishing X, do this habit" (section 9). */
    val stackAfterHabitId: Long? = null,
    /** Habit dependency: this habit only unlocks once its dependency is done today (section 10). */
    val dependsOnHabitId: Long? = null,

    val createdAtEpochMillis: Long = System.currentTimeMillis(),
)
