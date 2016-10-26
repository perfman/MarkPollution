package com.project.markpollution;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.project.markpollution.Objects.Category;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * IDE: Android Studio
 * Created by Nguyen Trong Cong  - 2DEV4U.COM
 * Name packge: com.project.markpollution
 * Name project: MarkPollution
 * Date: 10/11/2016
 * Time: 11:22 AM
 */

public class SendReportActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {
    private GoogleMap mMap;
    private double lat, lng;
    private EditText etTitle, etDesc;
    private Button btnSubmit;
    private ImageView ivCamera;
    private Spinner spCate;
    // Server's URL to interact with database
    private String url_retrieve_cate = "http://indi.com.vn/dev/markpollution/RetrieveCategory.php";
    private String url_insert_pollutionPoint = "http://indi.com.vn/dev/markpollution/InsertPollutionPoint.php";

    private ArrayList<Category> listCate;
    private FirebaseStorage storage;
    private String id_cate;     // to store id's category on selected item (In spinner)

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_report);
        // reference to map fragment then get async from it
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapSubmit);
        mapFragment.getMapAsync(this);

        storage = FirebaseStorage.getInstance();
        initView();
        captureOrGetFromGallery();
        loadSpinner();
        getCateID();

    }

    private void initView() {
        etTitle = (EditText) findViewById(R.id.editTextSubmitTitle);
        etDesc = (EditText) findViewById(R.id.editTextSubmitDesc);
        btnSubmit = (Button) findViewById(R.id.buttonSubmit);
        ivCamera = (ImageView) findViewById(R.id.ivCameraSubmit);
        spCate = (Spinner) findViewById(R.id.spinnerCate);

        btnSubmit.setOnClickListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Get intent from MapFragment
        Intent i = getIntent();
        lat = i.getDoubleExtra("Lat", 0);
        lng = i.getDoubleExtra("Long", 0);

        LatLng point = new LatLng(lat, lng);
        mMap.addMarker(new MarkerOptions().position(point)
                .title("This is Pollution Point "))
                .showInfoWindow();

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 17));
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
    }

    private void captureOrGetFromGallery() {
        ivCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(SendReportActivity.this);
                dialog.setContentView(R.layout.custom_dialog_choose_media);
                dialog.setTitle("Select option:");
                dialog.show();

                TextView tvCapture = (TextView) dialog.findViewById(R.id.textViewCapture);
                TextView tvGallery = (TextView) dialog.findViewById(R.id.textViewGallery);
                tvCapture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        capture();
                        dialog.dismiss();
                    }
                });
                tvGallery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        choosePictureFromGallery();
                        dialog.dismiss();
                    }
                });
            }
        });
    }

    private void capture(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 10);
    }

    private void choosePictureFromGallery(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Choose picture"), 11);
    }

    private Bitmap rotateImageIfRequired(Bitmap img, Uri UriImage) {
        if(getRotation(UriImage)!=0){
            Matrix matrix = new Matrix();
            matrix.postRotate(getRotation(UriImage));
            Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
            img.recycle();
            return rotatedImg;
        }else{
            return img;
        }
    }

    private int getRotation(Uri UriImage) {
        String[] filePathColumn = {MediaStore.Images.Media.ORIENTATION};
        Cursor cursor = getContentResolver().query(UriImage,
                filePathColumn, null, null, null);
        cursor.moveToFirst();

        int rotation = 0;
        rotation = cursor.getInt(0);
        cursor.close();
        return rotation;
    }

    private String getPicturePath(Uri uriImage)
    {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uriImage,
                filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String path = cursor.getString(columnIndex);
        cursor.close();
        return path;
    }

    private Bitmap setPic(String picPath) {
        // get width and height of imageView
        int targetW = ivCamera.getWidth();
        int targetH = ivCamera.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(picPath, bmOptions);
        // get width and height of photo
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        return BitmapFactory.decodeFile(picPath, bmOptions);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 10 && resultCode == RESULT_OK){
            String picPath = getPicturePath(data.getData());
            Bitmap bm = setPic(picPath);    // resize picture
            Bitmap bitmap = rotateImageIfRequired(bm, data.getData());  // rotate picture with right orientation
            ivCamera.setImageBitmap(bitmap);
        }else if(requestCode == 11 && resultCode == RESULT_OK){
            Uri uri = data.getData();
            Bitmap bm = null;
            try {
                bm = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Bitmap bitmap = rotateImageIfRequired(bm, uri);
            ivCamera.setImageBitmap(bitmap);
        }
    }

    private String getUserID(){
        SharedPreferences sharedPreferences = getSharedPreferences("sharedpref_id_user",MODE_PRIVATE);
        return sharedPreferences.getString("sharedpref_id_user","");
    }

    private void loadSpinner(){
        StringRequest stringReq = new StringRequest(Request.Method.GET, url_retrieve_cate, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jObj = new JSONObject(response);
                    JSONArray arr = jObj.getJSONArray("result");
                    listCate = new ArrayList<>();
                    for (int i=0; i<arr.length(); i++){
                        JSONObject cate = arr.getJSONObject(i);
                        listCate.add(new Category(cate.getString("id_cate"), cate.getString("name_cate")));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                ArrayAdapter<Category> adapter = new ArrayAdapter<>(SendReportActivity.this, android.R.layout.simple_list_item_1, listCate);
                spCate.setAdapter(adapter);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(SendReportActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        Volley.newRequestQueue(this).add(stringReq);
    }

    private void getCateID(){
        spCate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Category cate = (Category) spCate.getItemAtPosition(position);
                id_cate = cate.getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v == btnSubmit){
            StorageReference storeRef = storage.getReferenceFromUrl("gs://markpollution.appspot.com");
            StorageReference picRef = storeRef.child("images/IMG_" + new SimpleDateFormat("ddMMyyyy_hhmmss").format(new Date())+".jpg");

            // set enable drawing catch & build drawing catch for imageView
            ivCamera.setDrawingCacheEnabled(true);
            ivCamera.buildDrawingCache();

            // get drawing catch from imageView and return bitmap
            Bitmap bitmap = ivCamera.getDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);     // compress bitmap and pass it to baos

            byte[] data = baos.toByteArray();
            UploadTask uploadTask = picRef.putBytes(data);      // pass value to node
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(SendReportActivity.this, "Upload image failure", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    final String image = taskSnapshot.getDownloadUrl().toString();  // get image's URL
                    // Send data into database
                    StringRequest strReq = new StringRequest(Request.Method.POST, url_insert_pollutionPoint, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Toast.makeText(SendReportActivity.this, response, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(SendReportActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }){
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();
                            params.put("id_cate", id_cate);
                            params.put("id_user", getUserID());
                            params.put("lat", Double.toString(lat));
                            params.put("lng", Double.toString(lng));
                            params.put("title", etTitle.getText().toString());
                            params.put("desc", etDesc.getText().toString());
                            params.put("image", image);
                            return params;
                        }
                    };

                    Volley.newRequestQueue(SendReportActivity.this).add(strReq);

                }
            });
        }
    }
}
