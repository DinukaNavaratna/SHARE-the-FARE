package com.DinukaNavaratna.SHAREtheFARE;

import android.os.AsyncTask;
import android.util.Log;

import com.DinukaNavaratna.SHAREtheFARE.dataStoring.localDB;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

public class insertData {

    String ServerURL = "" ;
    private List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

    public insertData(String URL, List<NameValuePair> nameValuePairs1){
        this.nameValuePairs = nameValuePairs1;
        this.ServerURL = URL;
        InsertData();
    }

    public void InsertData(){

        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                try {

// ---------------------HTTP---------------------------------
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(ServerURL);
// Url Encoding the POST parameters
                    try {
                        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    }
                    catch (UnsupportedEncodingException e) {
                        // writing error to Log
                        e.printStackTrace();
                    }
                    try {
                        HttpResponse response = httpClient.execute(httpPost);

                        // writing response to log
                        Log.d("Http Response:", response.toString());

                    } catch (ClientProtocolException e) {
                        // writing exception to log
                        e.printStackTrace();

                    } catch (IOException e) {
                        // writing exception to log
                        e.printStackTrace();
                    }
// ---------------------HTTP---------------------------------


// ---------------------HTTPS---------------------------------

//                    HostnameVerifier hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
//                    DefaultHttpClient client = new DefaultHttpClient();
//
//                    Log.i("Insert", "1");
//
//                    SchemeRegistry registry = new SchemeRegistry();
//                    SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
//                    socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
//                    registry.register(new Scheme("https", socketFactory, 443));
//                    SingleClientConnManager mgr = new SingleClientConnManager(client.getParams(), registry);
//                    DefaultHttpClient httpClient = new DefaultHttpClient(mgr, client.getParams());
//
//                    Log.i("Insert", "2");
//
//                    // Set verifier
//
//                    HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
//                    Log.i("Insert", "2.1");
//                    HttpPost httpPost = new HttpPost(ServerURL);
//                    Log.i("Insert", "2.2");
//                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//                    Log.i("Insert", "2.3");
//                    HttpResponse httpResponse = httpClient.execute(httpPost);
//                    Log.i("Insert", "2.4");
//                    HttpEntity httpEntity = httpResponse.getEntity();
//                    Log.i("Insert", "2.5");
//                    Log.i("Insert Response", httpEntity.toString());
//                    Log.i("Insert", "3");
//
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                    Log.d("Ex1", e.toString());
//                } catch (ClientProtocolException e) {
//                    Log.d("Ex2", e.toString());

// ---------------------HTTPS---------------------------------
                } catch (Exception e) {
                    Log.d("Ex3", e.toString());
                }
                Log.i("Insert", "4");

                return "Data Inserted Successfully";

            }

            @Override
            protected void onPostExecute(String result) {

                Log.i("Insert", "5");

                super.onPostExecute(result);

                final String[] tables = {"requests"};
                com.DinukaNavaratna.SHAREtheFARE.dataStoring.localDB localdb = new localDB(appBase.getAppContext());
                localdb.getAllData(tables);

                Log.i("Insert", "6");

            }
        }

        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();

        sendPostReqAsyncTask.execute();

        Log.i("Insert", "7");

    }
}
