package com.example.data

import kotlinx.coroutines.flow.Flow

class ScoreRepository(private val scoreDao: ScoreDao) {
    val topScores: Flow<List<HighScore>> = scoreDao.getTopScores()

    suspend fun insertScore(score: HighScore) {
        scoreDao.insertScore(score)
    }

    suspend fun clearScores() {
        scoreDao.clearAllScores()
    }
}
