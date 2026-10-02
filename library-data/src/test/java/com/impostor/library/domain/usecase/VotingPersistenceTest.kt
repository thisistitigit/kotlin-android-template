package com.impostor.library.domain.usecase

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.impostor.library.data.local.AppDatabase
import com.impostor.library.data.local.VOTING_MIGRATION_1_2
import com.impostor.library.data.local.GUESS_MIGRATION_2_3
import com.impostor.library.data.repository.GameRepository
import com.impostor.library.data.repository.PlayerRepository
import com.impostor.library.domain.enums.GameModeType
import com.impostor.library.domain.enums.RoleType
import com.impostor.library.domain.model.VotingPhaseStatus
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class VotingPersistenceTest {
    private lateinit var db: AppDatabase
    private lateinit var repository: GameRepository

    @Before fun open() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java).build()
        repository = GameRepository(db)
    }
    @After fun close() { db.close() }

    private suspend fun round(size: Int, impostors: Int = 1, whites: Int = 0): Int {
        repository.prepareDefaults()
        val players = PlayerRepository(db.playerDao())
        val ids = (1..size).map { players.createPlayer("Player $it", null).toInt() }
        val game = CreateGameUseCase(repository)(ids, impostors, whites).toInt()
        return StartConfiguredRoundUseCase(repository, AssignRolesUseCase { it })(game, GameModeType.CLASSIC, null, null).toInt()
    }

    @Test fun votesAreLockedAndIndependentAcrossSequentialPhases() = runBlocking {
        val round = round(5, 2, 1)
        val first = repository.beginVoting(round)
        val target = first.voterIds.first()
        for (voter in first.voterIds) {
            SubmitVoteUseCase(repository)(round, first.phaseId, voter,
                if (voter == target) first.voterIds.last() else target)
            assertTrue(repository.votingSnapshot(round).pendingHandoff)
            assertNull(repository.votingSnapshot(round).revealedRole)
            repository.acknowledgeVote(round)
        }
        assertEquals(VotingPhaseStatus.RESULTS, repository.votingSnapshot(round).status)
        val revealed = repository.revealVotedRole(round)
        assertEquals(RoleType.IMPOSTOR, revealed.revealedRole)
        assertFalse(revealed.roundFinished)
        val next = repository.continueVoting(round)
        assertNotEquals(first.phaseId, next.phaseId)
        assertFalse(target in next.candidateIds)
        assertFalse(target in next.voterIds)
        assertTrue(next.ballots.isEmpty())
        assertEquals(5, repository.getVotes(round).size)
    }

    @Test fun tieStartsRestrictedRunoffWithoutDeletingBallots() = runBlocking {
        val round = round(4)
        val phase = repository.beginVoting(round)
        val ids = phase.voterIds
        val targets = listOf(ids[1], ids[0], ids[0], ids[1])
        ids.zip(targets).forEach { (voter, candidate) ->
            SubmitVoteUseCase(repository)(round, phase.phaseId, voter, candidate)
            repository.acknowledgeVote(round)
        }
        val tied = repository.votingSnapshot(round)
        assertEquals(VotingPhaseStatus.TIE, tied.status)
        val next = repository.continueVoting(round)
        assertEquals(ids.take(2), next.candidateIds)
        assertEquals(ids, next.voterIds)
        assertEquals(4, repository.getVotes(round).size)
    }

    @Test fun duplicateAndPrematureRevelationCannotModifyPersistedVote() = runBlocking {
        val round = round(3)
        val phase = repository.beginVoting(round)
        val voter = phase.voterIds[0]
        SubmitVoteUseCase(repository)(round, phase.phaseId, voter, phase.voterIds[1])
        assertTrue(runCatching {
            SubmitVoteUseCase(repository)(round, phase.phaseId, voter, phase.voterIds[2])
        }.isFailure)
        assertTrue(runCatching { repository.revealVotedRole(round) }.isFailure)
        assertEquals(phase.voterIds[1], repository.getVotes(round).single().votedGamePlayerId)
    }

    @Test fun migrationPreservesLegacyVotesAndValidatesRoomSchema() = runBlocking {
        db.close()
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val name = "migration-${java.util.UUID.randomUUID()}.db"
        db = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
        repository = GameRepository(db)
        val round = round(3)
        val phase = repository.beginVoting(round)
        SubmitVoteUseCase(repository)(round, phase.phaseId, phase.voterIds[0], phase.voterIds[1])
        db.close()
        android.database.sqlite.SQLiteDatabase.openDatabase(context.getDatabasePath(name).path, null, 0).use { legacy ->
            legacy.execSQL("CREATE TABLE legacy_votes AS SELECT vote_id, round_id, voter_game_player_id, voted_game_player_id, created_at FROM votes")
            legacy.execSQL("DROP TABLE votes")
            legacy.execSQL("DROP TABLE voting_phases")
            legacy.execSQL("ALTER TABLE legacy_votes RENAME TO votes")
            legacy.version = 1
        }
        db = Room.databaseBuilder(context, AppDatabase::class.java, name).addMigrations(VOTING_MIGRATION_1_2, GUESS_MIGRATION_2_3).build()
        val migrated = db.voteDao().getVotes(round).single()
        assertEquals(phase.voterIds[1], migrated.votedGamePlayerId)
        assertEquals(db.voteDao().getPhases(round).single().id, migrated.phaseId)
        db.close()
        context.deleteDatabase(name)
        Unit
    }

    @Test fun finalRevelationScoresRoundExactlyOnce() = runBlocking {
        val round = round(3)
        val phase = repository.beginVoting(round)
        val ids = phase.voterIds
        ids.forEach { voter ->
            SubmitVoteUseCase(repository)(round, phase.phaseId, voter, if (voter == ids[0]) ids[1] else ids[0])
            repository.acknowledgeVote(round)
        }
        val revealed = repository.revealVotedRole(round)
        assertTrue(revealed.roundFinished)
        assertEquals(0, revealed.remainingAdversaries)
        val scoreCount = db.scoreDao().countRoundScores(round)
        val result = FinishRoundUseCase(repository, CalculateScoreUseCase())(round)
        assertEquals(setOf(RoleType.CIVILIAN), result.winningRoles)
        assertEquals(scoreCount, db.scoreDao().countRoundScores(round))
        assertTrue(runCatching { repository.revealVotedRole(round) }.isFailure)
    }

    @Test fun questionsRoundNeverAssignsMrWhiteWhenSwitchingMode() = runBlocking {
        val classic = round(4, 1, 1)
        val game = requireNotNull(db.roundDao().getRound(classic)).gameId
        val questions = StartConfiguredRoundUseCase(repository, AssignRolesUseCase { it })(
            game, GameModeType.QUESTION, null, null).toInt()
        assertFalse(repository.getAssignments(questions).any { it.role.type == RoleType.MR_WHITE })
        assertEquals(2, requireNotNull(db.roundDao().getRound(questions)).roundNumber)
    }
    private suspend fun whiteRound(whites: Int = 1): Int {
        repository.prepareDefaults()
        val players = PlayerRepository(db.playerDao())
        val ids = (1..5).map { players.createPlayer("Player $it", null).toInt() }
        val game = CreateGameUseCase(repository)(ids, 1, whites).toInt()
        val content = com.impostor.library.data.repository.ContentRepository(db.contentDao())
            .saveCustomContent(GameModeType.CLASSIC, "Blue moon", "Red sun")
        return StartConfiguredRoundUseCase(repository, AssignRolesUseCase { it })(
            game, GameModeType.CLASSIC, content.first, content.second).toInt()
    }

    private suspend fun eliminate(roundId: Int, target: Int) {
        val phase = repository.votingSnapshot(roundId)
        for (voter in phase.voterIds) {
            val candidate = if (voter == target) phase.candidateIds.first { it != target } else target
            SubmitVoteUseCase(repository)(roundId, phase.phaseId, voter, candidate)
            repository.acknowledgeVote(roundId)
        }
        repository.revealVotedRole(roundId)
    }

    @Test fun whiteGuessIsPrivateRequiredAndScoresExactlyOnce() = runBlocking {
        val round = whiteRound()
        val white = repository.getAssignments(round).single { it.role.type == RoleType.MR_WHITE }.gamePlayer.id
        repository.beginVoting(round)
        eliminate(round, white)
        val pending = repository.votingSnapshot(round)
        assertEquals(VotingPhaseStatus.GUESS_PENDING, pending.status)
        assertEquals(RoleType.MR_WHITE, pending.revealedRole)
        assertFalse(pending.roundFinished)
        assertEquals(0, db.scoreDao().countRoundScores(round))
        assertTrue(runCatching { repository.continueVoting(round) }.isFailure)
        assertTrue(runCatching { repository.submitMrWhiteGuess(round, pending.voterIds.first(), "Blue moon") }.isFailure)
        val result = repository.submitMrWhiteGuess(round, white, "  BLUE   moon  ")
        assertTrue(result.roundFinished)
        val finished = FinishRoundUseCase(repository, CalculateScoreUseCase())(round)
        assertEquals(white, finished.mrWhiteWinnerId)
        assertEquals(setOf(RoleType.MR_WHITE), finished.winningRoles)
        assertEquals(9, finished.scores[white]) // +2 correct vote and +7 individual victory
        assertEquals(7, db.scoreDao().getRoundScores(round).single {
            it.gamePlayerId == white && it.reason == com.impostor.library.domain.enums.ScoreReason.MR_WHITE_WIN
        }.points)
        val count = db.scoreDao().countRoundScores(round)
        assertTrue(runCatching { repository.submitMrWhiteGuess(round, white, "Blue moon") }.isFailure)
        assertEquals(count, db.scoreDao().countRoundScores(round))
    }

    @Test fun wrongGuessContinuesWithoutScoringOrRevealingPublicContent() = runBlocking {
        val round = whiteRound()
        val assignments = repository.getAssignments(round)
        val white = assignments.single { it.role.type == RoleType.MR_WHITE }.gamePlayer.id
        val impostor = assignments.single { it.role.type == RoleType.IMPOSTOR }.gamePlayer.id
        repository.beginVoting(round)
        eliminate(round, white)
        val result = repository.submitMrWhiteGuess(round, white, "moon")
        assertFalse(result.roundFinished)
        assertEquals(0, db.scoreDao().countRoundScores(round))
        val next = repository.continueVoting(round)
        assertFalse(white in next.voterIds || white in next.candidateIds)
        eliminate(round, impostor)
        assertTrue(repository.votingSnapshot(round).roundFinished)
        assertEquals(setOf(RoleType.CIVILIAN), requireNotNull(repository.getVotingResult(round)).winningRoles)
    }

    @Test fun nextRoundKeepsPlayersAndScoresAcrossModeChanges() = runBlocking {
        val previous = whiteRound()
        val assignments = repository.getAssignments(previous)
        val white = assignments.single { it.role.type == RoleType.MR_WHITE }.gamePlayer.id
        val gameId = requireNotNull(db.roundDao().getRound(previous)).gameId
        val nextRound = PrepareNextRoundUseCase(repository,
            com.impostor.library.data.repository.ContentRepository(db.contentDao()),
            StartConfiguredRoundUseCase(repository, AssignRolesUseCase { it }))
        assertTrue(runCatching { nextRound(gameId, previous, GameModeType.QUESTION,
            RoundContentInput("Who is funniest?", "Who is quietest?")) }.isFailure)
        repository.beginVoting(previous)
        eliminate(previous, white)
        repository.submitMrWhiteGuess(previous, white, "Blue moon")
        val before = repository.classification(gameId)
        val next = nextRound(gameId, previous, GameModeType.QUESTION,
            RoundContentInput("Who is funniest?", "Who is quietest?"))
        assertEquals(gameId, next.gameId)
        assertEquals(assignments.map { it.gamePlayer.id }.toSet(), next.players.map { it.gamePlayerId }.toSet())
        assertFalse(next.players.any { it.role == RoleType.MR_WHITE })
        assertEquals(before, repository.classification(gameId))
        assertEquals(2, requireNotNull(db.roundDao().getRound(next.roundId)).roundNumber)
        assertTrue(runCatching { nextRound(gameId, previous, GameModeType.QUESTION,
            RoundContentInput("Who is funniest?", "Who is quietest?")) }.isFailure)
    }

    @Test fun lastAdversaryWhiteStillGetsOneGuessBeforeRoundEnds() = runBlocking {
        val round = whiteRound()
        val roles = repository.getAssignments(round)
        val impostor = roles.single { it.role.type == RoleType.IMPOSTOR }.gamePlayer.id
        val white = roles.single { it.role.type == RoleType.MR_WHITE }.gamePlayer.id
        repository.beginVoting(round)
        eliminate(round, impostor)
        repository.continueVoting(round)
        eliminate(round, white)
        assertEquals(VotingPhaseStatus.GUESS_PENDING, repository.votingSnapshot(round).status)
        assertFalse(repository.votingSnapshot(round).roundFinished)
        assertEquals(0, db.scoreDao().countRoundScores(round))
        val finished = repository.submitMrWhiteGuess(round, white, "Wrong word")
        assertTrue(finished.roundFinished)
        assertEquals(setOf(RoleType.CIVILIAN), requireNotNull(repository.getVotingResult(round)).winningRoles)
    }

    @Test fun multipleWhitesHaveIndependentAttemptsAndHistory() = runBlocking {
        val round = whiteRound(2)
        val roles = repository.getAssignments(round)
        val whites = roles.filter { it.role.type == RoleType.MR_WHITE }.map { it.gamePlayer.id }
        repository.beginVoting(round)
        for (white in whites) {
            eliminate(round, white)
            val result = repository.submitMrWhiteGuess(round, white, "Wrong word")
            assertFalse(result.roundFinished)
            repository.continueVoting(round)
        }
        val phases = db.voteDao().getPhases(round).filter { it.guessText != null }
        assertEquals(2, phases.size)
        assertEquals(whites.toSet(), phases.map { it.selectedPlayerId }.toSet())
        assertTrue(phases.all { it.guessCorrect == false })
        assertTrue(repository.getVotes(round).isNotEmpty())
    }

    @Test fun questionCategoryIsPersistedAndCannotBeAppliedToClassic() = runBlocking {
        val content = com.impostor.library.data.repository.ContentRepository(db.contentDao())
        content.saveCustomContent(GameModeType.QUESTION, "Who is the best scorer?", "Who is the best defender?",
            com.impostor.library.domain.enums.QuestionCategory.NBA)
        val pack = db.contentDao().getPacks().single()
        val set = db.contentDao().getSets(pack.id, GameModeType.QUESTION).single()
        assertEquals("nba", set.category)
        assertTrue(runCatching {
            content.saveCustomContent(GameModeType.CLASSIC, "Moon", "Sun",
                com.impostor.library.domain.enums.QuestionCategory.NBA)
        }.isFailure)
        assertEquals(1, db.contentDao().getPacks().size)
    }

}
