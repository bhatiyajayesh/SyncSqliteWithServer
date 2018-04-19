package com.example.jayesh.syncsqlitewithserver;

/**
 * Created by Jayesh on 19-Apr-18.
 */

public class DbContact {

    public static final int SYNC_STATUS_OK = 0;
    public static final int SYNC_STATUS_FAILED = 1;
    public static final String SERVER_URI="http://192.168.1.107/syncdemo/syncinfo.php";
    public static final String UI_UPDATE_BROADCAST="com.example.synctest.uiupdatebroadcast";

    public static final String DATABASE_NAME="contactdb";
    public static final String TABLE_NAME="contactinfo";
    public static final String NAME="name";
    public static final String SYNC_STATUS="syncstatus";

}
