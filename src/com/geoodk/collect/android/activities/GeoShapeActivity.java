package com.geoodk.collect.android.activities;

import com.geoodk.collect.android.R;
import com.geoodk.collect.android.R.layout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class GeoShapeActivity extends Activity {
	String point1;
	String point2;
	String point3;
	String point4;
	public String final_return_string;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.geo_shape_layout);
		Button return_button = (Button) findViewById(R.id.geoshape_Button);
		
		
		
		point1 = "36.15524344399181 -81.80068351328373 0.0 0.0;";
		point2 = "37.092621633465136 -98.3268303796649 0.0 0.0;";
		point3 = "31.962375302600794 -92.09862772375345 0.0 0.0;";
		point4 = "36.15524344399181 -81.80068351328373 0.0 0.0;";
		
		final_return_string = point1+point2+point3;
		//final_return_string = point1;
		return_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				returnLocation();
				
			}
		});
		
	}
	
    private void returnLocation() {
            Intent i = new Intent();
            i.putExtra(
                FormEntryActivity.GEOSHAPE_RESULTS,
                final_return_string);
            setResult(RESULT_OK, i);
        finish();
    }
}
