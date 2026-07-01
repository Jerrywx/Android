package com.example.appdemo.demo.storage

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar

/**
 * SQLite 原生演示 —— SQLiteOpenHelper + CRUD 全流程。
 *
 * 表结构：note (id INTEGER PK AUTOINCREMENT, title TEXT, content TEXT, ts INTEGER)
 *
 * 演示要点：
 *   1) onCreate / onUpgrade 建表升级
 *   2) insert / update / delete / query 四大操作
 *   3) ContentValues 参数化写入，防止 SQL 注入
 *   4) Cursor 遍历读取结果集，用完必须 close
 */
class SqliteDemoActivity : AppCompatActivity() {

    private data class Note(val id: Long, val title: String, val content: String, val ts: Long)

    private class NoteDbHelper(ctx: Context) : SQLiteOpenHelper(ctx, "demo_notes.db", null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE note (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    content TEXT NOT NULL,
                    ts INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS note")
            onCreate(db)
        }
    }

    private lateinit var helper: NoteDbHelper
    private lateinit var adapter: NoteAdapter
    private lateinit var log: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_storage_sqlite)
        setupDemoToolbar(R.string.storage_sqlite_title, R.id.storage_sqlite_root)

        helper = NoteDbHelper(this)
        log = findViewById(R.id.tv_sqlite_log)

        val list = findViewById<RecyclerView>(R.id.rv_sqlite)
        list.layoutManager = LinearLayoutManager(this)
        adapter = NoteAdapter(
            onDelete = { note ->
                val n = delete(note.id)
                appendLog("🗑️ 删除 id=${note.id}，影响 $n 行")
                refresh()
            },
            onEdit = { note ->
                val n = update(note.id, note.title + " (edited)", note.content)
                appendLog("✏️ 更新 id=${note.id}，影响 $n 行")
                refresh()
            }
        )
        list.adapter = adapter

        val titleInput = findViewById<EditText>(R.id.et_sqlite_title)
        val contentInput = findViewById<EditText>(R.id.et_sqlite_content)

        findViewById<TextView>(R.id.btn_sqlite_insert).setOnClickListener {
            val id = insert(
                titleInput.text.toString().ifBlank { "无标题" },
                contentInput.text.toString().ifBlank { "空内容" }
            )
            appendLog("➕ 插入成功，id=$id")
            titleInput.setText("")
            contentInput.setText("")
            refresh()
        }

        findViewById<TextView>(R.id.btn_sqlite_clear).setOnClickListener {
            val n = helper.writableDatabase.delete("note", null, null)
            appendLog("🗑️ 清空整表，影响 $n 行")
            refresh()
        }

        refresh()
    }

    // ─────── CRUD ───────

    private fun insert(title: String, content: String): Long {
        /// ContentValues + insert 会自动参数化，防止 SQL 注入
        val cv = ContentValues().apply {
            put("title", title)
            put("content", content)
            put("ts", System.currentTimeMillis())
        }
        return helper.writableDatabase.insert("note", null, cv)
    }

    private fun update(id: Long, title: String, content: String): Int {
        val cv = ContentValues().apply {
            put("title", title)
            put("content", content)
        }
        return helper.writableDatabase.update("note", cv, "id = ?", arrayOf(id.toString()))
    }

    private fun delete(id: Long): Int =
        helper.writableDatabase.delete("note", "id = ?", arrayOf(id.toString()))

    private fun queryAll(): List<Note> {
        val list = mutableListOf<Note>()
        val cursor = helper.readableDatabase.query(
            "note", arrayOf("id", "title", "content", "ts"),
            null, null, null, null, "id DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list += Note(
                    id = it.getLong(0),
                    title = it.getString(1),
                    content = it.getString(2),
                    ts = it.getLong(3),
                )
            }
        }
        return list
    }

    private fun refresh() {
        adapter.submit(queryAll())
    }

    private fun appendLog(msg: String) {
        log.text = if (log.text.isNullOrBlank() || log.text.startsWith("点击")) msg
        else "${log.text}\n$msg"
    }

    override fun onDestroy() {
        helper.close()
        super.onDestroy()
    }

    // ─────── Adapter ───────

    private class NoteAdapter(
        private val onDelete: (Note) -> Unit,
        private val onEdit: (Note) -> Unit,
    ) : RecyclerView.Adapter<NoteAdapter.VH>() {

        private val data = mutableListOf<Note>()

        fun submit(list: List<Note>) {
            data.clear()
            data.addAll(list)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_sqlite_note, parent, false)
            return VH(v)
        }
        override fun getItemCount() = data.size
        override fun onBindViewHolder(holder: VH, position: Int) =
            holder.bind(data[position], onDelete, onEdit)

        class VH(view: View) : RecyclerView.ViewHolder(view) {
            private val title: TextView = view.findViewById(R.id.tv_note_title)
            private val content: TextView = view.findViewById(R.id.tv_note_content)
            private val btnEdit: TextView = view.findViewById(R.id.btn_note_edit)
            private val btnDel: TextView = view.findViewById(R.id.btn_note_del)
            fun bind(note: Note, onDelete: (Note) -> Unit, onEdit: (Note) -> Unit) {
                title.text = "#${note.id}  ${note.title}"
                content.text = note.content
                btnDel.setOnClickListener { onDelete(note) }
                btnEdit.setOnClickListener { onEdit(note) }
            }
        }
    }
}
