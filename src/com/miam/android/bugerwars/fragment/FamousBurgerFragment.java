package com.miam.android.bugerwars.fragment;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.miam.android.bugerwars.BurgerMenuActivity;
import com.miam.android.bugerwars.R;
import com.miam.android.bugerwars.task.BuyBurgerTask;
import com.miam.android.bugerwars.utils.ShadowedTransformation;
import com.miam.android.bugerwars.utils.Utils;
import com.squareup.picasso.Picasso;

/**
 * Fragment for display the famous burger data in the view pager.
 * It is displayed as an image with the title and a buy button. 
 * @author Julien Salvi
 *
 */
public class FamousBurgerFragment extends Fragment implements OnClickListener {
	
	//UI references.
	private ImageView imageBurger;
	private TextView textBurgerName, textVegie;
	private Button buttonBuy;
	
	//JSON references
	private JSONObject burgerObject;
	
	//AsyncTask reference
	private BuyBurgerTask buyTask;

	/**
	 * New instance for this fragment
	 * @return
	 */
	public static FamousBurgerFragment newInstance() {
		FamousBurgerFragment fragment = new FamousBurgerFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * Default constructor
	 */
	public FamousBurgerFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			try {
				burgerObject = new JSONObject(getArguments().getString("famous_data", "{}"));
			} catch (JSONException e) {
				burgerObject = null;
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_famous_burger, container, false);
		
		//Setup the UI
		imageBurger = (ImageView) view.findViewById(R.id.image_famous_burger);
		imageBurger.setOnClickListener(this);
		textBurgerName = (TextView) view.findViewById(R.id.text_famous_burger_name);
		textVegie = (TextView) view.findViewById(R.id.text_vegie_indicator);
		buttonBuy = (Button) view.findViewById(R.id.button_buy_famouos_burger);
		
		//Setup UI with burger data
		Typeface tfBName = Typeface.createFromAsset(view.getContext().getAssets(), "fonts/Roboto-Thin.ttf");
		textBurgerName.setTypeface(tfBName);
		if (Utils.isOnline(getActivity())) {
			//Load the image
			Picasso
				.with(getActivity())
				.load("http://coffeeport.herokuapp.com"+burgerObject.optString("image"))
				.error(R.drawable.offline_promoted_burger_big)
				.transform(new ShadowedTransformation())
				.into(imageBurger);
			
			//Display name, price and if it vegan or not
			textBurgerName.setText(burgerObject.optString("name", "no title"));
			if (burgerObject.optBoolean("vegetarian", false)) {
				textVegie.setVisibility(View.VISIBLE);
			} else {
				textVegie.setVisibility(View.INVISIBLE);
			}
			buttonBuy.setText("B "+burgerObject.optInt("bitcoin", 0));
			buttonBuy.setOnClickListener(this);
		} else {
			//Retrieve bitmap from the cache
			Bitmap bitmap = Utils.decodeStringToBitmap(burgerObject.optString("bmpEncoded"));
			imageBurger.setImageBitmap(Utils.getShadowedBitmap(bitmap));
			
			//Display name, price and if it vegan or not
			textBurgerName.setText(burgerObject.optString("name", "no title"));
			if (burgerObject.optBoolean("vegetarian", false)) {
				textVegie.setVisibility(View.VISIBLE);
			} else {
				textVegie.setVisibility(View.INVISIBLE);
			}
			buttonBuy.setText("B "+burgerObject.optInt("bitcoin", 0));
			buttonBuy.setOnClickListener(this);
		}
		
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_buy_famouos_burger:
			if (Utils.isOnline(getActivity())) {
				if (buyTask != null) {
					return;
				} else {
					//Build JSON body
					JSONObject body = new JSONObject();
					try {
						body.put("id", burgerObject.optInt("id", -1));
						body.put("bitcoin", burgerObject.optInt("bitcoin", 0));
						buyTask = new BuyBurgerTask(FamousBurgerFragment.this);
						buyTask.execute(body.toString());
						((BurgerMenuActivity) getActivity()).showSpinner(true);
					} catch (Exception e) {
						Toast.makeText(getActivity(), "Unable to buy this burger.", Toast.LENGTH_SHORT).show();
					}
				}
			} else {
				Toast.makeText(getActivity(), "You are offline. You cannot buy this burger.", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.image_famous_burger:
			//Display burger note thanks to an alert dialog
			AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
			alert.setTitle("Note");
			alert.setMessage(burgerObject.optString("notes", "no note available."));

			alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// Close dialog.
				}
			});
			
			alert.show();
			break;
		}
	}

	/**
	 * Get the buy AsyncTask
	 * @return The current buy AsyncTask
	 */
	public BuyBurgerTask getBuyTask() {
		return buyTask;
	}

	/**
	 * Set the buy AsyncTask
	 * @param buyTask New buy AsyncTask
	 */
	public void setBuyTask(BuyBurgerTask buyTask) {
		this.buyTask = buyTask;
	}

}
