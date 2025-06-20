package com.aquaa.tictactoe; // Remember to replace with your package name

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.AudioManager; // This might be unused, but keeping it if you had it.
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
// Removed: import android.widget.ProgressBar; // Removed ProgressBar import

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate; // This might be unused if theme is handled elsewhere.

import java.util.ArrayList; // This might be unused.
import java.util.List; // This might be unused.
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    // UI elements
    private TextView turnLabel;
    private TextView statusLabel;
    private TextView scoreLabel;
    private Button[][] buttons = new Button[3][3];
    private Button newGameButton;
    private Button undoButton;
    private Button resetScoresButton;
    // Removed: private ProgressBar aiThinkingIndicator; // Removed ProgressBar declaration

    // Game state
    private GameState gameState;
    private String player1Symbol; // User's symbol (X or O)
    private String player2Symbol; // AI's symbol (X or O)
    private String currentPlayer; // Current turn (X or O)
    private boolean isAITurn = false;
    private boolean gameEnded = false;

    // AI related
    private AILogic.Difficulty aiDifficulty;
    private Handler aiHandler = new Handler();

    // Scores
    private int userWins = 0;
    private int aiWins = 0;
    private int draws = 0;

    // Sound
    private SoundPool soundPool;
    private int clickSoundId, winSoundId, tieSoundId;
    private MediaPlayer backgroundMusicPlayer;
    private boolean soundEnabled;

    // Preferences
    private SharedPreferences sharedPreferences;
    private static final String PREF_KEY_FIRST_TURN_USER = "first_turn_user";
    private static final String PREF_KEY_USER_WINS = "user_wins";
    private static final String PREF_KEY_AI_WINS = "ai_wins";
    private static final String PREF_KEY_DRAWS = "draws";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Initialize Shared Preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Initialize UI elements
        turnLabel = findViewById(R.id.turn_label);
        statusLabel = findViewById(R.id.status_label);
        scoreLabel = findViewById(R.id.score_label);
        newGameButton = findViewById(R.id.new_game_button);
        undoButton = findViewById(R.id.undo_button);
        resetScoresButton = findViewById(R.id.reset_scores_button);
        // Removed: aiThinkingIndicator = findViewById(R.id.ai_thinking_indicator); // Removed ProgressBar initialization

        // Initialize game board buttons
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                String buttonID = "button_" + r + c;
                int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
                buttons[r][c] = findViewById(resID);
                final int finalR = r;
                final int finalC = c;
                buttons[r][c].setOnClickListener(v -> onCellClick(finalR, finalC));
            }
        }

        // Set up button listeners
        newGameButton.setOnClickListener(v -> startNewGame());
        undoButton.setOnClickListener(v -> undoLastMove());
        resetScoresButton.setOnClickListener(v -> resetScores());

        // Load preferences (excluding theme setting here)
        loadSettings();
        loadScores();

        // Initialize sound
        setupSoundPool();
        loadSoundAssets();
        playBackgroundMusic();

        // Start a new game
        startGame();
    }

    private void loadSettings() {
        // Load AI Difficulty
        String difficultyPref = sharedPreferences.getString("ai_difficulty", "Medium");
        switch (difficultyPref) {
            case "Easy":
                aiDifficulty = AILogic.Difficulty.EASY;
                break;
            case "Hard":
                aiDifficulty = AILogic.Difficulty.DIFFICULT;
                break;
            case "Medium":
            default:
                aiDifficulty = AILogic.Difficulty.AVERAGE;
                break;
        }

        // Load sound enabled state
        soundEnabled = sharedPreferences.getBoolean("sound_enabled", true);
    }

    private void loadScores() {
        userWins = sharedPreferences.getInt(PREF_KEY_USER_WINS, 0);
        aiWins = sharedPreferences.getInt(PREF_KEY_AI_WINS, 0);
        draws = sharedPreferences.getInt(PREF_KEY_DRAWS, 0);
        updateScoreLabel();
    }

    private void saveScores() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PREF_KEY_USER_WINS, userWins);
        editor.putInt(PREF_KEY_AI_WINS, aiWins);
        editor.putInt(PREF_KEY_DRAWS, draws);
        editor.apply();
    }

    private void resetScores() {
        userWins = 0;
        aiWins = 0;
        draws = 0;
        saveScores();
        updateScoreLabel();
        Toast.makeText(this, "Scores reset!", Toast.LENGTH_SHORT).show();
    }

    private void setupSoundPool() {
        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .build();
    }

    private void loadSoundAssets() {
        clickSoundId = soundPool.load(this, R.raw.click, 1);
        winSoundId = soundPool.load(this, R.raw.win, 1);
        tieSoundId = soundPool.load(this, R.raw.tie, 1);

        backgroundMusicPlayer = MediaPlayer.create(this, R.raw.song1);
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.setLooping(true);
            backgroundMusicPlayer.setVolume(0.2f, 0.2f);
        }
    }

    private void playBackgroundMusic() {
        Log.d("GameActivity", "playBackgroundMusic called. soundEnabled: " + soundEnabled + ", backgroundMusicPlayer null: " + (backgroundMusicPlayer == null));
        if (soundEnabled && backgroundMusicPlayer != null && !backgroundMusicPlayer.isPlaying()) {
            backgroundMusicPlayer.start();
            Log.d("GameActivity", "Background music started.");
        } else if (!soundEnabled && backgroundMusicPlayer != null && backgroundMusicPlayer.isPlaying()) {
            backgroundMusicPlayer.pause();
            Log.d("GameActivity", "Background music paused.");
        }
    }

    private void playSound(int soundId) {
        if (soundEnabled) {
            soundPool.play(soundId, 1, 1, 0, 0, 1);
        }
    }

    private void startGame() {
        boolean userStartsNext = sharedPreferences.getBoolean(PREF_KEY_FIRST_TURN_USER, true);

        player1Symbol = "X";
        player2Symbol = "O";

        if (userStartsNext) {
            currentPlayer = player1Symbol; // User (X) starts
        } else {
            currentPlayer = player2Symbol; // AI (O) starts
        }

        sharedPreferences.edit().putBoolean(PREF_KEY_FIRST_TURN_USER, !userStartsNext).apply();

        gameState = new GameState();
        resetBoardUI();
        updateTurnLabel();
        statusLabel.setText(R.string.game_in_progress);
        gameEnded = false;
        // Removed: aiThinkingIndicator.setVisibility(View.GONE); // Hide indicator at game start

        if (currentPlayer.equals(player2Symbol)) {
            isAITurn = true;
            // Removed: aiThinkingIndicator.setVisibility(View.VISIBLE); // Show indicator
            disableAllButtons(); // Disable buttons while AI thinks
            aiHandler.postDelayed(this::makeAIMove, 2000);
        }
    }

    private void startNewGame() {
        startGame();
        Toast.makeText(this, "New game started!", Toast.LENGTH_SHORT).show();
    }

    private void resetBoardUI() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                buttons[r][c].setText("");
                buttons[r][c].setEnabled(true);
            }
        }
    }

    private void updateTurnLabel() {
        // Updated to display "Current Turn: AI/User (O/X)"
        String turnText;
        if (currentPlayer.equals(player1Symbol)) {
            turnText = getString(R.string.current_turn_user, player1Symbol); // Example: "Current Turn: User (X)"
        } else {
            turnText = getString(R.string.current_turn_ai, player2Symbol); // Example: "Current Turn: AI (O)"
        }
        turnLabel.setText(turnText);

        ObjectAnimator animator = ObjectAnimator.ofFloat(turnLabel, "alpha", 0f, 1f);
        animator.setDuration(500);
        animator.start();
    }

    private void updateScoreLabel() {
        scoreLabel.setText(getString(R.string.scores, userWins, aiWins, draws));
    }

    private void onCellClick(int r, int c) {
        if (gameEnded || isAITurn || !gameState.isCellEmpty(r, c)) {
            return;
        }

        playSound(clickSoundId);
        makeMove(r, c);

        if (!gameEnded) {
            isAITurn = true;
            // Removed: aiThinkingIndicator.setVisibility(View.VISIBLE); // Show indicator when AI is about to move
            disableAllButtons(); // Disable buttons while AI thinks
            aiHandler.postDelayed(this::makeAIMove, 2000);
        }
    }

    private void makeMove(int r, int c) {
        // Save current board state for undo before making a move
        gameState.saveBoardState();

        // Update GameState and UI
        gameState.makeMove(r, c, currentPlayer);
        buttons[r][c].setText(currentPlayer);

        // Check for winner or tie
        String winner = gameState.checkWinner();
        if (winner != null) {
            handleGameEnd(winner);
        } else if (gameState.isBoardFull()) {
            handleGameEnd(null); // It's a tie
        } else {
            // Switch player
            currentPlayer = (currentPlayer.equals(player1Symbol)) ? player2Symbol : player1Symbol;
            updateTurnLabel();
        }
    }

    private void makeAIMove() {
        // Removed: aiThinkingIndicator.setVisibility(View.GONE); // Hide indicator when AI starts moving
        if (gameEnded) {
            isAITurn = false;
            return;
        }

        int[] aiMove = AILogic.getAIMove(gameState.getBoard(), aiDifficulty, player2Symbol, player1Symbol);

        if (aiMove != null && gameState.isCellEmpty(aiMove[0], aiMove[1])) {
            playSound(clickSoundId);
            makeMove(aiMove[0], aiMove[1]);
        } else {
            Toast.makeText(this, "AI tried an invalid move or no moves available!", Toast.LENGTH_SHORT).show();
            if (gameState.isBoardFull()) {
                handleGameEnd(null);
            }
        }
        isAITurn = false; // AI's turn is over
        if (!gameEnded) { // Re-enable buttons only if game is not over
            enableAllButtons();
        }
    }

    private void handleGameEnd(String winner) {
        gameEnded = true;
        disableAllButtons();
        // Removed: aiThinkingIndicator.setVisibility(View.GONE); // Hide indicator if game ends during AI turn

        if (winner != null) {
            // Reverted to original winner_found string resource
            statusLabel.setText(getString(R.string.winner_found, winner));
            playSound(winSoundId);
            if (winner.equals(player1Symbol)) {
                userWins++;
            } else {
                aiWins++;
            }
            Toast.makeText(this, "Game Over! " + winner + " wins!", Toast.LENGTH_LONG).show();
        } else {
            statusLabel.setText(R.string.game_tie);
            playSound(tieSoundId);
            draws++;
            Toast.makeText(this, "Game Over! It's a tie!", Toast.LENGTH_LONG).show();
        }
        saveScores();
        updateScoreLabel();
    }

    private void disableAllButtons() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                buttons[r][c].setEnabled(false);
            }
        }
    }

    private void enableAllButtons() {
        // Only enable buttons that are currently empty
        String[][] currentBoard = gameState.getCurrentBoardState();
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (currentBoard[r][c].isEmpty()) {
                    buttons[r][c].setEnabled(true);
                }
            }
        }
    }


    /**
     * Undoes the last turn, typically both the human's move and the AI's response.
     */
    private void undoLastMove() {
        // Stop any pending AI moves if undo is pressed during AI's delay
        aiHandler.removeCallbacksAndMessages(null);
        isAITurn = false; // Ensure AI turn flag is reset
        // Removed: aiThinkingIndicator.setVisibility(View.GONE); // Hide thinking indicator on undo

        // We need at least 2 states in history for a human-AI turn to undo (initial empty board + human's first move).
        // If history size is 1, it's just the initial empty board, so no moves to undo.
        if (gameState.getHistorySize() <= 1) {
            Toast.makeText(this, "No more moves to undo!", Toast.LENGTH_SHORT).show();
            return;
        }

        // If the game had just ended, reset the game state and re-enable buttons
        if (gameEnded) {
            gameEnded = false;
            // The board UI will be reset below after popping states.
            // Ensure all buttons are re-enabled if they were disabled due to game end.
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    buttons[r][c].setEnabled(true);
                }
            }
        }

        // Pop two states: AI's move and then human's move
        // Always try to undo the AI's last move (most recent)
        gameState.restorePreviousBoardState(); // Pop AI's move

        // Then, if there's another move (human's move) AND it's not just the initial empty board, pop it too.
        if (gameState.getHistorySize() > 1) {
            gameState.restorePreviousBoardState(); // Pop human's move
        } else {
            // This case happens if only the human's first move was present, and we just popped it.
            // The board is now effectively empty, and history size is 1 (initial empty board).
        }


        // Update UI with the restored board state
        String[][] restoredBoard = gameState.getCurrentBoardState();
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                buttons[r][c].setText(restoredBoard[r][c]);
                buttons[r][c].setEnabled(true); // Re-enable all buttons to allow new moves
            }
        }

        // Set the current player back to the human, as they should make the next move after an undo
        currentPlayer = player1Symbol;
        updateTurnLabel();
        statusLabel.setText(R.string.game_in_progress); // Always set status to in progress
        gameEnded = false; // Ensure gameEnded is false
        enableAllButtons(); // Ensure all relevant buttons are enabled

        Toast.makeText(this, "Last turn undone!", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadSettings(); // Reload settings when activity resumes (e.g., after returning from settings)
        playBackgroundMusic(); // Resume background music if enabled
        updateScoreLabel(); // Ensure scores are up-to-date
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (backgroundMusicPlayer != null && backgroundMusicPlayer.isPlaying()) {
            backgroundMusicPlayer.pause(); // Pause background music when activity is not in foreground
        }
        aiHandler.removeCallbacksAndMessages(null); // Stop any pending AI moves
        // Removed: aiThinkingIndicator.setVisibility(View.GONE); // Hide indicator if activity pauses
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.release(); // Release MediaPlayer resources
            backgroundMusicPlayer = null;
        }
        if (soundPool != null) {
            soundPool.release(); // Release SoundPool resources
            soundPool = null;
        }
        aiHandler.removeCallbacksAndMessages(null); // Ensure all AI handler messages are removed
    }
}
