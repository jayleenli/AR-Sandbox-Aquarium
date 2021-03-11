package com.google.ar.sceneform;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

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
      // Delayed display of UI elements
//      ActionBar actionBar = getSupportActionBar();
//      if (actionBar != null) {
//        actionBar.show();
//      }
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
          startActivityForResult(cameraIntent, CAMERA_REQUEST);
        }
      }
    });
  }

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
      float degrees = 90; //rotation degree
      Matrix matrix = new Matrix();
      matrix.setRotate(degrees);
      Bitmap origBitMap = (Bitmap) data.getExtras().get("data");
      Bitmap photo = Bitmap.createBitmap(origBitMap, 0, 0, origBitMap.getWidth(), origBitMap.getHeight(), matrix, true);
      imageView.setImageBitmap(photo);
    }
  }

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
}