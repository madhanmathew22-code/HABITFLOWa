package com.habitflow.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A habit category (section 22). The nine defaults are inserted once via
 * [com.habitflow.app.data.local.HabitFlowDatabase]'s onCreate callback;
 * everything else the user adds is [isCustom] = true.
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconKey: String,
    val colorArgb: Int,
    val isCustom: Boolean = false,
    val sortOrder: Int = 0,
)

object DefaultCategories {
    val seed: List<CategoryEntity> = listOf(
        CategoryEntity(name = "Health", iconKey = "health", colorArgb = 0xFF1F6E63.toInt(), sortOrder = 0),
        CategoryEntity(name = "Fitness", iconKey = "fitness", colorArgb = 0xFFB1543F.toInt(), sortOrder = 1),
        CategoryEntity(name = "Study", iconKey = "study", colorArgb = 0xFF3C6E9E.toInt(), sortOrder = 2),
        CategoryEntity(name = "Work", iconKey = "work", colorArgb = 0xFF6E5A9E.toInt(), sortOrder = 3),
        CategoryEntity(name = "Personal", iconKey = "personal", colorArgb = 0xFF8A6D3B.toInt(), sortOrder = 4),
        CategoryEntity(name = "Mindfulness", iconKey = "mindfulness", colorArgb = 0xFF5C7A3C.toInt(), sortOrder = 5),
        CategoryEntity(name = "Finance", iconKey = "finance", colorArgb = 0xFF3F7A6E.toInt(), sortOrder = 6),
        CategoryEntity(name = "Lifestyle", iconKey = "lifestyle", colorArgb = 0xFF7B5AA6.toInt(), sortOrder = 7),
        CategoryEntity(name = "Social", iconKey = "social", colorArgb = 0xFF9E6E5A.toInt(), sortOrder = 8),
    )
}
