package com.weiaett.cruelalarm.img_proc

import android.app.IntentService
import android.content.Intent
import android.graphics.Bitmap
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import java.io.File


class ComparatorService(): IntentService("ComparatorService") {

    val broadcast: LocalBroadcastManager = LocalBroadcastManager.getInstance(this)

    fun setState(s:String): Unit{
        val i = Intent(BROADCAST_ACTION).putExtra(STATE, s)
        broadcast.sendBroadcast(i)
    }

    override fun onHandleIntent(intent: Intent) {
        Log.d("Match","Service started")

        //if(intent == null) throw Exception("Intent not passed to ComparatorService")
        val path1 = intent.getStringExtra(PATH_1)
        val path2 = intent.getStringExtra(PATH_2)
        val draw = intent.getBooleanExtra(DRAW, false)

        if(path2.equals(NO_PATH)){
            Log.d("Match","NoPath received")
            val i = Intent(BROADCAST_ACTION).putExtra(RESULT, "NoPath received")
            broadcast.sendBroadcast(i)
            return
        }

        setState("Loading images")
        val comparator = Comparator(path1, path2, {s->setState(s)})
        val match: Bitmap? = comparator.matchImages(draw)

        val i = Intent(BROADCAST_ACTION).putExtra(RESULT, comparator.result)

        if(draw) {
            val fileName = "matches"
            val file = File.createTempFile(fileName, null, cacheDir)
            val stream = file.outputStream()
            match?.compress(Bitmap.CompressFormat.PNG, 90/*not sure about quality*/, stream)
            stream.close()

            i.putExtra(MATCH_IMAGE, file.path)
        }
        broadcast.sendBroadcast(i)
        Log.d("Match","Response send")
    }
    companion object{
        val BROADCAST_ACTION = "com.weiaett.cruelalarm.ComparatorService.BROADCAST_ACTION"
        val STATE = "State"
        val RESULT = "Result"
        val MATCH_IMAGE = "MatchImage"
        val NO_PATH = "NoPath"

        val PATH_1 = "path1"
        val PATH_2 = "path2"
        val DRAW  = "drawMatches"
    }
}