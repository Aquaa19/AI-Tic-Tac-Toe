package com.aquaa.tictactoe; // Remember to replace with your package name

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AILogic {

    public enum Difficulty {
        EASY, AVERAGE, DIFFICULT
    }

    /**
     * Determines the AI's move based on the specified difficulty.
     *
     * @param board The current game board.
     * @param difficulty The AI difficulty level.
     * @param aiPlayer The symbol of the AI player (e.g., "O").
     * @param humanPlayer The symbol of the human player (e.g., "X").
     * @return An array of two integers representing the row and column of the AI's move, or null if no move is possible.
     */
    public static int[] getAIMove(String[][] board, Difficulty difficulty, String aiPlayer, String humanPlayer) {
        switch (difficulty) {
            case EASY:
                return getRandomMove(board);
            case AVERAGE:
                return getAverageMove(board, aiPlayer, humanPlayer);
            case DIFFICULT:
                return getDifficultMove(board, aiPlayer, humanPlayer);
            default:
                return getRandomMove(board);
        }
    }

    /**
     * Easy AI: Makes a random valid move.
     */
    private static int[] getRandomMove(String[][] board) {
        List<int[]> emptyCells = new ArrayList<>();
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[r][c].isEmpty()) {
                    emptyCells.add(new int[]{r, c});
                }
            }
        }
        if (!emptyCells.isEmpty()) {
            Random random = new Random();
            return emptyCells.get(random.nextInt(emptyCells.size()));
        }
        return null; // No empty cells
    }

    /**
     * Average AI: Blocks immediate threats and wins if possible (looks one step ahead).
     */
    private static int[] getAverageMove(String[][] board, String aiPlayer, String humanPlayer) {
        // 1. Check for winning move for AI
        int[] winningMove = findWinningMove(board, aiPlayer);
        if (winningMove != null) {
            return winningMove;
        }

        // 2. Check to block human's winning move
        int[] blockingMove = findWinningMove(board, humanPlayer);
        if (blockingMove != null) {
            return blockingMove;
        }

        // 3. Take center if available
        if (board[1][1].isEmpty()) {
            return new int[]{1, 1};
        }

        // 4. Take a corner if available
        int[][] corners = {{0, 0}, {0, 2}, {2, 0}, {2, 2}};
        for (int[] corner : corners) {
            if (board[corner[0]][corner[1]].isEmpty()) {
                return corner;
            }
        }

        // 5. Take any empty side if available
        int[][] sides = {{0, 1}, {1, 0}, {1, 2}, {2, 1}};
        for (int[] side : sides) {
            if (board[side[0]][side[1]].isEmpty()) {
                return side;
            }
        }

        // If no strategic move, fall back to random
        return getRandomMove(board);
    }

    /**
     * Helper for Average AI: Finds a move that leads to a win for the given player.
     */
    private static int[] findWinningMove(String[][] board, String player) {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[r][c].isEmpty()) {
                    board[r][c] = player; // Temporarily make the move
                    if (checkWinner(board, player)) {
                        board[r][c] = ""; // Undo the temporary move
                        return new int[]{r, c};
                    }
                    board[r][c] = ""; // Undo the temporary move
                }
            }
        }
        return null;
    }

    /**
     * Difficult AI: Uses Minimax with Alpha-Beta Pruning and randomization for equally optimal moves.
     */
    private static int[] getDifficultMove(String[][] board, String aiPlayer, String humanPlayer) {
        int bestScore = Integer.MIN_VALUE;
        List<int[]> bestMoves = new ArrayList<>(); // Store all equally optimal moves

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[r][c].isEmpty()) {
                    board[r][c] = aiPlayer; // Make the move
                    int score = minimax(board, 0, false, aiPlayer, humanPlayer, Integer.MIN_VALUE, Integer.MAX_VALUE);
                    board[r][c] = ""; // Undo the move

                    if (score > bestScore) {
                        bestScore = score;
                        bestMoves.clear(); // Clear previous best moves, as a new best found
                        bestMoves.add(new int[]{r, c});
                    } else if (score == bestScore) {
                        bestMoves.add(new int[]{r, c}); // Add to equally optimal moves
                    }
                }
            }
        }

        // Randomly select one of the best moves to make AI less predictable
        if (!bestMoves.isEmpty()) {
            Random random = new Random();
            return bestMoves.get(random.nextInt(bestMoves.size()));
        }
        return null; // Should not happen in a solvable game like Tic Tac Toe
    }

    /**
     * Minimax algorithm with Alpha-Beta Pruning.
     *
     * @param board The current board state.
     * @param depth The current depth of recursion.
     * @param isMaximizingPlayer True if the current call is for the maximizing player (AI).
     * @param aiPlayer The AI's symbol.
     * @param humanPlayer The human's symbol.
     * @param alpha Alpha value for alpha-beta pruning.
     * @param beta Beta value for alpha-beta pruning.
     * @return The optimal score for the current board state.
     */
    private static int minimax(String[][] board, int depth, boolean isMaximizingPlayer, String aiPlayer, String humanPlayer, int alpha, int beta) {
        String winner = checkGameEnd(board);

        if (winner != null) {
            if (winner.equals(aiPlayer)) {
                return 10 - depth; // AI wins
            } else if (winner.equals(humanPlayer)) {
                return depth - 10; // Human wins
            } else {
                return 0; // Tie
            }
        }

        if (isBoardFull(board)) {
            return 0; // Tie
        }

        if (isMaximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    if (board[r][c].isEmpty()) {
                        board[r][c] = aiPlayer;
                        int eval = minimax(board, depth + 1, false, aiPlayer, humanPlayer, alpha, beta);
                        board[r][c] = ""; // Undo the move
                        maxEval = Math.max(maxEval, eval);
                        alpha = Math.max(alpha, eval);
                        if (beta <= alpha) {
                            break; // Beta cut-off
                        }
                    }
                }
                if (beta <= alpha) {
                    break;
                }
            }
            return maxEval;
        } else { // Minimizing player (human)
            int minEval = Integer.MAX_VALUE;
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    if (board[r][c].isEmpty()) {
                        board[r][c] = humanPlayer;
                        int eval = minimax(board, depth + 1, true, aiPlayer, humanPlayer, alpha, beta);
                        board[r][c] = ""; // Undo the move
                        minEval = Math.min(minEval, eval);
                        beta = Math.min(beta, eval);
                        if (beta <= alpha) {
                            break; // Alpha cut-off
                        }
                    }
                }
                if (beta <= alpha) {
                    break;
                }
            }
            return minEval;
        }
    }

    /**
     * Checks if the game has ended (win or tie).
     * Returns the winner's symbol ("X" or "O"), "Tie" if it's a tie, or null if game is ongoing.
     */
    private static String checkGameEnd(String[][] board) {
        // Corrected logic: checkWinner returns boolean, use it in if condition
        if (checkWinner(board, "X")) {
            return "X";
        }
        if (checkWinner(board, "O")) {
            return "O";
        }
        if (isBoardFull(board)) {
            return "Tie";
        }
        return null; // Game is still ongoing
    }

    /**
     * Checks if the given player has won the game.
     */
    private static boolean checkWinner(String[][] board, String player) {
        // Check rows
        for (int r = 0; r < 3; r++) {
            if (board[r][0].equals(player) && board[r][1].equals(player) && board[r][2].equals(player)) {
                return true;
            }
        }
        // Check columns
        for (int c = 0; c < 3; c++) {
            if (board[0][c].equals(player) && board[1][c].equals(player) && board[2][c].equals(player)) {
                return true;
            }
        }
        // Check diagonals
        if (board[0][0].equals(player) && board[1][1].equals(player) && board[2][2].equals(player)) {
            return true;
        }
        if (board[0][2].equals(player) && board[1][1].equals(player) && board[2][0].equals(player)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if the board is full.
     */
    private static boolean isBoardFull(String[][] board) {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[r][c].isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
}
