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

/**
 * GeoPointWidget is the widget that allows the user to get GPS readings.
 *
 * @author Jon Nordling (jonnordling@gmail.com)
 */
public class GeoShapeWidget extends QuestionWidget implements IBinaryWidget {
	private Button mGetLocationButton;
	private Button mViewButton;

	private TextView mStringAnswer;
	private TextView mAnswerDisplay;

	public GeoShapeWidget(Context context, FormEntryPrompt prompt) {
		super(context, prompt);
		// assemble the widget...
		setOrientation(LinearLayout.VERTICAL);
		TableLayout.LayoutParams params = new TableLayout.LayoutParams();
		params.setMargins(7, 5, 7, 5);

		mStringAnswer = new TextView(getContext());
		mStringAnswer.setId(QuestionWidget.newUniqueId());

		//mAnswerDisplay = new TextView(getContext());
		//mAnswerDisplay.setId(QuestionWidget.newUniqueId());
		//mAnswerDisplay.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
		//mAnswerDisplay.setGravity(Gravity.CENTER);

		// setup play button
		mViewButton = new Button(getContext());
		mViewButton.setId(QuestionWidget.newUniqueId());
		mViewButton.setText(getContext().getString(R.string.record_geoshape));
		mViewButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
		mViewButton.setPadding(20, 20, 20, 20);
		mViewButton.setLayoutParams(params);
				// finish complex layout
				// control what gets shown with setVisibility(View.GONE)
				//addView(mGetLocationButton);
		addView(mViewButton);
				//addView(mAnswerDisplay);
	}

	@Override
	public void setBinaryData(Object answer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cancelWaitingForBinaryData() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isWaitingForBinaryData() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IAnswerData getAnswer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clearAnswer() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFocus(Context context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setOnLongClickListener(OnLongClickListener l) {
		// TODO Auto-generated method stub
		
	}

	



}
