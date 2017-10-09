package fi.jamk.k1786student.mapapp;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        FetchDataTask task = new FetchDataTask();
        task.execute("http://student.labranet.jamk.fi/~K1786/download/locations.json");

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if(marker != null && marker.getTitle() != null)
                    try {
                        Intent openUrl = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.fi/search?q="+ URLEncoder.encode(marker.getTitle(), "utf-8")));
                        startActivity(openUrl);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                //Toast.makeText(getApplicationContext(), marker.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    class FetchDataTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... urls) {
            HttpURLConnection urlConnection = null;
            JSONObject json = null;

            try {
                URL url = new URL(urls[0]);
                urlConnection = (HttpURLConnection)url.openConnection();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                json = new JSONObject(stringBuilder.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) urlConnection.disconnect();
            }

            return json;
        }

        private JSONArray testJson;

        protected void onPostExecute(JSONObject json) {
            //StringBuffer text = new StringBuffer("");
            try {
                testJson = json.getJSONArray("locations");
                for (int i=0;i < testJson.length();i++) {
                    JSONObject hs = testJson.getJSONObject(i);
                    //text.append(hs.getString("title")+":"+hs.getString("latitude")+","+hs.getString("longitude")+"\n");
                    mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(hs.getString("latitude")), Double.parseDouble(hs.getString("longitude")))).title(hs.getString("title")));
                }
            } catch (JSONException e) {
                Log.e("JSON", "Error getting data.");
            }
            //Log.d("asdfasdfasdfasdf", text.toString());
        }
    }
}
