package com.example.appdemo.demo.media.lrc

/**
 * LRC 歌词解析器。
 *
 * 支持格式：
 *   [ti:标题] [ar:艺人] [al:专辑]   → 头部元信息
 *   [mm:ss.xx]歌词                → 单时间戳
 *   [mm:ss.xx][mm:ss.xx]歌词       → 同一句多时间戳（副歌复用）
 *   [mm:ss]歌词                   → 无毫秒也允许
 *
 * 解析规则：
 *   1. 逐行扫描，遇到 [xx:xx] 反复截取时间戳
 *   2. 最后剩下的文本是歌词内容
 *   3. 每个时间戳产出一个 LrcLine
 *   4. 结果按 timeMs 升序返回
 */
object LrcParser {

    private val TIME_TAG = Regex("""\[(\d{1,2}):(\d{1,2})(?:[.:](\d{1,3}))?]""")
    private val META_TAG = Regex("""\[(ti|ar|al|by|offset):([^]]*)]""", RegexOption.IGNORE_CASE)

    data class LrcLine(val timeMs: Long, val text: String)

    data class LrcFile(
        val title: String?,
        val artist: String?,
        val album: String?,
        val offsetMs: Long,
        val lines: List<LrcLine>,
    )

    fun parse(raw: String): LrcFile {
        var title: String? = null
        var artist: String? = null
        var album: String? = null
        var offset = 0L
        val lines = mutableListOf<LrcLine>()

        raw.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty()) return@forEach

            META_TAG.matchEntire(line)?.let { m ->
                val tag = m.groupValues[1].lowercase()
                val value = m.groupValues[2].trim()
                when (tag) {
                    "ti" -> title = value
                    "ar" -> artist = value
                    "al" -> album = value
                    "offset" -> offset = value.toLongOrNull() ?: 0L
                }
                return@forEach
            }

            val stamps = TIME_TAG.findAll(line).toList()
            if (stamps.isEmpty()) return@forEach
            val text = line.substring(stamps.last().range.last + 1).trim()
            stamps.forEach { s ->
                val min = s.groupValues[1].toInt()
                val sec = s.groupValues[2].toInt()
                val fracRaw = s.groupValues[3]
                val ms = when (fracRaw.length) {
                    0 -> 0
                    1 -> fracRaw.toInt() * 100
                    2 -> fracRaw.toInt() * 10
                    else -> fracRaw.take(3).toInt()
                }
                lines += LrcLine((min * 60L + sec) * 1000L + ms, text)
            }
        }

        lines.sortBy { it.timeMs }
        return LrcFile(title, artist, album, offset, lines)
    }

    /**
     * 二分查找：给定当前播放位置 positionMs，返回应高亮的行索引。
     * 找不到（还没到第一句）返回 -1。
     */
    fun indexAt(lines: List<LrcLine>, positionMs: Long): Int {
        if (lines.isEmpty() || positionMs < lines[0].timeMs) return -1
        var lo = 0
        var hi = lines.size - 1
        while (lo < hi) {
            val mid = (lo + hi + 1) ushr 1
            if (lines[mid].timeMs <= positionMs) lo = mid else hi = mid - 1
        }
        return lo
    }
}
