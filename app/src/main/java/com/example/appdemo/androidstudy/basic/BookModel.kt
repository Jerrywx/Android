package com.example.appdemo.androidstudy.basic

/**
 * books.json 最外层结构
 * { "code": 200, "message": "ok!", "result": [ BookItem, BookItem, ... ] }
 */
data class BookListResponse(
    val code: Int = 0,
    val message: String = "",
    val result: List<BookItem> = emptyList()
)

/**
 * 数组中的每一项，还有一层包装
 * { "code": 200, "message": "ok!", "result": { ...书籍字段... } }
 */
data class BookItem(
    val code: Int = 0,
    val message: String = "",
    val result: BookModel? = null
)

/**
 * 书籍模型，对应 books.json 中最内层 result 对象的字段
 */
data class BookModel(
    /// 作者相关
    val authorId: Long = 0,
    val authorImg: String = "",
    val authorName: String = "",
    val authorization: Int = 0,

    /// 书籍基础信息
    val bookId: Long = 0,
    val bookType: Int = 0,
    val name: String = "",
    val originalName: String = "",
    val picUrl: String = "",
    val description: String = "",
    val keywords: String = "",
    val wishWord: String = "",
    val recommendMsg: String = "",

    /// 分类
    val cateSite: Int = 0,
    val categoryId: Long = 0,
    val categoryName: String = "",
    val categoryPid: Long = 0,

    /// 章节相关
    val chapterCount: Int = 0,
    val firstChapterIdNoIntroduction: Long = 0,
    val firstReaderChapterId: Long = 0,
    val latestChapterId: Long = 0,
    val latestChapterName: String = "",
    val latestChapterUpateDateStr: String = "",
    val latestChapterUpdateTime: Long? = null,
    val readRecordChapterId: Long = 0,
    val progress: Int = 0,

    /// 版权信息
    val copyright: String = "",
    val copyrightNotice: String = "",

    /// 阅读/展示相关
    val coverIconDesc: String = "",
    val scoreLevelImg: String = "",
    val fansScoreLevel: Int = 0,
    val marvellous: Int = 0,
    val highlightAuthorName: String = "",
    val highlightBookName: String = "",
    val highlightDescription: String = "",

    /// 状态/标记位
    val bmFlag: Int = 0,
    val female: Int = 0,
    val hasPlane: Int = 0,
    val hasSideStory: Int = 1,
    val sideStoryTxt: String = "",
    val isAutoBuyChapter: Int = 0,
    val isCH: Int = 0,
    val serialStatus: Int = 0,
    val memberType: Int = 0,
    val site: Int = 0,
    val type: Int = 0,
    val zhBook: Int = 0,

    /// 跳转
    val jmpDesc: String = "",
    val jmpUrl: String = "",

    /// 其他
    val chUniqueCharId: String = "",
    val uniqCharId: String = "",
    val sourceType: String = "",
    val freeDayEndTimeStamp: Long = 0,
    val totalWord: Long = 0,
    val updateTime: Long = 0
)
