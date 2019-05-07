package com.example.kruchishya.qrscanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.w3c.dom.Text;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSION_STORAGE=1;
    private static final int RESULT_LOAD_IMAGE=0;
    private static final int REQUEST_CAMERA=1;
    TextView tv;
    Button b,read,click;
    ImageView iv;
    BarcodeDetector br;
    TextToSpeech ts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},MY_PERMISSION_STORAGE);
        }

        b= (Button) findViewById(R.id.button);
        read= (Button) findViewById(R.id.button3);
        iv= (ImageView) findViewById(R.id.imageView);
        tv= (TextView) findViewById(R.id.textView);

        //code to read out the text

        ts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int ttsLang = ts.setLanguage(Locale.US);

                    if (ttsLang == TextToSpeech.LANG_MISSING_DATA
                            || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "The Language is not supported!");
                    } else {
                        Log.i("TTS", "Language Supported.");
                    }
                    Log.i("TTS", "Initialization success.");
                } else {
                    Toast.makeText(getApplicationContext(), "TTS Initialization failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        read.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                String data = tv.getText().toString();
                Log.i("TTS", "button clicked: " + data);
                int speechStatus = ts.speak(data, TextToSpeech.QUEUE_FLUSH, null);

                if (speechStatus == TextToSpeech.ERROR) {
                    Log.e("TTS", "Error in converting Text to Speech!");
                }
            }

        });

//code to upload the qrcode from media storage and display the content
        br =new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                .build();
        if(!br.isOperational())
        {
            tv.setText("COULD NOT SET UP DETECTOR");
        }
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,RESULT_LOAD_IMAGE);
            }
        });


        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==RESULT_LOAD_IMAGE&&resultCode==RESULT_OK&&null!=data){
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn,null,null,null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
            iv.setImageBitmap(bitmap);

            processData(bitmap);
    }

}
    private void processData(Bitmap myBitmap)
    {
        Frame frame= new Frame.Builder().setBitmap(myBitmap).build();
        SparseArray<Barcode> barcodes = br.detect(frame);
        Barcode thisCode = barcodes.valueAt(0);
        tv.setText(thisCode.rawValue);
    }
   @Override
   public void onDestroy() {
       super.onDestroy();
       if (ts != null) {
           ts.stop();
           ts.shutdown();
       }
   }
}
