package com.archiekuo.tvcalib

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

private const val VERSION = "v1.0"
private const val AUTHOR = "作者：ArchieKUO"
private val GOLD = Color.parseColor("#D4A017")
private val BG = Color.parseColor("#14161A")
private val RED_REF = Color.rgb(216, 27, 27)

private data class Step(val title: String, val action: String, val path: String)

private data class Pattern(val name: String, val hint: String, val kind: String)

class MainActivity : Activity() {

    private enum class Screen { HOME, HELP, GUIDE, RED_TEST, RED_ADVICE, RESULT, PATTERNS }

    private val steps = listOf(
        Step("圖像模式", "選「標準」或「電影」\n不要選「鮮豔」「動態」", "設定 → 畫面 → 圖像模式"),
        Step("色域", "選「標準」\n或 Rec.709、sRGB", "設定 → 畫面 → 進階 → 色域\n找不到這項就按確定跳過"),
        Step("AI 優畫", "全部關閉\n（AI 影像、人臉、場景、物件）", "設定 → 畫面 → AI 優畫"),
        Step("色溫", "選「暖」", "設定 → 畫面 → 色溫"),
        Step("色彩（飽和度）", "先調到 45\n下一步再看紅色微調", "設定 → 畫面 → 色彩")
    )

    private val patterns = listOf(
        Pattern("紅色", "紅色是否刺眼？", "red"),
        Pattern("綠色", "綠色是否過於螢光？", "green"),
        Pattern("藍色", "藍色是否過深或過亮？", "blue"),
        Pattern("白色", "有沒有偏藍或偏黃？", "white"),
        Pattern("灰階", "最左兩格、最右兩格能分辨嗎？", "gray"),
        Pattern("膚色", "三種膚色看起來自然嗎？", "skin"),
        Pattern("黑色", "暗處有沒有發亮或不均？", "black")
    )

    private lateinit var root: FrameLayout
    private var screen = Screen.HOME
    private var stepIndex = 0
    private var patternIndex = 0
    private var captionVisible = true
    private var redRound = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        root = FrameLayout(this)
        root.setBackgroundColor(BG)
        setContentView(root)
        showScreen(Screen.HOME)
    }

    private fun showScreen(s: Screen) {
        screen = s
        root.removeAllViews()
        when (s) {
            Screen.HOME -> buildHome()
            Screen.HELP -> buildHelp()
            Screen.GUIDE -> buildGuide()
            Screen.RED_TEST -> buildRedTest()
            Screen.RED_ADVICE -> buildRedAdvice()
            Screen.RESULT -> buildResult()
            Screen.PATTERNS -> buildPatterns()
        }
    }

    private fun text(
        t: String,
        sp: Float,
        color: Int = Color.WHITE,
        bold: Boolean = false,
        italic: Boolean = false,
        gravity: Int = Gravity.CENTER
    ): TextView {
        val v = TextView(this)
        v.text = t
        v.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, sp)
        v.setTextColor(color)
        v.gravity = gravity
        val style = when {
            bold && italic -> Typeface.BOLD_ITALIC
            bold -> Typeface.BOLD
            italic -> Typeface.ITALIC
            else -> Typeface.NORMAL
        }
        v.setTypeface(Typeface.DEFAULT, style)
        v.setLineSpacing(0f, 1.2f)
        return v
    }

    private fun credit(): TextView =
        text("$AUTHOR　$VERSION", 26f, GOLD, italic = true)

    private fun full(v: View) {
        root.addView(
            v,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    private fun column(): LinearLayout {
        val c = LinearLayout(this)
        c.orientation = LinearLayout.VERTICAL
        c.gravity = Gravity.CENTER
        c.setPadding(dp(48), dp(24), dp(48), dp(24))
        return c
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun gap(h: Int): View {
        val v = View(this)
        v.layoutParams = LinearLayout.LayoutParams(1, dp(h))
        return v
    }

    private fun menuButton(label: String, onClick: () -> Unit): TextView {
        val b = text(label, 40f, Color.WHITE, bold = true)
        b.isFocusable = true
        b.setPadding(dp(24), dp(16), dp(24), dp(16))
        val lp = LinearLayout.LayoutParams(dp(520), ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.setMargins(0, dp(10), 0, dp(10))
        b.layoutParams = lp
        b.setBackgroundColor(Color.parseColor("#2F3540"))
        b.setOnFocusChangeListener { v, hasFocus ->
            val t = v as TextView
            if (hasFocus) {
                t.setBackgroundColor(Color.WHITE)
                t.setTextColor(BG)
            } else {
                t.setBackgroundColor(Color.parseColor("#2F3540"))
                t.setTextColor(Color.WHITE)
            }
        }
        b.setOnClickListener { onClick() }
        return b
    }

    private fun buildHome() {
        val c = column()
        c.addView(text("電視校色小幫手", 56f, bold = true))
        c.addView(gap(8))
        c.addView(text("遙控器上下選，按確定進入", 30f, Color.parseColor("#B8BCC4")))
        c.addView(gap(24))
        val b1 = menuButton("開始引導校色") {
            stepIndex = 0
            redRound = 1
            showScreen(Screen.GUIDE)
        }
        val b2 = menuButton("單獨看測試圖") {
            patternIndex = 0
            captionVisible = true
            showScreen(Screen.PATTERNS)
        }
        val b3 = menuButton("使用說明") { showScreen(Screen.HELP) }
        c.addView(b1)
        c.addView(b2)
        c.addView(b3)
        c.addView(gap(24))
        c.addView(credit())
        full(c)
        b1.requestFocus()
    }

    private fun buildHelp() {
        val c = column()
        c.addView(text("使用說明", 52f, bold = true))
        c.addView(gap(20))
        c.addView(
            text(
                "這個程式只負責顯示測試圖和說明\n" +
                    "電視設定要用你自己的遙控器調整\n" +
                    "選單名稱依電視可能略有不同\n\n" +
                    "左右鍵：上一步／下一步\n" +
                    "確定鍵：下一步\n" +
                    "返回鍵：回到首頁\n\n" +
                    "按任意鍵回首頁",
                36f
            )
        )
        c.addView(gap(20))
        c.addView(credit())
        full(c)
    }

    private fun buildGuide() {
        val step = steps[stepIndex]
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.setPadding(dp(32), dp(24), dp(32), dp(24))
        row.gravity = Gravity.CENTER_VERTICAL

        val patch = LinearLayout(this)
        patch.orientation = LinearLayout.VERTICAL
        val top = View(this)
        top.setBackgroundColor(RED_REF)
        val bottom = View(this)
        bottom.setBackgroundColor(Color.rgb(224, 172, 105))
        patch.addView(top, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 2f))
        patch.addView(bottom, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        row.addView(patch, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0.8f))

        val right = LinearLayout(this)
        right.orientation = LinearLayout.VERTICAL
        right.gravity = Gravity.CENTER
        right.setPadding(dp(24), 0, 0, 0)
        right.addView(text("步驟 ${stepIndex + 1}／${steps.size}", 32f, Color.parseColor("#B8BCC4")))
        right.addView(gap(8))
        right.addView(text(step.title, 52f, bold = true))
        right.addView(gap(12))
        right.addView(text(step.action, 44f, bold = true))
        right.addView(gap(16))
        right.addView(text(step.path, 30f, Color.parseColor("#B8BCC4")))
        right.addView(gap(24))
        right.addView(text("◀ 上一步　　確定 下一步 ▶", 32f, Color.parseColor("#DDDDDD")))
        right.addView(gap(12))
        right.addView(credit())
        row.addView(right, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.2f))
        full(row)
    }

    private fun buildRedTest() {
        val f = FrameLayout(this)
        f.setBackgroundColor(RED_REF)
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.gravity = Gravity.CENTER
        box.addView(text("這個紅色刺眼嗎？", 64f, bold = true))
        box.addView(gap(16))
        box.addView(text("第 $redRound 次", 32f))
        f.addView(
            box,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        val bar = LinearLayout(this)
        bar.orientation = LinearLayout.VERTICAL
        bar.gravity = Gravity.CENTER
        bar.setBackgroundColor(Color.parseColor("#CC14161A"))
        bar.setPadding(dp(24), dp(14), dp(24), dp(14))
        bar.addView(text("◀ 太紅　　　　　自然 ▶", 44f, bold = true))
        bar.addView(credit())
        val lp = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lp.gravity = Gravity.BOTTOM
        f.addView(bar, lp)
        full(f)
    }

    private fun buildRedAdvice() {
        val c = column()
        c.addView(text("紅色還是太紅", 52f, bold = true))
        c.addView(gap(20))
        c.addView(
            text(
                "請回到電視設定\n把「色彩」再降 5\n\n" +
                    "如果有「色域」選項\n確認已設成「標準」\n\n" +
                    "調好後按確定，再看一次紅色",
                40f
            )
        )
        c.addView(gap(20))
        c.addView(text("返回鍵：回首頁", 30f, Color.parseColor("#B8BCC4")))
        c.addView(gap(8))
        c.addView(credit())
        full(c)
    }

    private fun buildResult() {
        val c = column()
        c.addView(text("校色完成", 56f, bold = true))
        c.addView(gap(16))
        val redLine = if (redRound <= 1) "紅色：自然" else "紅色：自然（調整了 ${redRound - 1} 次）"
        c.addView(
            text(
                "圖像模式：標準或電影\n" +
                    "色域：標準\n" +
                    "AI 優畫：關閉\n" +
                    "色溫：暖\n" +
                    "色彩：依你調整的結果\n" +
                    redLine,
                40f
            )
        )
        c.addView(gap(16))
        c.addView(text("建議用手機拍下目前的設定", 30f, Color.parseColor("#B8BCC4")))
        c.addView(gap(8))
        c.addView(text("◀ 再校一次　　確定 回首頁 ▶", 34f, Color.parseColor("#DDDDDD")))
        c.addView(gap(12))
        c.addView(credit())
        full(c)
    }

    private fun patternView(kind: String): View {
        return when (kind) {
            "red" -> solid(RED_REF)
            "green" -> solid(Color.rgb(30, 170, 60))
            "blue" -> solid(Color.rgb(30, 60, 200))
            "white" -> solid(Color.WHITE)
            "black" -> solid(Color.BLACK)
            "gray" -> {
                val row = LinearLayout(this)
                row.orientation = LinearLayout.HORIZONTAL
                for (i in 0..10) {
                    val g = (255 * i / 10)
                    val v = View(this)
                    v.setBackgroundColor(Color.rgb(g, g, g))
                    row.addView(
                        v,
                        LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
                    )
                }
                row
            }
            else -> {
                val row = LinearLayout(this)
                row.orientation = LinearLayout.HORIZONTAL
                val tones = listOf(
                    Color.rgb(255, 224, 189),
                    Color.rgb(224, 172, 105),
                    Color.rgb(141, 85, 36)
                )
                for (t in tones) {
                    val v = View(this)
                    v.setBackgroundColor(t)
                    row.addView(
                        v,
                        LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
                    )
                }
                row
            }
        }
    }

    private fun solid(color: Int): View {
        val v = View(this)
        v.setBackgroundColor(color)
        return v
    }

    private fun buildPatterns() {
        val p = patterns[patternIndex]
        val f = FrameLayout(this)
        f.addView(
            patternView(p.kind),
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        if (captionVisible) {
            val bar = LinearLayout(this)
            bar.orientation = LinearLayout.VERTICAL
            bar.gravity = Gravity.CENTER
            bar.setBackgroundColor(Color.parseColor("#CC14161A"))
            bar.setPadding(dp(24), dp(14), dp(24), dp(14))
            bar.addView(text("${patternIndex + 1}／${patterns.size}　${p.name}", 40f, bold = true))
            bar.addView(text(p.hint, 34f))
            bar.addView(text("◀ ▶ 換圖　確定 隱藏文字　返回 首頁", 28f, Color.parseColor("#B8BCC4")))
            bar.addView(credit())
            val lp = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            lp.gravity = Gravity.BOTTOM
            f.addView(bar, lp)
        }
        full(f)
    }

    private fun isOk(k: Int) = k == KeyEvent.KEYCODE_DPAD_CENTER ||
        k == KeyEvent.KEYCODE_ENTER ||
        k == KeyEvent.KEYCODE_NUMPAD_ENTER

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (screen == Screen.HOME) {
            return super.onKeyDown(keyCode, event)
        }
        val back = keyCode == KeyEvent.KEYCODE_BACK
        val left = keyCode == KeyEvent.KEYCODE_DPAD_LEFT
        val right = keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
        val ok = isOk(keyCode)

        when (screen) {
            Screen.HELP -> {
                showScreen(Screen.HOME)
                return true
            }
            Screen.GUIDE -> {
                when {
                    back -> showScreen(Screen.HOME)
                    left -> if (stepIndex > 0) {
                        stepIndex--
                        showScreen(Screen.GUIDE)
                    }
                    right || ok -> {
                        if (stepIndex < steps.size - 1) {
                            stepIndex++
                            showScreen(Screen.GUIDE)
                        } else {
                            redRound = 1
                            showScreen(Screen.RED_TEST)
                        }
                    }
                    else -> return super.onKeyDown(keyCode, event)
                }
                return true
            }
            Screen.RED_TEST -> {
                when {
                    back -> showScreen(Screen.HOME)
                    left -> showScreen(Screen.RED_ADVICE)
                    right || ok -> showScreen(Screen.RESULT)
                    else -> return super.onKeyDown(keyCode, event)
                }
                return true
            }
            Screen.RED_ADVICE -> {
                when {
                    back -> showScreen(Screen.HOME)
                    ok -> {
                        redRound++
                        showScreen(Screen.RED_TEST)
                    }
                    else -> return super.onKeyDown(keyCode, event)
                }
                return true
            }
            Screen.RESULT -> {
                when {
                    back || ok -> showScreen(Screen.HOME)
                    left -> {
                        stepIndex = 0
                        redRound = 1
                        showScreen(Screen.GUIDE)
                    }
                    else -> return super.onKeyDown(keyCode, event)
                }
                return true
            }
            Screen.PATTERNS -> {
                when {
                    back -> showScreen(Screen.HOME)
                    left -> {
                        patternIndex = (patternIndex + patterns.size - 1) % patterns.size
                        showScreen(Screen.PATTERNS)
                    }
                    right -> {
                        patternIndex = (patternIndex + 1) % patterns.size
                        showScreen(Screen.PATTERNS)
                    }
                    ok -> {
                        captionVisible = !captionVisible
                        showScreen(Screen.PATTERNS)
                    }
                    else -> return super.onKeyDown(keyCode, event)
                }
                return true
            }
            else -> return super.onKeyDown(keyCode, event)
        }
    }
}
