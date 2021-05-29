package com.hemant239.portraitmaker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.hemant239.portraitmaker.Image.ImageAdapter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {


    ImageView mainImage;
    //the main image will be displayed in this layout

    TextView progressText;
    //this is to show the different messages during the execution

    Button  mainImageButton,        // button to get main Image from gallery
            allImagesButton,        // button to get other Images from gallery
            startProcessButton;     // button to start the processing of images

    LinearProgressIndicator linearProgressIndicator;
    //this will indicate the the progress of the AsyncTask(here the conversion of Image Uris to encoded String)

    RecyclerView mImageList;
    RecyclerView.Adapter<ImageAdapter.ViewHolder> mImageListAdapter;
    RecyclerView.LayoutManager mImageListLayoutManager;
    //to display the other images in gridLayout

    static ArrayList<String> encodedImages;     //will store the encoded String for other images
    static String mainEncodedString;            //will store the encoded String for main image

    static ArrayList<Uri> imageList;            //to store the image uris of other images

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //initialize the arrayLists
        imageList=new ArrayList<>();
        encodedImages=new ArrayList<>();

        //check if python instance has started or not, if not we will take it's instance
        if(!Python.isStarted()){
            Python.start(new AndroidPlatform(getApplicationContext()));
        }
        
        initializeViews();                  //initialize the different views present in the Activity
        initializeRecyclerViews();          //  initialize the recycler views

        allImagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGalleryForMultiple();
            }
        });

        mainImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGalleryForSingle();
            }
        });

        startProcessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mainEncodedString!=null || imageList.size()<1) {
                    startLoadingActivity();
                }
                else{
                    Toast.makeText(getApplicationContext(),"Please select main image/ other images",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startLoadingActivity() {
        Intent intent=new Intent(getApplicationContext(),LoadingActivity.class);
        startActivity(intent);
        finish();
    }


    //open gallery for taking the main images
    private void openGalleryForSingle() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,false);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select an image"),GET_MAIN_IMAGE);
    }


    //open gallery to taking other images
    private void openGalleryForMultiple() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select at least 5 images image"),GET_MULTIPLE_IMAGES);
    }

    int GET_MULTIPLE_IMAGES=1;
    int GET_MAIN_IMAGE=2;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==RESULT_OK){
            if(requestCode==GET_MULTIPLE_IMAGES){
                assert data != null;

                if(data.getClipData()!=null) {
                    for (int i = 0; i < Math.min(Objects.requireNonNull(data.getClipData()).getItemCount(),4); i++) {
                        Uri uri = data.getClipData().getItemAt(i).getUri();
                        imageList.add(uri);
                    }
                    mImageListAdapter.notifyDataSetChanged();
                }
                else{
                    Uri uri = data.getData();
                    imageList.add(uri);
                    mImageListAdapter.notifyDataSetChanged();
                }
                //starting the Async task to get the encoded string from the list of image uris
                new MyAsyncTask().execute();
            }

            else if(requestCode == GET_MAIN_IMAGE){
                assert data != null;
                Uri uri= data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                    mainEncodedString=bitmapToStringImage(bitmap);
                    Glide.with(getApplicationContext()).load(uri).into(mainImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else{
            Toast.makeText(getApplicationContext(),"something went wrong, please try again later onActivity result",Toast.LENGTH_SHORT).show();
        }
    }



    //this function is to get encoded String from the bitmap of the array
    private String bitmapToStringImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);

        byte[] arr=byteArrayOutputStream.toByteArray();

        return Base64.encodeToString(arr,Base64.DEFAULT);
    }


    private void initializeViews() {

        mainImage=findViewById(R.id.mainImage);

        mainImageButton=findViewById(R.id.buttonGetMainImage);
        allImagesButton=findViewById(R.id.buttonGetAllImages);
        startProcessButton=findViewById(R.id.buttonStartProcess);

        progressText=findViewById(R.id.progressText);

        linearProgressIndicator=findViewById(R.id.linearProgressBar);


    }

    private void initializeRecyclerViews() {

        mImageList=findViewById(R.id.recyclerViewListImages);
        mImageList.setHasFixedSize(false);
        mImageList.setNestedScrollingEnabled(false);


        mImageListAdapter= new ImageAdapter(imageList,this);
        mImageListAdapter.setHasStableIds(true);
        mImageList.setAdapter(mImageListAdapter);

        mImageListLayoutManager=new GridLayoutManager(getApplicationContext(),3);
        mImageList.setLayoutManager(mImageListLayoutManager);
    }



    //custom Async task to  get encoded images
    public class MyAsyncTask extends AsyncTask<String,Integer,String>{


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            linearProgressIndicator.setVisibility(View.VISIBLE);
            progressText.setText(R.string.check_compatible_message);
            linearProgressIndicator.setProgressCompat(0,true);

        }

        @Override
        protected String doInBackground(String... strings) {

            encodedImages=new ArrayList<>();
            int len=imageList.size();
            int count=0;
            for(Uri uri:imageList){
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    String encodedImage = bitmapToStringImage(bitmap);
                    encodedImages.add(encodedImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                count++;
                publishProgress(count*100/len);
            }
            return null;
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            linearProgressIndicator.setProgressCompat(values[0],true);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            startProcessButton.setVisibility(View.VISIBLE);
            progressText.setText(R.string.compatible_message);
            linearProgressIndicator.setVisibility(View.INVISIBLE);
        }
    }
    
    
}