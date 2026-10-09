package com.impostor.library.domain.model

import com.impostor.library.domain.enums.GameModeType
import com.impostor.library.domain.enums.QuestionCategory

/** Paired prompts: everyone sees the civilian text except the impostor. */
data class BuiltInContent(val key: String, val mode: GameModeType,
    val category: QuestionCategory?, val civilian: String, val impostor: String)

/** Original prompts inspired by the linked NBA/NFL games; kept short for mobile screens. */
object BuiltInCatalog {
    val entries: List<BuiltInContent> = listOf(
        classic("coffee-tea", "Coffee", "Tea"),
        classic("beach-pool", "Beach", "Swimming pool"),
        classic("cat-dog", "Cat", "Dog"),
        classic("train-bus", "Train", "Bus"),
        classic("pizza-burger", "Pizza", "Burger"),
        classic("rain-snow", "Rain", "Snow"),
        classic("cinema-theatre", "Cinema", "Theatre"),
        classic("mountain-forest", "Mountain", "Forest"),
        classic("phone-tablet", "Phone", "Tablet"),
        classic("airport-station", "Airport", "Train station"),
        question("nba-principal", QuestionCategory.NBA,
            "Which NBA player would make the best school principal?",
            "Which NBA player would be the hardest person to argue with?"),
        question("nba-performance", QuestionCategory.NBA,
            "Which NBA player puts on the biggest show?",
            "Which NBA player is the most misunderstood?"),
        question("nba-zombies", QuestionCategory.NBA,
            "Which NBA player would survive longest in a zombie apocalypse?",
            "Which NBA player would win a hot dog eating contest?"),
        question("nba-superman", QuestionCategory.NBA,
            "Which NBA player would you cast as Superman?",
            "Which NBA player would you cast as a flashy stockbroker?"),
        question("nba-flight", QuestionCategory.NBA,
            "Which NBA player would be the worst companion on a long flight?",
            "Which NBA player would be most likely to start a podcast?"),
        question("nfl-looks", QuestionCategory.NFL,
            "Which NFL player would look best on a magazine cover?",
            "Which NFL player has the most style?"),
        question("nfl-actor", QuestionCategory.NFL,
            "Which NFL player would make the best movie actor?",
            "Which NFL player would be the best television host?"),
        question("nfl-president", QuestionCategory.NFL,
            "Which NFL player would make the best president?",
            "Which NFL player would make the best team captain?"),
        question("nfl-coach", QuestionCategory.NFL,
            "Which NFL player would make the best coach?",
            "Which NFL player would make the best teacher?"),
        question("nfl-survival", QuestionCategory.NFL,
            "Which NFL player would survive longest alone in the wild?",
            "Which NFL player would win a camping competition?"),
        question("actors-villain", QuestionCategory.ACTORS,
            "Which actor would play the most convincing villain?",
            "Which actor would make the best secret agent?"),
        question("actors-comedy", QuestionCategory.ACTORS,
            "Which actor would be funniest at a dinner party?",
            "Which actor would be most convincing as a teacher?"),
        question("movies-repeat", QuestionCategory.MOVIES,
            "Which movie could you watch ten times?",
            "Which movie would you show to an alien?"),
        question("movies-escape", QuestionCategory.MOVIES,
            "Which movie would be the hardest to live through?",
            "Which movie world would you most want to visit?"),
        question("football-captain", QuestionCategory.FOOTBALL,
            "Which footballer would make the best captain?",
            "Which footballer would make the best coach?"),
        question("football-final", QuestionCategory.FOOTBALL,
            "Which footballer would you trust with the last penalty?",
            "Which footballer would you put in goal for one match?"),
        question("series-house", QuestionCategory.SERIES,
            "Which TV character would be the best roommate?",
            "Which TV character would be the best boss?"),
        question("series-weekend", QuestionCategory.SERIES,
            "Which TV series would you binge in a weekend?",
            "Which TV series would you never want to live in?"),
        question("singers-party", QuestionCategory.SINGERS,
            "Which singer would start the best party?",
            "Which singer would make the best wedding guest?"),
        question("singers-duet", QuestionCategory.SINGERS,
            "Which singer would you choose for a duet?",
            "Which singer would you ask to write a song about you?")
    )

    private fun classic(key: String, normal: String, adversary: String) =
        BuiltInContent("classic-$key", GameModeType.CLASSIC, null, normal, adversary)

    private fun question(key: String, category: QuestionCategory, normal: String, adversary: String) =
        BuiltInContent(key, GameModeType.QUESTION, category, normal, adversary)
}
