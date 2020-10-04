package com.example.associateandopenafiletxt;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    private Button btnOpen;
    private Button btnSend;
    private TextView txtFile;
    public Intent intent;

    private static final int PERMISSION_REQUEST_STORAGE = 1000;
    private static final int READ_REQUEST_CODE= 42;
    private String path_share = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // запрашиваем разрешение к доступу к хранилищу (для новых апи)
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
        }

        btnOpen = (Button) findViewById(R.id. btnOpen);
        btnSend = (Button) findViewById(R.id. btnSend);
        txtFile = (TextView) findViewById(R.id. txtFile);

        intent=getIntent();
        String message = getFilePathFromIntentObj(intent);
        path_share = getFilePathFromIntentObj(intent);
        Toast.makeText(this,"Путь: " +message, Toast.LENGTH_LONG).show();
        txtFile.setText(getTxt(message));

        btnOpen.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectfile();
            }
        });

        btnSend.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_share = new Intent(Intent.ACTION_SEND);
                intent_share.putExtra(Intent.EXTRA_TEXT, path_share);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Uri path_ = FileProvider.getUriForFile(MainActivity.this, "com.example.associateandopenafiletxt", new File(path_share));
                    intent_share.putExtra(Intent.EXTRA_STREAM, path_);
                } else {
                    intent_share.putExtra(Intent.EXTRA_STREAM , Uri.fromFile(new File(path_share)));
                }
                intent_share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent_share.setType("plain/*");
                startActivity(intent_share);
            }
        });
    }

    // надо
    private String getFilePathFromIntentObj(Intent intent) {
        String filepath = "No path";
        if(intent != null) {
            String action=intent.getAction();
            if(Intent.ACTION_VIEW.equals(action) ) {
                Uri file_uri=intent.getData();
                if(file_uri!=null)
                    filepath=file_uri.getPath();
                else
                    filepath="No file";
            } else if(Intent.ACTION_SEND.equals(action) ) {
                Uri uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                filepath = uri.getPath();
            }
        }
        return filepath;
    }
    //

    private String getTxt(String path) {
        File file = new File(path);
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while((line = br.readLine()) != null) {
                text.append(line);
                text.append("\n");
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text.toString();
    }

    private void selectfile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        if(requestCode==READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//            if(data != null) {
//                Uri uri = data.getData();
//                String path = uri.getPath();
//                path = path.substring(path.indexOf(":") + 1);
//                Toast.makeText(this," " + path, Toast.LENGTH_LONG).show();
//                txtFile.setText(getTxt(path));
//            }
//        }
        if(requestCode == READ_REQUEST_CODE) {
            if (data != null) {
                String selectedFile = data.getData().getPath();
        //        String filename = selectedFile.substring(selectedFile.lastIndexOf("/") + 1);
                selectedFile = selectedFile.substring(selectedFile.lastIndexOf(":") + 1);
                try {
                    File myFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + selectedFile);
                    path_share = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + selectedFile;
                    FileInputStream fIn = new FileInputStream(myFile);
                    BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
                    String aDataRow = "";
                    String aBuffer = "";
                    while ((aDataRow = myReader.readLine()) != null) {
                        aBuffer += aDataRow + "\n";
                    }
                    txtFile.setText(aBuffer);
                    myReader.close();
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show(); }
                }
        }
//                Uri file_uri=data.getData();
//                String path = file_uri.getPath();
//                Toast.makeText(this,"Путть: " + getTxt(path), Toast.LENGTH_LONG).show();
//                txtFile.setText(getTxt(path));
//            } else {
//                Toast.makeText(this,"Пустой", Toast.LENGTH_LONG).show();
//            }
        }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSION_REQUEST_STORAGE) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,"Права даны", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this,"Права не даны", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
