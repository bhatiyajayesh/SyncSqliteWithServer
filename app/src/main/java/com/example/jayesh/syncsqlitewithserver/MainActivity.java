package com.example.jayesh.syncsqlitewithserver;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText edtName;
    RecyclerView.LayoutManager layoutManager;
    RecycleviewAdapter recycleviewAdapter;
    ArrayList<Contact> contactArrayList = new ArrayList<>();
    BroadcastReceiver broadcastReceiver;
    ProgressDialog pd;

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
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                readFromLocalStorage();
            }
        };
    }

    public void submitName(View view) {
        String name = edtName.getText().toString();
        if(name.length()>0) {
            saveToAppServer(name);
        }
        else
        {
            Toast.makeText(MainActivity.this,"Please Enter Name",Toast.LENGTH_LONG).show();
        }
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

            pd=new ProgressDialog(MainActivity.this);
            pd.setMessage("Please Wait...");
            pd.show();
            Retrofit retrofit = new Retrofit.Builder().baseUrl(DbContact.SERVER_URI).addConverterFactory(GsonConverterFactory.create()).build();
            RetrofitObjectApi service = retrofit.create(RetrofitObjectApi.class);
            Call<AddPostResponse> call = service.addPost(edtName.getText().toString());

            call.enqueue(new Callback<AddPostResponse>() {
                @Override
                public void onResponse(Call<AddPostResponse> call, Response<AddPostResponse> response) {
                    pd.dismiss();
                    if (response != null && response.body() != null) {
                        AddPostResponse addPostResponse = response.body();
                        String Response = addPostResponse.getResponse();
                        if (Response.equals("OK")) {
                            saveToLocalStorage(name, DbContact.SYNC_STATUS_OK);
                        } else {
                            saveToLocalStorage(name, DbContact.SYNC_STATUS_FAILED);
                        }
                    }
                }

                @Override
                public void onFailure(Call<AddPostResponse> call, Throwable t) {
                    pd.dismiss();
                    saveToLocalStorage(name, DbContact.SYNC_STATUS_FAILED);
                }
            });


        } else {
            saveToLocalStorage(name, DbContact.SYNC_STATUS_FAILED);
        }

    }

    private void saveToLocalStorage(String name, int synct) {
        DbHelper dbHelper = new DbHelper(this);
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();
        dbHelper.saveToLocalDatabase(name, synct, sqLiteDatabase);
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
        registerReceiver(broadcastReceiver, new IntentFilter(DbContact.UI_UPDATE_BROADCAST));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }
}
