package com.hemant239.portraitmaker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class MainActivity extends AppCompatActivity {

    EditText ed1,ed2;
    TextView t1;
    Button b1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!Python.isStarted()){
            Python.start(new AndroidPlatform(getApplicationContext()));
        }

        ed1=findViewById(R.id.ed1);
        ed2=findViewById(R.id.ed2);
        t1=findViewById(R.id.t1);
        b1=findViewById(R.id.butt);

        Python python=Python.getInstance();
        final PyObject pyObject=python.getModule("hello");


        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int num1= Integer.parseInt(ed1.getText().toString());
                int num2= Integer.parseInt(ed2.getText().toString());
                Toast.makeText(getApplicationContext(),"button clicked",Toast.LENGTH_SHORT).show();

                try{
                    Toast.makeText(getApplicationContext(),"in try",Toast.LENGTH_SHORT).show();
                    Integer ans=pyObject.callAttr("sum",num1,num2).toInt();
                    Toast.makeText(getApplicationContext(),"function call done",Toast.LENGTH_SHORT).show();
                    t1.setText(ans+"");

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(),"in catch yani gadbad",Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });


    }
}