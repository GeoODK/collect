/*
 * Copyright (C) 2014 GeoODK
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

package com.geoodk.collect.android.preferences;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

import com.geoodk.collect.android.R;

import java.io.File;

/**
 * Handles Map preferences.
 *
 * @author Jon Nordling IIASA-UMD 2014
 */
public class MapSettings extends PreferenceActivity implements
		OnPreferenceChangeListener {
	public static String MAP_PREFERENCES = "map_prefs";
	public static final String KEY_offlineLayer_URL = "oflinelayers_url";
	public static final String KEY_online_offlinePrefernce = "online_maps_key";
	public static final String KEY_map_basemap = "map_basemap";
	public static final String KEY_point_editable = "points_editable";
	public static final String KEY_geoodk_theme = "geoodk_theme";
	
	private CheckBoxPreference online_offlinePrefernce;
	private CheckBoxPreference points_editable;
	private EditTextPreference offlineLayerUrl;
	private ListPreference basemapList;
	private ListPreference themeList;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.map_preferences);
		//setContentView(R.xml.map_preferences);
		//(R.xml.map_preferences);
		setTitle(getString(R.string.app_name) + " > "
				+ getString(R.string.map_preferences));
		setMapSettings();

	}
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// TODO Auto-generated method stub
		//Find out what this does
		preference.setSummary((CharSequence) newValue);
		return false;
	}

    
    private void setMapSettings(){
    	// This is the section for the offline layer URL
    	offlineLayerUrl = (EditTextPreference) findPreference(KEY_offlineLayer_URL);
    	if (offlineLayerUrl.getText().endsWith("/")){
    		offlineLayerUrl.setSummary(offlineLayerUrl.getText());
    	}else{
    		offlineLayerUrl.setSummary(offlineLayerUrl.getText()+File.separatorChar);
    		offlineLayerUrl.setText(offlineLayerUrl.getText()+File.separatorChar);
    	}
    	offlineLayerUrl.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
							String url = newValue.toString();
							if (!url.endsWith("/")) {
								offlineLayerUrl.setText(newValue.toString()+File.separatorChar);
								preference.setSummary(newValue.toString()+File.separatorChar);
							}else{
								preference.setSummary(newValue.toString());
							}
							return true;
						}
					
				});
    	

    	online_offlinePrefernce = (CheckBoxPreference) findPreference(KEY_online_offlinePrefernce);
    	if (online_offlinePrefernce.isChecked()){
    		online_offlinePrefernce.setSummary("Maps are ONLINE");
    	}else{
    		online_offlinePrefernce.setSummary("Maps are OFFLINE");
    	}

    	online_offlinePrefernce.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				// TODO Auto-generated method stub
				if (newValue.equals(true)){
					preference.setSummary("Maps are ONLINE");
				}else{
					preference.setSummary("Maps are OFFLINE");
				}
				return true;
			}
    		
    	});
    	
    	// basemapList
    	basemapList = (ListPreference) findPreference(KEY_map_basemap);
    	basemapList.setSummary(basemapList.getEntry());
    	basemapList.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference,
											  Object newValue) {
				// TODO Auto-generated method stub
				if (newValue.equals(true)) {
					preference.setSummary(newValue.toString());
				} else {
					preference.setSummary(newValue.toString());
				}
				return true;
			}

		});
		themeList = (ListPreference) findPreference(KEY_geoodk_theme);
		themeList.setSummary(themeList.getEntry());
		themeList.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference,
											  Object newValue) {
				// TODO Auto-generated method stub
				if (newValue.equals(true)) {
					preference.setSummary(newValue.toString());
				} else {
					preference.setSummary(newValue.toString());
				}
				Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage( getBaseContext().getPackageName() );
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
				return true;
			}

		});


		points_editable = (CheckBoxPreference) findPreference(KEY_point_editable);
		points_editable.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference,
											  Object newValue) {
				// TODO Auto-generated method stub
				if (newValue.equals(true)) {
					preference.setSummary("Points are Editable");
				} else {
					preference.setSummary("Points are NOT Editable");
				}
				return true;
			}

		});

    	
    	//End of Settings function
    }






}
