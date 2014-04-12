package com.adhikari.liveearth;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

public class EarthLive extends Activity {

	private ProgressDialog pDialog;
	Button save_btn;
	ArrayList<String> countries = new ArrayList<String>();
	JSONArray jsonArr = new JSONArray();
	private static final String TAG = "Main";
	public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
	private ProgressDialog progressBar;
	Bitmap decodedByte;
	ImageView iv;
	Spinner sp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_earth_live);

		sp = (Spinner) findViewById(R.id.spinner);
		iv = (ImageView) findViewById(R.id.image);
		save_btn = (Button) findViewById(R.id.archive);
		save_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String root = Environment.getExternalStorageDirectory().toString();
				File myDir = new File(root + "/liveEarthImages");    
				myDir.mkdirs();
				Random generator = new Random();
				int n = 10000;
				n = generator.nextInt(n);
				String fname = "Image-"+ n +".jpg";
				File file = new File (myDir, fname);
				if (file.exists ()) file.delete (); 
				try {
				       FileOutputStream out = new FileOutputStream(file);
				       decodedByte.compress(Bitmap.CompressFormat.JPEG, 90, out);
				       out.flush();
				       out.close();
				       Toast.makeText(EarthLive.this, "Image Archived", Toast.LENGTH_LONG).show();

				} catch (Exception e) {
				       e.printStackTrace();
				}
			}
		});
		try {
			jsonArr = new JSONArray(loadJSONFromAsset().toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		countries = country_name();
		// Collections.sort(countries);
		sp.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, countries));

		sp.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView,
					View selectedItemView, int position, long id) {
				String selected = parentView.getItemAtPosition(position)
						.toString();
				try {
					JSONObject selected_country = (JSONObject) jsonArr
							.get(countries.indexOf(selected));
					String latitude = selected_country.getString("Latitude");
					String longitude = selected_country.getString("Longitude");
					String ns = selected_country.getString("ns");
					String ew = selected_country.getString("ew");
					DownloadWebPageTask task = new DownloadWebPageTask();
			        task.execute(new String[] { "http://adhikariutils.appspot.com/image?ew="+ew+"&ns="+ns+"&lat="+latitude+"&lon="+longitude });
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}});
			


		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.earth_live, menu);
		return true;
	}
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.archive:
	        //newGame();
	    	Intent intent = new Intent(EarthLive.this,GridViewActivity.class);
	    	startActivity(intent);
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

	public String loadJSONFromAsset() {
		String json = null;
		try {

			InputStream is = getAssets().open("data.json");

			int size = is.available();

			byte[] buffer = new byte[size];

			is.read(buffer);

			is.close();

			json = new String(buffer, "UTF-8");

		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
		return json;

	}

	public ArrayList<String> country_name() {
		ArrayList<String> countries = new ArrayList<String>();

		try {

			for (int i = 0; i < jsonArr.length(); i++) {
				JSONObject c = jsonArr.getJSONObject(i);
				String country = c.getString("Country");
				countries.add(new String(country));
			}

		} catch (JSONException e) {
			e.printStackTrace();

		}
		return countries;

	}

	private class DownloadWebPageTask extends AsyncTask<String, Void, String> {
        @Override
        
        
        protected String doInBackground(String... urls) {
            String response = "";
            for (String url : urls) {
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);
                try {
                    HttpResponse execute = client.execute(httpGet);
                    InputStream content = execute.getEntity().getContent();

                    BufferedReader buffer = new BufferedReader(
                            new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return response;
        }

        @Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			pDialog = new ProgressDialog(EarthLive.this);
			 pDialog.setMessage("Loading Live Image...");
			 pDialog.setCancelable(false); pDialog.show();
		}

		@Override
        protected void onPostExecute(String result) {
            //textView.setText(Html.fromHtml(result));
			if (pDialog.isShowing()) pDialog.dismiss();
			byte[] decodedString = Base64.decode(Html.fromHtml(result)+"", Base64.DEFAULT);
			decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
			iv.setImageBitmap(decodedByte);
        	//Toast.makeText(getApplicationContext(), Html.fromHtml(result), Toast.LENGTH_LONG).show();
        }
    }
	
	public static String now() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(cal.getTime());
		}
}
