package com.lionel.stickynote;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lionel.stickynote.customview.Paper;
import com.lionel.stickynote.fieldclass.PaperProperty;
import com.lionel.stickynote.sqliteopenhelper.PaperContentDbHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import static com.lionel.stickynote.PaperContentActivity.DB_NAME;


public class MainActivity extends AppCompatActivity implements Paper.DeletePaperInterface, Paper.OpenPaperContent {
    private static final int REQUEST_CODE_FOR_OPEN_CONTENT = 100;
    private static final int REQUEST_CODE_FOR_PICK_PHOTO_ = 200;
    private static final int REQUEST_CODE_FOR_CROP_PHOTO = 300;
    private static final int REQUEST_PERMISSION_FOR_WRITE_EXTERNAL_STORAGE = 999;

    private ConstraintLayout mRootView;
    private ArrayList<PaperProperty> mPaperPropertyArrayList; // store Paper object
    private int mChildViewCount, mPaperId;
    private boolean isReenter;
    public static double iDeleteRegionX, iDeleteRegionY;
    private ImageView mImgViewTrashCan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_main);

        mRootView = findViewById(R.id.mainRootView);
        mChildViewCount = mRootView.getChildCount();
        mPaperPropertyArrayList = new ArrayList<>();

        setupDeleteRegion();
        setupTransition();
        showIntroPages();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // only use setupDeskTop() in onStart() in the following situations: first time to startup app or the paper was disappeared
        if (mChildViewCount == 2 || mRootView.getChildCount() != mChildViewCount) {
            setupDeskTop();
            mChildViewCount = mRootView.getChildCount();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // save Paper Property to sharedPreferences
        SharedPreferences.Editor sharedPreferences =
                getSharedPreferences("PaperPropertyData", MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = gson.toJson(mPaperPropertyArrayList);
        sharedPreferences.putString("PaperProperty", json);

        // to save Paper Id is how much to add now
        sharedPreferences.putInt("PaperId", mPaperId);
        sharedPreferences.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItemBackground:
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_PERMISSION_FOR_WRITE_EXTERNAL_STORAGE);
                } else {
                    pickPhoto();
                }
                break;
            case R.id.menuItemResetBackground:
                mRootView.setBackgroundColor(Color.parseColor("#ffffff"));
                SharedPreferences.Editor spBackgroundPic = getSharedPreferences("BackgroundData", MODE_PRIVATE).edit();
                spBackgroundPic.remove("PicUri");
                spBackgroundPic.apply();
                break;
            case R.id.menuItemIntroPage:
                startActivity(new Intent(this, IntroPageActivity.class));
                break;
            case R.id.menuItemAbout:

                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_FOR_PICK_PHOTO_:
                if (data != null) cropPhoto(data.getData());
                break;
            case REQUEST_CODE_FOR_CROP_PHOTO:
                if (data != null) {
                    Uri cropUri = data.getData();
                    if (cropUri != null) {
                        Intent it = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, cropUri);
                        sendBroadcast(it);
                        try {
                            Bitmap cropBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(cropUri), null, null);
                            mRootView.setBackground(new BitmapDrawable(getResources(), cropBitmap));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Can not find file", Toast.LENGTH_LONG).show();
                        }
                        // to save background picture which user chose
                        SharedPreferences.Editor spBackgroundPic = getSharedPreferences("BackgroundData", MODE_PRIVATE).edit();
                        spBackgroundPic.putString("PicUri", cropUri.toString());
                        spBackgroundPic.apply();
                    }
                }
                break;
            default:
                isReenter = true;
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_FOR_WRITE_EXTERNAL_STORAGE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Need Permission for write_external_storage", Toast.LENGTH_LONG).show();
            } else {
                pickPhoto();
            }
        }
    }

    private void pickPhoto() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        getIntent.setType("image/*");
        pickIntent.setType("image/*");
        Intent chooserIntent = Intent.createChooser(getIntent, "Select Wallpaper");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
        startActivityForResult(chooserIntent, REQUEST_CODE_FOR_PICK_PHOTO_);
    }

    private void cropPhoto(Uri uri) {
        String tempFileName = "StickyNote_" + System.currentTimeMillis() + ".jpg";
        File cropFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), tempFileName);
        Uri cropUri = Uri.fromFile(cropFile);

        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(uri, "image/*");
        cropIntent.putExtra("crop", "true");
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1.56);
        cropIntent.putExtra("outputX", 1080);
        cropIntent.putExtra("outputY", 1680);
        cropIntent.putExtra("scale", "true");
        cropIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        cropIntent.putExtra("return-data", false);
        cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, cropUri);
        startActivityForResult(cropIntent, REQUEST_CODE_FOR_CROP_PHOTO);
    }

    private void showIntroPages() {
        // if app is first time launched, then show the intro pages
        SharedPreferences sharedPreferences = getSharedPreferences("FirstTimeUser", MODE_PRIVATE);
        if (sharedPreferences.getBoolean("isFirstTime", true)) {
            startActivity(new Intent(this, IntroPageActivity.class));
            sharedPreferences.edit().putBoolean("isFirstTime", false).apply();
        }
    }

    private void setupDeleteRegion() {
        // define delete region's X & Y
        mImgViewTrashCan = findViewById(R.id.imgViewTrashCan);
        mImgViewTrashCan.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                iDeleteRegionX = mImgViewTrashCan.getX() + mImgViewTrashCan.getWidth() * 0.85;
                iDeleteRegionY = mImgViewTrashCan.getY() + mImgViewTrashCan.getHeight() * 0.15;
                mImgViewTrashCan.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void setupTransition() {
        Transition transition1 = TransitionInflater.from(this).inflateTransition(R.transition.transition1);
        Transition transition2 = TransitionInflater.from(this).inflateTransition(R.transition.transition2);
        getWindow().setExitTransition(transition1);
        getWindow().setReenterTransition(transition2);
    }

    @Override
    public void onEnterAnimationComplete() {
        // exit animation would call this method, too. have to prevent it.
        if (isReenter) {
            setupDeskTop();
            isReenter = false;
        }
        super.onEnterAnimationComplete();
    }

    private void setupDeskTop() {
        // to avoid there is view and data remain
        mRootView.removeViews(2, mRootView.getChildCount() - 2);
        mPaperPropertyArrayList.clear();

        // read Paper Property from sharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("PaperPropertyData", MODE_PRIVATE);
        String json = sharedPreferences.getString("PaperProperty", null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<PaperProperty>>() {
            }.getType();
            mPaperPropertyArrayList = gson.fromJson(json, type);
        }
        // get Paper id from sharedPreferences
        if (mPaperId == 0) mPaperId = sharedPreferences.getInt("PaperId", 0);

        // setup papers
        for (int i = 0; i < mPaperPropertyArrayList.size(); i++) {
            PaperProperty pp = mPaperPropertyArrayList.get(i);
            Paper paper = new Paper(this, pp);
            mRootView.addView(paper);
        }

        // setBackground if user have chosen picture before.
        SharedPreferences sharedPreferencesBackground = getSharedPreferences("BackgroundData", MODE_PRIVATE);
        String backgroundUri = sharedPreferencesBackground.getString("PicUri", null);
        if (backgroundUri != null) {
            Uri cropUri = Uri.parse(backgroundUri);
            try {
                Bitmap cropBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(cropUri), null, null);
                mRootView.setBackground(new BitmapDrawable(getResources(), cropBitmap));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Can not find wallpaper file", Toast.LENGTH_LONG).show();
            }
        } else {
            mRootView.setBackgroundColor(Color.parseColor("#ffffff"));
        }
        // to avoid content transition preforms incorrect
        mRootView.setTransitionGroup(false);
    }

    public void AddPaper(View view) {
        PaperProperty mPp = new PaperProperty(mPaperId, null,
                new String[]{null, null, null, null},
                "#AF626262",
                new float[]{0, 0});
        Paper paper = new Paper(this, mPp);
        mPaperPropertyArrayList.add(mPp);
        mRootView.addView(paper);
        mPaperId++;

        mChildViewCount = mRootView.getChildCount();
    }

    @Override
    public void deletePaper(Paper paper, PaperProperty pp) {
        mRootView.removeView(paper);
        mPaperPropertyArrayList.remove(pp); // delete this paper's property from the list array
        PaperContentDbHelper paperContentDbHelper = new PaperContentDbHelper(getApplicationContext(),
                DB_NAME, null, 1, "Paper" + pp.getPaperId());
        paperContentDbHelper.deleteTable();
        paperContentDbHelper.close();

        mChildViewCount = mRootView.getChildCount();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void openContent(PaperProperty pp) {
        Intent intent = new Intent(this, PaperContentActivity.class);
        // passing this Paper's Id,
        // then PaperContentActivity can save/load content into/from SharedPreference with specific name like "IdXItemO"
        // passing Property to ContentActivity,
        // so can save title and Top 4 content to the specific paper property.
        Bundle bundle = new Bundle();
        bundle.putSerializable("PaperProperty", pp);
        bundle.putSerializable("PaperPropertyList", mPaperPropertyArrayList);
        intent.putExtras(bundle);
        startActivityForResult(intent, REQUEST_CODE_FOR_OPEN_CONTENT, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }
}