package com.weiaett.cruelalarm.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ImageLoader {

    public static Uri savePhoto(Context context, Bitmap bitmap) {
        String path = Environment.getExternalStorageDirectory().getPath() + "/Weiaett/alarm/";
//        String path = context.getFilesDir().getAbsolutePath() + File.separator + "photos";
        File dir = new File(path);
        if (!dir.exists()) {
            try{
                dir.mkdirs();
            }
            catch(SecurityException se){
                se.printStackTrace();
                Toast.makeText(context, "Dir creation error", Toast.LENGTH_LONG).show();
            }
        }

        final File file = new File(path + ".nomedia");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "File creation error", Toast.LENGTH_LONG).show();
            }
        }

        OutputStream outputStream = null;
        String imageName = String.format(Locale.US, "%d.png", System.currentTimeMillis()) ;
        File image = new File(path, imageName);
        try {
            outputStream = new FileOutputStream(image);
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                Toast.makeText(context, "Photo compressing error", Toast.LENGTH_LONG).show();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return Uri.fromFile(image);
    }

    public static Uri prepareUri(Context context) {
        return Uri.fromFile(prepareFile(context));
    }

    public static File prepareFile(Context context) {
        String path = Environment.getExternalStorageDirectory().getPath() + "/Weiaett/alarm/";
//        String path = context.getFilesDir().getAbsolutePath() + File.separator + "photos";
        File dir = new File(path);
        if (!dir.exists()) {
            try{
                dir.mkdirs();
            }
            catch(SecurityException se){
                se.printStackTrace();
                Toast.makeText(context, "Dir creation error", Toast.LENGTH_LONG).show();
            }
        }

        final File file = new File(path + ".nomedia");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "File creation error", Toast.LENGTH_LONG).show();
            }
        }

        String imageName = String.format(Locale.US, "%d.jpg", System.currentTimeMillis()) ;
        File image = new File(path, imageName);
        return image;
    }

    public static List<File> getImages(Context context) {
//        String path = context.getFilesDir().getAbsolutePath() + File.separator + "photos";
        String path = Environment.getExternalStorageDirectory().getPath() + "/Weiaett/alarm/";
        File dir = new File(path);
        if (!dir.exists()) {
            try{
                dir.mkdirs();
            }
            catch(SecurityException se){
                se.printStackTrace();
                Toast.makeText(context, "Dir creation error", Toast.LENGTH_LONG).show();
            }
        }
        File[] files = dir.listFiles();
        List<File> filesList = new ArrayList<>();
        for (File file: files) {
            if (new ImageFileFilter().accept(file)) {
                filesList.add(file);
            }
        }
        return filesList;
    }

    public static List<String> getImagesPaths(Context context) {
//        String path = context.getFilesDir().getAbsolutePath() + File.separator + "photos";
        String path = Environment.getExternalStorageDirectory().getPath() + "/Weiaett/alarm/";
        File dir = new File(path);
        if (!dir.exists()) {
            try{
                dir.mkdirs();
            }
            catch(SecurityException se){
                se.printStackTrace();
                Toast.makeText(context, "Dir creation error", Toast.LENGTH_LONG).show();
            }
        }
        File[] files = dir.listFiles();
        List<String> filesList = new ArrayList<>();
        for (File file: files) {
            if (new ImageFileFilter().accept(file)) {
                filesList.add(file.getAbsolutePath());
            }
        }
        return filesList;
    }
}
