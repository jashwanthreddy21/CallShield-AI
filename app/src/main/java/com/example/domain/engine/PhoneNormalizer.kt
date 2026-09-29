package com.example.domain.engine

object PhoneNormalizer {
    /**
     * Cleans phone number to standard comparable format.
     * Keeps leading '+' if present and digits.
     */
    fun normalize(phone: String): String {
        val trimmed = phone.trim()
        val hasPlus = trimmed.startsWith("+")
        val digitsOnly = trimmed.filter { it.isDigit() }
        return if (hasPlus) "+$digitsOnly" else digitsOnly
    }

    /**
     * Strips leading country code if present to compare against national number patterns.
     */
    fun stripCountryCode(phone: String): String {
        val digits = normalize(phone).removePrefix("+")
        return when {
            digits.startsWith("91") && digits.length > 10 -> digits.removePrefix("91")
            digits.startsWith("1") && digits.length == 11 -> digits.removePrefix("1")
            digits.startsWith("44") && digits.length > 10 -> digits.removePrefix("44")
            else -> digits
        }
    }

    /**
     * Checks if a phone number matches a pattern (e.g. "91140*", "+9191140*", "1800*").
     */
    fun matchesPattern(normalizedPhone: String, pattern: String): Boolean {
        val cleanPattern = pattern.trim()
        val rawDigits = normalize(normalizedPhone).removePrefix("+")
        val nationalDigits = stripCountryCode(normalizedPhone)

        if (cleanPattern.endsWith("*")) {
            val prefix = cleanPattern.removeSuffix("*").trim()
            val cleanPrefix = normalize(prefix).removePrefix("+")
            val nationalPrefix = stripCountryCode(prefix)

            return rawDigits.startsWith(cleanPrefix) ||
                    rawDigits.startsWith(nationalPrefix) ||
                    nationalDigits.startsWith(cleanPrefix) ||
                    nationalDigits.startsWith(nationalPrefix)
        }

        val cleanExact = normalize(cleanPattern).removePrefix("+")
        val nationalExact = stripCountryCode(cleanPattern)

        return rawDigits == cleanExact ||
                nationalDigits == nationalExact ||
                nationalDigits == cleanExact ||
                rawDigits == nationalExact
    }
}
