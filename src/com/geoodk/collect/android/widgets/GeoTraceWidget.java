package com.geoodk.collect.android.widgets;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;

import com.geoodk.collect.android.R;
import com.geoodk.collect.android.activities.FormEntryActivity;
import com.geoodk.collect.android.activities.GeoShapeActivity;
import com.geoodk.collect.android.activities.GeoTraceActivity;
import com.geoodk.collect.android.application.Collect;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

/**
 * GeoShapeTrace is the widget that allows the user to get Collect multiple GPS points based on the locations.
 *
 * Date
 * @author Jon Nordling (jonnordling@gmail.com)
 */

public class GeoTraceWidget extends QuestionWidget implements IBinaryWidget {
	
	public static final String ACCURACY_THRESHOLD = "accuracyThreshold";
	public static final String READ_ONLY = "readOnly";
	private final boolean mReadOnly;
	public static final String TRACE_LOCATION = "gp";
	private Button createTraceButton;
	private Button viewShapeButton;

	private TextView mStringAnswer;
	private TextView mAnswerDisplay;

	public GeoTraceWidget(Context context, FormEntryPrompt prompt) {
		super(context, prompt);
		setOrientation(LinearLayout.VERTICAL);
		TableLayout.LayoutParams params = new TableLayout.LayoutParams();
		params.setMargins(7, 5, 7, 5);
		
		mReadOnly = prompt.isReadOnly();
		
		mStringAnswer = new TextView(getContext());
		mStringAnswer.setId(QuestionWidget.newUniqueId());

		mAnswerDisplay = new TextView(getContext());
		mAnswerDisplay.setId(QuestionWidget.newUniqueId());
		mAnswerDisplay.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
		mAnswerDisplay.setGravity(Gravity.CENTER);
		// TODO Auto-generated constructor stub
		
		createTraceButton = new Button(getContext());
		createTraceButton.setId(QuestionWidget.newUniqueId());
		createTraceButton.setText(getContext().getString(R.string.record_trace));
		createTraceButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
		createTraceButton.setPadding(20, 20, 20, 20);
		createTraceButton.setLayoutParams(params);
		
		createTraceButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Collect.getInstance().getFormController().setIndexWaitingForData(mPrompt.getIndex());
				Intent i = null;
				i = new Intent(getContext(), GeoTraceActivity.class);
				String s = mStringAnswer.getText().toString();
				if ( s.length() != 0 ) {
					i.putExtra(TRACE_LOCATION, s);
				}
				//((Activity) getContext()).startActivity(i);
				((Activity) getContext()).startActivityForResult(i,FormEntryActivity.GEOTRACE_CAPTURE);
				
			}
		});
		
		addView(createTraceButton);
		addView(mAnswerDisplay);
		
		boolean dataAvailable = false;
		updateButtonLabelsAndVisibility(dataAvailable);
		
	}
	
	private void updateButtonLabelsAndVisibility(boolean dataAvailable) {
		
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
