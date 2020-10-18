package com.codeillusion.prometheus;

import android.os.AsyncTask;
import android.os.Build;


import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

class PrometheusCheckerService extends AsyncTask<String, String, JSONObject> {
    @Override
    protected JSONObject doInBackground(String... strings) {
        // TODO: it checks everything

        //try {
            //JSONObject result = Requester.getJSONObjectFromURL(null, url, username, password);
            //System.out.println(result.toString());
            //return result;
        //} catch (IOException e) {
            // TODO:
            //e.printStackTrace();
        //} catch (JSONException e) {
            // TODO:
        //    e.printStackTrace();
        //}
        return null;
    }
}
