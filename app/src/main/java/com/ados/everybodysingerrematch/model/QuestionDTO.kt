package com.ados.everybodysingerrematch.model

data class QuestionDTO(var stat: STAT? = STAT.INFO,
                       var title: String? = null,
                       var content: String? = null) {
    enum class STAT {
        INFO, WARNING, ERROR
    }
}