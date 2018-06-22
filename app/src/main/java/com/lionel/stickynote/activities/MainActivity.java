package com.lionel.stickynote.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lionel.stickynote.R;
import com.lionel.stickynote.views.Paper;
import com.lionel.stickynote.fieldclass.PaperProperty;
import com.lionel.stickynote.helper.FirebaseCloudHelper;
import com.lionel.stickynote.helper.PaperContentDbHelper;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import static com.lionel.stickynote.helper.PaperContentDbHelper.DB_NAME;


public class MainActivity extends AppCompatActivity implements Paper.DeletePaperInterface, Paper.OpenPaperContent, NavigationView.OnNavigationItemSelectedListener, FirebaseCloudHelper.resetUserInterface {
    private static final int REQUEST_CODE_FOR_OPEN_CONTENT = 100;
    private static final int REQUEST_CODE_FOR_PICK_PHOTO_ = 200;
    private static final int REQUEST_CODE_FOR_CROP_PHOTO = 300;
    private static final int REQUEST_PERMISSION_FOR_PICK_PHOTO = 999;
    private static final int REQUEST_PERMISSION_FOR_RESTORE_DATA = 888;

    public static int toolbarHeight;

    private ConstraintLayout mRootView;
    private ArrayList<PaperProperty> mPaperPropertyArrayList; // store Paper object
    private int mChildViewCount, mPaperIdNow;
    private boolean isReenter;
    public static double iDeleteRegionX, iDeleteRegionY;
    private ImageView mImgViewTrashCan, mImgViewHeaderProfile;
    private TextView mTxtViewHeaderName, mTxtViewHeaderEmail;
    private String mPicUri;
    private DrawerLayout mDrawerLayout;
    public NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_main);

        mRootView = findViewById(R.id.mainRootView);
        mChildViewCount = mRootView.getChildCount();
        mPaperPropertyArrayList = new ArrayList<>();
        mDrawerLayout = findViewById(R.id.drawerLayout);
        mNavigationView = findViewById(R.id.navigationView);
        mNavigationView.setNavigationItemSelectedListener(this);
        mImgViewHeaderProfile = mNavigationView.getHeaderView(0).findViewById(R.id.imgViewHeaderProfile);
        mTxtViewHeaderName = mNavigationView.getHeaderView(0).findViewById(R.id.txtViewHeaderName);
        mTxtViewHeaderEmail = mNavigationView.getHeaderView(0).findViewById(R.id.txtViewHeaderEmail);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                toolbarHeight = toolbar.getHeight();
                toolbar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu);

        setupDeleteRegion();
        setupTransition();
        showIntroPages();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // only use setupDeskTop() in onStart() in the following situations: first time to startup app or the paper was disappeared
        if (mChildViewCount == 3 || mRootView.getChildCount() != mChildViewCount) {
            setupDeskTop();
            mChildViewCount = mRootView.getChildCount();
        }

      /*  Map<String, ?> allEntries = getSharedPreferences("MainData", MODE_PRIVATE).getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("<<<", "\n" + entry.getKey() + ": " + entry.getValue().toString());
        }*/

        // check is there current user now
        // set user's profile
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            setupUserDisplay();
            mNavigationView.getMenu().findItem(R.id.drawerItemLogout).setVisible(true);
            mNavigationView.getMenu().findItem(R.id.drawerItemLogin).setVisible(false);
        } else {
            mNavigationView.getMenu().findItem(R.id.drawerItemLogout).setVisible(false);
            mNavigationView.getMenu().findItem(R.id.drawerItemLogin).setVisible(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // save Paper Property to sharedPreferences
        SharedPreferences.Editor sharedPreferences =
                getSharedPreferences("MainData", MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = gson.toJson(mPaperPropertyArrayList);
        sharedPreferences.putString("PaperProperty", json);

        // to save Paper Id is how much to add now
        sharedPreferences.putInt("PaperIdNow", mPaperIdNow);
        // to save background picture which user chose
        sharedPreferences.putString("PicUri", mPicUri);

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
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.menuItemAbout:
                LayoutInflater layoutInflater = LayoutInflater.from(this);
                View view = layoutInflater.inflate(R.layout.about_layout, null);
                new AlertDialog.Builder(this)
                        .setView(view)
                        .setCancelable(true)
                        .show();
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
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
                            getSharedPreferences("MainData", MODE_PRIVATE).edit()
                                    .putString("PicUri", cropUri.toString()).apply();
                            mPicUri = cropUri.toString();
                        }
                    }
                    break;
                case FirebaseCloudHelper.REQUEST_CODE_FOR_AUTH:
                    Toast.makeText(this, "Login successfully!", Toast.LENGTH_SHORT).show();
                    setupUserDisplay();
                    break;
                case REQUEST_CODE_FOR_OPEN_CONTENT:
                    isReenter = true;
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_FOR_PICK_PHOTO:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Need Permission for write_external_storage", Toast.LENGTH_LONG).show();
                } else {
                    pickPhoto();
                }
                break;
            case REQUEST_PERMISSION_FOR_RESTORE_DATA:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Need Permission for write_external_storage", Toast.LENGTH_LONG).show();
                } else {
                    RestoreDataFromCloud();
                }
        }
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

    private void showIntroPages() {
        // if app is first time launched, then show the intro pages
        SharedPreferences sharedPreferences = getSharedPreferences("MainData", MODE_PRIVATE);
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

    public void setupDeskTop() {
        // to avoid there is view and data remain
        mRootView.removeViews(3, mRootView.getChildCount() - 3);
        mPaperPropertyArrayList.clear();

        // read Paper Property from sharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MainData", MODE_PRIVATE);
        String json = sharedPreferences.getString("PaperProperty", null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<PaperProperty>>() {
            }.getType();
            mPaperPropertyArrayList = gson.fromJson(json, type);
        }
        // get Paper id from sharedPreferences
        mPaperIdNow = sharedPreferences.getInt("PaperIdNow", 0);

        // setup papers
        for (int i = 0; i < mPaperPropertyArrayList.size(); i++) {
            PaperProperty pp = mPaperPropertyArrayList.get(i);
            Paper paper = new Paper(this, pp);
            mRootView.addView(paper);
        }

        // setBackground if user have chosen picture before.
        mRootView.setBackgroundColor(Color.parseColor("#ffffff"));
        String backgroundUri = sharedPreferences.getString("PicUri", null);
        if (backgroundUri != null) {
            Uri cropUri = Uri.parse(backgroundUri);
            mPicUri = backgroundUri;
            try {
                Bitmap cropBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(cropUri), null, null);
                mRootView.setBackground(new BitmapDrawable(getResources(), cropBitmap));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Can not find wallpaper file", Toast.LENGTH_LONG).show();
            }
        }
        // to avoid content transition preforms incorrect
        mRootView.setTransitionGroup(false);
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

    public void AddPaper(View view) {
        PaperProperty mPp = new PaperProperty(mPaperIdNow, null,
                new String[]{null, null, null, null},
                "#E1626262",
                new float[]{0, toolbarHeight});
        Paper paper = new Paper(this, mPp);
        mPaperPropertyArrayList.add(mPp);
        paper.setX(0);
        paper.setY(toolbarHeight);
        mRootView.addView(paper);
        mPaperIdNow++;

        mChildViewCount = mRootView.getChildCount();
    }

    @Override
    public void deletePaper(Paper paper, PaperProperty pp) {
        mRootView.removeView(paper);
        mPaperPropertyArrayList.remove(pp); // delete this paper's property from the list array
        PaperContentDbHelper paperContentDbHelper = new PaperContentDbHelper(getApplicationContext(),
                DB_NAME, null, 1);
        paperContentDbHelper.deletePaper("Paper" + pp.getPaperId());
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

    private void backupDataToCloud() {
        // check network connection
        if (isNetworkAvailable()) {
            final FirebaseCloudHelper readWriteCloudHelper = new FirebaseCloudHelper(MainActivity.this);
            // check if log or not
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                new AlertDialog.Builder(this)
                        .setTitle("Backup")
                        .setMessage("Are you sure you want to upload data to cloud? \n\n(If you do, you'll lose the last saved data in Cloud.)\n")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // show loading screen
                                AlertDialog.Builder adBuilder = new AlertDialog.Builder(MainActivity.this);
                                AlertDialog progress = adBuilder.create();
                                View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.progress_dialog, null);
                                ((TextView) view.findViewById(R.id.txtViewProgressTitle)).setText("Backup");
                                ((TextView) view.findViewById(R.id.txtViewPregressMessage)).setText("Wait while Updating...");
                                ((ProgressBar) view.findViewById(R.id.progressBar)).getIndeterminateDrawable().setColorFilter(Color.parseColor("#FFFF345A"), PorterDuff.Mode.MULTIPLY);
                                progress.setView(view);
                                progress.show();
                                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                                readWriteCloudHelper.WriteToCloud(mPicUri, mPaperIdNow, mPaperPropertyArrayList, progress);
                            }
                        }).setNegativeButton("No", null)
                        .setCancelable(true)
                        .show();
            } else {
                Toast.makeText(this, "You need to login your account!", Toast.LENGTH_SHORT).show();
                readWriteCloudHelper.Login();
            }
        } else
            Toast.makeText(MainActivity.this, "Need internet connection!", Toast.LENGTH_LONG).show();
    }

    private void RestoreDataFromCloud() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_FOR_RESTORE_DATA);
        } else {
            // check network connection
            if (isNetworkAvailable()) {
                final FirebaseCloudHelper readWriteCloudHelper = new FirebaseCloudHelper(MainActivity.this);
                // check if log or not
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    new AlertDialog.Builder(this)
                            .setTitle("Restore")
                            .setMessage("Are you sure to restore the data you saved last time? \n\n(if you do, the current data will be lost.)\n")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (isNetworkAvailable()) {
                                        // show loading screen
                                        AlertDialog.Builder adBuilder = new AlertDialog.Builder(MainActivity.this);
                                        AlertDialog progress = adBuilder.create();
                                        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.progress_dialog, null);
                                        ((TextView) view.findViewById(R.id.txtViewProgressTitle)).setText("Restore");
                                        ((TextView) view.findViewById(R.id.txtViewPregressMessage)).setText("Wait while Loading...");
                                        ((ProgressBar) view.findViewById(R.id.progressBar)).getIndeterminateDrawable().setColorFilter(Color.parseColor("#FFFF345A"), PorterDuff.Mode.MULTIPLY);
                                        progress.setView(view);
                                        progress.show();
                                        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                                        readWriteCloudHelper.ReadFromCloud(progress);
                                    } else
                                        Toast.makeText(MainActivity.this, "Need internet connection!", Toast.LENGTH_LONG).show();
                                }
                            })
                            .setNegativeButton("No", null)
                            .setCancelable(true)
                            .show();
                } else {
                    Toast.makeText(this, "You need to login your account!", Toast.LENGTH_SHORT).show();
                    readWriteCloudHelper.Login();
                }
            } else {
                Toast.makeText(MainActivity.this, "Need internet connection!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.drawerItemBackground:
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_PERMISSION_FOR_PICK_PHOTO);
                } else {
                    pickPhoto();
                }
                break;
            case R.id.drawerItemResetBackground:
                new AlertDialog.Builder(this)
                        .setTitle("Reset WallPaper")
                        .setMessage("Your wallpaper will be blank, is it ok?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mRootView.setBackgroundColor(Color.parseColor("#ffffff"));
                                SharedPreferences.Editor sp = getSharedPreferences("MainData", MODE_PRIVATE).edit();
                                sp.remove("PicUri");
                                sp.apply();
                                mPicUri = null;
                            }
                        })
                        .setNegativeButton("NO", null)
                        .setCancelable(true)
                        .show();
                break;

            case R.id.drawerItemBackupDataToCloud:
                backupDataToCloud();
                break;
            case R.id.drawerItemRestoreData:
                RestoreDataFromCloud();
                break;
            case R.id.drawerItemIntroPage:
                startActivity(new Intent(this, IntroPageActivity.class));
                break;
            case R.id.drawerItemLogout:
                new FirebaseCloudHelper(MainActivity.this).Logout();
                break;
            case R.id.drawerItemLogin:
                if (isNetworkAvailable())
                    new FirebaseCloudHelper(MainActivity.this).Login();
                else
                    Toast.makeText(MainActivity.this, "Need internet connection!", Toast.LENGTH_LONG).show();
                break;
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setupUserDisplay() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            mTxtViewHeaderName.setText(user.getProviderData().get(0).getDisplayName());
            mTxtViewHeaderEmail.setText(user.getProviderData().get(0).getEmail());

            // set user's profile photo in circle shape
            Picasso.get()
                    .load(user.getProviderData().get(0).getPhotoUrl())
                    .transform(new Transformation() {
                        @Override
                        public Bitmap transform(Bitmap source) {
                            int size = Math.min(source.getWidth(), source.getHeight());

                            int x = (source.getWidth() - size) / 2;
                            int y = (source.getHeight() - size) / 2;

                            Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
                            if (squaredBitmap != source) {
                                source.recycle();
                            }

                            Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

                            Canvas canvas = new Canvas(bitmap);
                            Paint paint = new Paint();
                            BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
                            paint.setShader(shader);
                            paint.setAntiAlias(true);

                            float r = size / 2f;
                            canvas.drawCircle(r, r, r, paint);

                            squaredBitmap.recycle();
                            return bitmap;
                        }

                        @Override
                        public String key() {
                            return "circle";
                        }
                    })
                    .into(mImgViewHeaderProfile);
        }
    }

    @Override
    public void resetUser() {
        Toast.makeText(this, "Logout successfully!", Toast.LENGTH_LONG).show();
        mNavigationView.getMenu().findItem(R.id.drawerItemLogout).setVisible(false);
        mNavigationView.getMenu().findItem(R.id.drawerItemLogin).setVisible(true);
        mImgViewHeaderProfile.setImageDrawable(null);
        mTxtViewHeaderName.setText("");
        mTxtViewHeaderEmail.setText("");
    }
}