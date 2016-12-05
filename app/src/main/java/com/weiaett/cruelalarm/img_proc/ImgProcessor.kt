package com.weiaett.cruelalarm.img_proc

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.weiaett.cruelalarm.utils.ImageLoader
import java.io.File
import java.io.FileInputStream

val tag = "MaImageProcessor"

class ImgProcessor{

    companion object{
        fun process(ctx: Context, fileUri: Uri, turn: Boolean):Uri?{

            Log.d(tag, "Compressing started")
            val bmp = compress(fileUri.path, turn)
            val delSuccess = File(fileUri.path).delete()
            Log.d(tag, "Compressing finished, deleted=$delSuccess")

            val enough = Comparator.Companion.hasEnoughFeatures(bmp)
            if(enough) {
                Log.d(tag, "Save started")
                val uri = ImageLoader.savePhoto(ctx, bmp)
                Log.d(tag, "Save finished")
                return uri
            }
            return null
        }

        fun compress(path: String, turn: Boolean): Bitmap{

            val options = BitmapFactory.Options()
            // get size
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(path, options)

            options.inJustDecodeBounds = false
            // really will be closest power of 2
            options.inSampleSize = Math.max(options.outWidth / 800.0, options.outHeight / 1000.0).toInt()
            val bitmap = BitmapFactory.decodeFile(path, options)

            Log.d(tag, "after scale w=${bitmap.width} h=${bitmap.height}")
            return if(turn)
                transToBitmap(rotateMat(loadToMat(bitmap)))
            else bitmap
        }
    }
}
