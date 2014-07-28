package com.geoodk.collect.android.activities;

import com.geoodk.collect.android.R;
import com.geoodk.collect.android.R.layout;
import com.geoodk.collect.android.application.Collect;
import com.geoodk.collect.android.preferences.AdminPreferencesActivity;
import com.geoodk.collect.android.preferences.MapSettings;
import com.geoodk.collect.android.preferences.PreferencesActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MainSettingsActivity extends Activity implements OnItemClickListener  {
	ListView listView;
	final static int form_settings_id = 0;
	final static int general_settings_id = 1;
	final static int admin_settings_id = 2;
	final static int map_settings_id = 3;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_settings_layout);
		setTitle(getString(R.string.app_name) + " > Settings"); // Setting title of the action
		listView = (ListView) findViewById(R.id.mapSettingsList);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> adpater, View view, int position,
			long id) {
		launchSettingActivity(position);
		
		
	}

	private void launchSettingActivity(int position) {
		// TODO Auto-generated method stub
		Collect.getInstance().getActivityLogger().logAction(this, "SettingsClicked", "click");
		Intent i = null;
		//Toast.makeText(this,position +" ", Toast.LENGTH_SHORT).show();
		if (position == 0){
			i = new Intent(getApplicationContext(),FormDownloadList.class);
		}else if(position == 1){
			i = new Intent(getApplicationContext(),PreferencesActivity.class);
		}else if (position ==2){
			i = new Intent(getApplicationContext(),AdminPreferencesActivity.class);
		}else{
			// It is 3
			i = new Intent(getApplicationContext(),MapSettings.class);
		}
		startActivity(i);
		
	}
}
