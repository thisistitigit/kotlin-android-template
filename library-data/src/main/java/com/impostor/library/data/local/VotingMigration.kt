package com.impostor.library.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Preserves every legacy ballot in a separate phase rather than deleting history. */
val VOTING_MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS voting_phases (
                phase_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                round_id INTEGER NOT NULL, target TEXT NOT NULL, voters TEXT NOT NULL,
                candidates TEXT NOT NULL, status TEXT NOT NULL, selected_player_id INTEGER,
                pending_handoff INTEGER NOT NULL,
                FOREIGN KEY(round_id) REFERENCES rounds(round_id) ON DELETE CASCADE)
        """.trimIndent())
        db.execSQL("CREATE INDEX index_voting_phases_round_id ON voting_phases(round_id)")
        db.execSQL("""
            INSERT INTO voting_phases (round_id, target, voters, candidates, status, pending_handoff)
            SELECT DISTINCT v.round_id, 'IMPOSTOR',
                COALESCE((SELECT GROUP_CONCAT(game_player_id) FROM round_assignments a WHERE a.round_id = v.round_id), ''),
                COALESCE((SELECT GROUP_CONCAT(game_player_id) FROM round_assignments a WHERE a.round_id = v.round_id), ''),
                'OPEN', 0 FROM votes v
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE votes_new (
                vote_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, round_id INTEGER NOT NULL,
                phase_id INTEGER NOT NULL, voter_game_player_id INTEGER NOT NULL,
                voted_game_player_id INTEGER NOT NULL, created_at INTEGER NOT NULL,
                FOREIGN KEY(round_id) REFERENCES rounds(round_id) ON DELETE CASCADE,
                FOREIGN KEY(phase_id) REFERENCES voting_phases(phase_id) ON DELETE CASCADE,
                FOREIGN KEY(voter_game_player_id) REFERENCES game_players(game_player_id) ON DELETE CASCADE,
                FOREIGN KEY(voted_game_player_id) REFERENCES game_players(game_player_id) ON DELETE CASCADE)
        """.trimIndent())
        db.execSQL("""
            INSERT INTO votes_new SELECT v.vote_id, v.round_id, p.phase_id,
                v.voter_game_player_id, v.voted_game_player_id, v.created_at
            FROM votes v JOIN voting_phases p ON p.round_id = v.round_id
        """.trimIndent())
        db.execSQL("DROP TABLE votes")
        db.execSQL("ALTER TABLE votes_new RENAME TO votes")
        listOf("round_id", "phase_id", "voter_game_player_id", "voted_game_player_id").forEach {
            db.execSQL("CREATE INDEX index_votes_$it ON votes($it)")
        }
        db.execSQL("""
            CREATE UNIQUE INDEX index_votes_phase_id_voter_game_player_id ON votes(phase_id, voter_game_player_id)
        """.trimIndent())
    }
}
