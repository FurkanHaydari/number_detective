package com.brainfocus.numberdetective.data.typeconverters

import androidx.room.TypeConverter
import com.brainfocus.numberdetective.missions.MissionType

class MissionTypeConverter {
    @TypeConverter
    fun fromMissionType(type: MissionType): String {
        return type.name
    }

    @TypeConverter
    fun toMissionType(value: String): MissionType {
        return MissionType.valueOf(value)
    }
}
