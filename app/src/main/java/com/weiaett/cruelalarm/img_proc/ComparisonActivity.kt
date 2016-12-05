package com.weiaett.cruelalarm.img_proc


import android.content.Intent
import android.content.IntentFilter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.weiaett.cruelalarm.R

class ComparisonActivity : AppCompatActivity() {

    lateinit var matchView: ImageView
    lateinit var statusView: TextView
    lateinit var compReceiver: ComparisonReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comparison)

        matchView = findViewById(R.id.imageView2) as ImageView
        statusView = findViewById(R.id.statusView) as TextView

        val path1 = intent.getStringExtra(PATH_1)
        val path2 = intent.getStringExtra(PATH_2)
        Log.d(tag, "compare files $path1 $path2")

        val filter = IntentFilter(ComparatorService.BROADCAST_ACTION)
        compReceiver = ComparisonReceiver(matchView, statusView)
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(compReceiver, filter)

        val i = Intent(this, ComparatorService::class.java)
        i.putExtra(PATH_1, path1)
        i.putExtra(PATH_2, path2)
        startService(i)
    }

    override fun onDestroy(){
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(compReceiver)
        super.onDestroy()
    }

    companion object{
        val PATH_1 = "path1"
        val PATH_2 = "path2"
    }
}
