package com.example.chesstacticstrainer.presentation

import androidx.compose.runtime.compositionLocalOf
import com.example.chesstacticstrainer.domain.model.AnimalDifficulty
import com.example.chesstacticstrainer.domain.model.GoDifficulty

class AppStrings(
    val isEnglish: Boolean,

    // Punctuation
    val colon: String,

    // Bottom nav
    val navHome: String,
    val navStats: String,
    val navSettings: String,

    // Home
    val homeTitle: String,
    val homeSelectMode: String,
    val homeSubtitle: String,
    val homeChessTitle: String,
    val homeChessSubtitle: String,
    val homeXiangqiTitle: String,
    val homeXiangqiSubtitle: String,
    val homeAnimalTitle: String,
    val homeAnimalSubtitle: String,
    val homeGoTitle: String,
    val homeGoSubtitle: String,
    val homeStreak: String,
    val homeRating: String,
    val homeDaysUnit: String,
    val homePlayed: String,
    val homeSolved: String,
    val homeWinRate: String,
    val homeAccuracy: String,
    val homeDifficulty: String,
    val homeStartPractice: String,
    val homeStartGame: String,

    // Stats
    val statsTitle: String,
    val statsChessTab: String,
    val statsXiangqiTab: String,
    val statsChessOverview: String,
    val statsXiangqiOverview: String,
    val statsRating: String,
    val statsCurrentStreak: String,
    val statsBestStreak: String,
    val statsPuzzlesSolved: String,
    val statsAccuracy: String,
    val statsTacticBreakdown: String,
    val statsDaysUnit: String,

    // Settings
    val settingsTitle: String,
    val settingsNotifications: String,
    val settingsDailyReminder: String,
    val settingsDailyReminderDesc: String,
    val settingsAbout: String,
    val settingsVersion: String,

    // Shared puzzle
    val back: String,
    val hint: String,
    val ratingPrefix: String,
    val findBestMove: String,
    val goodMoveFindNext: String,
    val findWinningMove: String,
    val excellent: String,
    val puzzleComplete: String,
    val incorrect: String,
    val notRightMove: String,
    val nextPuzzle: String,
    val tryAgain: String,
    val solutionShownOnBoard: String,
    val showSolution: String,
    val skipToNext: String,
    val aiCoach: String,
    val aiCoachThinking: String,
    val explainWithAi: String,

    // Chess puzzle
    val chessPuzzleTitle: String,

    // Xiangqi puzzle
    val xiangqiPuzzleTitle: String,
    val retry: String,
    val nextXiangqi: String,
    val skipXiangqi: String,

    // Animal game
    val animalAiCaptured: String,
    val animalAiThinking: String,
    val animalYourTurn: String,
    val animalWaitingAi: String,
    val animalYouCaptured: String,
    val animalYouRed: String,
    val animalAiBlue: String,
    val animalYouWin: String,
    val animalAiWins: String,
    val animalLostDesc: String,
    val animalPlayAgain: String,
    val animalNewGame: String,
    val animalBackToMenu: String,

    // Go puzzle
    val goPuzzleTitle: String,
    val goRetry: String,
    val goDifficultyPrefix: String,
    val goBlackCaptures: String,
    val goWhiteCaptures: String,
    val goSolutionShown: String,
    val goTryAgain: String,
    val goSkip: String,
    val goGoodMove: String,
    val goFindSolution: String,
    val goCorrect: String,
    val goCongrats: String,
    val goWrongMove: String,
    val goThinkAgain: String,
    val goNext: String,
    val goShowSolution: String,
    val goAiCoach: String,
    val goAiAnalyzing: String,
    val goAskAi: String,
) {
    fun animalDifficultyLabel(diff: AnimalDifficulty): String =
        if (isEnglish) when (diff) {
            AnimalDifficulty.EASY   -> "Easy"
            AnimalDifficulty.MEDIUM -> "Medium"
            AnimalDifficulty.HARD   -> "Hard"
        } else diff.label

    fun goDifficultyName(diff: GoDifficulty): String =
        if (isEnglish) when (diff) {
            GoDifficulty.BEGINNER -> "Beginner"
            GoDifficulty.EASY     -> "Easy"
            GoDifficulty.MEDIUM   -> "Medium"
            GoDifficulty.HARD     -> "Hard"
            GoDifficulty.EXPERT   -> "Expert"
        } else diff.displayName

    fun animalWonDesc(difficultyLabel: String): String =
        if (isEnglish) "Congrats! You beat the $difficultyLabel AI!"
        else "恭喜击败${difficultyLabel}难度的AI！"

    fun goLoadingText(diffName: String): String =
        if (isEnglish) "Loading $diffName puzzle…"
        else "正在获取${diffName}题目…"

    fun chessPuzzleThemeHint(theme: String): String =
        if (isEnglish) when (theme) {
            "mateIn1"          -> "Checkmate in 1"
            "mateIn2"          -> "Checkmate in 2"
            "mateIn3"          -> "Checkmate in 3"
            "mateIn4"          -> "Checkmate in 4"
            "mateIn5"          -> "Checkmate in 5"
            "fork"             -> "Fork"
            "pin"              -> "Pin"
            "skewer"           -> "Skewer"
            "discoveredAttack" -> "Discovered Attack"
            "discoveredCheck"  -> "Discovered Check"
            "doubleCheck"      -> "Double Check"
            "hangingPiece"     -> "Hanging Piece"
            "sacrifice"        -> "Sacrifice"
            "deflection"       -> "Deflection"
            "interference"     -> "Interference"
            "trappedPiece"     -> "Trapped Piece"
            "zugzwang"         -> "Zugzwang"
            else               -> theme.replaceFirstChar { it.uppercase() }
        } else when (theme) {
            "mateIn1"          -> "一步将杀"
            "mateIn2"          -> "两步将杀"
            "mateIn3"          -> "三步将杀"
            "mateIn4"          -> "四步将杀"
            "mateIn5"          -> "五步将杀"
            "fork"             -> "双打"
            "pin"              -> "牵制"
            "skewer"           -> "穿刺"
            "discoveredAttack" -> "发现进攻"
            "discoveredCheck"  -> "发现将军"
            "doubleCheck"      -> "双将"
            "hangingPiece"     -> "悬子"
            "sacrifice"        -> "弃子"
            "deflection"       -> "解除防御"
            "interference"     -> "干扰"
            "trappedPiece"     -> "困子"
            "zugzwang"         -> "逼走"
            else               -> theme.replaceFirstChar { it.uppercase() }
        }

    fun chessTacticDescription(theme: String): String =
        if (isEnglish) when (theme) {
            "mateIn1"          -> "The king has no escape — this move delivers immediate checkmate."
            "mateIn2"          -> "A forced two-move sequence that traps the king with no escape."
            "mateIn3"          -> "A three-move combination leading to an inescapable checkmate."
            "mateIn4"          -> "A four-move sequence that forces checkmate."
            "mateIn5"          -> "A five-move forced sequence ending in checkmate."
            "fork"             -> "One piece attacks two enemy pieces simultaneously, guaranteeing material gain."
            "pin"              -> "A piece is pinned — moving it would expose a more valuable piece behind it."
            "skewer"           -> "Like a reverse pin: the more valuable piece is attacked first, and a lesser piece hides behind it."
            "discoveredAttack" -> "Moving one piece uncovers an attack from another piece behind it."
            "discoveredCheck"  -> "Moving a piece reveals a check from another piece — the king must respond."
            "doubleCheck"      -> "Two pieces give check simultaneously — the king must move."
            "hangingPiece"     -> "A piece is left undefended — capture it to win material."
            "sacrifice"        -> "Giving up material to gain a decisive positional or tactical advantage."
            "deflection"       -> "Forcing an overloaded defending piece away from its duty."
            "interference"     -> "A piece is placed between two enemy pieces to cut off their coordination."
            "trappedPiece"     -> "An enemy piece has no safe square to move to — win it with tempo."
            "zugzwang"         -> "Any move the opponent makes worsens their position."
            else               -> "This is the strongest available move in the position."
        } else when (theme) {
            "mateIn1"          -> "王无路可逃——这一步直接形成将杀。"
            "mateIn2"          -> "通过强制二步棋序列，困住对方王，无处可逃。"
            "mateIn3"          -> "三步组合，形成无法逃脱的将杀。"
            "mateIn4"          -> "四步强制将杀序列。"
            "mateIn5"          -> "五步强制将杀序列。"
            "fork"             -> "一子同时攻击两枚对方棋子，必得其一。"
            "pin"              -> "被牵制——移动该子会暴露其身后更有价值的棋子。"
            "skewer"           -> "穿刺：先攻击价值较高的棋子，迫使其移走，再吃掉身后的棋子。"
            "discoveredAttack" -> "移动一枚棋子，揭开其身后另一枚棋子的进攻。"
            "discoveredCheck"  -> "移动一枚棋子，揭开另一枚棋子的将军——对方王必须应对。"
            "doubleCheck"      -> "两枚棋子同时将军——对方王必须移动。"
            "hangingPiece"     -> "一枚棋子无人保护——直接吃掉以获得子力优势。"
            "sacrifice"        -> "弃出子力，换取决定性的战略或战术优势。"
            "deflection"       -> "迫使一枚身兼多职的防守棋子离开其防守位置。"
            "interference"     -> "将一枚棋子置于两枚敌方棋子之间，切断其协作。"
            "trappedPiece"     -> "一枚对方棋子无处可逃——用先手将其吃掉。"
            "zugzwang"         -> "对方无论走什么都会恶化自身局面。"
            else               -> "这是当前局面最有力的着法。"
        }

    fun xiangqiTacticDescription(theme: String): String =
        if (isEnglish) when (theme) {
            "mateIn1" -> "This move delivers immediate checkmate — the king has no escape."
            "mateIn2" -> "A forced two-move sequence delivers checkmate."
            "mateIn3" -> "A three-move combination forces checkmate."
            "mateIn4" -> "A four-move sequence forces checkmate."
            "mateIn5" -> "A five-move sequence forces checkmate."
            "tactics" -> "This is the strongest tactical move in the position."
            else      -> "This is the best move in the position."
        } else when (theme) {
            "mateIn1" -> "此着法直接将死对方，将军无路可逃。"
            "mateIn2" -> "两步强制将杀，对方将军无法解脱。"
            "mateIn3" -> "三步连续将杀，形成无法破解的必杀棋。"
            "mateIn4" -> "四步强制将杀序列。"
            "mateIn5" -> "五步强制将杀序列。"
            "tactics" -> "此着法是当前局面的最强着法。"
            else      -> "此着法是当前局面的最强选择。"
        }

    fun chessThemeDisplayName(theme: String): String =
        if (isEnglish) when (theme) {
            "mateIn1"          -> "Checkmate in 1"
            "mateIn2"          -> "Checkmate in 2"
            "mateIn3"          -> "Checkmate in 3"
            "fork"             -> "Fork"
            "pin"              -> "Pin"
            "skewer"           -> "Skewer"
            "discoveredAttack" -> "Discovered Attack"
            "hangingPiece"     -> "Hanging Piece"
            "sacrifice"        -> "Sacrifice"
            "deflection"       -> "Deflection"
            else               -> theme.replaceFirstChar { it.uppercase() }
        } else when (theme) {
            "mateIn1"          -> "一步将杀 / Checkmate in 1"
            "mateIn2"          -> "两步将杀 / Checkmate in 2"
            "mateIn3"          -> "三步将杀 / Checkmate in 3"
            "fork"             -> "双打 / Fork"
            "pin"              -> "牵制 / Pin"
            "skewer"           -> "穿刺 / Skewer"
            "discoveredAttack" -> "发现进攻 / Discovered Attack"
            "hangingPiece"     -> "悬子 / Hanging Piece"
            "sacrifice"        -> "弃子 / Sacrifice"
            "deflection"       -> "解除防御 / Deflection"
            else               -> theme.replaceFirstChar { it.uppercase() }
        }

    fun xiangqiThemeDisplayName(theme: String): String =
        if (isEnglish) when (theme) {
            "mateIn1" -> "Checkmate in 1"
            "mateIn2" -> "Checkmate in 2"
            "mateIn3" -> "Checkmate in 3"
            "mateIn4" -> "Checkmate in 4"
            "mateIn5" -> "Checkmate in 5"
            "tactics" -> "Tactical Moves"
            else      -> theme
        } else when (theme) {
            "mateIn1" -> "一步将杀"
            "mateIn2" -> "两步将杀"
            "mateIn3" -> "三步将杀"
            "mateIn4" -> "四步将杀"
            "mateIn5" -> "五步将杀"
            "tactics" -> "战术妙手"
            else      -> theme
        }

    companion object {
        val ENGLISH = AppStrings(
            isEnglish = true,
            colon = ": ",
            navHome = "Home",
            navStats = "Stats",
            navSettings = "Settings",
            homeTitle = "Tactics Training",
            homeSelectMode = "Select Training Mode",
            homeSubtitle = "Improve your chess through tactics",
            homeChessTitle = "International Chess",
            homeChessSubtitle = "International Chess",
            homeXiangqiTitle = "Chinese Chess",
            homeXiangqiSubtitle = "Chinese Chess",
            homeAnimalTitle = "Animal Chess",
            homeAnimalSubtitle = "Animal Chess",
            homeGoTitle = "Go",
            homeGoSubtitle = "Go · Tsumego Puzzles",
            homeStreak = "Streak",
            homeRating = "Rating",
            homeDaysUnit = "d",
            homePlayed = "Played",
            homeSolved = "Solved",
            homeWinRate = "Win Rate",
            homeAccuracy = "Accuracy",
            homeDifficulty = "Difficulty",
            homeStartPractice = "Start Practice",
            homeStartGame = "Start Game",
            statsTitle = "Statistics",
            statsChessTab = "♟ Chess",
            statsXiangqiTab = "象 Chinese Chess",
            statsChessOverview = "Chess Overview",
            statsXiangqiOverview = "Chinese Chess Overview",
            statsRating = "Rating",
            statsCurrentStreak = "Current Streak",
            statsBestStreak = "Best Streak",
            statsPuzzlesSolved = "Puzzles Solved",
            statsAccuracy = "Accuracy",
            statsTacticBreakdown = "Tactic Breakdown",
            statsDaysUnit = " days",
            settingsTitle = "Settings",
            settingsNotifications = "Notifications",
            settingsDailyReminder = "Daily Puzzle Reminder",
            settingsDailyReminderDesc = "Get notified at 9 AM every day",
            settingsAbout = "About",
            settingsVersion = "Version 1.0",
            back = "Back",
            hint = "Hint",
            ratingPrefix = "Rating: ",
            findBestMove = "Find the best move",
            goodMoveFindNext = "Good move! Find the next one",
            findWinningMove = "Find the winning move",
            excellent = "Excellent!",
            puzzleComplete = "Puzzle complete!",
            incorrect = "Incorrect",
            notRightMove = "That's not the right move.",
            nextPuzzle = "Next Puzzle",
            tryAgain = "Try Again",
            solutionShownOnBoard = "Solution shown on board",
            showSolution = "Show Solution",
            skipToNext = "Skip to Next Puzzle",
            aiCoach = "AI Coach",
            aiCoachThinking = "AI Coach is thinking…",
            explainWithAi = "Explain with AI",
            chessPuzzleTitle = "Chess Puzzle",
            xiangqiPuzzleTitle = "Xiangqi Tactics",
            retry = "Retry",
            nextXiangqi = "Next",
            skipXiangqi = "Skip",
            animalAiCaptured = "AI Captured",
            animalAiThinking = "AI Thinking…",
            animalYourTurn = "Your Turn",
            animalWaitingAi = "Waiting for AI…",
            animalYouCaptured = "You Captured",
            animalYouRed = "You (Red)",
            animalAiBlue = "AI (Blue)",
            animalYouWin = "🎉 You Win!",
            animalAiWins = "😢 AI Wins",
            animalLostDesc = "Keep trying, challenge the AI!",
            animalPlayAgain = "Play Again",
            animalNewGame = "New Game",
            animalBackToMenu = "Back to Menu",
            goPuzzleTitle = "Go · Tsumego Puzzles",
            goRetry = "Retry",
            goDifficultyPrefix = "Difficulty: ",
            goBlackCaptures = "Black captures: ",
            goWhiteCaptures = "White captures: ",
            goSolutionShown = "Solution shown – place or skip",
            goTryAgain = "Try Again",
            goSkip = "Skip",
            goGoodMove = "Good move! Continue",
            goFindSolution = "Black to play – find the solution",
            goCorrect = "Correct!",
            goCongrats = "Congratulations, the stones have been captured.",
            goWrongMove = "Wrong move",
            goThinkAgain = "Think again.",
            goNext = "Next",
            goShowSolution = "Show Solution",
            goAiCoach = "Go AI Coach",
            goAiAnalyzing = "AI analyzing…",
            goAskAi = "Ask AI to analyze",
        )

        val CHINESE = AppStrings(
            isEnglish = false,
            colon = "：",
            navHome = "主页",
            navStats = "统计",
            navSettings = "设置",
            homeTitle = "战术训练",
            homeSelectMode = "选择训练模式",
            homeSubtitle = "通过战术题提升你的棋艺",
            homeChessTitle = "国际象棋",
            homeChessSubtitle = "International Chess",
            homeXiangqiTitle = "中国象棋",
            homeXiangqiSubtitle = "Chinese Chess",
            homeAnimalTitle = "斗兽棋",
            homeAnimalSubtitle = "Animal Chess",
            homeGoTitle = "围棋",
            homeGoSubtitle = "Go · Tsumego Puzzles",
            homeStreak = "连胜",
            homeRating = "评分",
            homeDaysUnit = "天",
            homePlayed = "已玩",
            homeSolved = "已解",
            homeWinRate = "胜率",
            homeAccuracy = "正确率",
            homeDifficulty = "难度",
            homeStartPractice = "开始练习",
            homeStartGame = "开始游戏",
            statsTitle = "统计数据",
            statsChessTab = "♟ 国际象棋",
            statsXiangqiTab = "象 中国象棋",
            statsChessOverview = "国际象棋概览",
            statsXiangqiOverview = "中国象棋概览",
            statsRating = "评分",
            statsCurrentStreak = "当前连胜",
            statsBestStreak = "最长连胜",
            statsPuzzlesSolved = "已解题数",
            statsAccuracy = "正确率",
            statsTacticBreakdown = "战术分类",
            statsDaysUnit = " 天",
            settingsTitle = "设置",
            settingsNotifications = "通知",
            settingsDailyReminder = "每日战术题提醒",
            settingsDailyReminderDesc = "每天上午9点推送提醒",
            settingsAbout = "关于",
            settingsVersion = "版本 1.0",
            back = "返回",
            hint = "提示",
            ratingPrefix = "评分：",
            findBestMove = "寻找最佳着法",
            goodMoveFindNext = "好棋！请继续寻找下一步",
            findWinningMove = "寻找制胜着法",
            excellent = "精彩！",
            puzzleComplete = "题目完成！",
            incorrect = "走法有误",
            notRightMove = "请再试一次。",
            nextPuzzle = "下一题",
            tryAgain = "再试一次",
            solutionShownOnBoard = "已在棋盘上显示答案",
            showSolution = "显示答案",
            skipToNext = "跳过此题",
            aiCoach = "AI教练",
            aiCoachThinking = "AI教练思考中…",
            explainWithAi = "AI解说",
            chessPuzzleTitle = "象棋战术",
            xiangqiPuzzleTitle = "象棋战术",
            retry = "重试",
            nextXiangqi = "下一题",
            skipXiangqi = "跳过此题",
            animalAiCaptured = "AI捕获",
            animalAiThinking = "AI思考中…",
            animalYourTurn = "你的回合",
            animalWaitingAi = "等待AI…",
            animalYouCaptured = "你捕获",
            animalYouRed = "你 (红方)",
            animalAiBlue = "AI (蓝方)",
            animalYouWin = "🎉 你赢了！",
            animalAiWins = "😢 AI获胜",
            animalLostDesc = "再接再厉，挑战AI！",
            animalPlayAgain = "再来一局",
            animalNewGame = "新游戏",
            animalBackToMenu = "返回主菜单",
            goPuzzleTitle = "围棋死活题",
            goRetry = "重试",
            goDifficultyPrefix = "难度：",
            goBlackCaptures = "黑提：",
            goWhiteCaptures = "白提：",
            goSolutionShown = "答案已显示，可落子或跳过",
            goTryAgain = "再试一次",
            goSkip = "跳过此题",
            goGoodMove = "好棋！请继续",
            goFindSolution = "黑先，寻找正解",
            goCorrect = "正解！",
            goCongrats = "恭喜，棋子已被提掉。",
            goWrongMove = "走法有误",
            goThinkAgain = "请再思考一下。",
            goNext = "下一题",
            goShowSolution = "显示答案",
            goAiCoach = "AI围棋教练",
            goAiAnalyzing = "AI解析中…",
            goAskAi = "请AI解析此题",
        )
    }
}

val LocalStrings = compositionLocalOf { AppStrings.CHINESE }
