package com.example.ui

import android.app.Application
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.HighScore
import com.example.data.ScoreRepository
import com.example.data.TetrisDatabase
import com.example.game.Piece
import com.example.game.TetrominoGenerator
import com.example.game.TetrominoType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class GameStatus {
    START,
    PLAYING,
    PAUSED,
    GAME_OVER
}

data class TetrisState(
    val board: Array<IntArray> = Array(20) { IntArray(10) { 0 } },
    val currentPiece: Piece? = null,
    val nextPiece: TetrominoType? = null,
    val holdPiece: TetrominoType? = null,
    val hasHeldThisTurn: Boolean = false,
    val score: Int = 0,
    val linesCleared: Int = 0,
    val level: Int = 1,
    val gameStatus: GameStatus = GameStatus.START,
    val isHapticEnabled: Boolean = true,
    val isRetroTheme: Boolean = true,
    val highScores: List<HighScore> = emptyList(),
    val showNamePrompt: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TetrisState) return false
        if (!board.contentDeepEquals(other.board)) return false
        if (currentPiece != other.currentPiece) return false
        if (nextPiece != other.nextPiece) return false
        if (holdPiece != other.holdPiece) return false
        if (hasHeldThisTurn != other.hasHeldThisTurn) return false
        if (score != other.score) return false
        if (linesCleared != other.linesCleared) return false
        if (level != other.level) return false
        if (gameStatus != other.gameStatus) return false
        if (isHapticEnabled != other.isHapticEnabled) return false
        if (isRetroTheme != other.isRetroTheme) return false
        if (highScores != other.highScores) return false
        if (showNamePrompt != other.showNamePrompt) return false
        return true
    }

    override fun hashCode(): Int {
        var result = board.contentDeepHashCode()
        result = 31 * result + (currentPiece?.hashCode() ?: 0)
        result = 31 * result + (nextPiece?.hashCode() ?: 0)
        result = 31 * result + (holdPiece?.hashCode() ?: 0)
        result = 31 * result + hasHeldThisTurn.hashCode()
        result = 31 * result + score
        result = 31 * result + linesCleared
        result = 31 * result + level
        result = 31 * result + gameStatus.hashCode()
        result = 31 * result + isHapticEnabled.hashCode()
        result = 31 * result + isRetroTheme.hashCode()
        result = 31 * result + highScores.hashCode()
        result = 31 * result + showNamePrompt.hashCode()
        return result
    }
}

class TetrisViewModel(application: Application) : AndroidViewModel(application) {
    private val database = TetrisDatabase.getDatabase(application)
    private val repository = ScoreRepository(database.scoreDao())

    private val _state = MutableStateFlow(TetrisState())
    val state: StateFlow<TetrisState> = _state.asStateFlow()

    private val generator = TetrominoGenerator()
    private var gameLoopJob: Job? = null
    private var hapticFeedback: HapticFeedback? = null

    init {
        // Collect high scores from repository
        viewModelScope.launch {
            repository.topScores.collect { scores ->
                _state.update { it.copy(highScores = scores) }
            }
        }
    }

    fun setHapticFeedback(feedback: HapticFeedback) {
        this.hapticFeedback = feedback
    }

    private fun triggerHaptic() {
        if (_state.value.isHapticEnabled) {
            try {
                hapticFeedback?.performHapticFeedback(HapticFeedbackType.LongPress)
            } catch (e: Exception) {
                // Background fallback
            }
        }
    }

    fun startGame() {
        _state.update {
            it.copy(
                board = Array(20) { IntArray(10) { 0 } },
                score = 0,
                linesCleared = 0,
                level = 1,
                holdPiece = null,
                hasHeldThisTurn = false,
                currentPiece = Piece(generator.next()),
                nextPiece = generator.next(),
                gameStatus = GameStatus.PLAYING,
                showNamePrompt = false
            )
        }
        startGameLoop()
    }

    fun pauseGame() {
        _state.update {
            if (it.gameStatus == GameStatus.PLAYING) {
                it.copy(gameStatus = GameStatus.PAUSED)
            } else if (it.gameStatus == GameStatus.PAUSED) {
                it.copy(gameStatus = GameStatus.PLAYING)
            } else {
                it
            }
        }
    }

    fun toggleHaptic() {
        _state.update { it.copy(isHapticEnabled = !it.isHapticEnabled) }
    }

    fun toggleTheme() {
        _state.update { it.copy(isRetroTheme = !it.isRetroTheme) }
    }

    fun moveLeft() {
        val current = _state.value.currentPiece ?: return
        if (_state.value.gameStatus != GameStatus.PLAYING) return

        val moved = current.copy(x = current.x - 1)
        if (isValidPosition(moved, _state.value.board)) {
            _state.update { it.copy(currentPiece = moved) }
            triggerHaptic()
        }
    }

    fun moveRight() {
        val current = _state.value.currentPiece ?: return
        if (_state.value.gameStatus != GameStatus.PLAYING) return

        val moved = current.copy(x = current.x + 1)
        if (isValidPosition(moved, _state.value.board)) {
            _state.update { it.copy(currentPiece = moved) }
            triggerHaptic()
        }
    }

    fun rotatePiece() {
        val current = _state.value.currentPiece ?: return
        if (_state.value.gameStatus != GameStatus.PLAYING) return

        val rotated = current.rotateClockwise()
        val offsets = listOf(
            Pair(0, 0),
            Pair(-1, 0),
            Pair(1, 0),
            Pair(0, -1),
            Pair(-2, 0),
            Pair(2, 0)
        )
        for (offset in offsets) {
            val testPiece = rotated.copy(
                x = rotated.x + offset.first,
                y = rotated.y + offset.second
            )
            if (isValidPosition(testPiece, _state.value.board)) {
                _state.update { it.copy(currentPiece = testPiece) }
                triggerHaptic()
                return
            }
        }
    }

    fun softDrop() {
        val current = _state.value.currentPiece ?: return
        if (_state.value.gameStatus != GameStatus.PLAYING) return

        val moved = current.copy(y = current.y + 1)
        if (isValidPosition(moved, _state.value.board)) {
            _state.update {
                it.copy(
                    currentPiece = moved,
                    score = it.score + 1
                )
            }
        } else {
            lockPiece()
        }
    }

    fun hardDrop() {
        val current = _state.value.currentPiece ?: return
        if (_state.value.gameStatus != GameStatus.PLAYING) return

        val ghostY = getGhostY(current, _state.value.board)
        val dropDistance = ghostY - current.y
        val droppedPiece = current.copy(y = ghostY)

        _state.update {
            it.copy(
                currentPiece = droppedPiece,
                score = it.score + (2 * dropDistance)
            )
        }
        triggerHaptic()
        lockPiece()
    }

    fun holdPiece() {
        val stateVal = _state.value
        val current = stateVal.currentPiece ?: return
        if (stateVal.gameStatus != GameStatus.PLAYING || stateVal.hasHeldThisTurn) return

        val currentHold = stateVal.holdPiece
        if (currentHold == null) {
            val next = stateVal.nextPiece ?: generator.next()
            _state.update {
                it.copy(
                    holdPiece = current.type,
                    currentPiece = Piece(next),
                    nextPiece = generator.next(),
                    hasHeldThisTurn = true
                )
            }
        } else {
            _state.update {
                it.copy(
                    holdPiece = current.type,
                    currentPiece = Piece(currentHold),
                    hasHeldThisTurn = true
                )
            }
        }
        triggerHaptic()
    }

    private fun lockPiece() {
        val stateVal = _state.value
        val current = stateVal.currentPiece ?: return
        val updatedBoard = Array(20) { r -> stateVal.board[r].clone() }

        val size = current.grid.size
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (current.grid[r][c] > 0) {
                    val boardX = current.x + c
                    val boardY = current.y + r
                    if (boardY in 0..19 && boardX in 0..9) {
                        updatedBoard[boardY][boardX] = current.type.id
                    }
                }
            }
        }

        // Check line clearing
        val (clearedBoard, lines) = selectAndClearLines(updatedBoard)
        val linePoints = when (lines) {
            1 -> 100 * stateVal.level
            2 -> 300 * stateVal.level
            3 -> 500 * stateVal.level
            4 -> 800 * stateVal.level // TETRIS!
            else -> 0
        }

        val totalLines = stateVal.linesCleared + lines
        val newLevel = (totalLines / 10) + 1

        val finalScore = stateVal.score + linePoints

        // Spawn next piece
        val spawnPiece = Piece(stateVal.nextPiece ?: generator.next())
        val isGameOver = !isValidPosition(spawnPiece, clearedBoard)

        _state.update {
            it.copy(
                board = clearedBoard,
                score = finalScore,
                linesCleared = totalLines,
                level = newLevel,
                hasHeldThisTurn = false,
                currentPiece = if (isGameOver) null else spawnPiece,
                nextPiece = generator.next(),
                gameStatus = if (isGameOver) GameStatus.GAME_OVER else GameStatus.PLAYING,
                showNamePrompt = isGameOver && finalScore > 0
            )
        }

        if (isGameOver) {
            gameLoopJob?.cancel()
        } else {
            // Trigger haptic drop settle feedback
            triggerHaptic()
        }
    }

    fun submitHighScore(name: String) {
        val finalScore = _state.value.score
        if (finalScore <= 0) return

        val formattedName = name.trim().ifEmpty { "BlockMaster" }
        viewModelScope.launch {
            repository.insertScore(
                HighScore(
                    playerName = formattedName,
                    score = finalScore,
                    linesCleared = _state.value.linesCleared,
                    level = _state.value.level
                )
            )
            _state.update {
                it.copy(
                    showNamePrompt = false,
                    gameStatus = GameStatus.START
                )
            }
        }
    }

    fun cancelNamePrompt() {
        _state.update {
            it.copy(
                showNamePrompt = false,
                gameStatus = GameStatus.START
            )
        }
    }

    fun clearScores() {
        viewModelScope.launch {
            repository.clearScores()
        }
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            while (true) {
                val speed = getSpeedForLevel(_state.value.level)
                delay(speed)
                if (_state.value.gameStatus == GameStatus.PLAYING) {
                    softDrop()
                }
            }
        }
    }

    private fun getSpeedForLevel(level: Int): Long {
        return when (level) {
            1 -> 800L
            2 -> 700L
            3 -> 600L
            4 -> 500L
            5 -> 420L
            6 -> 340L
            7 -> 260L
            8 -> 180L
            9 -> 120L
            else -> 80L
        }
    }

    private fun isValidPosition(piece: Piece, board: Array<IntArray>): Boolean {
        val size = piece.grid.size
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (piece.grid[r][c] > 0) {
                    val boardX = piece.x + c
                    val boardY = piece.y + r
                    if (boardX !in 0..9 || boardY >= 20) {
                        return false
                    }
                    if (boardY >= 0 && board[boardY][boardX] > 0) {
                        return false
                    }
                }
            }
        }
        return true
    }

    private fun getGhostY(piece: Piece, board: Array<IntArray>): Int {
        var ghostY = piece.y
        while (isValidPosition(piece.copy(y = ghostY + 1), board)) {
            ghostY++
        }
        return ghostY
    }

    fun getGhostY(): Int {
        val current = _state.value.currentPiece ?: return 0
        return getGhostY(current, _state.value.board)
    }

    private fun selectAndClearLines(board: Array<IntArray>): Pair<Array<IntArray>, Int> {
        val clearedBoard = Array(20) { IntArray(10) { 0 } }
        var linesCleared = 0
        var targetRow = 19
        for (r in 19 downTo 0) {
            val isRowFull = board[r].all { it > 0 }
            if (isRowFull) {
                linesCleared++
            } else {
                clearedBoard[targetRow] = board[r].clone()
                targetRow--
            }
        }
        return Pair(clearedBoard, linesCleared)
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
    }
}
