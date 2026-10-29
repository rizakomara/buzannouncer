package com.erkasolusi.buspis3;

/*
FD1A,Term. BENOWO > TUNJUNGAN,0,0,0,FF0000,FFFFFF,FF0000,00FF00
                              [mode depan],[mode samping],[mode indoor],[koderutebgcolor],[koderutecolor],[namarutecolor],[busstopcolor]
 */

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PilihRute extends AppCompatActivity {

    private ListView lvPilihRute;
    private ArrayAdapter<String> listAdapter;
    File path;

    List<String> routes=new ArrayList<String>();

    @Override
    protected void attachBaseContext(Context newBase) {
        // Get the current configuration
        Configuration newConfig = new Configuration(newBase.getResources().getConfiguration());
        // Set a specific font scale (e.g., 1.0 for normal, 1.2 for 20% larger)
        newConfig.fontScale = 1f;
        newConfig.densityDpi = 170;

        // Apply the new configuration
        applyOverrideConfiguration(newConfig);
        super.attachBaseContext(newBase);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pilih_rute);

        Button button8=findViewById(R.id.button8);
        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        lvPilihRute=findViewById(R.id.lvPilihRute);

        listAdapter = new ArrayAdapter<String>(this, R.layout.isilistview);

        path = Environment.getExternalStoragePublicDirectory("obuconfig");
        //path=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);


        File routelist=new File(path,"/routelist.txt");

        int totroute=0;
        routes.clear();
        listAdapter.clear();

        try (BufferedReader br = new BufferedReader( new InputStreamReader(new FileInputStream(routelist)))) {

            String line;
            while ((line = br.readLine()) != null) {
                String[] lines=line.split(",");

                listAdapter.add(lines[0]+"|"+lines[1]);
                routes.add(line);
                totroute++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        lvPilihRute.setAdapter(listAdapter);

        lvPilihRute.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(PilihRute.this,MainActivity.class);
                String selrute=listAdapter.getItem(position);

                intent.putExtra("RUTE",selrute);
                intent.putExtra("MODE", routes.get(position));
                setResult(RESULT_OK,intent);
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        hideSystemUI();
        super.onStart();
    }

    @Override
    protected void onStop() {
        showSystemUI();
        super.onStop();
    }

    private void hideSystemUI() {

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
        );
    }

    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
}