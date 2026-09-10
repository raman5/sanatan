package com.bhakti.app.data.model

/**
 * The initial deity catalogue from the PRD. [aliases] cover common spelling
 * variations so search stays forgiving (e.g. "krishnaa", "ganpati").
 */
enum class Deity(
    val displayName: String,
    val aliases: List<String>,
    val accentHex: Long
) {
    SHIVA("Shiva", listOf("shiv", "mahadev", "shankar"), 0xFF5C6BC0),
    KRISHNA("Krishna", listOf("krishn", "krishnaa", "kanha", "kanhaiya"), 0xFF3949AB),
    RAM("Ram", listOf("rama", "shri ram", "raghunath"), 0xFF1E88E5),
    HANUMAN("Hanuman", listOf("bajrangbali", "anjaneya", "hanumanji"), 0xFFD84315),
    GANESH("Ganesh", listOf("ganesha", "ganpati", "vinayak"), 0xFFE65100),
    DURGA("Durga", listOf("durgaa", "amba", "sherawali"), 0xFFAD1457),
    LAKSHMI("Lakshmi", listOf("laxmi", "laxmiji", "mahalakshmi"), 0xFFF9A825),
    SARASWATI("Saraswati", listOf("sharda", "sarswati"), 0xFF00897B),
    VISHNU("Vishnu", listOf("narayan", "vishnuji"), 0xFF1565C0),
    RADHA("Radha", listOf("radhaji", "radharani"), 0xFFAB47BC),
    SAI_BABA("Sai Baba", listOf("shirdi sai", "sai", "saibaba"), 0xFF6D4C41),
    JAGANNATH("Jagannath", listOf("jagannathji", "puri wale baba"), 0xFF00695C),
    SHANI_DEV("Shani Dev", listOf("shani", "shanidev", "shaniji"), 0xFF37474F),
    KALI("Kali", listOf("kaali", "kalika", "maa kali"), 0xFF4527A0),
    BALAJI("Balaji", listOf("venkateswara", "tirupati balaji"), 0xFFEF6C00);

    companion object {
        fun matches(query: String, deity: Deity): Boolean {
            val q = query.trim().lowercase()
            if (q.isEmpty()) return true
            return deity.displayName.lowercase().contains(q) ||
                deity.aliases.any { it.contains(q) || q.contains(it) }
        }
    }
}
