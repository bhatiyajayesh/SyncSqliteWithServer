package com.example.jayesh.syncsqlitewithserver;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Jayesh on 19-Apr-18.
 */

public class NetworkMonitor extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.e("Eror", "OnReceive");
        if (checkNetworkConnection(context)) {
            Log.e("Eror", "Network Available");
            final DbHelper dbHelper = new DbHelper(context);
            final SQLiteDatabase sqLiteDatabase = dbHelper.getReadableDatabase();
            Cursor cursor = dbHelper.readFromLocalDatabase(sqLiteDatabase);

            while (cursor.moveToNext()) {
                int sync_status = cursor.getInt(cursor.getColumnIndex(DbContact.SYNC_STATUS));
                if (sync_status == DbContact.SYNC_STATUS_FAILED) {
                    final String name = cursor.getString(cursor.getColumnIndex(DbContact.NAME));
                    Log.e("Eror", "In Iff=>" + name);

                    Retrofit retrofit = new Retrofit.Builder().baseUrl(DbContact.SERVER_URI).addConverterFactory(GsonConverterFactory.create()).build();
                    RetrofitObjectApi service = retrofit.create(RetrofitObjectApi.class);
                    Call<AddPostResponse> call = service.addPost(name);

                    call.enqueue(new Callback<AddPostResponse>() {
                        @Override
                        public void onResponse(Call<AddPostResponse> call, Response<AddPostResponse> response) {
                            if (response != null && response.body() != null) {
                                AddPostResponse addPostResponse = response.body();
                                String Response = addPostResponse.getResponse();
                                if (Response.equals("OK")) {
                                    dbHelper.updateLocalDatabase(name, DbContact.SYNC_STATUS_OK, sqLiteDatabase);
                                    context.sendBroadcast(new Intent(DbContact.UI_UPDATE_BROADCAST));
                                }

                            }
                        }

                        @Override
                        public void onFailure(Call<AddPostResponse> call, Throwable t) {
                        }
                    });


                }
            }
        }
    }

    public boolean checkNetworkConnection(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}
