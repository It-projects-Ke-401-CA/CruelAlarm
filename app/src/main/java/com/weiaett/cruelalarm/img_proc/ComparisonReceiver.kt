package com.weiaett.cruelalarm.img_proc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import java.io.File
import java.io.FileInputStream


val RECEIVER = "MatchReceiver"

class ComparisonReceiver(val matchView: ImageView, val statusView: TextView) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(RECEIVER, "Response received")
        val result: String? = intent.getStringExtra(ComparatorService.RESULT)
        if (result != null){
            val pathname = intent.getStringExtra(ComparatorService.MATCH_IMAGE)
            Log.d(RECEIVER, pathname)

            val cached = File(pathname)
            try {
                val stream = FileInputStream(cached)
                val bitmap = BitmapFactory.decodeStream(stream)
                matchView.setImageBitmap(bitmap)
                statusView.text = result
            } catch (e: Exception) {
                Log.e("MatchMain", "IOException", e)
            }
        }else{
            val state = intent.getStringExtra(ComparatorService.STATE)
            statusView.text = state
        }
    }
}