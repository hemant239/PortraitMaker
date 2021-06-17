package com.hemant239.portraitmaker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.Calendar;

public class LoadingActivity extends AppCompatActivity {


    TextView loadingText;                                   // to display the different messages during the execution of activity

    Button homeButton,                                      // button to go back to main activity
            saveButton;                                     // button to save resultant image in gallery

    ArrayList<String> encodedImages;                        //will store the encoded String for other images
    String mainEncodedString;                               //will store the encoded String for main image



    Bitmap bitmap;                                          //will store the bitmap of the final image

    LinearLayout buttonsLinearLayout;

    ImageView finalImage;                                   //imageView to display the final Image


    LinearProgressIndicator linearProgressIndicator;
    //this will indicate the the progress of the AsyncTask (here running the python code on encoded Strings)


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        initializeViews();

        encodedImages           =MainActivity.encodedImages;
        mainEncodedString       =MainActivity.mainEncodedString;

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMainActivity();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long time=Calendar.getInstance().getTime().getTime();
                //stores the resultant image to gallery
                MediaStore.Images.Media.insertImage(getContentResolver(),bitmap,"App"+time,"this is from portraitMaker app");

                Toast.makeText(getApplicationContext(),"Image saved to Gallery",Toast.LENGTH_SHORT).show();
            }
        });



        //start Async task to run the python code
        new MyAsyncTask().execute();


    }

    private void startPythonCode() {

        //get python instance
        //get module numpy and call array function to convert java array to numpy array
        Python python=Python.getInstance();
        PyObject np=python.getModule("numpy");
        String []images=new String[encodedImages.size()];
        encodedImages.toArray(images);                                      //converting arrayList to Array
        PyObject image_array=np.callAttr("array",(Object) images);
        PyObject hello=python.getModule("hello");

        String final_image=hello.callAttr("process_images",mainEncodedString,image_array).toString();
        //this will call our function in python code and will return encoded string

        //decode the string to byte array and then to bitmap
        byte[] data=android.util.Base64.decode(final_image,Base64.DEFAULT);
        bitmap= BitmapFactory.decodeByteArray(data,0,data.length);
    }

    private void startMainActivity() {
        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void initializeViews() {
        loadingText             =findViewById(R.id.loadingText);

        finalImage              =findViewById(R.id.finalImage);


        homeButton             =findViewById(R.id.homeButton);
        saveButton             =findViewById(R.id.saveButton);

        buttonsLinearLayout     =findViewById(R.id.buttonsLinearLayout);

        linearProgressIndicator =findViewById(R.id.loadingProgressBar);
    }


    public class MyAsyncTask extends AsyncTask<String,Integer,String>{
        @Override
        protected String doInBackground(String... strings) {
            startPythonCode();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            linearProgressIndicator.setVisibility(View.GONE);
            loadingText.setVisibility(View.GONE);
            buttonsLinearLayout.setVisibility(View.VISIBLE);
            finalImage.setVisibility(View.VISIBLE);
            finalImage.setImageBitmap(Bitmap.createScaledBitmap(bitmap,500,500,false));

        }
    }
}