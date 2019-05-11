package com.lionel.stickynote.helper;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.WindowManager;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.gson.Gson;
import com.lionel.stickynote.activities.MainActivity;
import com.lionel.stickynote.R;
import com.lionel.stickynote.fieldclass.PaperProperty;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;
import static com.lionel.stickynote.helper.PaperContentDbHelper.DB_NAME;
import static com.lionel.stickynote.helper.PaperContentDbHelper.TABLE_NAME;

public class FirebaseCloudHelper {
    public static final int REQUEST_CODE_FOR_AUTH = 777;
    private final MainActivity mActivity;
    private final FirebaseFirestore mFireStoreDB;
    private final FirebaseAuth mAuth;
    private String user;

    public interface resetUserInterface {
        void resetUser();
    }

    public FirebaseCloudHelper(MainActivity activity) {
        mActivity = activity;
        mFireStoreDB = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            user = firebaseUser.getProviderData().get(0).getEmail();
        }
    }

    public void Login() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build());

        mActivity.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setLogo(R.drawable.app_icon)
                        .setTheme(R.style.AppTheme)
                        .build(),
                REQUEST_CODE_FOR_AUTH);
    }

    public void Logout() {
        AuthUI.getInstance()
                .signOut(mActivity)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mActivity.resetUser();
                    }
                });
    }

    public void WriteToCloud(String mPicUri, int mPaperIdNow, ArrayList<PaperProperty> mPaperPropertyArrayList, final AlertDialog progressDialog) {
        WriteBatch batch = mFireStoreDB.batch();
        // write SharedPreferences data to FireStore
        Map<String, Object> SPData = new HashMap<>();
        SPData.put("isFirstTime", false);
        SPData.put("PicUri", mPicUri);
        SPData.put("PaperIdNow", mPaperIdNow);
        Gson gson = new Gson();
        String jsonSP = gson.toJson(mPaperPropertyArrayList);
        SPData.put("PaperProperty", jsonSP);
        DocumentReference SPDataRef = mFireStoreDB.collection("users").document(user)
                .collection("Data").document("SharedPreference");
        batch.set(SPDataRef, SPData);

        // write SQLite data to FireStore
        PaperContentDbHelper contentDbHelper = new PaperContentDbHelper(mActivity, DB_NAME, null, 1);
        contentDbHelper.createTable();
        Cursor c = contentDbHelper.query(TABLE_NAME, null, null, null, null, null, null);
        JSONArray result = new JSONArray();
        if (c != null && c.moveToFirst()) {
            try {
                do {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("paper_name", c.getString(c.getColumnIndex("paper_name")));
                    jsonObject.put("title", c.getString(c.getColumnIndex("title")));
                    jsonObject.put("item", c.getString(c.getColumnIndex("item")));
                    jsonObject.put("theme_index", c.getInt(c.getColumnIndex("theme_index")));
                    result.put(jsonObject);
                } while (c.moveToNext());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            c.close();
            contentDbHelper.close();
        }
        Map<String, Object> SQLiteData = new HashMap<>();
        SQLiteData.put("sqlite_data", result.toString());
        DocumentReference SQLiteDataRef = mFireStoreDB.collection("users").document(user)
                .collection("Data").document("SQLite");
        batch.set(SQLiteDataRef, SQLiteData);

        batch.commit()
                .addOnSuccessListener(mActivity, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(mActivity, "Successfully uploaded!", Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                        mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    }
                })
                .addOnFailureListener(mActivity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mActivity, "Uploading failed..", Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                        mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    }
                });
    }

    public void ReadFromCloud(final AlertDialog progressDialog) {
        // fetch SharedPreference and SQLite data from cloud
        FirebaseFirestore fireStoreDB = FirebaseFirestore.getInstance();
        fireStoreDB.collection("users").document(user).collection("Data")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                if (document != null && document.exists()) {
                                    switch (document.getId()) {
                                        case "SharedPreference":
                                            SharedPreferences.Editor sp = mActivity.getSharedPreferences("MainData", MODE_PRIVATE).edit();
                                            if (document.getString("PicUri") != null)
                                                sp.putString("PicUri", document.getString("PicUri"));
                                            else sp.remove("PicUri");
                                            sp.putBoolean("isFirstTime", document.getBoolean("isFirstTime"));
                                            sp.putInt("PaperIdNow", Integer.parseInt(document.getLong("PaperIdNow").toString()));
                                            sp.putString("PaperProperty", document.getString("PaperProperty"));
                                            sp.apply();
                                            mActivity.setupDeskTop();
                                            break;
                                        case "SQLite":
                                            PaperContentDbHelper paperContentDbHelper = new PaperContentDbHelper(mActivity, DB_NAME, null, 1);
                                            paperContentDbHelper.createTable();
                                            paperContentDbHelper.deleteALLPaper();
                                            ContentValues cv = new ContentValues();
                                            try {
                                                JSONArray jsonArray = new JSONArray(document.getString("sqlite_data"));
                                                for (int i = 0; i < jsonArray.length(); i++) {
                                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                                    cv.put("paper_name", jsonObject.getString("paper_name"));
                                                    cv.put("title", jsonObject.getString("title"));
                                                    cv.put("item", jsonObject.getString("item"));
                                                    cv.put("theme_index", jsonObject.getInt("theme_index"));
                                                    paperContentDbHelper.insert(TABLE_NAME, null, cv);
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            paperContentDbHelper.close();
                                            break;
                                    }
                                }
                            }
                            progressDialog.dismiss();
                            mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            Toast.makeText(mActivity, "Successfully restored!", Toast.LENGTH_LONG).show();
                        } else {
                            progressDialog.dismiss();
                            mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            Toast.makeText(mActivity, "Restoring failed..", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
