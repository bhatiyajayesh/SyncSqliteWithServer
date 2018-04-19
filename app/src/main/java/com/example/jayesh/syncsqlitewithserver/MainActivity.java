package com.example.jayesh.syncsqlitewithserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText edtName;
    RecyclerView.LayoutManager layoutManager;
    RecycleviewAdapter recycleviewAdapter;
    ArrayList<Contact> contactArrayList = new ArrayList<>();
    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = (RecyclerView) findViewById(R.id.recycleview);
        edtName = (EditText) findViewById(R.id.edtName);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recycleviewAdapter = new RecycleviewAdapter(contactArrayList);
        recyclerView.setAdapter(recycleviewAdapter);
        readFromLocalStorage();
        broadcastReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                    readFromLocalStorage();
            }
        };
    }

    public void submitName(View view) {
        String name = edtName.getText().toString();
        saveToAppServer(name);
        edtName.setText("");
    }

    private void readFromLocalStorage() {
        contactArrayList.clear();
        DbHelper dbHelper = new DbHelper(this);
        SQLiteDatabase sqLiteDatabase = dbHelper.getReadableDatabase();
        Cursor cursor = dbHelper.readFromLocalDatabase(sqLiteDatabase);

        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(DbContact.NAME));
            int sync_status = cursor.getInt(cursor.getColumnIndex(DbContact.SYNC_STATUS));

            contactArrayList.add(new Contact(name, sync_status));
        }

        recycleviewAdapter.notifyDataSetChanged();
        cursor.close();
        dbHelper.close();

    }

    private void saveToAppServer(final String name) {


        if (checkNetworkConnection()) {

            StringRequest stringRequest=new StringRequest(Request.Method.POST, DbContact.SERVER_URI,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            try {
                                JSONObject jsonObject=new JSONObject(response);
                                String Response=jsonObject.getString("response");
                                Log.d("Response",Response);
                                Toast.makeText(getApplicationContext(),Response,Toast.LENGTH_LONG).show();
                                if(Response.equals("OK"))
                                {
                                    saveToLocalStorage(name,DbContact.SYNC_STATUS_OK);
                                }
                                else
                                {
                                    saveToLocalStorage(name,DbContact.SYNC_STATUS_FAILED);
                                }
                            } catch (JSONException e) {
                                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                                Log.d("Response",e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("Response",error.getMessage());
                    saveToLocalStorage(name,DbContact.SYNC_STATUS_FAILED);
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String,String> params=new HashMap<>();
                    params.put("name",name);
                    return params;
                }
            };
            MySingleton.getmInstance(MainActivity.this).addToRequestQue(stringRequest);

        } else {
            saveToLocalStorage(name,DbContact.SYNC_STATUS_FAILED);
        }

    }

    private void saveToLocalStorage(String name,int synct)
    {
        DbHelper dbHelper = new DbHelper(this);
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();
        dbHelper.saveToLocalDatabase(name,synct, sqLiteDatabase);
        readFromLocalStorage();
        dbHelper.close();
    }

    public boolean checkNetworkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(broadcastReceiver,new IntentFilter(DbContact.UI_UPDATE_BROADCAST));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }
}
