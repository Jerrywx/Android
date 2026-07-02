package com.example.appdemo.demo.jetpack

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar

/**
 * ViewModel + LiveData 演示 ——
 *
 * 要点：
 *   1) ViewModel 生命周期比 Activity 更长，配置变更（横竖屏）不销毁
 *   2) LiveData 具备生命周期感知，UI 不活跃时不会收到回调
 *   3) 通过 viewModels() 属性委托，同一个 Activity 拿到同一个 VM 实例
 *
 * 验证方式：点几下加号后横竖屏切换 —— 计数不会丢失。
 */
class ViewModelLiveDataDemoActivity : AppCompatActivity() {

    class CounterViewModel : ViewModel() {
        val count = MutableLiveData(0)
        val history = MutableLiveData<List<Int>>(emptyList())

        fun inc() = update(1)
        fun dec() = update(-1)
        fun reset() {
            count.value = 0
            history.value = emptyList()
        }

        private fun update(delta: Int) {
            val next = (count.value ?: 0) + delta
            count.value = next
            history.value = (history.value ?: emptyList()) + next
        }
    }

    /// 通过属性委托绑定 VM，作用域与 Activity 一致
    private val vm: CounterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_jetpack_vm)
        setupDemoToolbar(R.string.jetpack_vm_title, R.id.jetpack_vm_root)

        val counter = findViewById<TextView>(R.id.tv_vm_count)
        val history = findViewById<TextView>(R.id.tv_vm_history)

        /// observe：在 STARTED / RESUMED 时才回调，避免内存泄漏
        vm.count.observe(this) { counter.text = it.toString() }
        vm.history.observe(this) { list ->
            history.text = if (list.isEmpty()) "—" else list.joinToString(" → ")
        }

        findViewById<TextView>(R.id.btn_vm_inc).setOnClickListener { vm.inc() }
        findViewById<TextView>(R.id.btn_vm_dec).setOnClickListener { vm.dec() }
        findViewById<TextView>(R.id.btn_vm_reset).setOnClickListener { vm.reset() }
    }
}
