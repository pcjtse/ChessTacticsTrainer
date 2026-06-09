# Chess Tactics Trainer

An Android app for practising chess tactics with puzzles sourced live from Lichess, with an optional AI coach powered by OpenAI.

## Features

- **Daily puzzles** — fetches random rated puzzles from the Lichess public API (no account required)
- **Interactive board** — tap to select and move pieces; the board flips so your pieces are always at the bottom
- **Multi-move puzzles** — computer replies are played automatically after each correct move
- **Hints & solution** — reveal the piece to move or show the full solution on the board
- **AI Coach** — after completing or failing a puzzle, request a GPT-powered explanation of the tactic
- **Stats tracking** — solve streak, per-theme win rates, and overall progress stored locally
- **Background prefetch** — puzzles are cached ahead of time so there's no wait between rounds

## Tech Stack

| Layer | Library |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose |
| Chess engine | [chesslib](https://github.com/bhlangonijr/chesslib) 1.3.3 |
| Networking | Retrofit + OkHttp + Moshi |
| Local storage | DataStore Preferences |
| Background work | WorkManager |
| DI | Manual `AppContainer` service locator (no Hilt/KSP — AGP 9.x compat) |
| AI | OpenAI Chat Completions API |

## Getting Started

### Prerequisites

- Android Studio Meerkat or later
- Android SDK 36+
- An OpenAI API key (optional — AI Coach is disabled if the key is absent)

### Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/ChessTacticsTrainer.git
   ```

2. Create `local.properties` in the project root (this file is gitignored):
   ```properties
   sdk.dir=/path/to/your/Android/Sdk
   OPENAI_API_KEY=sk-...
   ```
   Omit `OPENAI_API_KEY` or leave it blank to run without AI Coach.

3. Open the project in Android Studio and run on a device or emulator (API 36+).

## Architecture

```
app/
├── data/
│   ├── local/          # DataStore-backed puzzle cache, progress, theme stats
│   ├── mapper/         # Lichess API DTO → domain model (self-correcting FEN reconstruction)
│   ├── remote/         # Retrofit services for Lichess and OpenAI
│   └── repository/     # PuzzleRepositoryImpl with background prefetch
├── di/
│   └── AppContainer.kt # Service locator wired in Application class
├── domain/
│   ├── engine/         # ChessEngine interface
│   ├── model/          # Puzzle, BoardState, UserProgress, …
│   ├── repository/     # PuzzleRepository interface
│   └── usecase/        # GetNextPuzzle, ValidateMove, GetAiExplanation, …
├── engine/
│   └── ChessLibEngine  # chesslib adapter — move generation, FEN, legality
└── presentation/
    ├── board/          # ChessBoardComponent, piece renderer
    ├── home/           # Streak + quick-start screen
    ├── puzzle/         # PuzzleScreen, PuzzleViewModel, PuzzleUiState
    ├── stats/          # Per-theme stats screen
    └── settings/       # Settings screen
```

## Puzzle Format

Puzzles are fetched from `GET /api/puzzle/next`. The Lichess response includes a full game PGN and `initialPly`. The mapper reconstructs the puzzle starting FEN by trying offsets around `initialPly` and selecting the position where `solution[0]` is a legal move — this handles the inconsistent offset across puzzles.

Solution moves follow the convention: even indices (0, 2, 4, …) are the player's moves, odd indices (1, 3, 5, …) are the computer's replies.

## License

MIT
