package com.weiaett.cruelalarm.img_proc

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.features2d.*
import org.opencv.imgproc.Imgproc
import java.io.File
import java.util.*

const val MATCH = "Match"
//const val MATCH_AREA = 150.0
const val MAX_WIDTH = 800.0

val O = 0.0
val A = 128.0
val B = 192.0
val C = 255.0
val Colors = arrayOf(
        Scalar(O, O, O),
        Scalar(O, O, A),
        Scalar(O, A, O),
        Scalar(A, O, O),
        Scalar(O, A, A),
        Scalar(A, O, A),
        Scalar(A, A, O),
        Scalar(O, O, B),
        Scalar(O, B, O),
        Scalar(B, O, O),
        Scalar(O, A, B),
        Scalar(A, O, B),
        Scalar(O, B, A),
        Scalar(A, B, O),
        Scalar(B, O, A),
        Scalar(B, A, O),
        Scalar(O, B, B),
        Scalar(B, O, B),
        Scalar(B, B, O),
        Scalar(O, O, C),
        Scalar(O, C, O),
        Scalar(C, O, O),
        Scalar(O, C, C),
        Scalar(C, O, C),
        Scalar(C, C, O)
)

private fun detectWithMSER(img: Mat, kPoints: MatOfKeyPoint, mser: FeatureDetector) {
    val grayScale = img.clone()
    Imgproc.cvtColor(img, grayScale, Imgproc.COLOR_BGR2GRAY)
    mser.detect(grayScale, kPoints)
}

fun loadImage(/*ctx: Context,*/path: String): Mat{
//    val o = BitmapFactory.Options()
//    // turn off "automatic screen density scaling", which turns n-channeled image
//    // to one-channeled n times larger
//    o.inScaled = false

    val bmp = BitmapFactory.decodeFile(path)
    // Log.d(MATCH, String.format("w=%d h=%d", bmp.width, bmp.height))
    return loadToMat(bmp)
}

fun loadToMat(bmp: Bitmap): Mat{
    // Height, then width, because measure units are rows/columns
    // You can specify CV_8UC3, it will be CV_8UC4 all the same
    val mat = Mat(bmp.height, bmp.width, CvType.CV_8UC4)
    Log.d("Match","width=${bmp.width} height=${bmp.height}")
    Utils.bitmapToMat(bmp, mat)

    // Convert 4-channeled to 3-channeled
    val matC3 = Mat()
    Imgproc.cvtColor(mat, matC3, Imgproc.COLOR_BGRA2BGR)
    //val inv = Mat()
    //Core.bitwise_not(matC3, inv)
    return matC3
}

fun rotateMat(matImage: Mat):Mat{
    Core.transpose(matImage, matImage)
    Core.flip(matImage, matImage,1) //transpose+flip(1)=CW
    return matImage
}

fun transToBitmap(dst: Mat): Bitmap{
    val bm = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.RGB_565)
    Utils.matToBitmap(dst, bm)
    return bm
}

class Comparator(path1:String, path2: String, val setState:(s:String)->Unit){

    val img1 = loadImage(path1)
    val img2 = loadImage(path2)
    var kPoints1 = MatOfKeyPoint()
    var kPoints2 = MatOfKeyPoint()
    lateinit var keypoints1: Array<KeyPoint>
    lateinit var keypoints2: Array<KeyPoint>

    var shiftX = 0.0
    var shiftY = 0.0

    lateinit var result: String

    init {
        File(path2).delete()
    }

    private fun clamp(v: Int, min: Int, max: Int) = if(v < min) min else if(v > max) max else v

    private fun colorDist(color1: DoubleArray?, color2: DoubleArray?):Double{
        val dR = (color1?.get(0) ?: 0.0) - (color2?.get(0) ?: 0.0)
        val dG = (color1?.get(1) ?: 0.0) - (color2?.get(1) ?: 0.0)
        val dB = (color1?.get(2) ?: 0.0) - (color2?.get(2) ?: 0.0)
        return Math.sqrt(dR*dR+dG*dG+dB*dB)
    }

    fun comparePixels(shiftX: Int, shiftY: Int): Long{
        var matched = 0L
        for(i in 0 until img1.width()){
            for(j in 0 until img1.height()){
                val i2 = i+shiftX
                val j2 = j+shiftY
                if(i2 >= 0 && i2 < img2.width() && j2 >= 0 && j2 < img2.height()
                && colorDist(img1.get(i,j), img2.get(i2, j2)) < 100){
                    matched++
                }
            }
        }
        return matched
//        val mat1 = MatOfPoint3f(img1)
//        val mat2 = MatOfPoint3f(img2)
//        val dist = MatOfPoint3f()
//        Core.subtract(mat1, mat2, dist)
//        val mult = MatOfPoint3f()
//        Core.multiply(dist, dist.clone(), mult)
//        val res = MatOfPoint3f()
//        Core.sqrt(mult, res)
    }

    private fun drawMatches(matches: ArrayList<DMatch>): Bitmap {
        val dst = Mat(img1.rows().toInt(), img1.cols().toInt()*2, img1.type())
        Core.hconcat(listOf(img1, img2), dst)

        var colorCount = 0
        for(match in matches){
            val from = keypoints1[match.queryIdx].pt
            val to = keypoints2[match.trainIdx].pt.clone()
            to.x += img1.width()
            Core.line(dst, from, to, Colors[colorCount], 2)
            colorCount = (colorCount+1)% Colors.size
        }


        val bm = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.RGB_565)
        Utils.matToBitmap(dst, bm)
        return bm
    }

    private inline fun filterMatches(matches: Array<Array<DMatch>>, filter:(DMatch)->Boolean) =
            Array(matches.size, {i->matches[i].filter(filter).toTypedArray()})

    private fun filterSymmetric(mat: Array<Array<DMatch>>, rev: Array<Array<DMatch>>):Array<Array<DMatch>>{
        val res = Array(mat.size, { arrayListOf<DMatch>()})
        for(i in 0 until mat.size) for(match in mat[i]){
            for(j in 0 until rev.size) for(reverse in rev[j]){
                if(match.queryIdx == reverse.trainIdx && reverse.queryIdx == match.trainIdx){
                    res[i].add(match)
                }
            }
        }
        return Array(mat.size, {i->res[i].toTypedArray()})
    }

    private fun getArr(img: Mat, p: Point) = img.get(
            clamp(p.x.toInt(), 0, img.width()),
            clamp(p.y.toInt(), 0, img.height())
    )

    fun matchImages(draw: Boolean): Bitmap? {
        setState("Applying ORB/MSER")
        val detector = FeatureDetector.create(FeatureDetector.MSER)
        detectWithMSER(img1, kPoints1, detector)
        detectWithMSER(img2, kPoints2, detector)
        Log.d(MATCH, "Keypoint num=${kPoints1.total()} ${kPoints2.total()}")
        keypoints1 = kPoints1.toArray()
        keypoints2 = kPoints2.toArray()

        val minSize = Math.min(keypoints1.size, keypoints2.size)
        keypoints1 = keypoints1.sliceArray(0 until minSize)
        keypoints2 = keypoints2.sliceArray(0 until minSize)
        kPoints1.fromArray(*keypoints1)
        kPoints2.fromArray(*keypoints2)
        Log.d(MATCH, "Equal keypoint num=${kPoints1.total()} ${kPoints2.total()}")

        // image descriptors
        val d1 = Mat()
        val d2 = Mat()
        val extractor = DescriptorExtractor.create(DescriptorExtractor.ORB)
        extractor.compute(img1, kPoints1, d1)
        extractor.compute(img2, kPoints2, d2)

        val matches = ArrayList<MatOfDMatch>()
        val matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT)
        matcher.knnMatch(d1, d2, matches, 14)
        val reverse = ArrayList<MatOfDMatch>()
        matcher.knnMatch(d2, d1, reverse, 14)

        setState("Detecting good matches")
        val goodMatches = selectGoodMatches(matches, reverse)

        setState("Comparing pixels")
        val pxNum = comparePixels(shiftX.toInt(), shiftY.toInt())
        Log.d(MATCH, "matched pixel num=$pxNum")

        if(goodMatches.size < 10){
            result = "Not enough good matches: ${goodMatches.size}"
        }else if(pxNum < 450000){
            result = "Not enough matched pixels: $pxNum / 450000"
        }else{
            result = "Images matched!"
        }

        setState("Drawing matches")
        return if(draw) drawMatches(goodMatches) else null
    }

    private fun selectGoodMatches(mat: ArrayList<MatOfDMatch>, rev: ArrayList<MatOfDMatch>): ArrayList<DMatch> {
        val allMatches = Array(mat.size, {i->mat[i].toArray()})
        Log.d(MATCH, "Matches options=${allMatches.size} pairs=${allMatches[0].size}")

        val allReverse = Array(rev.size, {i->rev[i].toArray()})
        var matches = filterSymmetric(allMatches, allReverse)
        matches = filterMatches(matches, {match->colorDist(
                getArr(img1, keypoints1[match.queryIdx].pt),
                getArr(img2, keypoints2[match.trainIdx].pt)
        ) < 90})

        Log.d(MATCH, matches.map { row->row.size}.joinToString(","))
        //Log.d(MATCH, "Matches options=${allMatches.size} pairs=${allMatches[0].size}")

        shiftX = 0.0; shiftY = 0.0
        val T = arrayOf(400.0, 350.0, 300.0, 250.0, 200.0, 200.0, 150.0, 150.0, 100.0, 80.0)

        for(i in 0..8){
            var counter = 0
            var sumX = 0.0
            var sumY = 0.0
            traverseMatches(matches, shiftX, shiftY, T[i], {curr, dx, dy, next->
                if((next?.distance ?: curr.distance) - curr.distance > 2){
                    counter++
                    sumX += dx
                    sumY += dy
                }
            })
            shiftX += sumX / counter
            shiftY += sumY / counter
            Log.d(MATCH, "shiftX=$shiftX shiftY=$shiftY pairNum=$counter")
        }

        val good = ArrayList<DMatch>()
        traverseMatches(matches, shiftX, shiftY, 100.0, {curr, dx, dy, next->
            if((next?.distance ?: curr.distance) - curr.distance > 0) {
                good.add(curr)
            }
        })
        Log.d(MATCH, good.map { match->keypoints1[match.queryIdx].size / keypoints2[match.trainIdx].size}
                .joinToString(","))
        Log.d(MATCH, "pairNumber=${good.size}")

        //val res = MatOfDMatch(*good.toTypedArray())
        return good
    }

    private fun traverseMatches(matches: Array<Array<DMatch>>, shiftX: Double, shiftY: Double, thres: Double,
                                callback:(DMatch, Double, Double, DMatch?)->Unit){
        for (i in 0 until matches.size) {
            for (j in 0 until matches[i].size) {
                val match = matches[i][j]
                val from = keypoints1[match.queryIdx].pt
                val to = keypoints2[match.trainIdx].pt

                val dx = from.x - to.x - shiftX
                val dy = from.y - to.y - shiftY
                if (Math.hypot(dx, dy) < thres) {
                    callback(matches[i][j], dx, dy, if (j + 1 < matches[i].size) matches[i][j + 1] else null)
                    break // search best pair for the next keypoint
                }
            }
        }
    }

    companion object{
        fun hasEnoughFeatures(bmp: Bitmap):Boolean{
            val img = loadToMat(bmp)
            val kPoints =  MatOfKeyPoint()

            val detector = FeatureDetector.create(FeatureDetector.MSER)
            detectWithMSER(img, kPoints, detector)
            Log.d(tag, "keypoints number = ${kPoints.total()}")
            return kPoints.total() > 100
        }
    }
}
