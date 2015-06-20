/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.geoodk.collect.android.widgets;

import java.text.DecimalFormat;

import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;

import com.geoodk.collect.android.R;
import com.geoodk.collect.android.activities.FormEntryActivity;
import com.geoodk.collect.android.activities.GeoPointActivity;
import com.geoodk.collect.android.activities.GeoPointMapActivity;
import com.geoodk.collect.android.activities.GeoPointMapNewActivity;
import com.geoodk.collect.android.activities.GeoPointMapActivitySdk7;
import com.geoodk.collect.android.application.Collect;
import com.geoodk.collect.android.utilities.CompatibilityUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * GeoPointWidget is the widget that allows the user to get GPS readings.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class GeoPointNewWidget extends QuestionWidget implements IBinaryWidget {
	public static final String ACCURACY_THRESHOLD = "accuracyThreshold";
	public static final String READ_ONLY = "readOnly";
	public static final String POINT_LOCATION = "gp";

	public static final double DEFAULT_LOCATION_ACCURACY = 5.0;

	private Button mGetLocationButton;
	private TextView mAnswerDisplay;
	private final boolean mReadOnly;
	private boolean mUseMapsV2;
	private boolean mUseMaps;
	private String mAppearance;
	private double mAccuracyThreshold;

	public GeoPointNewWidget(Context context, FormEntryPrompt prompt) {
		super(context, prompt);
        // assemble the widget...
        setOrientation(LinearLayout.VERTICAL);
        TableLayout.LayoutParams params = new TableLayout.LayoutParams();
        params.setMargins(7, 5, 7, 5);
        mReadOnly = prompt.isReadOnly();

		// Determine the accuracy threshold to use
		String acc = prompt.getQuestion().getAdditionalAttribute(null, ACCURACY_THRESHOLD);
		if ( acc != null && acc.length() != 0 ) {
			mAccuracyThreshold = Double.parseDouble(acc);
		} else {
			mAccuracyThreshold = DEFAULT_LOCATION_ACCURACY;
		}

        setMap(context, prompt);

		mAnswerDisplay = new TextView(getContext());
		mAnswerDisplay.setId(QuestionWidget.newUniqueId());
		mAnswerDisplay.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
		mAnswerDisplay.setGravity(Gravity.CENTER);

        /*
		// on play, launch the appropriate viewer
		mViewButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Collect.getInstance()
						.getActivityLogger()
						.logInstanceAction(this, "showLocation", "click",
								mPrompt.getIndex());

				Intent i;
				if (mUseMapsV2 ) {
					i = new Intent(getContext(), GeoPointMapActivity.class);
				} else {
					i = new Intent(getContext(), GeoPointMapActivitySdk7.class);
				}

				String s = mStringAnswer.getText().toString();
				if ( s.length() != 0 ) {
					String[] sa = s.split(" ");
					double gp[] = new double[4];
					gp[0] = Double.valueOf(sa[0]).doubleValue();
					gp[1] = Double.valueOf(sa[1]).doubleValue();
					gp[2] = Double.valueOf(sa[2]).doubleValue();
					gp[3] = Double.valueOf(sa[3]).doubleValue();
					i.putExtra(SHAPE_LOCATION, gp);
				}
				i.putExtra(READ_ONLY, true);
				i.putExtra(ACCURACY_THRESHOLD, mAccuracyThreshold);
				((Activity) getContext()).startActivity(i);

			}
		});
		*/

		mGetLocationButton = new Button(getContext());
		mGetLocationButton.setId(QuestionWidget.newUniqueId());
        mGetLocationButton.setText(getContext().getString(R.string.record_geoshape));
		mGetLocationButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP,mAnswerFontsize);
        mGetLocationButton.setPadding(20, 20, 20, 20);
		//mGetLocationButton.setEnabled(!prompt.isReadOnly());
		mGetLocationButton.setLayoutParams(params);

		// when you press the button
		mGetLocationButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Collect.getInstance()
						.getActivityLogger()
						.logInstanceAction(this, "recordLocation", "click",
								mPrompt.getIndex());
				Intent i = null;
                /*
				if ( mUseMapsV2 ) {
					i = new Intent(getContext(), GeoPointMapActivity.class);
				} else if (mUseMaps) {
					i = new Intent(getContext(), GeoPointMapActivitySdk7.class);
				} else {
					i = new Intent(getContext(), GeoPointActivity.class);
				}
                */
                i = new Intent(getContext(), GeoPointMapNewActivity.class);
				String s = mAnswerDisplay.getText().toString();

				if ( s.length() != 0 ) {
					i.putExtra(GeoPointNewWidget.POINT_LOCATION, s);
				}

				i.putExtra(READ_ONLY, mReadOnly);
				i.putExtra(ACCURACY_THRESHOLD, mAccuracyThreshold);
                Collect.getInstance().getFormController().setIndexWaitingForData(mPrompt.getIndex());
				((Activity) getContext()).startActivityForResult(i,FormEntryActivity.LOCATION_CAPTURE);
			}
		});

		// finish complex layout
		// control what gets shown with setVisibility(View.GONE)
		addView(mGetLocationButton);
		//addView(mStringAnswer);
		addView(mAnswerDisplay);

		// figure out what text and buttons to enable or to show...
		boolean dataAvailable = false;
		String s = prompt.getAnswerText();
		if (s != null && !s.equals("")) {
			//Toast.makeText(getContext(), prompt.getAnswerText()+" ", Toast.LENGTH_LONG).show();
			dataAvailable = true;
			setBinaryData(s);
		}
		updateButtonLabelsAndVisibility(dataAvailable);

	}

	private void updateButtonLabelsAndVisibility(boolean dataAvailable) {
		if (dataAvailable == true){
			// There is already a shape recorded
			mGetLocationButton.setText(getContext().getString(R.string.view_change_location));
		}else{
			mGetLocationButton.setText(getContext().getString(R.string.get_location));
		}
		/*
		// BUT for mapsV2, we only show the mGetLocationButton, altering its text.
		// for maps, we show the view button.
		if ( mUseMapsV2 ) {
			// show the GetLocation button
			mGetLocationButton.setVisibility(View.VISIBLE);
			// hide the view button
			mViewButton.setVisibility(View.GONE);
			if ( mReadOnly ) {
				mGetLocationButton.setText(getContext()
						.getString(R.string.show_location));
			} else {
				mGetLocationButton.setText(getContext()
						.getString(R.string.view_change_location));
			}
		} else {
			// if it is read-only, hide the get-location button...
			if ( mReadOnly ) {
				mGetLocationButton.setVisibility(View.GONE);
			} else {
				mGetLocationButton.setVisibility(View.VISIBLE);
				mGetLocationButton.setText(getContext()
						.getString(dataAvailable ?
							R.string.replace_location : R.string.get_location));
			}

			if (mUseMaps) {
				// show the view button
				mViewButton.setVisibility(View.VISIBLE);
				mViewButton.setEnabled(dataAvailable);
			} else {
				mViewButton.setVisibility(View.GONE);
			}
		}
		*/
	}

	@Override
	public void setBinaryData(Object answer) {
		String s = (String) answer;
		mAnswerDisplay.setText(s);
		Collect.getInstance().getFormController().setIndexWaitingForData(null);
	}

	@Override
	public void cancelWaitingForBinaryData() {
		Collect.getInstance().getFormController().setIndexWaitingForData(null);
	}

	@Override
	public boolean isWaitingForBinaryData() {
		return mPrompt.getIndex().equals(
				Collect.getInstance().getFormController()
						.getIndexWaitingForData());
	}

	@Override
	public IAnswerData getAnswer() {
		//String s = mStringAnswer.getText().toString();
		String s = mAnswerDisplay.getText().toString();
		if (s == null || s.equals("")) {
			return null;
		} else {
			try {
				// segment lat and lon
				String[] sa = s.trim().split(" ");
				double gp[] = new double[4];
				gp[0] = Double.valueOf(sa[0]).doubleValue();
				gp[1] = Double.valueOf(sa[1]).doubleValue();
				gp[2] = Double.valueOf(sa[2]).doubleValue();
				gp[3] = Double.valueOf(sa[3]).doubleValue();

				return new GeoPointData(gp);
			} catch (Exception NumberFormatException) {
				return null;
			}
		}
	}

	@Override
	public void clearAnswer() {
		//mStringAnswer.setText(null);
		mAnswerDisplay.setText(null);
	}


	private String truncateDouble(String s) {
		DecimalFormat df = new DecimalFormat("#.##");
		return df.format(Double.valueOf(s));
	}

	@Override
	public void setFocus(Context context) {
		// Hide the soft keyboard if it's showing.
		InputMethodManager inputManager = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
	}

	@Override
	public void setOnLongClickListener(OnLongClickListener l) {
		/*
		mViewButton.setOnLongClickListener(l);
		*/
		mGetLocationButton.setOnLongClickListener(l);
		//mStringAnswer.setOnLongClickListener(l);
		mAnswerDisplay.setOnLongClickListener(l);

	}

	@Override
	public void cancelLongPress() {
		super.cancelLongPress();
		//mViewButton.cancelLongPress();
		mGetLocationButton.cancelLongPress();
		//mStringAnswer.cancelLongPress();
		mAnswerDisplay.cancelLongPress();
	}

	private void setMap(Context context, FormEntryPrompt prompt){

		// Determine whether or not to use the plain, maps, or mapsV2 activity
		mAppearance = prompt.getAppearanceHint();

		boolean requestV2 = false;
		boolean requestMaps = false;
		if ( mAppearance != null && mAppearance.equalsIgnoreCase("placement-map") ) {
			requestV2 = true;
			requestMaps = true;
		}

		if (mAppearance != null && mAppearance.equalsIgnoreCase("maps")) {
			requestMaps = true;
		}

		// use mapsV2 if it is available and was requested
		mUseMapsV2 = requestV2 && CompatibilityUtils.useMapsV2(context);

		if ( mUseMapsV2 ) {
			// if we are using mapsV2, we are using maps...
			mUseMaps = true;
		} else if ( requestMaps ) {
			// using the legacy maps widget... if MapActivity is available
			// otherwise just use the plain widget
			try {
				// do google maps exist on the device
				Class.forName("com.google.android.maps.MapActivity");
				mUseMaps = true;
			} catch (ClassNotFoundException e) {
				// use the plain geolocation activity
				mUseMaps = false;
			}
		} else {
			// use the plain geolocation activity
			mUseMaps = false;
		}
	}

	private String formatGps(double coordinates, String type) {
		String location = Double.toString(coordinates);
		String degreeSign = "\u00B0";
		String degree = location.substring(0, location.indexOf("."))
				+ degreeSign;
		location = "0." + location.substring(location.indexOf(".") + 1);
		double temp = Double.valueOf(location) * 60;
		location = Double.toString(temp);
		String mins = location.substring(0, location.indexOf(".")) + "'";

		location = "0." + location.substring(location.indexOf(".") + 1);
		temp = Double.valueOf(location) * 60;
		location = Double.toString(temp);
		String secs = location.substring(0, location.indexOf(".")) + '"';
		if (type.equalsIgnoreCase("lon")) {
			if (degree.startsWith("-")) {
				degree = "W " + degree.replace("-", "") + mins + secs;
			} else
				degree = "E " + degree.replace("-", "") + mins + secs;
		} else {
			if (degree.startsWith("-")) {
				degree = "S " + degree.replace("-", "") + mins + secs;
			} else
				degree = "N " + degree.replace("-", "") + mins + secs;
		}
		return degree;
	}

}
