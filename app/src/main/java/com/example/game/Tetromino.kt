package com.example.game

enum class TetrominoType(val id: Int, val rawGrid: Array<IntArray>) {
    I(1, arrayOf(
        intArrayOf(0, 0, 0, 0),
        intArrayOf(1, 1, 1, 1),
        intArrayOf(0, 0, 0, 0),
        intArrayOf(0, 0, 0, 0)
    )),
    O(2, arrayOf(
        intArrayOf(1, 1),
        intArrayOf(1, 1)
    )),
    T(3, arrayOf(
        intArrayOf(0, 1, 0),
        intArrayOf(1, 1, 1),
        intArrayOf(0, 0, 0)
    )),
    S(4, arrayOf(
        intArrayOf(0, 1, 1),
        intArrayOf(1, 1, 0),
        intArrayOf(0, 0, 0)
    )),
    Z(5, arrayOf(
        intArrayOf(1, 1, 0),
        intArrayOf(0, 1, 1),
        intArrayOf(0, 0, 0)
    )),
    J(6, arrayOf(
        intArrayOf(1, 0, 0),
        intArrayOf(1, 1, 1),
        intArrayOf(0, 0, 0)
    )),
    L(7, arrayOf(
        intArrayOf(0, 0, 1),
        intArrayOf(1, 1, 1),
        intArrayOf(0, 0, 0)
    ));
}

data class Piece(
    val type: TetrominoType,
    val grid: Array<IntArray> = type.rawGrid.map { it.clone() }.toTypedArray(),
    val x: Int = 3,
    val y: Int = -1
) {
    fun rotateClockwise(): Piece {
        val n = grid.size
        val rotated = Array(n) { IntArray(n) }
        for (r in 0 until n) {
            for (c in 0 until n) {
                if (type == TetrominoType.O) {
                    rotated[r][c] = grid[r][c]
                } else {
                    rotated[c][n - 1 - r] = grid[r][c]
                }
            }
        }
        return copy(grid = rotated)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Piece) return false
        if (type != other.type) return false
        if (!grid.contentDeepEquals(other.grid)) return false
        if (x != other.x) return false
        if (y != other.y) return false
        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + grid.contentDeepHashCode()
        result = 31 * result + x
        result = 31 * result + y
        return result
    }
}

class TetrominoGenerator {
    private val bag = mutableListOf<TetrominoType>()

    fun next(): TetrominoType {
        if (bag.isEmpty()) {
            bag.addAll(TetrominoType.values())
            bag.shuffle()
        }
        return bag.removeAt(0)
    }
}
