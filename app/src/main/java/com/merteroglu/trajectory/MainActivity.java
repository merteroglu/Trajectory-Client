package com.merteroglu.trajectory;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.merteroglu.trajectory.Model.Coordinate;
import com.merteroglu.trajectory.Model.Coordinates;
import com.merteroglu.trajectory.Model.ReducedResponse;
import com.merteroglu.trajectory.Model.SearchingBody;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private String API_URL = "https://localhost:8080/api/";
    private GoogleMap mMap;
    private Services services;
    private String filePath = "";
    private ArrayList<Coordinate> coordinateList;
    private ReducedResponse reducedResponse;
    private Coordinates foundCoordinates;
    private boolean searching = false;
    private Coordinate sRectTopLeft;
    private Coordinate sRectBottomRight;
    int sRect = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION},1001);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        services = RetrofitClient.getClient(API_URL).create(Services.class);

        coordinateList = new ArrayList<>();


        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_openFile) {
            new MaterialFilePicker()
                    .withActivity(this)
                    .withRequestCode(1)
                    .withFilter(Pattern.compile(".*\\.txt$")) // Filtering files and directories by file name using regexp
                    .withHiddenFiles(true) // Show hidden files and folders
                    .start();
        } else if (id == R.id.nav_reduction) {
            if(coordinateList.size() > 0){
                reduceCoordinates();
            }else {
                Toast.makeText(this, "First load the coordinates", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.nav_search) {
            if(coordinateList.size() > 0){
                searching = true;
                Toast.makeText(this, "Please Select Top Left Corner", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "First load the coordinates", Toast.LENGTH_SHORT).show();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);

            File file = new File(filePath);

            if(file.exists()){

                try{
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String line;

                    while((line = br.readLine()) != null){
                       String[] coor = line.split(",");
                       coordinateList.add(new Coordinate(Double.parseDouble(coor[0]),Double.parseDouble(coor[1])));
                    }

                }catch (IOException e){

                }

                drawCoordinates(coordinateList,0    );

            }


        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(searching){
                    if(sRect == 0){
                        sRectTopLeft = new Coordinate(latLng.latitude,latLng.longitude);
                        Toast.makeText(MainActivity.this, "Please Select Bottom Right Corner", Toast.LENGTH_SHORT).show();
                        sRect++;
                    }else if(sRect == 1){
                        sRectBottomRight = new Coordinate(latLng.latitude,latLng.longitude);
                        sRect = 0;
                        searching = false;
                        drawSRect();
                        searchCoordinates(sRectTopLeft,sRectBottomRight);
                    }
                }
            }
        });

    }

    public void drawSRect(){
        PolylineOptions rectOptions = new PolylineOptions()
                .add(new LatLng(sRectTopLeft.getLatitude(), sRectBottomRight.getLongitude()))
                .add(new LatLng(sRectBottomRight.getLatitude(), sRectBottomRight.getLongitude()))
                .add(new LatLng(sRectBottomRight.getLatitude(), sRectTopLeft.getLongitude()))
                .add(new LatLng(sRectTopLeft.getLatitude(), sRectTopLeft.getLongitude()))
                .add(new LatLng(sRectTopLeft.getLatitude(), sRectBottomRight.getLongitude()));

        rectOptions.color(Color.YELLOW);

        mMap.addPolyline(rectOptions);
    }


    public void drawCoordinates(ArrayList<Coordinate> coordinateList,int type){
        Double lat,lng,lat2,lng2;

        for(Coordinate c : coordinateList){
            lat = c.getLatitude();
            lng = c.getLongitude();

            if(type == 0){
                mMap.addMarker(new MarkerOptions().position(new LatLng(lat,lng)));
            }else if(type == 1){
                mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).position(new LatLng(lat,lng)));
            }else if(type == 2){
                mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)).position(new LatLng(lat,lng)));
            }

        }

        int color = Color.RED;

        if(type == 0){
            color = Color.RED;
        }else if(type == 1){
            color = Color.GREEN;
        }else if(type == 2){
            color = Color.MAGENTA;
        }

        for (int i = 0; i < coordinateList.size() - 1; i++) {
            lat = coordinateList.get(i).getLatitude();
            lng = coordinateList.get(i).getLongitude();
            lat2 = coordinateList.get(i+1).getLatitude();
            lng2 = coordinateList.get(i+1).getLongitude();

              mMap.addPolyline(new PolylineOptions()
                    .add(new LatLng(lat,lng),new LatLng(lat2,lng2))
                    .width(7f)
                    .color(color));

        }

        updateMaps(new LatLng(coordinateList.get(coordinateList.size()-1).getLatitude(),coordinateList.get(coordinateList.size()-1).getLongitude()));

    }

    public void reduceCoordinates(){
        final SweetAlertDialog mDialog = new SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE);
        mDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        mDialog.setTitleText("Receiving data ...");
        mDialog.setCancelable(false);
        mDialog.show();

        Coordinates coordinates = new Coordinates();
        coordinates.setCoordinates(coordinateList);

        services.reduceCoordinates(coordinates).enqueue(new Callback<ReducedResponse>() {
            @Override
            public void onResponse(Call<ReducedResponse> call, Response<ReducedResponse> response) {
                reducedResponse = response.body();

                mDialog.setTitleText("Successful");
                mDialog.setContentText("Response Time :" + reducedResponse.getResponseTime() + "\n Rate :" + reducedResponse.getReducedRate());
                mDialog.setConfirmText("OK");
                mDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        drawCoordinates(reducedResponse.getReducedCoordinates(),1);
                    }
                });
                mDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                mDialog.setCancelable(true);

            }

            @Override
            public void onFailure(Call<ReducedResponse> call, Throwable t) {
                mDialog.setTitleText("Error");
                mDialog.setContentText("Unable to connect");
                mDialog.setConfirmText("OK");
                mDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                mDialog.setCancelable(true);
            }
        });

    }

    public void searchCoordinates(Coordinate topLeft , Coordinate bottomRight){
        final SweetAlertDialog mDialog = new SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE);
        mDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        mDialog.setTitleText("Receiving data ...");
        mDialog.setCancelable(false);
        mDialog.show();

        SearchingBody searchingBody = new SearchingBody();
        searchingBody.setAllCoordinates(coordinateList);
        searchingBody.setTopLeft(topLeft);
        searchingBody.setBottomRight(bottomRight);

        services.searchCoordinates(searchingBody).enqueue(new Callback<Coordinates>() {
            @Override
            public void onResponse(Call<Coordinates> call, Response<Coordinates> response) {
                foundCoordinates = response.body();

                mDialog.setTitleText("Successful");
                mDialog.setContentText("Search successfully");
                mDialog.setConfirmText("OK");
                mDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        drawCoordinates(foundCoordinates.getCoordinates(),2);
                    }
                });
                mDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                mDialog.setCancelable(true);

            }

            @Override
            public void onFailure(Call<Coordinates> call, Throwable t) {
                mDialog.setTitleText("Error");
                mDialog.setContentText("Unable to connect");
                mDialog.setConfirmText("OK");
                mDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                mDialog.setCancelable(true);
            }
        });

    }

    public void updateMaps(LatLng position){
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(position)
                .zoom(15f).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }


}
