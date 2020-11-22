package com.DinukaNavaratna.SHAREtheFARE.dataStoring;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.DinukaNavaratna.SHAREtheFARE.appBase;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

public class localDB {

    private Context context = appBase.getAppContext();
    private SQLiteDatabase mydatabase;
    private Context activityContext;

    //final String[] tables = {"159753123", "159753456", "159753789", "159753147", "159753258", "159753369"};

    public localDB(Context activityContext){
        this.activityContext = activityContext;
        try {
            mydatabase = context.openOrCreateDatabase("LocalDB", MODE_PRIVATE, null);
            mydatabase.execSQL("CREATE TABLE IF NOT EXISTS requests(id INTEGER PRIMARY KEY, name TEXT(500), mobile TEXT(20), passcode TEXT(32), donation_count INTEGER, timestamp TEXT(20), lat REAL, lng REAL, comments TEXT(1000));");
            mydatabase.execSQL("CREATE TABLE IF NOT EXISTS donations(id INTEGER PRIMARY KEY, request_id INTEGER(5), timestamp TEXT(20), food TEXT(1000));");
            mydatabase.execSQL("CREATE TABLE IF NOT EXISTS donators(id INTEGER PRIMARY KEY, f_name TEXT(100), l_name TEXT(250), mobile TEXT(20), email TEXT(250), city TEXT(250), lat REAL, lng REAL);");
        } catch (Exception x){
        }
    }

    public void getAllData(final String[] tables) {
        mydatabase = context.openOrCreateDatabase("LocalDB", MODE_PRIVATE, null);
        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS requests(id INTEGER PRIMARY KEY, name TEXT(500), mobile TEXT(20), passcode TEXT(32), donation_count INTEGER, timestamp TEXT(20), lat REAL, lng REAL, comments TEXT(1000));");

        for (final String table : tables) {
            Log.i("Table", table);
            final String URL_PAYMENTS = "https://infotechdesigners.com/SHARE%20the%20FARE/readAllData.php?dbName=infotech_SHARE_the_FARE&code="+table;

            StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_PAYMENTS,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONArray array = new JSONArray(response);

                                if(table.equals("requests")) {
                                    mydatabase.execSQL("DELETE FROM requests;");
                                    //mydatabase.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE name='requests';");
                                } else if(table.equals("donators")) {
                                    mydatabase.execSQL("DELETE FROM donators;");
                                    mydatabase.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE name='donators';");
                                } else if(table.equals("donations")) {
                                    mydatabase.execSQL("DELETE FROM donations;");
                                    mydatabase.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE name='donations';");
                                }

                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject value = array.getJSONObject(i);

                                    if(table.equals("requests")) {
                                        mydatabase.execSQL("INSERT INTO requests VALUES(" +
                                                "'"+value.getInt("id")+"'," +
                                                "'"+value.getString("name")+"'," +
                                                "'"+value.getString("mobile")+"'," +
                                                "'"+value.getString("passcode")+"'," +
                                                ""+value.getInt("donation_count")+"," +
                                                "'"+value.getString("timestamp")+"'," +
                                                ""+value.getString("lat")+"," +
                                                ""+value.getString("lng")+"," +
                                                "'"+value.getString("comments")+"'" +
                                                ");");

                                        Log.i("LocalDB", value.getString("name"));
                                    } /*else if(table.equals("donators")) {
                                        mydatabase.execSQL("INSERT INTO donators VALUES(" +
                                                ""+value.getInt("id")+"," +
                                                "'"+value.getString("nic")+"'," +
                                                "'"+value.getString("name")+"'," +
                                                "'"+value.getString("date")+"'," +
                                                "'"+value.getString("address")+"'," +
                                                "'"+value.getString("city")+"'," +
                                                "'"+value.getString("user")+"'," +
                                                "'"+value.getInt("purchased")+"'," +
                                                "'"+value.getString("primary_key")+"'" +
                                                ");");
                                    } else if(table.equals("donations")) {
                                        mydatabase.execSQL("INSERT INTO donations VALUES(" +
                                                "'"+value.getString("customers_primary")+"'," +
                                                "'"+value.getString("num")+"'" +
                                                ");");
                                    }*/
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.i("DBException", e.toString());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    });

            //adding our stringrequest to queue
            try {
                Volley.newRequestQueue(context).add(stringRequest);
            } catch (Exception x){}
        }

    }
}
