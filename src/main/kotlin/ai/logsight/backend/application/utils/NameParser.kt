package ai.logsight.backend.application.utils

import javax.naming.InvalidNameException

class NameParser() {
    val pattern = Regex("^[a-zA-Z0-9][ a-zA-Z0-9_.-]+$")

    fun slugify(string: String): String {
        if (!pattern.matches(string)) {
            throw InvalidNameException("String $string does not comply with regex ${pattern.pattern}")
        }
        return Regex(" ").replace(string, "-") // replace whitespace
            .replace(Regex("/-+/g"), "-") // replace extra dashes
    }

    fun validateString(string: String): Boolean {
        return pattern.matches(string)
    }

    fun toElasticsearchStandard(string: String): String {
        return this.slugify(string).lowercase()
    }
}
