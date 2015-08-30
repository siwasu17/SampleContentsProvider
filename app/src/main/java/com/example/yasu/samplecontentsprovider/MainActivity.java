package com.example.yasu.samplecontentsprovider;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends ActionBarActivity {

    private static final String LOG_TAG = "SampleProvider";

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }


    public File saveNewFile(String name, String text) {
        if(!isExternalStorageWritable()){
            return null;
        }
        File dir = getExternalFilesDir(null);
        String filename = name;
        File file = new File(dir, filename);
        if (file.exists()) {
            file.delete();
            Log.i("Save file", "Remove old file");
        }
        Log.i(LOG_TAG, "ABSOLUTE PATH: " + file.getAbsolutePath());
        Log.i(LOG_TAG, "PATH: " + file.getPath());

        try {
            FileOutputStream stream = new FileOutputStream(file, false);
            stream.write(text.getBytes());
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button save = (Button) findViewById(R.id.saveBtn);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    File file = saveNewFile("myfile.txt", "Hello,New File!");
                    //コンテンツプロバイダ登録
                    String[] filePathes = {
                            file.getPath()
                    };
                    String[] mimeTypes = {
                            "text/plain"
                    };
                    MediaScannerConnection.scanFile(getApplicationContext(), filePathes, mimeTypes,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i(LOG_TAG, "path: " + path);
                                    Log.i(LOG_TAG, "uri: " + uri.getPath() + " is completed");

                                }
                            }
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Button save2 = (Button) findViewById(R.id.save2Btn);
        save2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File f = saveNewFile("myfile2.txt", "One,Two,Three");

                Intent itt = new Intent();
                itt.setAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(f);
                itt.setData(uri);

                Log.i(LOG_TAG, "Scan path: " + uri.getPath());

                sendBroadcast(itt);
            }
        });


        Button load = (Button) findViewById(R.id.loadBtn);
        load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[]projection = new String[]{
                        MediaStore.MediaColumns.TITLE,
                        MediaStore.MediaColumns.DATA,
                };

                //                        MediaStore.Audio.Media.INTERNAL_CONTENT_URI,

                //対象のコンテンツプロバイダを設定し、カーソルを取得する
                Cursor cursor = getContentResolver().query(
                        MediaStore.Files.getContentUri("/external"),
                        projection,
                        null,null,null);
                if(null == cursor){
                    //カーソルの取得に失敗
                    Log.i(LOG_TAG,"Failed to get cursor");
                }else if(cursor.getCount() < 1){
                    //取得したレコードが0
                    Log.i(LOG_TAG,"No record found");
                }else{
                    //取得したレコードに対する処理
                    getMediaInfo(cursor);
                }
            }
        });
    }

    private void getMediaInfo(Cursor cursor){
        //カーソル先頭に移動
        if(!cursor.moveToFirst()){
            cursor.close();
            return;
        }

        //取得したいフィールドのインデックスを保持
        int titleIndex = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE);
        int pathIndex = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA);

        do{
            Log.i(LOG_TAG,"title: " + cursor.getString(titleIndex));
            Log.i(LOG_TAG,"path: " + cursor.getString(pathIndex));
        }while(cursor.moveToNext());

        cursor.close();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
