package com.example.appdemo.demo.media

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.widget.Button
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar
import java.util.Locale

/**
 * TTS 朗读跟随。
 *
 * 核心：
 *   · TextToSpeech 合成 + UtteranceProgressListener 回调
 *   · 文本按标点切句，每句一个 utteranceId = 句索引
 *   · onStart 回调：整句底色高亮 + 自动滚动
 *   · onRangeStart（API 26+）：精细到字符区间，前景色跟随
 *   · onDone 回调：把句索引 +1，逻辑上"顺序播下一句"由入队顺序保证
 *
 * 暂停策略：TTS 没有暂停 API，stop() 会清空队列。这里记住当前句索引，
 * 恢复时从该句开始重新 speak(QUEUE_ADD)。
 */
class TtsReaderDemoActivity : AppCompatActivity() {

    private lateinit var textView: TextView
    private lateinit var scrollView: ScrollView
    private lateinit var playBtn: Button
    private lateinit var restartBtn: Button
    private lateinit var speedSeek: SeekBar
    private lateinit var speedLabel: TextView

    private var tts: TextToSpeech? = null
    private var engineReady = false

    private lateinit var fullText: String
    private lateinit var sentences: List<Sentence>
    private lateinit var spannable: SpannableString

    private val sentenceBg = BackgroundColorSpan(0x33FF9800)
    private val charFg = ForegroundColorSpan(0xFFFF6D00.toInt())

    /** 当前正在读的句索引；-1 表示未开始 / 已停止 */
    private var currentIndex = -1

    /** 用户主动暂停 vs 播完自然停止，UI 状态判定用 */
    private var isSpeaking = false

    private data class Sentence(val start: Int, val end: Int, val text: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_media_tts_reader)
        setupDemoToolbar(R.string.media_tts_title, R.id.media_tts_root)

        textView = findViewById(R.id.media_tts_text)
        scrollView = findViewById(R.id.media_tts_scroll)
        playBtn = findViewById(R.id.media_tts_play)
        restartBtn = findViewById(R.id.media_tts_restart)
        speedSeek = findViewById(R.id.media_tts_speed)
        speedLabel = findViewById(R.id.media_tts_speed_label)

        fullText = getString(R.string.media_tts_content)
        sentences = splitSentences(fullText)
        spannable = SpannableString(fullText)
        textView.text = spannable
        textView.movementMethod = LinkMovementMethod.getInstance()

        speedSeek.progress = 50
        updateSpeedLabel(50)
        speedSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, value: Int, fromUser: Boolean) {
                updateSpeedLabel(value)
                tts?.setSpeechRate(progressToRate(value))
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        playBtn.setOnClickListener { onPlayClicked() }
        restartBtn.setOnClickListener { restart() }

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                setupEngine()
            } else {
                Toast.makeText(this, R.string.media_tts_init_failed, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupEngine() {
        val engine = tts ?: return
        val chosen = listOf(Locale.CHINA, Locale.SIMPLIFIED_CHINESE, Locale.getDefault())
            .firstOrNull {
                val r = engine.isLanguageAvailable(it)
                r == TextToSpeech.LANG_AVAILABLE ||
                    r == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
                    r == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE
            } ?: Locale.US
        engine.language = chosen
        engine.setSpeechRate(progressToRate(speedSeek.progress))
        engine.setOnUtteranceProgressListener(listener)
        engineReady = true
        playBtn.isEnabled = true
    }

    private val listener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
            val idx = utteranceId?.toIntOrNull() ?: return
            runOnUiThread {
                currentIndex = idx
                isSpeaking = true
                playBtn.setText(R.string.media_tts_pause)
                highlightSentence(idx)
            }
        }

        /**
         * API 26+ 精细回调：start/end 是该句子文本内部的字符区间。
         * 加上句子的 offset 就是全文位置。
         */
        override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
            val idx = utteranceId?.toIntOrNull() ?: return
            val sentence = sentences.getOrNull(idx) ?: return
            val absStart = sentence.start + start
            val absEnd = (sentence.start + end).coerceAtMost(sentence.end)
            runOnUiThread { highlightRange(absStart, absEnd) }
        }

        override fun onDone(utteranceId: String?) {
            val idx = utteranceId?.toIntOrNull() ?: return
            runOnUiThread {
                if (idx == sentences.lastIndex) {
                    isSpeaking = false
                    currentIndex = -1
                    playBtn.setText(R.string.media_tts_play)
                    clearHighlights()
                }
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) {
            runOnUiThread { fallbackAfterError() }
        }

        override fun onError(utteranceId: String?, errorCode: Int) {
            runOnUiThread { fallbackAfterError() }
        }
    }

    private fun onPlayClicked() {
        if (!engineReady) return
        val engine = tts ?: return
        if (isSpeaking) {
            engine.stop()
            isSpeaking = false
            playBtn.setText(R.string.media_tts_play)
        } else {
            val startIdx = if (currentIndex < 0) 0 else currentIndex
            enqueueFrom(startIdx)
        }
    }

    private fun restart() {
        val engine = tts ?: return
        engine.stop()
        currentIndex = -1
        clearHighlights()
        scrollView.smoothScrollTo(0, 0)
        enqueueFrom(0)
    }

    private fun enqueueFrom(startIndex: Int) {
        val engine = tts ?: return
        for (i in startIndex..sentences.lastIndex) {
            val mode = if (i == startIndex) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            engine.speak(sentences[i].text, mode, null, i.toString())
        }
    }

    private fun fallbackAfterError() {
        isSpeaking = false
        playBtn.setText(R.string.media_tts_play)
        Toast.makeText(this, R.string.media_tts_error, Toast.LENGTH_SHORT).show()
    }

    private fun highlightSentence(index: Int) {
        val s = sentences.getOrNull(index) ?: return
        spannable.removeSpan(sentenceBg)
        spannable.removeSpan(charFg)
        spannable.setSpan(sentenceBg, s.start, s.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.text = spannable
        scrollTo(s.start)
    }

    private fun highlightRange(start: Int, end: Int) {
        if (start >= end) return
        spannable.removeSpan(charFg)
        spannable.setSpan(charFg, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.text = spannable
    }

    private fun clearHighlights() {
        spannable.removeSpan(sentenceBg)
        spannable.removeSpan(charFg)
        textView.text = spannable
    }

    private fun scrollTo(charIndex: Int) {
        textView.post {
            val layout = textView.layout ?: return@post
            val line = layout.getLineForOffset(charIndex)
            val y = layout.getLineTop(line) + textView.top - scrollView.height / 3
            scrollView.smoothScrollTo(0, y.coerceAtLeast(0))
        }
    }

    private fun updateSpeedLabel(progress: Int) {
        speedLabel.text = getString(R.string.media_tts_speed_fmt, progressToRate(progress))
    }

    /// 0..100 → 0.5x..2.0x 线性映射
    private fun progressToRate(progress: Int): Float = 0.5f + progress / 100f * 1.5f

    /// 按 。！？；\n 切句，保留标点作为句尾
    private fun splitSentences(text: String): List<Sentence> {
        val result = mutableListOf<Sentence>()
        val delims = setOf('。', '！', '？', '；', '\n', '.', '!', '?', ';')
        var start = 0
        var i = 0
        while (i < text.length) {
            if (text[i] in delims) {
                val end = i + 1
                val seg = text.substring(start, end).trim()
                if (seg.isNotEmpty()) result.add(Sentence(start, end, text.substring(start, end)))
                start = end
            }
            i++
        }
        if (start < text.length) {
            val seg = text.substring(start).trim()
            if (seg.isNotEmpty()) result.add(Sentence(start, text.length, text.substring(start)))
        }
        return result
    }

    override fun onStop() {
        super.onStop()
        tts?.stop()
        isSpeaking = false
        playBtn.setText(R.string.media_tts_play)
    }

    override fun onDestroy() {
        super.onDestroy()
        tts?.shutdown()
        tts = null
    }
}
