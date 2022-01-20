fun main() {
    val initialState = game()
    println(initialState.dictionary)
    var state = initialState
    while (!isWin(state) && !isLose(state)) {
        val guess = readln()
        val (message, newState) = next(state, guess)
        println(message)
        println(emojiBoard(newState))
        println(emojiAlphabet(newState))
        state = newState
    }
    System.currentTimeMillis()
}

const val maxRounds = 6

fun game(
    dictionary: Set<String> = wordsFromDictionaryFile(fromInternet = false).map { it.estonian }.shuffled().toSet(),
    chosenWord: String = dictionary.random(),
    alphabet: Set<Char> = dictionary.flatMap { it.toSet() }.toSortedSet()
) = State(dictionary, chosenWord, alphabet.associateWith { AlphabetLetterResult.Unknown })

data class State(
    val dictionary: Set<String>,
    val chosenWord: String,
    val alphabetHint: Map<Char, AlphabetLetterResult>,
    val guesses: List<String> = emptyList(),
    val results: List<List<LetterResult>> = emptyList()
) {
    init {
        require(guesses.size <= maxRounds)
        require(guesses.size == results.size)
        require(chosenWord in dictionary)
    }
}

fun isWin(state: State) = state.guesses.isNotEmpty() && state.chosenWord == state.guesses.last()
fun isLose(state: State) = state.results.size == maxRounds && !isWin(state)

fun next(state: State, guess: String): Pair<String, State> {
    if (isWin(state)) return "You already won!" to state
    if (isLose(state)) return "You already lost!" to state
    if (guess !in state.dictionary) return "Not a word according to our dictionary!" to state
    val correctIndices = guess.indices.filter { guess[it] == state.chosenWord[it] }
    val incorrectIndices = guess.indices - correctIndices.toSet()
    val guessLetters = incorrectIndices.groupBy { guess[it] }
    val chosenWordLetters = incorrectIndices.groupBy { state.chosenWord[it] }
    val absentLetters = guessLetters.keys - chosenWordLetters.keys
    val misplacedIndices = (guessLetters - absentLetters).entries.flatMap { (char, guessIndices) ->
        val chosenWordIndices = chosenWordLetters.getValue(char)
        guessIndices.take(
            minOf(guessIndices.size, chosenWordIndices.size)
        )
    }
    val guessResult =
        guess.indices.map {
            when (it) {
                in correctIndices -> LetterResult.Correct
                in misplacedIndices -> LetterResult.Misplaced
                else -> LetterResult.Absent
            }
        }
    val newAlphabetHint = (guess.toList() zip guessResult).fold(state.alphabetHint) { alphabetHint, (char, letterResult) ->
        val current = alphabetHint.getValue(char)
        val new = maxOf(current, letterResult.toAlphabetLetterResult())
        alphabetHint + (char to new)
    }
    val newGuesses = state.guesses + guess
    val newResults = state.results.plusElement(guessResult)
    val newState = state.copy(guesses = newGuesses, results = newResults, alphabetHint = newAlphabetHint)
    if (isWin(newState)) return "Win! Wordle Eesti, ${newState.guesses.size}/$maxRounds" to newState
    if (isLose(newState)) return "Wordle Eesti, X/$maxRounds" to newState
    return "Keep playing! ${newState.guesses.size}/$maxRounds" to newState
}

fun emojiBoard(state: State) = state.results.joinToString("\n") { result ->
    result.joinToString("") { it.toEmoji() }
}

fun emojiAlphabet(state: State) = state.alphabetHint.entries.joinToString("") { (char, result) ->
    val emoji = result.toEmoji()
    "$char:$emoji"
}

enum class LetterResult { Absent, Misplaced, Correct }
enum class AlphabetLetterResult { Unknown, Absent, Misplaced, AtLeastOneCorrect }

fun LetterResult.toAlphabetLetterResult() = when (this) {
    LetterResult.Absent -> AlphabetLetterResult.Absent
    LetterResult.Misplaced -> AlphabetLetterResult.Misplaced
    LetterResult.Correct -> AlphabetLetterResult.AtLeastOneCorrect
}

fun LetterResult.toEmoji() = when (this) {
    LetterResult.Absent -> "\u2B1C\uFE0F"
    LetterResult.Misplaced -> "\uD83D\uDFE8"
    LetterResult.Correct -> "\uD83D\uDFE9"
}

fun AlphabetLetterResult.toEmoji() = when (this) {
    AlphabetLetterResult.Unknown -> "\u2B1C\uFE0F"
    AlphabetLetterResult.Absent -> "\u2B1B\uFE0F"
    AlphabetLetterResult.Misplaced -> "\uD83D\uDFE8"
    AlphabetLetterResult.AtLeastOneCorrect -> "\uD83D\uDFE9"
}