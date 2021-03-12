package com.google.ar.sceneform;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

//import com.google.ar.sceneform.samples.hellosceneform.ArSandboxAquariumActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraMainActivity extends AppCompatActivity {
  private static final int CAMERA_REQUEST = 1888;
  private ImageView imageView;
  private static final int MY_CAMERA_PERMISSION_CODE = 100;
  /**
   * Whether or not the system UI should be auto-hidden after
   * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
   */
  private static final boolean AUTO_HIDE = true;

  /**
   * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
   * user interaction before hiding the system UI.
   */
  private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

  /**
   * Some older devices needs a small delay between UI widget updates
   * and a change of the status and navigation bar.
   */
  private static final int UI_ANIMATION_DELAY = 300;
  private final Handler mHideHandler = new Handler();
  private View mContentView;
  private String pictureFilePath;
  private String currentPhotoPath;
  private final Runnable mHidePart2Runnable = new Runnable() {
    @SuppressLint("InlinedApi")
    @Override
    public void run() {
      // Delayed removal of status and navigation bar

      // Note that some of these constants are new as of API 16 (Jelly Bean)
      // and API 19 (KitKat). It is safe to use them, as they are inlined
      // at compile-time and do nothing on earlier devices.
      mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
              | View.SYSTEM_UI_FLAG_FULLSCREEN
              | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
              | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
              | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
              | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
  };
  private View mControlsView;
  private final Runnable mShowPart2Runnable = new Runnable() {
    @Override
    public void run() {
      mControlsView.setVisibility(View.VISIBLE);
    }
  };
  private boolean mVisible;
  private final Runnable mHideRunnable = new Runnable() {
    @Override
    public void run() {
      hide();
    }
  };
  /**
   * Touch listener to use for in-layout UI controls to delay hiding the
   * system UI. This is to prevent the jarring behavior of controls going away
   * while interacting with activity UI.
   */
  private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
      switch (motionEvent.getAction()) {
        case MotionEvent.ACTION_DOWN:
          if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS);
          }
          break;
        case MotionEvent.ACTION_UP:
          view.performClick();
          break;
        default:
          break;
      }
      return false;
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    mVisible = false;
    mContentView= getWindow().getDecorView();


    super.onCreate(savedInstanceState);
    setContentView(com.google.ar.sceneform.samples.hellosceneform.R.layout.camera_main);
    this.imageView = (ImageView)this.findViewById(com.google.ar.sceneform.samples.hellosceneform.R.id.objectImageView);
    ImageButton photoButton = (ImageButton) this.findViewById(com.google.ar.sceneform.samples.hellosceneform.R.id.take_photo);
    photoButton.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
          requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
        }
        else
        {
           Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//          Log.i("FILE", " IN HERE");
//
//          File photoFile = null;
//          try {
//            Log.i("FILE", " IN HERE2");
//            photoFile = createImageFile();
//            //Intent i = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFile);
//
//            //galleryAddPic(photoFile, cameraIntent);

            startActivityForResult(cameraIntent, CAMERA_REQUEST);
//          } catch (IOException ex) {
//            // Error occurred while creating the File
//            Log.i("ERR", " couldnt make file");
//          }




          //startActivityForResult(cameraIntent, CAMERA_REQUEST);
        }
      }
    });

    ImageButton deleteButton = (ImageButton) findViewById(com.google.ar.sceneform.samples.hellosceneform.R.id.delete_button);
    TextView cameraImageText = (TextView) this.findViewById(com.google.ar.sceneform.samples.hellosceneform.R.id.cameraImageText);
    ImageView objectImageView = (ImageView) findViewById(com.google.ar.sceneform.samples.hellosceneform.R.id.objectImageView);
    Drawable blankImage = getResources().getDrawable(com.google.ar.sceneform.samples.hellosceneform.R.drawable.blankimg, getTheme());
    deleteButton.setOnClickListener(
            new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                cameraImageText.setVisibility(View.VISIBLE);
                objectImageView.setImageDrawable(blankImage);
              }
            });

    ImageButton backButton = (ImageButton) findViewById(com.google.ar.sceneform.samples.hellosceneform.R.id.back_to_home_button);
//    backButton.setOnClickListener(
//            new View.OnClickListener() {
//              @Override
//              public void onClick(View v) {
//                openArSandboxAquariumActivity();
//              }
//            });


  }

//  //Open Main Activity
//  public void openArSandboxAquariumActivity() {
//    Intent intent = new Intent(this, ArSandboxAquariumActivity.class);
//    startActivity(intent);
//  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    // Trigger the initial hide() shortly after the activity has been
    // created, to briefly hint to the user that UI controls
    // are available.
    delayedHide(100);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
  {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == MY_CAMERA_PERMISSION_CODE)
    {
      if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
      {
        Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        startActivityForResult(cameraIntent, CAMERA_REQUEST);
      }
      else
      {
        Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
      }
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK)
    {

      //Remove the text
      TextView cameraImageText = (TextView) this.findViewById(com.google.ar.sceneform.samples.hellosceneform.R.id.cameraImageText);
      cameraImageText.setVisibility(View.GONE);

      float degrees = 90; //rotation degree
      Matrix matrix = new Matrix();
      matrix.setRotate(degrees);
      Bitmap origBitMap = (Bitmap) data.getExtras().get("data");
      Bitmap photo = Bitmap.createBitmap(origBitMap, 0, 0, origBitMap.getWidth(), origBitMap.getHeight(), matrix, true);
      imageView.setImageBitmap(photo);

      //SAVES THE BIT MAP!!! HELLA BLURRY

      MediaStore.Images.Media.insertImage(getContentResolver(), photo, "guuk" , "wtf");
      Log.i("SAVED IDK ", " HUHUHUHUHUHU");

    }
  }

//  private void dispatchTakePictureIntent() {
//    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//    // Ensure that there's a camera activity to handle the intent
//    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//      // Create the File where the photo should go
//      File photoFile = null;
//      try {
//        photoFile = createImageFile();
//        //galleryAddPic();
//        Log.i("FILE", " HUHUHUHUHUHU");
//      } catch (IOException ex) {
//        // Error occurred while creating the File
//      }
//      // Continue only if the File was successfully created
////      if (photoFile != null) {
////        Uri photoURI = FileProvider.getUriForFile(this,
////                "com.example.android.fileprovider",
////                photoFile);
////        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
////        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
////      }
//    }
//  }

  private void hide() {
    // Hide UI first
    mVisible = false;

    // Schedule a runnable to remove the status and navigation bar after a delay
    mHideHandler.removeCallbacks(mShowPart2Runnable);
    mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
  }

  /**
   * Schedules a call to hide() in delay milliseconds, canceling any
   * previously scheduled calls.
   */
  private void delayedHide(int delayMillis) {
    mHideHandler.removeCallbacks(mHideRunnable);
    mHideHandler.postDelayed(mHideRunnable, delayMillis);
  }

  //Save a bitmap
  private File getBitMapFile(String name){
    PackageManager m = getPackageManager();
    String projectDir = getPackageName();
    try {
      PackageInfo p = m.getPackageInfo(projectDir, 0);
      projectDir = getApplicationContext().getFilesDir().getAbsolutePath();
      File mediaStorageDir = new File(projectDir);

      // Create the storage directory if it does not exist
      if (! mediaStorageDir.exists()){
        if (! mediaStorageDir.mkdirs()){
          return null;
        }
      }
      // Create a media file name
      File mediaFile;
      String mImageName=name +".jpg";
      mediaFile = new File(getAssets() + File.separator + mImageName);


      return mediaFile;


    } catch (PackageManager.NameNotFoundException e) {
      Log.w("yourtag", "Error Package name not found ", e);
    }
    return null;

    // To be safe, you should check that the SDCard is mounted
    // using Environment.getExternalStorageState() before doing this.


//            + "/Android/data/"
//            + getApplicationContext().getPackageName()
//            + "/Files");

    // This location works best if you want the created images to be shared
    // between applications and persist after your app has been uninstalled.
  }

//  private void save() {
//    try { getPictureFile(); }
//    catch(IOException e) {}
//    Intent galleryIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//    File f = new File(pictureFilePath);
//    Uri picUri = Uri.fromFile(f);
//    galleryIntent.setData(picUri);
//    this.sendBroadcast(galleryIntent);
//  }
//
//  private File getPictureFile() throws IOException {
//    String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
//    String pictureFile = "ZOFTINO_" + timeStamp;
//    File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//    File image = File.createTempFile(pictureFile,  ".jpg", storageDir);
//    pictureFilePath = image.getAbsolutePath();
//    return image;
//  }
//
////  private File createImageFile() throws IOException {
////    // Create an image file name
////    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
////    String imageFileName = "JPEG_" + timeStamp + "_";
////    File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
////    File image = File.createTempFile(
////            imageFileName,  /* prefix */
////            ".jpg",         /* suffix */
////            storageDir      /* directory */
////    );
////
////    // Save a file: path for use with ACTION_VIEW intents
////    currentPhotoPath = image.getAbsolutePath();
////    return image;
////  }
//public static File createImageFile() throws IOException {
//  // Create an image file name
//  String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//  File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Camera/");
//  //File storageDir = Environment.getExternalStorageDirectory();
//
//  if (!storageDir.exists())
//    storageDir.mkdirs();
//  File image = File.createTempFile(
//          timeStamp,                   /* prefix */
//          ".jpeg",                     /* suffix */
//          storageDir                   /* directory */
//  );
//  return image;
//}
//  private void galleryAddPic(File image, Intent intent) {
//    Intent mediaScanIntent =  intent;//// new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//    File f = new File(image.getPath());
//    Log.i("FILE", "current photopath" + image.getPath());
//    Uri contentUri = Uri.fromFile(f);
//    mediaScanIntent.setData(contentUri);
//    this.sendBroadcast(mediaScanIntent);
//  }


}