package com.gandring.ang.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.gandring.ang.AppConfig.MSG_MEASURE_CONFIG
import com.gandring.ang.AppConfig.MSG_MEASURE_CONFIG_CANCEL
import com.gandring.ang.AppConfig.MSG_MEASURE_CONFIG_SUCCESS
import com.gandring.ang.util.MessageUtil
import com.gandring.ang.util.SpeedtestUtil
import com.gandring.ang.util.Utils
import go.Seq
import kotlinx.coroutines.*
import libv2ray.Libv2ray
import java.util.concurrent.Executors

class V2RayTestService : Service() {
    private val realTestScope by lazy { CoroutineScope(Executors.newFixedThreadPool(10).asCoroutineDispatcher()) }

    override fun onCreate() {
        super.onCreate()
        Seq.setContext(this)
        Libv2ray.initV2Env(Utils.userAssetPath(this))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.getIntExtra("key", 0)) {
            MSG_MEASURE_CONFIG -> {
                val contentPair = intent.getSerializableExtra("content") as Pair<*, *>
                realTestScope.launch {
                    val result = SpeedtestUtil.realPing(contentPair.second as String)
                    MessageUtil.sendMsg2UI(this@V2RayTestService, MSG_MEASURE_CONFIG_SUCCESS, Pair(contentPair.first, result))
                }
            }
            MSG_MEASURE_CONFIG_CANCEL -> {
                realTestScope.coroutineContext[Job]?.cancelChildren()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
