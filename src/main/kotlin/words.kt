import java.io.File
import java.net.URL

fun main() {
//    downloadDictionary()
    val words = wordsFromDictionaryFile(fromInternet = false)
    println(words.size)
    println(words.randomSample)
}

fun downloadDictionary() {
    File("eestiinglise.txt").writeText(URL("https://www.eki.ee/litsents/vaba/ies/eestiinglise.txt").readText())
}

fun wordsFromDictionaryFile(fromInternet: Boolean = true): List<Entry> {
    val dictionaryText = if (fromInternet) {
        URL("https://www.eki.ee/litsents/vaba/ies/eestiinglise.txt").readText()
    } else {
        File("eestiinglise.txt").readText()
    }
    return wordsFromDictionaryText(dictionaryText)
}

fun wordsFromDictionaryText(dictionaryText: String): List<Entry> {
    val dictionary = dictionaryText.trim().lines()
    val entries = dictionary.map {
        val (estonian, english) = it.split('\t')
        Entry(estonian, english)
    }
    return entries.filter { it.estonian.matches("[a-zäöüõšž]{5}".toRegex()) }
}

data class Entry(val estonian: String, val english: String)

val <T> Iterable<T>.randomSample get() = shuffled().take(20)
