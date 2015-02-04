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

import java.io.File;
import java.util.ArrayList;

import com.geoodk.collect.android.R;
import com.geoodk.collect.android.utilities.UrlUtils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.MediaStore.Images;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.Toast;

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
	
	private CheckBoxPreference online_offlinePrefernce;	
	private EditTextPreference offlineLayerUrl;
	private ListPreference basemapList;

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
    	
    	
    	// This is for the offline line online settings
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
				if (newValue.equals(true)){
					preference.setSummary(newValue.toString());
				}else{
					preference.setSummary(newValue.toString());
				}
				return true;
			}
    		
    	});
    	
    	//End of Settings function
    }






}
