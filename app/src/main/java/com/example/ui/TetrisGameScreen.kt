package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.TetrominoType
import com.example.data.HighScore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min

// Glowing retro colors
val CyberBg = Color(0xFF0F111A)
val BoardBg = Color(0xFF131522)
val RetroPanelBg = Color(0xFF1B1D2E)
val PrimaryNeon = Color(0xFF00FFCC)
val AccentNeon = Color(0xFFFF007F)
val BorderNeon = Color(0xFF1E2640)

@Composable
fun TetrisGameScreen(
    modifier: Modifier = Modifier,
    viewModel: TetrisViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val haptic = LocalHapticFeedback.current

    // Share haptic with VM
    LaunchedEffect(haptic) {
        viewModel.setHapticFeedback(haptic)
    }

    val isRetro = state.isRetroTheme
    val backgroundColor = if (isRetro) CyberBg else Color(0xFFFEF7FF)
    val panelColor = if (isRetro) RetroPanelBg else Color(0xFFF3EDF7)
    val textColor = if (isRetro) Color.White else Color(0xFF1D1B20)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val isLandscape = maxWidth >= 600.dp

        if (isLandscape) {
            // Adaptive Landscape View
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Panel: Hold & Settings
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LogoHeader(isRetro = isRetro)

                    Spacer(modifier = Modifier.height(16.dp))

                    HoldPieceCard(
                        piece = state.holdPiece,
                        isRetro = isRetro,
                        isHeldDisabled = state.hasHeldThisTurn,
                        onHold = { viewModel.holdPiece() }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    SettingsCard(
                        isHaptic = state.isHapticEnabled,
                        isRetro = state.isRetroTheme,
                        onToggleHaptic = { viewModel.toggleHaptic() },
                        onToggleTheme = { viewModel.toggleTheme() }
                    )
                }

                // Center Panel: The Game Board
                Box(
                    modifier = Modifier
                        .weight(1.5f)
                        .fillMaxHeight()
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GameBoardContainer(
                        state = state,
                        isRetro = isRetro,
                        onMoveLeft = { viewModel.moveLeft() },
                        onMoveRight = { viewModel.moveRight() },
                        onRotate = { viewModel.rotatePiece() },
                        onSoftDrop = { viewModel.softDrop() },
                        onHardDrop = { viewModel.hardDrop() },
                        onHold = { viewModel.holdPiece() },
                        onPause = { viewModel.pauseGame() },
                        onStart = { viewModel.startGame() }
                    )
                }

                // Right Panel: Next Piece, Stats, Control Knobs
                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    NextPieceCard(piece = state.nextPiece, isRetro = isRetro)

                    Spacer(modifier = Modifier.height(16.dp))

                    StatsCard(
                        score = state.score,
                        level = state.level,
                        lines = state.linesCleared,
                        isRetro = isRetro
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    LandscapeControls(
                        onMoveLeft = { viewModel.moveLeft() },
                        onMoveRight = { viewModel.moveRight() },
                        onRotate = { viewModel.rotatePiece() },
                        onSoftDrop = { viewModel.softDrop() },
                        onHardDrop = { viewModel.hardDrop() },
                        isRetro = isRetro
                    )
                }
            }
        } else {
            // Adaptive Portrait View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Utilities Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LogoHeader(isRetro = isRetro)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.toggleHaptic() },
                            modifier = Modifier
                                .testTag("haptic_toggle")
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isRetro) Color(0xFF1B1D2E) else Color(0xFFF3EDF7))
                        ) {
                            Text(
                                text = if (state.isHapticEnabled) "📳" else "🔇",
                                fontSize = 16.sp
                            )
                        }
                        IconButton(
                            onClick = { viewModel.toggleTheme() },
                            modifier = Modifier
                                .testTag("theme_toggle")
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isRetro) Color(0xFF1B1D2E) else Color(0xFFF3EDF7))
                        ) {
                            Text(
                                text = if (isRetro) "🌌" else "☀️",
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                // Previews & Info Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HoldPieceCard(
                        piece = state.holdPiece,
                        isRetro = isRetro,
                        isHeldDisabled = state.hasHeldThisTurn,
                        onHold = { viewModel.holdPiece() },
                        compact = true
                    )

                    StatsRowCompact(
                        score = state.score,
                        level = state.level,
                        lines = state.linesCleared,
                        isRetro = isRetro
                    )

                    NextPieceCard(
                        piece = state.nextPiece,
                        isRetro = isRetro,
                        compact = true
                    )
                }

                // Dynamic Board Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GameBoardContainer(
                        state = state,
                        isRetro = isRetro,
                        onMoveLeft = { viewModel.moveLeft() },
                        onMoveRight = { viewModel.moveRight() },
                        onRotate = { viewModel.rotatePiece() },
                        onSoftDrop = { viewModel.softDrop() },
                        onHardDrop = { viewModel.hardDrop() },
                        onHold = { viewModel.holdPiece() },
                        onPause = { viewModel.pauseGame() },
                        onStart = { viewModel.startGame() }
                    )
                }

                // Bottom Circular Controls Layout (Arcade Console Style)
                CircleControls(
                    onMoveLeft = { viewModel.moveLeft() },
                    onMoveRight = { viewModel.moveRight() },
                    onRotate = { viewModel.rotatePiece() },
                    onSoftDrop = { viewModel.softDrop() },
                    onHardDrop = { viewModel.hardDrop() },
                    onHold = { viewModel.holdPiece() },
                    isRetro = isRetro
                )
            }
        }

        // Overlay Game States (Paused, Start, HighScores list, Core Overlay dialogs)
        OverlaysContainer(
            state = state,
            isRetro = isRetro,
            onStart = { viewModel.startGame() },
            onPause = { viewModel.pauseGame() },
            onClearScores = { viewModel.clearScores() }
        )

        // Game Over Save Name Prompt Dialog
        if (state.showNamePrompt) {
            NamePromptDialog(
                score = state.score,
                isRetro = isRetro,
                onSubmit = { name -> viewModel.submitHighScore(name) },
                onDismiss = { viewModel.cancelNamePrompt() }
            )
        }
    }
}

@Composable
fun LogoIconSleek() {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF6750A4)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Box(modifier = Modifier.size(9.dp).background(Color.White, RoundedCornerShape(2.dp)))
                Box(modifier = Modifier.size(9.dp).background(Color.White, RoundedCornerShape(2.dp)))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Box(modifier = Modifier.size(9.dp).background(Color.White, RoundedCornerShape(2.dp)))
                Box(modifier = Modifier.size(9.dp).background(Color.White, RoundedCornerShape(2.dp)))
            }
        }
    }
}

@Composable
fun LogoHeader(isRetro: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (!isRetro) {
            LogoIconSleek()
            Text(
                text = "Tetris Pro",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF1D1B20),
                modifier = Modifier.padding(start = 2.dp)
            )
        } else {
            Text(
                text = "TETRIS",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                color = PrimaryNeon,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 4.dp)
            )
            Text(
                text = "⚡",
                fontSize = 20.sp,
                color = AccentNeon
            )
        }
    }
}

@Composable
fun HoldPieceCard(
    piece: TetrominoType?,
    isRetro: Boolean,
    isHeldDisabled: Boolean,
    onHold: () -> Unit,
    compact: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(enabled = !isHeldDisabled) { onHold() }
    ) {
        Text(
            text = "HOLD",
            fontFamily = if (isRetro) FontFamily.Monospace else FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = if (compact) 10.sp else 12.sp,
            color = if (isHeldDisabled) Color.Gray else if (isRetro) AccentNeon else Color(0xFF6750A4)
        )
        Spacer(modifier = Modifier.height(4.dp))
        MiniPiecePreview(
            pieceType = piece,
            isRetro = isRetro,
            modifier = Modifier
                .testTag("hold_button")
                .size(if (compact) 56.dp else 76.dp)
        )
    }
}

@Composable
fun NextPieceCard(
    piece: TetrominoType?,
    isRetro: Boolean,
    compact: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "NEXT",
            fontFamily = if (isRetro) FontFamily.Monospace else FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = if (compact) 10.sp else 12.sp,
            color = if (isRetro) PrimaryNeon else Color(0xFF6750A4)
        )
        Spacer(modifier = Modifier.height(4.dp))
        MiniPiecePreview(
            pieceType = piece,
            isRetro = isRetro,
            modifier = Modifier.size(if (compact) 56.dp else 76.dp)
        )
    }
}

@Composable
fun MiniPiecePreview(
    pieceType: TetrominoType?,
    isRetro: Boolean,
    modifier: Modifier = Modifier
) {
    val containerBg = if (isRetro) retroCardBg() else Color(0xFFF3EDF7)
    val strokeColor = if (isRetro) BorderNeon else Color(0xFFEADDFF)
    val cardShape = if (isRetro) RoundedCornerShape(8.dp) else RoundedCornerShape(16.dp)

    Card(
        modifier = modifier,
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = BorderStroke(1.dp, strokeColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            if (pieceType != null) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val grid = pieceType.rawGrid
                    val rows = grid.size
                    val cols = grid[0].size

                    var minR = rows
                    var maxR = -1
                    var minC = cols
                    var maxC = -1

                    for (r in 0 until rows) {
                        for (c in 0 until cols) {
                            if (grid[r][c] > 0) {
                                if (r < minR) minR = r
                                if (r > maxR) maxR = r
                                if (c < minC) minC = c
                                if (c > maxC) maxC = c
                            }
                        }
                    }

                    val activeRows = if (maxR >= minR) maxR - minR + 1 else 0
                    val activeCols = if (maxC >= minC) maxC - minC + 1 else 0

                    if (activeRows > 0 && activeCols > 0) {
                        val maxDim = maxOf(activeRows, activeCols, 2)
                        val cellSize = size.width / maxDim
                        val startX = (size.width - activeCols * cellSize) / 2f
                        val startY = (size.height - activeRows * cellSize) / 2f

                        for (r in minR..maxR) {
                            for (c in minC..maxC) {
                                if (grid[r][c] > 0) {
                                    val drawX = startX + (c - minC) * cellSize
                                    val drawY = startY + (r - minR) * cellSize
                                    drawRoundRect(
                                        color = getPieceColor(pieceType.id, isRetro),
                                        topLeft = Offset(drawX + 1.dp.toPx(), drawY + 1.dp.toPx()),
                                        size = Size(cellSize - 2.dp.toPx(), cellSize - 2.dp.toPx()),
                                        cornerRadius = CornerRadius(2.dp.toPx())
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "---",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color.LightGray.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun StatsCard(
    score: Int,
    level: Int,
    lines: Int,
    isRetro: Boolean
) {
    val cardShape = if (isRetro) RoundedCornerShape(12.dp) else RoundedCornerShape(24.dp)
    val containerBg = if (isRetro) retroCardBg() else Color(0xFFF3EDF7)
    val borderCol = if (isRetro) BorderNeon else Color(0xFFEADDFF)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = BorderStroke(1.dp, borderCol)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isRetro) {
                StatItem(label = "SCORE", value = score.toString(), accentColor = PrimaryNeon, isRetro = true)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        StatItem(label = "LEVEL", value = level.toString(), accentColor = AccentNeon, isRetro = true)
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        StatItem(label = "LINES", value = lines.toString(), accentColor = Color.Yellow, isRetro = true)
                    }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SCORE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF49454F),
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Text(
                        text = score.toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1D1B20),
                        fontFamily = FontFamily.SansSerif
                    )
                }

                Box(modifier = Modifier.width(32.dp).height(1.dp).background(Color(0xFFEADDFF)))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "LEVEL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF49454F),
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Text(
                        text = String.format("%02d", level),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF6750A4),
                        fontFamily = FontFamily.SansSerif
                    )
                }

                Box(modifier = Modifier.width(32.dp).height(1.dp).background(Color(0xFFEADDFF)))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "LINES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF49454F),
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Text(
                        text = lines.toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1D1B20),
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    accentColor: Color,
    isRetro: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontFamily = if (isRetro) FontFamily.Monospace else FontFamily.SansSerif,
            fontWeight = if (isRetro) FontWeight.Normal else FontWeight.Bold,
            fontSize = 9.sp,
            color = if (isRetro) Color.LightGray else Color(0xFF49454F)
        )
        Text(
            text = value,
            fontFamily = if (isRetro) FontFamily.Monospace else FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = if (isRetro) accentColor else Color(0xFF1D1B20)
        )
    }
}

@Composable
fun StatsRowCompact(
    score: Int,
    level: Int,
    lines: Int,
    isRetro: Boolean
) {
    val panelBg = if (isRetro) retroCardBg() else Color(0xFFF3EDF7)
    val cardShape = if (isRetro) RoundedCornerShape(8.dp) else RoundedCornerShape(16.dp)
    val borderCol = if (isRetro) BorderNeon else Color(0xFFEADDFF)

    Card(
        modifier = Modifier
            .widthIn(max = 220.dp)
            .height(56.dp),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = panelBg),
        border = BorderStroke(1.dp, borderCol)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("PTS", fontSize = 8.sp, fontFamily = if (isRetro) FontFamily.Monospace else FontFamily.SansSerif, fontWeight = FontWeight.Bold, color = if (isRetro) Color.Gray else Color(0xFF49454F))
                Text(score.toString(), fontSize = 13.sp, fontFamily = if (isRetro) FontFamily.Monospace else FontFamily.SansSerif, fontWeight = FontWeight.Bold, color = if (isRetro) PrimaryNeon else Color(0xFF1D1B20))
            }
            Box(modifier = Modifier.fillMaxHeight(0.5f).width(1.dp).background(if (isRetro) Color.Gray.copy(alpha = 0.3f) else Color(0xFFEADDFF)))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("LVL", fontSize = 8.sp, fontFamily = if (isRetro) FontFamily.Monospace else FontFamily.SansSerif, fontWeight = FontWeight.Bold, color = if (isRetro) Color.Gray else Color(0xFF49454F))
                Text(level.toString(), fontSize = 13.sp, fontFamily = if (isRetro) FontFamily.Monospace else FontFamily.SansSerif, fontWeight = FontWeight.Bold, color = if (isRetro) AccentNeon else Color(0xFF6750A4))
            }
            Box(modifier = Modifier.fillMaxHeight(0.5f).width(1.dp).background(if (isRetro) Color.Gray.copy(alpha = 0.3f) else Color(0xFFEADDFF)))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("LNS", fontSize = 8.sp, fontFamily = if (isRetro) FontFamily.Monospace else FontFamily.SansSerif, fontWeight = FontWeight.Bold, color = if (isRetro) Color.Gray else Color(0xFF49454F))
                Text(lines.toString(), fontSize = 13.sp, fontFamily = if (isRetro) FontFamily.Monospace else FontFamily.SansSerif, fontWeight = FontWeight.Bold, color = if (isRetro) Color.Yellow else Color(0xFF1D1B20))
            }
        }
    }
}

@Composable
fun GameBoardContainer(
    state: TetrisState,
    isRetro: Boolean,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onRotate: () -> Unit,
    onSoftDrop: () -> Unit,
    onHardDrop: () -> Unit,
    onHold: () -> Unit,
    onPause: () -> Unit,
    onStart: () -> Unit
) {
    val borderColor = if (isRetro) BorderNeon else Color(0xFFEADDFF)
    val outerBg = if (isRetro) BoardBg else Color(0xFF211F26)
    val borderThickness = if (isRetro) 2.dp else 4.dp
    val boardShape = if (isRetro) RoundedCornerShape(12.dp) else RoundedCornerShape(28.dp)

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .aspectRatio(0.5f)
            .clip(boardShape)
            .background(outerBg)
            .border(borderThickness, borderColor, boardShape)
            .padding(1.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellWidth = size.width / 10f
            val cellHeight = size.height / 20f
            val cellSize = min(cellWidth, cellHeight)

            val boardWidth = cellSize * 10f
            val boardHeight = cellSize * 20f
            val offsetX = (size.width - boardWidth) / 2f
            val offsetY = (size.height - boardHeight) / 2f

            // 1. Draw Grid Lines (Background decoration)
            for (r in 0..20) {
                val y = offsetY + r * cellSize
                drawLine(
                    color = if (isRetro) Color(0xFF1B233D).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f),
                    start = Offset(offsetX, y),
                    end = Offset(offsetX + boardWidth, y),
                    strokeWidth = 0.5.dp.toPx()
                )
            }
            for (c in 0..10) {
                val x = offsetX + c * cellSize
                drawLine(
                    color = if (isRetro) Color(0xFF1B233D).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f),
                    start = Offset(x, offsetY),
                    end = Offset(x, offsetY + boardHeight),
                    strokeWidth = 0.5.dp.toPx()
                )
            }

            val board = state.board

            // 2. Draw Locked Settled Blocks
            for (r in 0 until 20) {
                for (c in 0 until 10) {
                    val id = board[r][c]
                    if (id > 0) {
                        val drawX = offsetX + c * cellSize
                        val drawY = offsetY + r * cellSize
                        drawBlockCell(
                            color = getPieceColor(id, isRetro),
                            topLeft = Offset(drawX, drawY),
                            size = cellSize,
                            isRetro = isRetro
                        )
                    }
                }
            }

            // 3. Draw Ghost Piece Outline (Previews Landing Site)
            val current = state.currentPiece
            if (current != null && state.gameStatus == GameStatus.PLAYING) {
                val ghostY = getGhostY(current, board)
                val sizeVal = current.grid.size
                for (r in 0 until sizeVal) {
                    for (c in 0 until sizeVal) {
                        if (current.grid[r][c] > 0) {
                            val boardX = current.x + c
                            val boardY = ghostY + r
                            if (boardY in 0..19 && boardX in 0..9) {
                                val drawX = offsetX + boardX * cellSize
                                val drawY = offsetY + boardY * cellSize
                                drawRoundRect(
                                    color = getPieceColor(current.type.id, isRetro).copy(alpha = 0.5f),
                                    topLeft = Offset(drawX + 1.dp.toPx(), drawY + 1.dp.toPx()),
                                    size = Size(cellSize - 2.dp.toPx(), cellSize - 2.dp.toPx()),
                                    cornerRadius = CornerRadius(4.dp.toPx()),
                                    style = Stroke(width = 1.5.dp.toPx())
                                )
                            }
                        }
                    }
                }
            }

            // 4. Draw Moving Active Piece Blocks
            if (current != null && state.gameStatus == GameStatus.PLAYING) {
                val sizeVal = current.grid.size
                for (r in 0 until sizeVal) {
                    for (c in 0 until sizeVal) {
                        if (current.grid[r][c] > 0) {
                            val boardX = current.x + c
                            val boardY = current.y + r
                            if (boardY in 0..19 && boardX in 0..9) {
                                val drawX = offsetX + boardX * cellSize
                                val drawY = offsetY + boardY * cellSize
                                drawBlockCell(
                                    color = getPieceColor(current.type.id, isRetro),
                                    topLeft = Offset(drawX, drawY),
                                    size = cellSize,
                                    isRetro = isRetro
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Play / Pause Floating Toggle inside Board
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            IconButton(
                onClick = { onPause() },
                modifier = Modifier
                    .testTag("pause_button")
                    .size(36.dp)
                    .background(if (isRetro) Color.Black.copy(alpha = 0.5f) else Color(0xFF6750A4).copy(alpha = 0.85f), CircleShape)
            ) {
                Text(
                    text = if (state.gameStatus == GameStatus.PLAYING) "⏸" else "▶",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}

private fun getGhostY(piece: com.example.game.Piece, board: Array<IntArray>): Int {
    var ghostY = piece.y
    while (isValidPosition(piece.copy(y = ghostY + 1), board)) {
        ghostY++
    }
    return ghostY
}

private fun isValidPosition(piece: com.example.game.Piece, board: Array<IntArray>): Boolean {
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

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBlockCell(
    color: Color,
    topLeft: Offset,
    size: Float,
    isRetro: Boolean
) {
    val padding = 1.dp.toPx()
    val drawSize = size - 2 * padding
    if (isRetro) {
        // Neon Arcade style: Rounded glowing blocks with thick colored borders
        drawRoundRect(
            color = color.copy(alpha = 0.2f),
            topLeft = Offset(topLeft.x + padding, topLeft.y + padding),
            size = Size(drawSize, drawSize),
            cornerRadius = CornerRadius(4.dp.toPx())
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(topLeft.x + padding, topLeft.y + padding),
            size = Size(drawSize, drawSize),
            cornerRadius = CornerRadius(4.dp.toPx()),
            style = Stroke(width = 1.5.dp.toPx())
        )
        // Accent dot inside
        drawRoundRect(
            color = color,
            topLeft = Offset(topLeft.x + size / 2.5f, topLeft.y + size / 2.5f),
            size = Size(size / 5f, size / 5f),
            cornerRadius = CornerRadius(1.dp.toPx())
        )
    } else {
        // Modern Solid M3 Block
        drawRoundRect(
            color = color,
            topLeft = Offset(topLeft.x + padding, topLeft.y + padding),
            size = Size(drawSize, drawSize),
            cornerRadius = CornerRadius(4.dp.toPx())
        )
        // Highlight layer on top-left edge
        drawRoundRect(
            color = Color.White.copy(alpha = 0.35f),
            topLeft = Offset(topLeft.x + padding + 1.dp.toPx(), topLeft.y + padding + 1.dp.toPx()),
            size = Size(drawSize - 2.dp.toPx(), drawSize - 2.dp.toPx()),
            cornerRadius = CornerRadius(3.dp.toPx()),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

@Composable
fun CircleControls(
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onRotate: () -> Unit,
    onSoftDrop: () -> Unit,
    onHardDrop: () -> Unit,
    onHold: () -> Unit,
    isRetro: Boolean
) {
    val consoleBg = if (isRetro) retroCardBg() else Color(0xFFF3EDF7)
    val outlineCol = if (isRetro) BorderNeon else Color(0xFFEADDFF)
    val cardShape = if (isRetro) RoundedCornerShape(24.dp) else RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = consoleBg),
        border = if (isRetro) BorderStroke(1.dp, outlineCol) else null
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Directional cross D-Pad (Left, Drop, Right)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // HOLD helper btn in DPAAD region
                ArcadeCircleButton(
                    symbol = "⇄",
                    desc = "HOLD",
                    accentColor = AccentNeon,
                    isRetro = isRetro,
                    size = 40,
                    onClick = onHold,
                    tag = "hold_button",
                    containerColor = if (isRetro) null else Color(0xFFEADDFF),
                    contentColor = if (isRetro) null else Color(0xFF21005D)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    ArcadeCircleButton(
                        symbol = "◀",
                        desc = "LEFT",
                        accentColor = PrimaryNeon,
                        isRetro = isRetro,
                        size = 48,
                        onClick = onMoveLeft,
                        tag = "left_button",
                        containerColor = if (isRetro) null else Color(0xFFEADDFF),
                        contentColor = if (isRetro) null else Color(0xFF21005D)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ArcadeCircleButton(
                            symbol = "▼",
                            desc = "DOWN",
                            accentColor = PrimaryNeon,
                            isRetro = isRetro,
                            size = 44,
                            onClick = onSoftDrop,
                            tag = "soft_drop_button",
                            containerColor = if (isRetro) null else Color(0xFFEADDFF),
                            contentColor = if (isRetro) null else Color(0xFF21005D)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    ArcadeCircleButton(
                        symbol = "▶",
                        desc = "RIGHT",
                        accentColor = PrimaryNeon,
                        isRetro = isRetro,
                        size = 48,
                        onClick = onMoveRight,
                        tag = "right_button",
                        containerColor = if (isRetro) null else Color(0xFFEADDFF),
                        contentColor = if (isRetro) null else Color(0xFF21005D)
                    )
                }
            }

            Box(modifier = Modifier.height(80.dp).width(1.dp).background(Color.Gray.copy(alpha = 0.15f)))

            // Major Action Buttons on the Right Side (Rotate, HardDrop)
            Row(
                modifier = Modifier.padding(end = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Hard drop
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ArcadeCircleButton(
                        symbol = "⤋",
                        desc = "SLAM",
                        accentColor = Color.Yellow,
                        isRetro = isRetro,
                        size = 46,
                        onClick = onHardDrop,
                        tag = "hard_drop_button",
                        containerColor = if (isRetro) null else Color(0xFFBA68C8),
                        contentColor = if (isRetro) null else Color.White
                    )
                }

                // Rotating Trigger
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ArcadeCircleButton(
                        symbol = "↻",
                        desc = "ROTATE",
                        accentColor = AccentNeon,
                        isRetro = isRetro,
                        size = 56,
                        onClick = onRotate,
                        tag = "rotate_button",
                        containerColor = if (isRetro) null else Color(0xFF6750A4),
                        contentColor = if (isRetro) null else Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun LandscapeControls(
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onRotate: () -> Unit,
    onSoftDrop: () -> Unit,
    onHardDrop: () -> Unit,
    isRetro: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isRetro) retroCardBg() else Color(0xFFF3EDF7)),
        border = BorderStroke(1.dp, if (isRetro) BorderNeon else Color(0xFFEADDFF))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ArcadeCircleButton("◀", "LEFT", PrimaryNeon, isRetro, 44, onMoveLeft, "left_button", containerColor = if (isRetro) null else Color(0xFFEADDFF), contentColor = if (isRetro) null else Color(0xFF21005D))
                ArcadeCircleButton("↻", "ROTATE", AccentNeon, isRetro, 44, onRotate, "rotate_button", containerColor = if (isRetro) null else Color(0xFF6750A4), contentColor = if (isRetro) null else Color.White)
                ArcadeCircleButton("▶", "RIGHT", PrimaryNeon, isRetro, 44, onMoveRight, "right_button", containerColor = if (isRetro) null else Color(0xFFEADDFF), contentColor = if (isRetro) null else Color(0xFF21005D))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ArcadeCircleButton("▼", "DROP", PrimaryNeon, isRetro, 44, onSoftDrop, "soft_drop_button", containerColor = if (isRetro) null else Color(0xFFEADDFF), contentColor = if (isRetro) null else Color(0xFF21005D))
                ArcadeCircleButton("⤋", "SLAM", Color.Yellow, isRetro, 44, onHardDrop, "hard_drop_button", containerColor = if (isRetro) null else Color(0xFFBA68C8), contentColor = if (isRetro) null else Color.White)
            }
        }
    }
}

@Composable
fun ArcadeCircleButton(
    symbol: String,
    desc: String,
    accentColor: Color,
    isRetro: Boolean,
    size: Int,
    onClick: () -> Unit,
    tag: String,
    containerColor: Color? = null,
    contentColor: Color? = null
) {
    val defaultBtnColor = if (isRetro) Color(0xFF141A32) else MaterialTheme.colorScheme.primaryContainer
    val defaultFontColor = if (isRetro) accentColor else MaterialTheme.colorScheme.onPrimaryContainer
    val rimColor = if (isRetro) accentColor.copy(alpha = 0.6f) else Color.Transparent

    val finalBtnColor = containerColor ?: defaultBtnColor
    val finalFontColor = contentColor ?: defaultFontColor

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .testTag(tag)
                .size(size.dp)
                .clip(CircleShape)
                .background(finalBtnColor)
                .then(
                    if (isRetro) Modifier.border(1.5.dp, rimColor, CircleShape)
                    else Modifier // Clean flat look in sleek mode
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = symbol,
                color = finalFontColor,
                fontSize = (size / 2.3f).sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = desc,
            color = if (isRetro) Color.LightGray else Color(0xFF49454F),
            fontSize = 8.sp,
            fontFamily = if (isRetro) FontFamily.Monospace else FontFamily.SansSerif,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SettingsCard(
    isHaptic: Boolean,
    isRetro: Boolean,
    onToggleHaptic: () -> Unit,
    onToggleTheme: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = if (isRetro) RoundedCornerShape(12.dp) else RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (isRetro) retroCardBg() else Color(0xFFF3EDF7)),
        border = BorderStroke(1.dp, if (isRetro) BorderNeon else Color(0xFFEADDFF))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "SETTINGS",
                fontFamily = if (isRetro) FontFamily.Monospace else FontFamily.SansSerif,
                fontSize = 11.sp,
                color = if (isRetro) Color.Gray else Color(0xFF49454F),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("VIBRATION", fontSize = 11.sp, fontFamily = if (isRetro) FontFamily.Monospace else FontFamily.SansSerif, color = if (isRetro) Color.White else Color(0xFF1D1B20))
                Switch(
                    checked = isHaptic,
                    onCheckedChange = { onToggleHaptic() },
                    modifier = Modifier.testTag("haptic_toggle").scaleCompact()
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("CYBER THEME", fontSize = 11.sp, fontFamily = if (isRetro) FontFamily.Monospace else FontFamily.SansSerif, color = if (isRetro) Color.White else Color(0xFF1D1B20))
                Switch(
                    checked = isRetro,
                    onCheckedChange = { onToggleTheme() },
                    modifier = Modifier.testTag("theme_toggle").scaleCompact()
                )
            }
        }
    }
}

private fun Modifier.scaleCompact(): Modifier = this.padding(0.dp)

@Composable
fun OverlaysContainer(
    state: TetrisState,
    isRetro: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onClearScores: () -> Unit
) {
    val overBg = if (isRetro) CyberBg.copy(alpha = 0.94f) else Color(0xFFFEF7FF).copy(alpha = 0.95f)

    if (state.gameStatus == GameStatus.START || state.gameStatus == GameStatus.GAME_OVER || state.gameStatus == GameStatus.PAUSED) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(overBg),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (state.gameStatus) {
                        GameStatus.START -> {
                            MainStartPanel(state = state, isRetro = isRetro, onStart = onStart, onClearScores = onClearScores)
                        }
                        GameStatus.PAUSED -> {
                            Text(
                                text = if (isRetro) "GAME PAUSED" else "Paused",
                                fontFamily = if (isRetro) FontFamily.Monospace else FontFamily.SansSerif,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isRetro) PrimaryNeon else Color(0xFF1D1B20),
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onPause,
                                colors = ButtonDefaults.buttonColors(containerColor = if (isRetro) AccentNeon else Color(0xFF6750A4)),
                                shape = if (isRetro) RoundedCornerShape(8.dp) else RoundedCornerShape(100),
                                modifier = Modifier.testTag("start_button")
                            ) {
                                Text("RESUME GAME", fontFamily = if (isRetro) FontFamily.Monospace else FontFamily.SansSerif, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        GameStatus.GAME_OVER -> {
                            Text(
                                text = "GAME OVER",
                                fontFamily = if (isRetro) FontFamily.Monospace else FontFamily.SansSerif,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isRetro) AccentNeon else Color(0xFFB3261E),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "FINAL SCORE: ${state.score}",
                                fontFamily = if (isRetro) FontFamily.Monospace else FontFamily.SansSerif,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isRetro) Color.White else Color(0xFF49454F),
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            // Leaderboard rankings
                            LeaderboardView(highScores = state.highScores, isRetro = isRetro)

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = onStart,
                                colors = ButtonDefaults.buttonColors(containerColor = if (isRetro) PrimaryNeon else Color(0xFF6750A4)),
                                shape = if (isRetro) RoundedCornerShape(8.dp) else RoundedCornerShape(100),
                                modifier = Modifier.testTag("play_again_button")
                            ) {
                                Text("PLAY AGAIN", fontFamily = if (isRetro) FontFamily.Monospace else FontFamily.SansSerif, fontWeight = FontWeight.Bold, color = if (isRetro) Color.Black else Color.White)
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
fun MainStartPanel(
    state: TetrisState,
    isRetro: Boolean,
    onStart: () -> Unit,
    onClearScores: () -> Unit
) {
    if (isRetro) {
        Text(
            text = "🎮 TETRIS ⚡",
            fontFamily = FontFamily.Monospace,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = PrimaryNeon,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "RETRO NEON COMPOSER EDITION",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = AccentNeon,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    } else {
        LogoIconSleek()
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Tetris Pro",
            fontFamily = FontFamily.SansSerif,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF1D1B20),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = "Sleek Material 3 Edition",
            fontFamily = FontFamily.SansSerif,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF6750A4),
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }

    if (state.highScores.isNotEmpty()) {
        Text(
            text = "🏆 HIGH SCORES 🏆",
            fontFamily = if (isRetro) FontFamily.Monospace else FontFamily.SansSerif,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isRetro) Color.Yellow else Color(0xFF6750A4),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LeaderboardView(highScores = state.highScores, isRetro = isRetro)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Clear Leaderboard",
            fontFamily = if (isRetro) FontFamily.Monospace else FontFamily.SansSerif,
            fontSize = 10.sp,
            color = if (isRetro) Color.LightGray.copy(alpha = 0.5f) else Color(0xFF49454F).copy(alpha = 0.6f),
            modifier = Modifier
                .clickable { onClearScores() }
                .padding(4.dp)
        )
    } else {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = if (isRetro) BoardBg else Color(0xFFF3EDF7)),
            border = BorderStroke(1.dp, if (isRetro) BorderNeon else Color(0xFFEADDFF)),
            shape = if (isRetro) RoundedCornerShape(12.dp) else RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No High Scores Yet!",
                    fontFamily = if (isRetro) FontFamily.Monospace else FontFamily.SansSerif,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Set a record & cement your name in the leaderboard!",
                    fontFamily = if (isRetro) FontFamily.Monospace else FontFamily.SansSerif,
                    fontSize = 9.sp,
                    color = Color.Gray.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = onStart,
        colors = ButtonDefaults.buttonColors(containerColor = if (isRetro) PrimaryNeon else Color(0xFF6750A4)),
        shape = if (isRetro) RoundedCornerShape(12.dp) else RoundedCornerShape(100),
        modifier = Modifier
            .testTag("start_button")
            .fillMaxWidth(0.8f)
            .height(50.dp)
    ) {
        Text(
            text = if (isRetro) "COMMENCE MATRIX" else "START GAME",
            fontFamily = if (isRetro) FontFamily.Monospace else FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = if (isRetro) Color.Black else Color.White
        )
    }
}

@Composable
fun LeaderboardView(
    highScores: List<HighScore>,
    isRetro: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (isRetro) BoardBg else Color(0xFFF3EDF7)),
        border = BorderStroke(1.dp, if (isRetro) BorderNeon else Color(0xFFEADDFF)),
        shape = if (isRetro) RoundedCornerShape(8.dp) else RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            val formatter = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }
            highScores.forEachIndexed { idx, score ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "#${idx + 1} ",
                            fontFamily = if (isRetro) FontFamily.Monospace else FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            color = when (idx) {
                                0 -> if (isRetro) Color.Yellow else Color(0xFF6750A4)
                                1 -> if (isRetro) Color.White else Color(0xFF49454F)
                                2 -> if (isRetro) AccentNeon else Color(0xFFBA68C8)
                                else -> Color.Gray
                             },
                            fontSize = 12.sp
                        )
                        Text(
                            text = score.playerName,
                            fontFamily = if (isRetro) FontFamily.Monospace else FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            color = if (isRetro) Color.White else Color(0xFF1D1B20),
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = "${score.score} pts",
                        fontFamily = if (isRetro) FontFamily.Monospace else FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        color = if (isRetro) PrimaryNeon else Color(0xFF6750A4),
                        fontSize = 12.sp
                    )
                }
                if (idx < highScores.size - 1) {
                    Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(if (isRetro) Color.Gray.copy(alpha = 0.15f) else Color(0xFFEADDFF)))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NamePromptDialog(
    score: Int,
    isRetro: Boolean,
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    val defaultName = if (isRetro) "MatrixLord" else "PlayerOne"

    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = if (isRetro) RoundedCornerShape(16.dp) else RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = if (isRetro) RetroPanelBg else Color(0xFFFEF7FF)),
            border = BorderStroke(1.5.dp, if (isRetro) PrimaryNeon else Color(0xFF6750A4))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🏆 NEW HIGH SCORE! 🏆",
                    fontFamily = if (isRetro) FontFamily.Monospace else FontFamily.SansSerif,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isRetro) Color.Yellow else Color(0xFF6750A4),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "YOU SCORED: $score",
                    fontFamily = if (isRetro) FontFamily.Monospace else FontFamily.SansSerif,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isRetro) Color.White else Color(0xFF1D1B20),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Enter your name for the leaderboard:",
                    fontFamily = if (isRetro) FontFamily.Monospace else FontFamily.SansSerif,
                    fontSize = 10.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 12) name = it },
                    placeholder = { Text(defaultName, fontFamily = if (isRetro) FontFamily.Monospace else FontFamily.SansSerif, color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (isRetro) Color.White else Color(0xFF1D1B20),
                        unfocusedTextColor = if (isRetro) Color.White else Color(0xFF1D1B20),
                        focusedBorderColor = if (isRetro) PrimaryNeon else Color(0xFF6750A4),
                        unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier
                        .testTag("name_input")
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                    ) {
                        Text("DISCARD", fontFamily = if (isRetro) FontFamily.Monospace else FontFamily.SansSerif, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { onSubmit(name.ifEmpty { defaultName }) },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isRetro) PrimaryNeon else Color(0xFF6750A4)),
                        shape = if (isRetro) RoundedCornerShape(8.dp) else RoundedCornerShape(100),
                        modifier = Modifier.testTag("submit_button")
                    ) {
                        Text(
                            "SUBMIT",
                            fontFamily = if (isRetro) FontFamily.Monospace else FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            color = if (isRetro) Color.Black else Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

fun getPieceColor(id: Int, isRetro: Boolean): Color {
    return if (isRetro) {
        when (id) {
            1 -> Color(0xFF00E5FF) // I - Neon Cyan
            2 -> Color(0xFFFFEA00) // O - Neon Yellow
            3 -> Color(0xFFD500F9) // T - Neon Purple
            4 -> Color(0xFF00E676) // S - Neon Green
            5 -> Color(0xFFFF1744) // Z - Neon Red
            6 -> Color(0xFF2979FF) // J - Neon Blue
            7 -> Color(0xFFFF9100) // L - Neon Orange
            else -> Color.Gray
        }
    } else {
        when (id) {
            1 -> Color(0xFF0288D1) // I
            2 -> Color(0xFFFBC02D) // O
            3 -> Color(0xFF7B1FA2) // T
            4 -> Color(0xFF388E3C) // S
            5 -> Color(0xFFD32F2F) // Z
            6 -> Color(0xFF1976D2) // J
            7 -> Color(0xFFF57C00) // L
            else -> Color.Gray
        }
    }
}

fun retroCardBg(): Color {
    return RetroPanelBg
}
