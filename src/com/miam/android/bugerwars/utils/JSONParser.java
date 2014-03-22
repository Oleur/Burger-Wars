package com.miam.android.bugerwars.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;

/**
 * JSON Parser in order to get the response JSON Object from a GET/POST HTTP request.
 * @author Julien Salvi
 */
public class JSONParser {
	
	//Parser references
	private Context context;
	private JSONObject jObj = null;
	private InputStream is = null;
	private String json = "";
	private OkHttpClient client;
	
	/**
	 * Default constructor with the current context.
	 * @param _context Current context.
	 */
	public JSONParser(Context _context) {
		context = _context;
		client = new OkHttpClient();
	}
	
	/**
	 * Fast GET HTTP request thanks to the OkHttp library.
	 * @param url URL as a string.
	 * @return
	 * @throws IOException
	 */
	public JSONObject fastGetRequest(String url) throws IOException {
		SSLContext sslContext = null;
	    try {
	    	sslContext = SSLContext.getInstance("TLS");
	        sslContext.init(null, null, null);
	    } catch (GeneralSecurityException e) {
	    	e.printStackTrace();
	    }
	    client.setSslSocketFactory(sslContext.getSocketFactory());
	    HttpURLConnection connection = client.open(new URL(url));
	    connection.setUseCaches(true);
	    InputStream in = null;
	    try {
	    	// Read the response.
	    	in = connection.getInputStream();
	    	byte[] response = readFully(in);
	    	json = new String(response, "UTF-8");
	    	return new JSONObject(json);
	    } catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			if (in != null) in.close();
	    }
		return null;
	}
	
	/**
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	private byte[] readFully(InputStream in) throws IOException {
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    byte[] buffer = new byte[1024];
	    for (int count; (count = in.read(buffer)) != -1; ) {
	    	out.write(buffer, 0, count);
	    }
	    return out.toByteArray();
	}
	
	/**
	 * Get a JSON file thanks to a HTTP POST request.
	 * @param url URL to make the HTTP POST request.
	 * @param body JSON body for the HTTP POST request.
	 * @return A JSON file as a reponse to the HTTP POST request.
	 */
	public JSONObject getJSONFromPostURL(String url, String body) {
		try {
            //Default HttpClient
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            StringEntity entity = new StringEntity(body, HTTP.UTF_8);
            
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();           

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error while parsing data " + e.toString());
        }

        // return JSON String
        return jObj;
	}
	
	/**
	 * Return a JSON file that contains the user data.
	 * @return A JSON Object.
	 */
    public JSONArray fileContentToJSON(String filename) {
		StringBuffer fileContent = null;
		try {
			FileInputStream fis = context.openFileInput(filename);
			fis.read();
			fileContent = new StringBuffer("");
			byte[] buffer = new byte[1024];
			while (fis.read(buffer) != -1) {
			    fileContent.append(new String(buffer));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		JSONArray jArray = null;
		try {
            jArray = new JSONArray("["+fileContent.toString());
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error while parsing data " + e.toString());
        }
		
		return jArray;
	}

}
