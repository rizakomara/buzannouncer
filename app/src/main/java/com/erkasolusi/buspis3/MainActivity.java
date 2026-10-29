package com.erkasolusi.buspis3;


import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.media.AudioManager.STREAM_MUSIC;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.text.format.DateFormat;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

// http://fms.erkasolusi.cloud:5055/?deviceid=12345&lat=48.8566&lon=2.3522&timestamp=2025-07-13T02:33:00Z&speed=15&bearing=270&altitude=35&accuracy=10&hdop=0.8&batt=75

public class MainActivity extends AppCompatActivity implements SerialInputOutputManager.Listener {

    public LocationManager locationManager;
    public LocationListener locationListener = new MyLocationListener();
    Double curlon = 0d;
    Double curlat = 0d;
    Float curspeed = 0f;
    Float curheading = 0f;
    Float curaltitude=0f;
    Date curdate;
    String locationprovider = "SEARCHING";
    String janit = "";
    String dajanit = "";
    String jamerah = "00:00:00";
    String serveraddress = "";
    String playeraddress = "";
    String deviceid = "";
    String buscode = "";
    String busregno = "";
    Float disttol = 50f;
    Float headtol = 50f;
    String rtport = "";
    String namarute = "-";
    String koderute = "-";
    String posisi = "-";
    String posisim1 = "-";
    int jumlahpis = 0;
    String voicetoplay = "";
    String udppacket = "";
    MediaPlayer mp = null;
    File pathmp3;
    String statusdinas = "TIDAK OPERASI";
    String rtkode = "";
    String rtnama = "";
    Boolean isfull = false;
    Boolean ispanic = false;
    Boolean isbbm = false;
    float speedalarm = 50;
    String currentdisplay = "";
    Boolean isRTconnect = false;
    Boolean isConnected=false;
    int sendinterval=5;
    Boolean gpsupdate=false;
    String ledcodebgcolor="000000";
    String ledcodecolor="FFFFFF";
    String lednamecolor="FFFFFF";
    String ledbusstopcolor="FFFFFF";
    String frontmode="0";
    String sidemode="0";
    String indoormode="0";
    String busstop="";
    String mutestatus="*UNMUTE*";
    String driverid="0000";
    String title1="CUSTOM 1";
    String voice1="custom1.mp3";
    String title2="CUSTOM 2";
    String voice2="custom2.mp3";
    String title3="CUSTOM 3";
    String voice3="custom3.mp3";

    class poi_data {
        final private double longitude;
        final private double latitude;
        final private double course;
        final private String lokasi;
        final private String mp3file;
        public poi_data(double longitude, double latitude, double course, String lokasi, String mp3file) {
            this.longitude = longitude;
            this.latitude = latitude;
            this.course = course;
            this.lokasi = lokasi;
            this.mp3file = mp3file;
        }

        public double getLongitude() {
            return longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getCourse() {
            return course;
        }

        public String getLokasi() {
            return lokasi;
        }

        public String getMp3File() {
            return mp3file;
        }
    }

    ArrayList<poi_data> poiData = new ArrayList<>();
    UsbSerialPort portrt;
    UsbDeviceConnection connection;
    UsbSerialDriver driver;
    UsbManager manager;
    boolean adaserialrt = false;
    private static final String INTENT_ACTION_GRANT_USB = "com.erkasolusi.buspis3-free" + ".GRANT_USB";
    private SerialInputOutputManager usbIoManager;
    private ActivityResultLauncher<Intent> pilihrutelauncher;
    String routedelimiter="|";

    RequestQueue requestQueue;

    @Override
    protected void attachBaseContext(Context newBase) {
        Configuration newConfig = new Configuration(newBase.getResources().getConfiguration());
        newConfig.fontScale = 1f;
        newConfig.densityDpi = 160; // 10 inch
//        newConfig.densityDpi = 140;   // 7 inch

        applyOverrideConfiguration(newConfig);
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (ContextCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, 100);
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{ACCESS_NETWORK_STATE}, 100);
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{CAMERA}, 100);
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        requestQueue = Volley.newRequestQueue(MainActivity.this);

        // ON BACK PRESSED ===========================================================================================================

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                System.exit(0);
            }
        });

        // LOCATION MANAGER ==========================================================================================================

        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(this, "GPS diaktifkan", Toast.LENGTH_SHORT).show();
            }

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        } catch (Exception e) {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_main);
        currentdisplay = "MAIN";
        loadconfig();

        TextView textView14 = findViewById(R.id.textView14);
        textView14.setText("OVER SPEED ALERT:" + String.format("%3.1f", speedalarm) + " KM/H");

        pathmp3 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);

        // ========================== PILIH ROUTE LAUNCHER INITIALIZATION ===========================

        pilihrutelauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        String rute = data.getStringExtra("RUTE");
                        String mod=data.getStringExtra("MODE");

                        String[] rutes = rute.split("\\|");
                        koderute = rutes[0];
                        namarute = rutes[1];

                        TextView textView3 = findViewById(R.id.textView3);
                        TextView textView4 = findViewById(R.id.textView4);
                        TextView textView15=findViewById(R.id.textView15);
                        TextView textView16=findViewById(R.id.textView16);
                        TextView textView11=findViewById(R.id.textView11);
                        textView3.setText(koderute);
                        textView4.setText(namarute);

                        // FD1A,Term. BENOWO > TUNJUNGAN,0,0,0,FF0001,FFFFFF,FF0000,00FF00
                        //                                     KODE   KODEBG,NAME,  BUSSTOP

                        String[] mods=mod.split(",");
                        frontmode=mods[2];
                        sidemode=mods[3];
                        indoormode=mods[4];
                        ledcodecolor=mods[5];
                        ledcodebgcolor=mods[6];
                        lednamecolor=mods[7];
                        ledbusstopcolor=mods[8];

                        textView15.setText("FRONT MODE:"+mods[2]+" ("+ledcodecolor+","+ledcodebgcolor+","+lednamecolor+","+ledbusstopcolor+")");
                        textView16.setText("SIDE MODE:"+mods[3]+" ("+ledcodecolor+","+ledcodebgcolor+","+lednamecolor+","+ledbusstopcolor+")");
                        textView11.setText("INDOOR MODE:"+mods[4]+" ("+ledcodecolor+","+ledcodebgcolor+","+lednamecolor+","+ledbusstopcolor+")");

                        rtkode = koderute;
                        rtnama = namarute;

                        loadcoordinate();
                    }
                }
        );

        // ======================= BUTTON ASSIGNMENT ================================================

        Button button2 = findViewById(R.id.button2); // pilih rute
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pilihrute = new Intent(MainActivity.this, PilihRute.class);
                pilihrutelauncher.launch(pilihrute);
            }
        });

        Button button4 = findViewById(R.id.button4);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (statusdinas.equals("TIDAK OPERASI")) {
                    statusdinas = "OPERASI";
                } else {
                    statusdinas = "TIDAK OPERASI";
                }

                if (statusdinas.equals("OPERASI")) {
                    rtkode = koderute;
                    rtnama = namarute;
                    button4.setBackgroundColor(Color.parseColor("#FF4040"));
                    Button button3 = findViewById(R.id.button3);
                    Button button6 = findViewById(R.id.button6);

                    button3.setBackgroundColor(Color.parseColor("#7E51D3"));
                    button6.setBackgroundColor(Color.parseColor("#7E51D3"));
                } else {
                    rtkode = "TMP";
                    rtnama = "MOHON MAAF TIDAK MELAYANI PENUMPANG";
                    button4.setBackgroundColor(Color.parseColor("#7E51D3"));
                }
                isbbm = false;
                ispanic = false;
            }
        });

        Button button7 = findViewById(R.id.button7); // system test
        button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("net.sourceforge.opencamera");
//                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("best.camera");
 //               Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.auto.hcamera"); // ESSGO
//                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.nwd.camera.app"); // ORCA
 //               Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.imotor.avin"); // ORCA NFC
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.qf.backcar"); // ORCA
                
                // ORCA NFC PAKAI SETTING AUTORUN NAVIGATION, SETELAH DIRUBAH, REBOOT LEWAT SETTING

                if (launchIntent != null) {
                    startActivity(launchIntent);
                }
            }
        });

        Button button9 = findViewById(R.id.button9); // sync file
        button9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("dk.tacit.android.foldersync.full");
                if (launchIntent != null) {
                    startActivity(launchIntent);
                }
            }
        });

        ImageButton imageButton = findViewById(R.id.imageButton); // playback mic
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("eu.timetools.playbackmic");
                if (launchIntent != null) {
                    startActivity(launchIntent);
                }
            }
        });

        Button button6 = findViewById(R.id.button6);  // panic button
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ispanic) {
                    ispanic = false;
                    button6.setBackgroundColor(Color.parseColor("#7E51D3"));
                } else {
                    ispanic = true;
                    button6.setBackgroundColor(Color.parseColor("#FF4040"));
                }

                if (ispanic) {
                    rtkode = "TMP";
                    rtnama = "-GANGGUAN- TIDAK MELAYANI PENUMPANG";

                    Button button3 = findViewById(R.id.button3);
                    Button button4 = findViewById(R.id.button4);

                    button3.setBackgroundColor(Color.parseColor("#7E51D3"));
                    button4.setBackgroundColor(Color.parseColor("#7E51D3"));
                    statusdinas = "TIDAK OPERASI";
                    isbbm = false;

                } else {
                    rtkode = koderute;
                    rtnama = namarute;
                }
            }
        });

        Button button5 = findViewById(R.id.button5); //
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playaudio("welcome.mp3");

            }
        });

        Button button3 = findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isbbm) {
                    isbbm = false;
                    button3.setBackgroundColor(Color.parseColor("#7E51D3"));
                    rtkode = koderute;
                    rtnama = namarute;
                } else {
                    isbbm = true;
                    rtkode = "BBM";
                    rtnama = "PENGISIAN BAHAN BAKAR MINYAK";

//                    rtkode = "CHG";
//                    rtnama = "Pengisian Energi Listrik";

                    button3.setBackgroundColor(Color.parseColor("#FF4040"));

                    Button button4 = findViewById(R.id.button4);
                    Button button6 = findViewById(R.id.button6);

                    button4.setBackgroundColor(Color.parseColor("#7E51D3"));
                    button6.setBackgroundColor(Color.parseColor("#7E51D3"));
                    ispanic = false;
                    statusdinas = "TIDAK OPERASI";
                }
            }
        });

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isfull) {
                    isfull = false;
                    button.setBackgroundColor(Color.parseColor("#7E51D3"));
                    rtkode = koderute;
                    rtnama = namarute;
                } else {
                    isfull = true;
                    button.setBackgroundColor(Color.parseColor("#FF4040"));
                    rtkode = koderute;
                    rtnama = "PENUH/FULL";
                }
            }
        });

        Button button10=findViewById(R.id.button10);
        button10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playaudio(voice1);
            }
        });

        Button button11=findViewById(R.id.button11);
        button11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playaudio(voice2);
            }
        });

        Button button12=findViewById(R.id.button12);
        button12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playaudio(voice3);
            }
        });

        // =============================== UI AWAL =====================================================

        TextView textView10 = findViewById(R.id.textView10);
        textView10.setText(deviceid+":"+buscode);

        button10.setText(title1);
        button11.setText(title2);
        button12.setText(title3);

/*
        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(STREAM_MUSIC,  0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
*/

        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        int maxVolume = audioManager.getStreamMaxVolume(STREAM_MUSIC);
        int desiredVolume = maxVolume;

        audioManager.setStreamVolume(STREAM_MUSIC, desiredVolume, AudioManager.FLAG_SHOW_UI);

        openconnection();
        initperiodictimer();
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

    void senddata() {

        Calendar cal = Calendar.getInstance();
        cal.setTime(curdate);
        cal.add(Calendar.HOUR_OF_DAY, -7);

        String url=serveraddress+"/"+
            "?deviceid="+deviceid+
            "&lat="+curlat+
            "&lon="+curlon+
            "&timestamp="+DateFormat.format("yyyy-MM-ddTHH:mm:ss",cal.getTime())+"Z"+
            "&speed="+(curspeed/1.85185f)+
            "&bearing="+curheading+
            "&altitude="+curaltitude+
            "&driverUniqueId="+driverid+
            "&route="+koderute;

        // driverUniqueId=443344&route=1-AGo
        if (ispanic) {
            url=url+"&alarm=SOS";
        }

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                       isConnected=true;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                isConnected=false;

            }
        });

        requestQueue.add(stringRequest);
    }

    void loadcoordinate() {
        File path = Environment.getExternalStoragePublicDirectory("obuconfig");
//        File path=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        File pisdata = new File(path, "/pisdata.txt");

        poiData.clear();
        posisi = namarute;
        posisim1 = "";

        try (BufferedReader br = new BufferedReader(new FileReader(pisdata))) {
            String line;

            // 1,106.802454,-6.243270,30,WELCOME BLOK M - KOTA,koridor 1 blok m-kota.wav

            jumlahpis = 0;
            try {
                while ((line = br.readLine()) != null) {
                    String[] lines = line.split(",");
                    if (lines[0].equals(koderute)) {
                        Double flon = Double.parseDouble(lines[1]);
                        Double flat = Double.parseDouble(lines[2]);
                        Double fcourse = Double.parseDouble(lines[3]);
                        String lokasi = lines[4];
                        String mp3file = lines[5];
                        poiData.add(new poi_data(flon, flat, fcourse, lokasi, mp3file));
                        //   System.out.println(mp3file);
                        jumlahpis++;
                    }
                }

                Toast.makeText(this, String.valueOf(jumlahpis) + " coordinates loaded", Toast.LENGTH_SHORT).show();

            } catch (FileNotFoundException ex) {
                Toast.makeText(this, "FILE OR COORDINATE NOT FOUND", Toast.LENGTH_SHORT).show();

            }
        } catch (IOException ex) {
            Toast.makeText(this, "FILE OR COORDINATE NOT FOUND", Toast.LENGTH_SHORT).show();
        }
    }

    void loadconfig() {
        File path = Environment.getExternalStoragePublicDirectory("obuconfig");
        //File path=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);

        File configfile = new File(path, "/config.xml");

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;

        try {
            dBuilder = dbFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = dBuilder.parse(configfile);
            doc.getDocumentElement().normalize();
            NodeList nodeList;

            nodeList = doc.getElementsByTagName("serveraddress");
            serveraddress = nodeList.item(0).getTextContent();

            nodeList = doc.getElementsByTagName("playeraddress");
            playeraddress = nodeList.item(0).getTextContent();

            nodeList = doc.getElementsByTagName("deviceid");
            deviceid = nodeList.item(0).getTextContent();

            nodeList = doc.getElementsByTagName("buscode");
            buscode = nodeList.item(0).getTextContent();

            nodeList = doc.getElementsByTagName("disttol");
            disttol = Float.parseFloat(nodeList.item(0).getTextContent());

            nodeList = doc.getElementsByTagName("headtol");
            headtol = Float.parseFloat(nodeList.item(0).getTextContent());

            nodeList = doc.getElementsByTagName("speedalarm");
            speedalarm = Float.parseFloat(nodeList.item(0).getTextContent());

            nodeList = doc.getElementsByTagName("routedelimiter");
            routedelimiter = nodeList.item(0).getTextContent();

            nodeList = doc.getElementsByTagName("sendinterval");
            sendinterval = Integer.parseInt(nodeList.item(0).getTextContent());

            nodeList = doc.getElementsByTagName("ledcodebgcolor");
            ledcodebgcolor = nodeList.item(0).getTextContent();

            nodeList = doc.getElementsByTagName("ledcodecolor");
            ledcodecolor = nodeList.item(0).getTextContent();

            nodeList = doc.getElementsByTagName("lednamecolor");
            lednamecolor = nodeList.item(0).getTextContent();

            nodeList = doc.getElementsByTagName("ledbusstopcolor");
            ledbusstopcolor = nodeList.item(0).getTextContent();

        } catch (SAXException | ParserConfigurationException | IOException e1) {

        }

        File customfile = new File(path, "/customvoice.xml");

        DocumentBuilderFactory dbFactory2 = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder2;

        try {
            dBuilder2 = dbFactory2.newDocumentBuilder();
            org.w3c.dom.Document doc2 = dBuilder2.parse(customfile);
            doc2.getDocumentElement().normalize();
            NodeList nodeList;

            nodeList = doc2.getElementsByTagName("button1title");
            title1 = nodeList.item(0).getTextContent();

            nodeList = doc2.getElementsByTagName("button1voice");
            voice1 = nodeList.item(0).getTextContent();

            nodeList = doc2.getElementsByTagName("button2title");
            title2 = nodeList.item(0).getTextContent();

            nodeList = doc2.getElementsByTagName("button2voice");
            voice2 = nodeList.item(0).getTextContent();

            nodeList = doc2.getElementsByTagName("button3title");
            title3 = nodeList.item(0).getTextContent();

            nodeList = doc2.getElementsByTagName("button3voice");
            voice3 = nodeList.item(0).getTextContent();

        } catch (SAXException | ParserConfigurationException | IOException e1) {

        }
    }

    void openconnection() {

        // KONEKSI KE USB TO SERIAL ============================================================================

        adaserialrt = false;

        manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);

        String ss = "";
        int selrtdriver = 0;

        for (int i = 0; i < availableDrivers.size(); i++) {
            if (availableDrivers.get(i).toString().contains("Ch34x")) {
                selrtdriver = i;
                adaserialrt = true;
            }
        }

        if (adaserialrt) {
            driver = availableDrivers.get(selrtdriver);

            do {
                PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(INTENT_ACTION_GRANT_USB), PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
                manager.requestPermission(driver.getDevice(), usbPermissionIntent);
                if (manager.hasPermission(driver.getDevice())) {
                    connection = manager.openDevice(driver.getDevice());
                }
            } while (connection == null);

            portrt = driver.getPorts().get(selrtdriver);

            try {
                portrt.open(connection);
                portrt.setParameters(2400, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                usbIoManager = new SerialInputOutputManager(portrt, this);
                Executors.newSingleThreadExecutor().submit(usbIoManager);
                isRTconnect = true;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void initperiodictimer() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                            cekpis();
                    }
                });
            }
        }, 1000, 1000);

        Timer timersend= new Timer();
        timersend.schedule(new TimerTask() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (gpsupdate && isRTconnect) senddata();
                    }
                });
            }
        }, (sendinterval* 1000L), (sendinterval* 1000L));
    }

    int angledifference(int a, int b) {
        int dist;
        dist = (a - b + 360) % 360;
        if (dist > 180) {
            dist = 360 - dist;
        }
        return dist;
    }

    double distance(double lon1, double lat1, double lon2, double lat2) {
        double dlon = 111320 * (lon2 - lon1);
        double dlat = 110570 * (lat2 - lat1);
        double jrk = Math.sqrt(dlon * dlon + dlat * dlat);
        return jrk;
    }

    void sendrunningtext(String msg) {
        if (isRTconnect) {
            try {
                portrt.write(msg.getBytes(), 1000);
            } catch (IOException e) {
                isRTconnect = false;
            }
        }
    }

    void playaudio(String mp3file) {

        try {
            if (mp == null) {
                mp = new MediaPlayer();

                mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mutestatus="*MUTE*";
                        mp.start();
                    }
                });

                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        mutestatus="*UNMUTE*";
                        mp.release();
                        mp = null;
                    }
                });
            }

            mp.setDataSource(pathmp3.getAbsolutePath() + "/" + mp3file);
            mp.prepare();

        } catch (Exception e) {
            e.printStackTrace();
            mp.release();
            mp = null;
        }
    }

    void cekpis() {
        Double jarak = 0d;
        Double minjarak = 1000000000d;
        String lokasi = "";
        String mp3file = "";
        String voc = "";
        Double cour = 0d;
        String mypos = "";

        if (!isRTconnect) openconnection();

        posisim1 = posisi;

        ImageView imageView2=findViewById(R.id.imageView2);

        if (isConnected) {
            imageView2.setBackgroundResource(R.drawable.icon_connected);
        } else {
            imageView2.setBackgroundResource(R.drawable.icon_disconnected);
        }

        for (poi_data tmp : poiData) {
            jarak = distance(tmp.getLatitude(), tmp.getLongitude(), curlat, curlon);

            if (jarak < minjarak) {
                minjarak = jarak;
                lokasi = tmp.getLokasi();
                mp3file = tmp.getMp3File();
                cour = tmp.getCourse();
            }
        }

        if (poiData.size() > 0) {

            mypos = String.format("%3.1f", minjarak / 1000) + " km -> " + (lokasi + "                                           ").substring(0, 40).trim() + "...";
            TextView textView13 = findViewById(R.id.textView13);
            textView13.setText(mypos);
        }

        if (minjarak < disttol) {
            if (cour > 400) {
                posisi = lokasi.trim();
                voc = mp3file;
            } else if (angledifference(cour.intValue(), curheading.intValue()) < headtol) {
                posisi = lokasi.trim();
                voc = mp3file;
            }
        }

        if (!posisim1.equals(posisi)) {
            if (!voc.isEmpty()) {
                voicetoplay = voc.trim();
                playaudio(voc);
            }
        }

        String oss;

        if (curspeed >= speedalarm) oss = "1";
        else oss = "0";

        try {
            String rtkodesend=rtkode.replace(">","").replace("<","").replace("_","").replace("*","");

            udppacket = "$PIS," + rtkodesend + "," + rtnama + "," + janit + "," + posisi + ","+mutestatus+"," + buscode + "," + dajanit + "," + voicetoplay + "," + oss + ","+routedelimiter+","+
                    busstop+","+frontmode+","+sidemode+","+indoormode+","+ledcodecolor+","+lednamecolor+","+ledbusstopcolor+","+ledcodebgcolor+",*";

            sendrunningtext("$" + udppacket + "\n");

            // ======================= UPDATE DISPLAY =============================================

            if (currentdisplay.equals("MAIN")) {

                TextView textView = findViewById(R.id.textView);
                textView.setText("LED DISPLAY:READY");

                TextView textView9 = findViewById(R.id.textView9);
                textView9.setText(posisi);

                TextView textView5 = findViewById(R.id.textView5);
                textView5.setText(String.format("%.1f", curspeed));

                TextView textView6 = findViewById(R.id.textView6);
                textView6.setText(jamerah);

                TextView textView7 = findViewById(R.id.textView7);
                textView7.setText("LOCATION PROVIDER:" + locationprovider);

                if (curspeed > speedalarm) {
                    textView5.setTextColor(Color.parseColor("#FF0000"));
                } else {
                    textView5.setTextColor(Color.parseColor("#00FF00"));
                }
            }
        } catch (NullPointerException e) {

        }
    }

    // ============================== SERIAL USB DATA LISTENER =======================================

    @Override
    public void onNewData(byte[] data) {

    }

    @Override
    public void onRunError(Exception e) {

    }

    // ============================ LOCATION LISTENER ===================================================

    class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(@NonNull Location location) {

            if (location != null) {
                curlon = location.getLongitude();
                curlat = location.getLatitude();
                curspeed = (float) location.getSpeed() * 3.6f;
                curheading = (float) location.getBearing();
                curaltitude=(float) location.getAltitude();
                curdate = new Date(location.getTime());
                locationprovider = location.getProvider().toUpperCase();
                gpsupdate=true;

                janit = DateFormat.format("HH:mm", location.getTime()).toString();
                dajanit = DateFormat.format("dd MMM yyyy", location.getTime()).toString();
                jamerah = DateFormat.format("HH:mm:ss", location.getTime()).toString();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {

        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {

        }
    }
}