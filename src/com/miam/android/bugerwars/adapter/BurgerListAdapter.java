package com.miam.android.bugerwars.adapter;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
 * List adapter in order to display the classic burgers data
 * @author Julien Salvi
 *
 */
public class BurgerListAdapter extends BaseAdapter {
	
	//Context references
	private Context context;
	private LayoutInflater inflater;
	
	//JSON reference
	private JSONArray burgerArray;
	
	//View holder reference
	private BurgerViewHolder bViewHolder; 
	
	//AsynTask reference
	private BuyBurgerTask buyTask;

	/**
	 * Burger view holder to display burger data
	 * @author Julien Salvi
	 *
	 */
	static class BurgerViewHolder {
		protected TextView textBurgerName, textVegieInd;
		protected ImageView burgerImage;
		protected Button buttonBuy;
	}
	
	/**
	 * Adapter constructor with the current context and the buger JSON array.
	 * @param c Current context
	 * @param bArray Burger JSON array.
	 */
	public BurgerListAdapter(Context c, JSONArray bArray) {
		context = c;
		inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		burgerArray = bArray;
	}

	@Override
	public int getCount() {
		try {
			return burgerArray.length();
		} catch (Exception e) {
			return 0;
		}
	}

	@Override
	public JSONObject getItem(int position) {
		try {
			return burgerArray.getJSONObject(position);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		bViewHolder = null;
		
		if (convertView == null) {
			bViewHolder = new BurgerViewHolder();
			view = inflater.inflate(R.layout.item_list_burger, null);
			bViewHolder.burgerImage = (ImageView) view.findViewById(R.id.image_list_burger);
			bViewHolder.textBurgerName = (TextView) view.findViewById(R.id.text_list_burger_name);
			bViewHolder.textVegieInd = (TextView) view.findViewById(R.id.text_list_vegie_indicator);
			bViewHolder.buttonBuy = (Button) view.findViewById(R.id.button_list_burger_buy);
			view.setTag(bViewHolder);
		} else {
			view = convertView;
			bViewHolder = (BurgerViewHolder)view.getTag();
		}
		
		try {
			if (Utils.isOnline(context)) {
				//load image URL thanks to Picasso and apply and a shadow effect
				Picasso
					.with(context)
					.load("http://coffeeport.herokuapp.com"+burgerArray.getJSONObject(position).optString("image"))
					.error(R.drawable.offline_burger_list)
					.transform(new ShadowedTransformation())
					.into(bViewHolder.burgerImage);
			} else {
				//Retrieve bitmap from the cache
				Bitmap bitmap = Utils.decodeStringToBitmap(burgerArray.getJSONObject(position).optString("bmpEncoded"));
				bViewHolder.burgerImage.setImageBitmap(Utils.getShadowedBitmap(bitmap));
			}
			
			//Burger name
			Typeface tfBurgerName = Typeface.createFromAsset(view.getContext().getAssets(), "fonts/Roboto-Regular.ttf");
			bViewHolder.textBurgerName.setTypeface(tfBurgerName);
			bViewHolder.textBurgerName.setText(Html.fromHtml(burgerArray.getJSONObject(position).optString("name", "no title")));
			if (burgerArray.getJSONObject(position).optBoolean("vegetarian", false)) {
				bViewHolder.textVegieInd.setVisibility(View.VISIBLE);
			} else {
				bViewHolder.textVegieInd.setVisibility(View.INVISIBLE);
			}
			bViewHolder.buttonBuy.setTag(position);
			bViewHolder.buttonBuy.setText("B "+burgerArray.getJSONObject(position).optInt("bitcoin", 0));
			bViewHolder.buttonBuy.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int pos = (Integer) v.getTag();
					if (Utils.isOnline(context)) {
						if (buyTask != null) {
							return;
						} else {
							//Build JSON body
							JSONObject body = new JSONObject();
							try {
								body.put("id", burgerArray.getJSONObject(pos).optInt("id", -1));
								body.put("bitcoin", burgerArray.getJSONObject(pos).optInt("bitcoin", 0));
								buyTask = new BuyBurgerTask(context, BurgerListAdapter.this);
								buyTask.execute(body.toString());
								((BurgerMenuActivity) context).showSpinner(true);
							} catch (Exception e) {
								Toast.makeText(context, "Unable to buy this burger.", Toast.LENGTH_SHORT).show();
							}
						}
					} else {
						Toast.makeText(context, "You are offline. You cannot buy this burger.", Toast.LENGTH_SHORT).show();
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return view;
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
