package com.app.siy.helper;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Base64;

import com.app.siy.utils.AppUtils;
import com.app.siy.utils.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import id.zelory.compressor.Compressor;

import static android.content.ContentValues.TAG;
import static android.media.ThumbnailUtils.createVideoThumbnail;

/**
 * Created by Manish-Pc on 03/01/2018.
 */

public class FileHelper {

    Context context;
    private String TAG = "GALLERY";

    public FileHelper(Context context) {
        this.context = context;
    }


    /*public static String getThumbnailPathForLocalFile(Context context, Uri fileUri) {
        long fileId = getFileId(context, fileUri);
        if (fileId == 0) {
            return "";
        }
        MediaStore.Video.Thumbnails.getThumbnail(context.getContentResolver(),
                fileId,
                MediaStore.Video.Thumbnails.MICRO_KIND,
                null);

        Cursor thumbCursor = null;
        try {
            thumbCursor = context.getContentResolver().query(
                    MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI
                    , THUMB_COLUMNS
                    , MediaStore.Video.Thumbnails.VIDEO_ID + " = " + fileId
                    , null
                    , null);

            if (thumbCursor != null && thumbCursor.moveToFirst()) {
                return thumbCursor.getString(thumbCursor.getColumnIndex(MediaStore.Video.Thumbnails.DATA));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (thumbCursor != null) {
                thumbCursor.close();
            }
        }

        return "";
    }*/

    public boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file != null) {
            if (file.exists()) {
                file.delete();
                return true;
            } else {
                AppUtils.log(TAG, "Unable to Delete File");
                return false;
            }
        } else {
            AppUtils.log(TAG, "File not found");
            return false;
        }
    }


    public static String bitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    public static ArrayList<Bitmap> readVideosFromSpecifiedFolder(File folderName) {
        ArrayList<Bitmap> bitmapArrayVideo = new ArrayList<>();
        File allVideoFiles[] = folderName.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getAbsolutePath().endsWith("mp4");
            }
        });

        AppUtils.log("All Video Files - FileHelper : " + allVideoFiles);

        for (File singleVideoFile : allVideoFiles) {
            AppUtils.log("SingleVideoFile Ab: " + singleVideoFile.getAbsolutePath());
            AppUtils.log("SingleVideoFile Path : " + singleVideoFile.getPath());
            AppUtils.log("SingleVideoFile Name : " + singleVideoFile.getName());

            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(singleVideoFile.getPath(),
                    MediaStore.Images.Thumbnails.MINI_KIND);
            bitmapArrayVideo.add(thumb);
        }
        return bitmapArrayVideo;
    }

    public static ArrayList<Bitmap> readImagesFromSpecifiedFolder(File folderName) {
        ArrayList<Bitmap> arrayListBitmap = new ArrayList();
        File allImagesFiles[] = folderName.listFiles();
        // There are Some Images
        for (File singleFile : allImagesFiles) {
            // Get file path of a single file.
            String singleFilePath = singleFile.getAbsolutePath();
            // Convert this file to Bitmap.
            Bitmap singleImageBitmap = BitmapFactory.decodeFile(singleFilePath);
            arrayListBitmap.add(singleImageBitmap);
        }
        return arrayListBitmap;
    }


    public static Uri saveVideoToSpecifiedFolder(Context context) {
        File rootFile = Environment.getExternalStorageDirectory();
        File folderInsideRootFile = new File(rootFile, "SIY");
        File videoFolder = new File(folderInsideRootFile, "VIDEOS");
        if (!videoFolder.exists()) {
            videoFolder.mkdirs();
        }

        File videoFileName = new File(videoFolder, FileHelper.setVideoName());
        Uri videoFileUri = FileProvider.getUriForFile(context,
                context.getApplicationContext().getPackageName() + ".com.app.siy.provider", videoFileName);
        //Uri videoFileUri = FileUtils.getUri(videoFileName);   // will not work
        //Uri videoFileUri = Uri.fromFile(videoFileName);       // will not work.
        return videoFileUri;
    }

    public static void saveImageToSpecifiedFolder(Bitmap bitmap) {
        //Getting root directory name.
        File rootFile = Environment.getExternalStorageDirectory();
        File folderInsideRootFile = new File(rootFile, "SIY");
        File imageFolder = new File(folderInsideRootFile, "IMAGES");
        //Create Image Directory
        if (!imageFolder.exists()) {
            imageFolder.mkdirs();
        }

        // Image File name inside IMAGES directory.
        File imageFileName = new File(imageFolder, FileHelper.setImageName());
        if (imageFileName.exists()) {
            imageFileName.delete();
        }

        // Now Write the Bitmap image to the image file
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(imageFileName);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            AppUtils.log("Error while writing the image " + e.getMessage());
        }
    }


    public static boolean isSDCardPresent() {
        // check whether SD Card is present or Not ?
        boolean sdCardPresentOrNot = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        return sdCardPresentOrNot;
    }


    public static String setImageName() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DATE);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        //System.out.println();
        String imageFileName = "IMG_" + day + month + year + "_" + hour + minute + second + ".jpg";

        return imageFileName;
    }


    public static String setVideoName() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DATE);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        //System.out.println();
        String imageFileName = "VID_" + day + month + year + "_" + hour + minute + second + ".mp4";

        return imageFileName;
    }

    public Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "image__", "description");
        try {
            bytes.flush();
            bytes.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.parse(path);
    }


    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    public String getOriginalImagePath() {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = ((Activity) context).managedQuery(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null);
        int column_index_data = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToLast();
        return cursor.getString(column_index_data);
    }


    public String saveImageInInternalStorage(Uri imageUri, String folderName, String imageFileName) {
        ContextWrapper cw = new ContextWrapper(context);

        AppUtils.log("Image Uri " + imageUri);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir(folderName, Context.MODE_PRIVATE);
        AppUtils.log("Directory : " + directory);
        // Create imageDir
        File mypath = new File(directory, imageFileName);

        AppUtils.log("File path : " + mypath.getPath());
        FileOutputStream fos = null;
        try {
            AppUtils.log("0");

            File file = FileUtils.getFile(context, imageUri);
            AppUtils.log("1");
            fos = new FileOutputStream(mypath);
            AppUtils.log("2");
            Compressor compressor = new Compressor(context)
                    .setMaxWidth(200)
                    .setMaxHeight(200)
                    .setQuality(50);
            AppUtils.log("3");

            Bitmap bitmapImage = compressor.compressToBitmap(file);

            AppUtils.log("Bit map Hash Code : " + bitmapImage.hashCode());

            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            AppUtils.log("Exception Image File 1: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                AppUtils.log("Exception Image File 2 : " + e.getMessage());
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath() + "/" + imageFileName;
    }

    // Bitmap to ByteArray.
    public byte[] bitmapToByteArray(Bitmap imageBitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return byteArray;
    }


    // Uri to ByteArray.
    public byte[] uriToByteArray(Uri imageUri) {
        File file = FileUtils.getFile(context, imageUri);
        byte[] byteArray = null;
        try {
            Compressor compressor = new Compressor(context).setMaxWidth(200)
                    .setMaxHeight(200).setQuality(50);
            Bitmap bitmapImage = compressor.compressToBitmap(file);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
            byteArray = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArray;
    }


    // Compress Image from Uri
    public Bitmap uriToBitmap(Uri imageUri) {
        File file = FileUtils.getFile(context, imageUri);
        Bitmap bitmapImage = null;
        try {
            Compressor compressor = new Compressor(context).setMaxWidth(200)
                    .setMaxHeight(200).setQuality(50);
            bitmapImage = compressor.compressToBitmap(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmapImage;
    }


    // Save Image to Storage
    public void saveImageInByteArrayToStorage(byte[] compressedImageInByteArray, String imageFileNameWithExtension) {
        File photo = new File(Environment.getExternalStorageDirectory(), imageFileNameWithExtension);
        try {
            FileOutputStream fos = new FileOutputStream(photo.getPath());
            fos.write(compressedImageInByteArray);
            fos.close();
            AppUtils.log("File Saved");
        } catch (IOException e) {
            e.printStackTrace();
            AppUtils.log("File Not saved : " + e.getMessage());
        }
    }

    // --- end of HELPER METHODS -


    // Save Image to Storage
    public String saveBitmapImageToStorage(Bitmap bitmapImage, String imageFileNameWithExtension) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmapImage.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        File photo = new File(Environment.getExternalStorageDirectory(), imageFileNameWithExtension);
        try {
            FileOutputStream fos = new FileOutputStream(photo.getPath());
            fos.write(byteArray);
            fos.close();
            //AppUtils.log("File Saved");
        } catch (IOException e) {
            e.printStackTrace();
            //AppUtils.log("File Not saved : " + e.getMessage());
        }
        return photo.getAbsolutePath();
    }


    public static boolean deleteVideo(Uri videoUri) {
        File fdelete = new File(videoUri.getPath());
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                System.out.println("file Deleted :" + videoUri.getPath());
                return true;
            } else {
                System.out.println("file not Deleted :" + videoUri.getPath());
                return false;
            }
        }
        return false;
    }

    public static boolean deleteVideoFromFile(String videoPath) {
        File fdelete = new File(videoPath);
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                System.out.println("file Deleted :" + videoPath);
                return true;
            } else {
                System.out.println("file not Deleted :" + videoPath);
                return false;
            }
        }
        return false;
    }

    // Save Image to Storage
    public boolean deleteImageFromStorage(String imageFileNameWithExtension) {

        File photo = new File(Environment.getExternalStorageDirectory(), imageFileNameWithExtension);
        return photo.delete();
    }
}
