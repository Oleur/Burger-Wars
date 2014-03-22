package com.miam.android.bugerwars;

import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.miam.android.bugerwars.adapter.BurgerListAdapter;
import com.miam.android.bugerwars.fragment.FamousBurgerFragment;
import com.miam.android.bugerwars.utils.JSONParser;
import com.miam.android.bugerwars.utils.Utils;
import com.slidingmenu.lib.SlidingMenu;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.Picasso;

/**
 * Burger menu where a user can view and/or order any burger he wants to eat
 * if he has enough money and if he is really hungry !
 * @author Julien Salvi
 *
 */
public class BurgerMenuActivity extends Activity implements OnItemClickListener {
	
	//UI references
	private ListView burgerList;
	private BurgerListAdapter burgerAdapter;
	private ProgressBar progressBurger;
	private ImageView fakeBackground;
	private SlidingMenu slidingMenu;
	
	//Image Loader references
	private Picasso picasso;
	
	//Pager references.
	private SectionsPagerAdapter sectionsPagerAdapter;
	private ViewPager pager;
	private List<Fragment> pagerFrags;
	
	//JSON references
	private JSONParser parser;
	private JSONObject burgerObject;
	private JSONArray famousBurgerArray;
	private JSONArray classicBurgerArray;
	
	//Preferences reference
	private SharedPreferences prefs;
	
	//AsyncTask reference
	private LoadBurgerTask burgerTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_burger_menu);
		
		//Init lists and arrays
		pagerFrags = new ArrayList<Fragment>();
		famousBurgerArray = new JSONArray();
		classicBurgerArray = new JSONArray();
		
		//In/Out animation.
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		
		//Setup action bar
		getActionBar().setTitle("A Burger for a soldier!");
		getActionBar().setHomeButtonEnabled(true);
		
		//Setup JSON Parser and picasso loader
		parser = new JSONParser(this);
		picasso = Picasso.with(this);
		
		//Setup loading UI
		progressBurger = (ProgressBar) findViewById(R.id.progressBar_load_burgers);
		fakeBackground = (ImageView) findViewById(R.id.image_fake_background);
		
		//Setup the other UI components.
		burgerList = (ListView) findViewById(R.id.list_burgers);
		burgerList.setFastScrollEnabled(false);
		burgerList.setOnItemClickListener(this);
		pager = (ViewPager) findViewById(R.id.pager_famous_burger);
		
		//Setup the sliding menu.
		slidingMenu = new SlidingMenu(this);
		slidingMenu.setMode(SlidingMenu.LEFT);
		slidingMenu.setBackgroundColor(Color.WHITE);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		slidingMenu.setFadeDegree(0.7f);
        slidingMenu.setBehindOffset(Utils.dpToPx(this, 115));
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        slidingMenu.setMenu(R.layout.layout_sliding_menu);
        
        //Round the profile picture
        Bitmap avatarBmp = BitmapFactory.decodeResource(getResources(), R.drawable.avatar_default);
        ((ImageView) slidingMenu.findViewById(R.id.image_profile)).setImageBitmap(Utils.getRoundedBitmap(avatarBmp));
        
        //Setup preferences.
        prefs = getSharedPreferences(Utils.PREFS_NAME, Activity.MODE_PRIVATE);
		boolean cached = prefs.getBoolean("cached", false);
		
		//Load the burgers
		if (Utils.isOnline(this)) {
			//Set handler factory for avoiding SIGNAL 11 failure if online
			try {
				URL.setURLStreamHandlerFactory(new OkHttpClient());
			} catch (Error e) {
				Log.i("INFO", "Factory already set.");
			} catch (Exception e) {
				Log.i("INFO", "Factory already set.");
			}
			
			//Load data and populate the list.
			loadList();
		} else {
			//Check if the data is cached or not
			if (cached) {
				//Setup the list with its adapter
				burgerAdapter = new BurgerListAdapter(this, parser.fileContentToJSON("classic_burgers"));
				burgerList.setAdapter(burgerAdapter);
				burgerAdapter.notifyDataSetChanged();
				
				//Setup the View pager with its adapter.
				try {
					famousBurgerArray = parser.fileContentToJSON("famous_burgers");
					int len = famousBurgerArray.length();
					for (int i = 0; i < len; i++) {
						//Setup fragment for the view pager
						Bundle arguments = new Bundle();
						arguments.putString("famous_data", famousBurgerArray.getJSONObject(i).toString());
						FamousBurgerFragment fragment = new FamousBurgerFragment();
						fragment.setArguments(arguments);
						if (fragment != null)
							pagerFrags.add(fragment);
					}
					sectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
					pager.setAdapter(sectionsPagerAdapter);
				} catch (Exception e) {
					Toast.makeText(this, "Fail to load the famous burgers.", Toast.LENGTH_SHORT).show();
				}
				
				//Hide progress view
				showProgress(false);
			} else {
				//Hide progress view and notify the user
				showProgress(false);
				Toast.makeText(this, "No data cached.", Toast.LENGTH_LONG).show();
			}
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			//Display the sliding menu
			slidingMenu.toggle(true);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Show the loading view.
	 * @param show True to show, false otherwise.
	 */
	private void showProgress(boolean show) {
		Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out_fast);
		if (show) {
			fakeBackground.setVisibility(View.VISIBLE);
			progressBurger.setVisibility(View.VISIBLE);
		} else {
			fakeBackground.startAnimation(fadeOut);
			progressBurger.startAnimation(fadeOut);
			fakeBackground.setVisibility(View.GONE);
			progressBurger.setVisibility(View.GONE);
		}
	}
	
	/**
	 * Show the spinner while buying the recipe
	 * @param show True if shown, false otherwise.
	 */
	public void showSpinner(boolean show) {
		if (show) {
			progressBurger.setVisibility(View.VISIBLE);
		} else {
			progressBurger.setVisibility(View.GONE);
		}
	}
	
	/**
	 * Load the burgers data by executing a AsyncTask to communicate with the API
	 */
	private void loadList() {
		if (burgerTask != null) {
			return;
		} else {
			try {
				burgerTask = new LoadBurgerTask();
				burgerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			} catch (Exception e) {
				Toast.makeText(this, "Unable to load burgers...", Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		//Display burger note thanks to an alert dialog
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Note");
		alert.setMessage(burgerAdapter.getItem(position).optString("notes", "no note available."));

		alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Canceled.
			}
		});
		
		alert.show();
	}
	
	/**
	 * Pager adapter in order to display and swipe between famous fragments
	 * @author Julien Salvi
	 *
	 */
	private class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			if (pagerFrags.get(position) != null) {
				return pagerFrags.get(position);
			} else {
				return null;
			}
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			super.destroyItem(container, position, object);
		}

		@Override
		public int getCount() {	
			return pagerFrags.size();
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			return "recipeFrag";
		}
	}
	
	/**
	 * Load burgers from the servers and split them in two array: famous and classic burgers.
	 * Then cache the differents array in order to be able to display the data offline.
	 * @author Julien Salvi
	 *
	 */
	private class LoadBurgerTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				burgerObject = parser.fastGetRequest(Utils.URL_INTERGALACTIC_BURGER);
				int len = burgerObject.getJSONArray("burgers").length();
				for (int i = 0; i < len; i++) {
					if (burgerObject.getJSONArray("burgers").getJSONObject(i).optBoolean("promoted", false)) {
						//Get bitmap from URL and store it as an encoded string
						String encodedBmp = Utils.bitmapToEncodedString(
								picasso.load("http://coffeeport.herokuapp.com"
										+burgerObject.getJSONArray("burgers").getJSONObject(i).optString("image")).get());
						burgerObject.getJSONArray("burgers").getJSONObject(i).put("bmpEncoded", encodedBmp);
						//Add the burger to the respective array.
						famousBurgerArray.put(burgerObject.getJSONArray("burgers").getJSONObject(i));
						//Setup fragment for the view pager
						Bundle arguments = new Bundle();
						arguments.putString("famous_data", burgerObject.getJSONArray("burgers").getJSONObject(i).toString());
						FamousBurgerFragment fragment = new FamousBurgerFragment();
						fragment.setArguments(arguments);
						if (fragment != null)
							pagerFrags.add(fragment);
					} else {
						//Get bitmap from URL and store it as an encoded string
						String encodedBmp = Utils.bitmapToEncodedString(
								picasso.load("http://coffeeport.herokuapp.com"
										+burgerObject.getJSONArray("burgers").getJSONObject(i).optString("image")).get());
						burgerObject.getJSONArray("burgers").getJSONObject(i).put("bmpEncoded", encodedBmp);
						classicBurgerArray.put(burgerObject.getJSONArray("burgers").getJSONObject(i));
					}
				}
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		
		@Override
		protected void onPostExecute(Boolean success) {
			burgerTask = null;
			if (success) {				
				//Update the UI
				try {
					//Write response in files
					FileOutputStream fosFamous = openFileOutput("famous_burgers", Context.MODE_PRIVATE);
					fosFamous.write(famousBurgerArray.toString().getBytes());
					fosFamous.close();
					FileOutputStream fosClassic = openFileOutput("classic_burgers", Context.MODE_PRIVATE);
					fosClassic.write(classicBurgerArray.toString().getBytes());
					fosClassic.close();
					
					//Update preferences
					prefs.edit().putBoolean("cached", true).commit();
					
					//Setup the list with its adapter
					burgerAdapter = new BurgerListAdapter(BurgerMenuActivity.this, classicBurgerArray);
					burgerList.setAdapter(burgerAdapter);
					burgerAdapter.notifyDataSetChanged();
					
					//Setup the View pager with its adapter.
					sectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
					pager.setAdapter(sectionsPagerAdapter);
				} catch (Exception e) {
					e.printStackTrace();
				}
				//Hide the progress bar
				showProgress(false);
			} else {
				if (BurgerMenuActivity.this != null) {
					showProgress(false);
					Toast.makeText(BurgerMenuActivity.this, "Fail to load the burgers.", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}
}
