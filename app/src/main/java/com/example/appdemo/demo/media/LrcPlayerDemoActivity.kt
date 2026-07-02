package com.example.appdemo.demo.media

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar
import com.example.appdemo.demo.media.lrc.LrcParser
import com.example.appdemo.demo.media.lrc.LrcView

/**
 * 方案 A：MediaPlayer + LRC 逐句高亮。
 *
 * 数据源：
 *   · LRC —— 从 assets/media/sample.lrc 内置读取（《静夜思》4 句配套）
 *   · 音频 —— 用户运行时通过系统 SAF 选一段 mp3；未选择前按钮"选择音频"
 *
 * 同步：Handler 100ms 轮询 currentPosition → LrcParser.indexAt 二分定位 → LrcView.updateProgress。
 * SeekBar 双向：播放时被动更新；用户拖动时暂停 poll，抬手 seekTo。
 * LrcView 拖动指示 + tap 回调 → 直接 seekTo 该行 timeMs，产品级双向同步。
 */
class LrcPlayerDemoActivity : AppCompatActivity() {

    private lateinit var lrcView: LrcView
    private lateinit var seekBar: SeekBar
    private lateinit var timeLabel: TextView
    private lateinit var pickBtn: Button
    private lateinit var playBtn: Button
    private lateinit var titleLabel: TextView

    private var player: MediaPlayer? = null
    private var prepared = false
    private var isSeeking = false

    private var lrcLines: List<LrcParser.LrcLine> = emptyList()

    private val handler = Handler(Looper.getMainLooper())
    private val poller = object : Runnable {
        override fun run() {
            val p = player ?: return
            if (prepared && p.isPlaying) {
                val pos = p.currentPosition
                if (!isSeeking) seekBar.progress = pos
                timeLabel.text = "${formatTime(pos.toLong())} / ${formatTime(p.duration.toLong())}"
                lrcView.updateProgress(LrcParser.indexAt(lrcLines, pos.toLong()))
            }
            handler.postDelayed(this, POLL_INTERVAL_MS)
        }
    }

    private val picker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@registerForActivityResult
        try {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: SecurityException) {
            // 有些提供方不支持持久化权限；忽略即可，本次会话内 uri 仍可读。
        }
        loadAudio(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_media_lrc_player)
        setupDemoToolbar(R.string.media_lrc_title, R.id.media_lrc_root)

        lrcView = findViewById(R.id.media_lrc_view)
        seekBar = findViewById(R.id.media_lrc_seek)
        timeLabel = findViewById(R.id.media_lrc_time)
        pickBtn = findViewById(R.id.media_lrc_pick)
        playBtn = findViewById(R.id.media_lrc_play)
        titleLabel = findViewById(R.id.media_lrc_song_title)

        lrcLines = loadBuiltInLrc()
        lrcView.setLines(lrcLines)
        lrcView.setOnSeekListener { timeMs ->
            player?.takeIf { prepared }?.seekTo(timeMs.toInt())
            seekBar.progress = timeMs.toInt()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, value: Int, fromUser: Boolean) {
                if (fromUser && prepared) {
                    timeLabel.text = "${formatTime(value.toLong())} / ${formatTime(player!!.duration.toLong())}"
                }
            }
            override fun onStartTrackingTouch(sb: SeekBar?) { isSeeking = true }
            override fun onStopTrackingTouch(sb: SeekBar?) {
                isSeeking = false
                if (prepared) player?.seekTo(sb!!.progress)
            }
        })

        pickBtn.setOnClickListener {
            picker.launch(arrayOf("audio/*"))
        }
        playBtn.setOnClickListener { togglePlay() }
        playBtn.isEnabled = false
    }

    private fun loadBuiltInLrc(): List<LrcParser.LrcLine> {
        val raw = assets.open("media/sample.lrc").bufferedReader().use { it.readText() }
        val parsed = LrcParser.parse(raw)
        titleLabel.text = buildString {
            append(parsed.title ?: getString(R.string.media_lrc_unknown_title))
            parsed.artist?.let { append(" · ").append(it) }
        }
        return parsed.lines
    }

    private fun loadAudio(uri: Uri) {
        releasePlayer()
        val mp = MediaPlayer()
        try {
            mp.setDataSource(this, uri)
        } catch (t: Throwable) {
            Toast.makeText(this, R.string.media_lrc_load_failed, Toast.LENGTH_LONG).show()
            return
        }
        mp.setOnPreparedListener {
            prepared = true
            seekBar.max = it.duration
            timeLabel.text = "00:00 / ${formatTime(it.duration.toLong())}"
            playBtn.isEnabled = true
            playBtn.setText(R.string.media_lrc_play)
        }
        mp.setOnCompletionListener {
            playBtn.setText(R.string.media_lrc_play)
            seekBar.progress = 0
            it.seekTo(0)
            lrcView.updateProgress(-1)
            handler.removeCallbacks(poller)
        }
        mp.setOnErrorListener { _, _, _ ->
            Toast.makeText(this, R.string.media_lrc_load_failed, Toast.LENGTH_LONG).show()
            true
        }
        player = mp
        mp.prepareAsync()
    }

    private fun togglePlay() {
        val mp = player ?: return
        if (!prepared) return
        if (mp.isPlaying) {
            mp.pause()
            playBtn.setText(R.string.media_lrc_play)
            handler.removeCallbacks(poller)
        } else {
            mp.start()
            playBtn.setText(R.string.media_lrc_pause)
            handler.post(poller)
        }
    }

    private fun releasePlayer() {
        handler.removeCallbacks(poller)
        player?.apply {
            try { if (isPlaying) stop() } catch (_: IllegalStateException) {}
            release()
        }
        player = null
        prepared = false
        playBtn.isEnabled = false
        playBtn.setText(R.string.media_lrc_play)
        seekBar.progress = 0
        timeLabel.text = "00:00 / 00:00"
        lrcView.updateProgress(-1)
    }

    private fun formatTime(ms: Long): String {
        val total = ms / 1000
        return "%02d:%02d".format(total / 60, total % 60)
    }

    override fun onPause() {
        super.onPause()
        player?.takeIf { prepared && it.isPlaying }?.let {
            it.pause()
            playBtn.setText(R.string.media_lrc_play)
            handler.removeCallbacks(poller)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    companion object {
        private const val POLL_INTERVAL_MS = 100L
    }
}
