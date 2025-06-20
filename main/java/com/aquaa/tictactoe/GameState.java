package com.aquaa.tictactoe; // Updated package name

import java.util.Stack;

public class GameState {
    private String[][] board;
    private Stack<String[][]> historyStack; // To store previous board states for undo

    public GameState() {
        board = new String[3][3];
        initializeBoard();
        historyStack = new Stack<>();
        saveBoardState(); // Save initial empty board state
    }

    private void initializeBoard() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                board[r][c] = ""; // Empty string for empty cells
            }
        }
    }

    /**
     * Returns a deep copy of the current board state.
     */
    public String[][] getBoard() {
        String[][] currentBoard = new String[3][3];
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                currentBoard[r][c] = board[r][c];
            }
        }
        return currentBoard;
    }

    /**
     * Gets the current board state without creating a new copy.
     * Useful when modifying the board directly, e.g., in Minimax.
     */
    public String[][] getCurrentBoardState() {
        return board;
    }

    /**
     * Checks if a cell is empty.
     */
    public boolean isCellEmpty(int row, int col) {
        return board[row][col].isEmpty();
    }

    /**
     * Makes a move on the board.
     *
     * @param row The row of the move.
     * @param col The column of the move.
     * @param player The symbol of the player making the move.
     * @return True if the move was valid and made, false otherwise.
     */
    public boolean makeMove(int row, int col, String player) {
        if (row >= 0 && row < 3 && col >= 0 && col < 3 && board[row][col].isEmpty()) {
            board[row][col] = player;
            // No need to save to history here, GameActivity will call saveBoardState() before makeMove()
            return true;
        }
        return false;
    }

    /**
     * Checks for a winner on the current board.
     *
     * @return The symbol of the winning player ("X" or "O"), or null if no winner.
     */
    public String checkWinner() {
        // Check rows
        for (int r = 0; r < 3; r++) {
            if (!board[r][0].isEmpty() && board[r][0].equals(board[r][1]) && board[r][1].equals(board[r][2])) {
                return board[r][0];
            }
        }
        // Check columns
        for (int c = 0; c < 3; c++) {
            if (!board[0][c].isEmpty() && board[0][c].equals(board[1][c]) && board[1][c].equals(board[2][c])) {
                return board[0][c];
            }
        }
        // Check diagonals
        if (!board[0][0].isEmpty() && board[0][0].equals(board[1][1]) && board[1][1].equals(board[2][2])) {
            return board[0][0];
        }
        if (!board[0][2].isEmpty() && board[0][2].equals(board[1][1]) && board[1][1].equals(board[2][0])) {
            return board[0][2];
        }
        return null;
    }

    /**
     * Checks if the board is full (a tie).
     */
    public boolean isBoardFull() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[r][c].isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Saves a deep copy of the current board state to the history stack.
     */
    public void saveBoardState() {
        String[][] currentCopy = new String[3][3];
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                currentCopy[r][c] = board[r][c];
            }
        }
        historyStack.push(currentCopy);
    }

    /**
     * Restores the board to the previous state from the history stack.
     * Does not pop the current state; pops the state *before* the current one.
     * This makes "undo" effectively go back two states for AI turns (user then AI).
     */
    public void restorePreviousBoardState() {
        if (historyStack.size() > 1) { // Need at least two states to restore to a previous one
            historyStack.pop(); // Pop the current state
            this.board = historyStack.peek(); // The actual previous state becomes current
        } else if (historyStack.size() == 1) {
            // If only initial empty board is left, clear it and re-initialize
            historyStack.pop();
            initializeBoard();
            saveBoardState(); // Add the empty board back
        }
    }

    /**
     * Returns the current size of the history stack.
     */
    public int getHistorySize() {
        return historyStack.size();
    }
}
