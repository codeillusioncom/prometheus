package com.codeillusion.prometheus;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.preference.PreferenceManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

enum RequestType {
    STATUSES, ALERTS, SERVICE;
}

public class Requester {
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static JSONObject getJSONObjectFromURL(final Context context, final RequestType requestType, final String url) throws IOException, JSONException {

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        final String username = sharedPreferences.getString("username", "");
        final String password = sharedPreferences.getString("password", "");
        String baseUrl = sharedPreferences.getString("url", "");
        String urlString = baseUrl + url;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, urlString, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response);
                        try {
                            if (requestType.equals(RequestType.STATUSES)) {
                                renderStatuses(response);
                            } else if (requestType.equals(RequestType.ALERTS)) {
                                renderAlerts(response);
                            } else if (requestType.equals(RequestType.SERVICE)) {
                                String status = null;
                                try {
                                    status = response.getString("status");
                                    if (status.equals("success")) {
                                        JSONObject data = response.getJSONObject("data");
                                        JSONArray alerts = data.getJSONArray("alerts");
                                        if (alerts.length() > 0) {
                                            playBeep();
                                        }
                                    } else {
                                        playBeep();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // textView.setText("Response: " + response.toString());
                    }

                    private void playBeep() {
                        MediaPlayer sing = MediaPlayer.create(context, R.raw.alert);
                        sing.start();
                    }

                    private void renderAlerts(JSONObject response) throws JSONException {
                        String status = response.getString("status");
                        if (status.equals("success")) {
                            JSONObject data = response.getJSONObject("data");

/*                            data = new JSONObject("{\n" +
                                    "    \"data\": {\n" +
                                    "        \"alerts\": [\n" +
                                    "            {\n" +
                                    "                \"activeAt\": \"2018-07-04T20:27:12.60602144+02:00\",\n" +
                                    "                \"annotations\": {},\n" +
                                    "                \"labels\": {\n" +
                                    "                    \"alertname\": \"my-alert\"\n" +
                                    "                },\n" +
                                    "                \"state\": \"firing\",\n" +
                                    "                \"value\": \"1e+00\"\n" +
                                    "            }\n" +
                                    "        ]\n" +
                                    "    },\n" +
                                    "    \"status\": \"success\"\n" +
                                    "}").getJSONObject("data");*/

                            JSONArray alerts = data.getJSONArray("alerts");
                            TableLayout tableLayout = ((Activity) context).findViewById(R.id.alerts_table);
                            tableLayout.removeAllViews();
                            tableLayout.refreshDrawableState();
                            for (int i = 0; i < alerts.length(); i++) {
                                JSONObject alertRow = (JSONObject) alerts.get(i);
                                JSONObject labels = alertRow.getJSONObject("labels");
                                String alertName = labels.getString("alertname");
                                TableRow demoTableRow = new TableRow(context);
                                demoTableRow.setBackgroundColor(Color.argb(100, 255, 0, 0));
                                String state = alertRow.getString("state");
                                String value = alertRow.getString("activeAt");

                                DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSZ");
                                try {
                                    Date result1 = df1.parse(value);
                                    SimpleDateFormat format = new SimpleDateFormat("MM/dd, HH:mm");
                                    String dateString = format.format(result1);
                                    value = dateString;
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                TextView healthView = new TextView(context);
                                healthView.setText(alertName);
                                demoTableRow.addView(healthView);

                                TextView textView = new TextView(context);
                                textView.setText(state);
                                demoTableRow.addView(textView);

                                TextView jobView = new TextView(context);
                                jobView.setText(value);
                                demoTableRow.addView(jobView);
                                tableLayout.addView(demoTableRow);

                                healthView.setTextColor(Color.argb(100, 255, 0, 0));
                                textView.setTextColor(Color.argb(100, 255, 0, 0));
                                jobView.setTextColor(Color.argb(100, 255, 0, 0));
                            }
                        } else {

                        }
                    }

                    private void renderStatuses(JSONObject response) throws JSONException {
                        String status = response.getString("status");
                        if (status.equals("success")) {
                            JSONObject data = response.getJSONObject("data");
                            JSONArray activeTargets = data.getJSONArray("activeTargets");
                            TableLayout tableLayout = ((Activity) context).findViewById(R.id.statuses_table);
                            tableLayout.removeAllViews();
                            tableLayout.refreshDrawableState();
                            for (int i = 0; i < activeTargets.length(); i++) {
                                JSONObject row = (JSONObject) activeTargets.get(i);
                                JSONObject labels = row.getJSONObject("labels");
                                String health = row.getString("health");
                                TableRow demoTableRow = new TableRow(context);
                                /*if(health.equals("up")) {
                                    demoTableRow.setBackgroundColor(Color.argb(100, 0, 255, 0));
                                } else if (health.equals("down")) {
                                    demoTableRow.setBackgroundColor(Color.argb(100, 255, 0, 0));
                                } else {

                                }*/
                                String instance = labels.getString("instance");
                                String job = labels.getString("job");

                                TextView healthView = new TextView(context);
                                healthView.setText(renderHealth(health));
                                demoTableRow.addView(healthView);

                                TextView textView = new TextView(context);
                                textView.setText(instance);
                                demoTableRow.addView(textView);

                                TextView jobView = new TextView(context);
                                jobView.setText(job);
                                demoTableRow.addView(jobView);
                                tableLayout.addView(demoTableRow);

                                if (health.equals("up")) {
                                    healthView.setTextColor(Color.argb(100, 0, 255, 0));
                                } else if (health.equals("down")) {
                                    healthView.setTextColor(Color.argb(100, 255, 0, 0));
                                } else {

                                }
                            }
                            JSONArray droppedTargets = data.getJSONArray("droppedTargets");
                        } else {

                        }
                    }

                    private String renderHealth(String health) {
                        if (health.equals("up")) {
                            return context.getResources().getString(R.string.up);
                        } else if (health.equals("down")) {
                            return context.getResources().getString(R.string.down);
                        } else {
                            return health;
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        System.out.println(error);
                    }


                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String credentials = username + ":" + password;
                String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                return headers;
            }
        };

        if (context != null) {
// Access the RequestQueue through your singleton class.
            RequestQueue requestQueue;

// Instantiate the cache
            Cache cache = new DiskBasedCache(context.getCacheDir(), 1024 * 1024); // 1MB cap

// Set up the network to use HttpURLConnection as the HTTP client.
            Network network = new BasicNetwork(new HurlStack());

// Instantiate the RequestQueue with the cache and network.
            requestQueue = new RequestQueue(cache, network);

// Start the queue
            requestQueue.start();

            requestQueue.add(jsonObjectRequest);
        }
        return null;
    }
}