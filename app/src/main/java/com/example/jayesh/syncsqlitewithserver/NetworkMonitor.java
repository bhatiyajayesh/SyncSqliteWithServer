package com.example.jayesh.syncsqlitewithserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jayesh on 19-Apr-18.
 */

public class NetworkMonitor extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.e("Eror","OnReceive");
        if (checkNetworkConnection(context)) {
            Log.e("Eror","Network Available");
            final DbHelper dbHelper = new DbHelper(context);
            final SQLiteDatabase sqLiteDatabase = dbHelper.getReadableDatabase();
            Cursor cursor = dbHelper.readFromLocalDatabase(sqLiteDatabase);

            while (cursor.moveToNext()) {
                int sync_status = cursor.getInt(cursor.getColumnIndex(DbContact.SYNC_STATUS));
                if (sync_status == DbContact.SYNC_STATUS_FAILED) {
                    final String name = cursor.getString(cursor.getColumnIndex(DbContact.NAME));
                    Log.e("Eror","In Iff=>"+name);
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, DbContact.SERVER_URI,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject jsonObject=new JSONObject(response);
                                        String Response=jsonObject.getString("response");
                                        Log.e("Eror","Response=>"+Response);
                                        if(Response.equals("OK"))
                                        {
                                            dbHelper.updateLocalDatabase(name,DbContact.SYNC_STATUS_OK,sqLiteDatabase);
                                            context.sendBroadcast(new Intent(DbContact.UI_UPDATE_BROADCAST));
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();
                            params.put("name", name);
                            return params;
                        }
                    };
                    MySingleton.getmInstance(context).addToRequestQue(stringRequest);
                }
            }
            //dbHelper.close();
        }
    }

    public boolean checkNetworkConnection(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}
