package com.geoodk.collect.android.activities;

import org.osmdroid.tileprovider.IRegisterReceiver;

import com.geoodk.collect.android.R;
import com.geoodk.collect.android.R.id;
import com.geoodk.collect.android.R.layout;
import com.geoodk.collect.android.R.menu;


import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class GeoTraceActivity extends Activity implements IRegisterReceiver {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.geotrace_layout);
	}

	
}
