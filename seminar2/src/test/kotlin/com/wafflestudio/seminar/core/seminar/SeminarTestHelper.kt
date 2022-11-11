package com.wafflestudio.seminar.core.seminar

import com.wafflestudio.seminar.core.seminar.database.SeminarEntity
import com.wafflestudio.seminar.core.seminar.database.SeminarRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
internal class SeminarTestHelper @Autowired constructor(
    private val seminarRepository: SeminarRepository
) {
    fun createSeminar(
        name: String = "",
        capacity : Int = 10,
        count : Int = 1,
        time : String = "00:00",
        online : Boolean = true,
    ): SeminarEntity {
        return seminarRepository.save(
            SeminarEntity(
                name,
                capacity,
                count,
                time,
                online,
            )
        )
    }
}