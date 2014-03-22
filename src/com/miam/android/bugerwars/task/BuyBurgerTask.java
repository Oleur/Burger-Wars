package com.miam.android.bugerwars.task;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.Toast;

import com.miam.android.bugerwars.BurgerMenuActivity;
import com.miam.android.bugerwars.adapter.BurgerListAdapter;
import com.miam.android.bugerwars.fragment.FamousBurgerFragment;
import com.miam.android.bugerwars.utils.JSONParser;
import com.miam.android.bugerwars.utils.Utils;

/**
 * AsyncTask in order to buy a burger from the menu.
 * Buying a burger is made by make a POST HTTP request.
 * @author Julien Salvi
 *
 */
public class BuyBurgerTask extends AsyncTask<String, Void, Boolean> {
	
	//UI references
	private Context context;
	private BurgerListAdapter adapter;
	private FamousBurgerFragment famousFrag;
	
	//JSON references
	private JSONParser parser;
	private JSONObject buyResponse;

	/**
	 * Constructor with the current context and the list adapter
	 * @param c Current context
	 * @param burgerListAdapter List adapter
	 */
	public BuyBurgerTask(Context c, BurgerListAdapter burgerListAdapter) {
		adapter = burgerListAdapter;
		context = c;
		parser = new JSONParser(c);
	}
	
	/**
	 * Constructor with the famous burger fragment
	 * @param famFrag Famous burger fragment
	 */
	public BuyBurgerTask(FamousBurgerFragment famFrag) {
		famousFrag = famFrag;
		context = famFrag.getActivity();
		parser = new JSONParser(famFrag.getActivity());
	}

	@Override
	protected Boolean doInBackground(String... params) {
		try {
			buyResponse = parser.getJSONFromPostURL(Utils.URL_INTERGALACTIC_BURGER, params[0]);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	@Override
	protected void onPostExecute(Boolean success) {
		if (success) {
			if (adapter != null) {
				((BurgerMenuActivity) context).showSpinner(false);
				adapter.setBuyTask(null);
			}
			if (famousFrag != null) {
				((BurgerMenuActivity) context).showSpinner(false);
				famousFrag.setBuyTask(null);
			}
			//Display dialog if order successful
			if (buyResponse.has("message")) {
				displaySuccessDialog(context, buyResponse.optString("message", "no message"));
			}
		} else {
			if (context != null) {
				((BurgerMenuActivity) context).showSpinner(false);
				Toast.makeText(context, "Unable to buy this burger. Sorry!", Toast.LENGTH_SHORT).show();
			}	
		}
	}
	
	/**
	 * Display a dialog for notifying the user that the burger has been successfully ordered.
	 * @param c Current context
	 * @param message Message to be displayed
	 */
	private void displaySuccessDialog(Context c, String message) {
		//Display burger note thanks to an alert dialog
		AlertDialog.Builder alert = new AlertDialog.Builder(c);
		alert.setTitle("Congratulation!");
		alert.setMessage(message);

		alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Canceled.
			}
		});
		
		alert.show();
	}

}
