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

/**
 * Responsible for displaying buttons to launch the major activities. Launches
 * some activities based on returns of others.
 *
 * @author Jon Nordling (jonnordling@gmail.com)
 */

package com.geoodk.collect.android.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.geoodk.collect.android.R;
import com.geoodk.collect.android.application.Collect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class GeoODKMapThemeActivity extends Activity {
	private static final String t = "GeoODK";
	private static boolean EXIT = true;
	private AlertDialog mAlertDialog;
	private String[] assestFormList;

	
    public static final String FORMS_PATH = Collect.ODK_ROOT + File.separator + "forms";
	

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.geoodk_maptheme_layout);
        

        Log.i(t, "Starting up, creating directories");
		try {
			Collect.createODKDirs();
		} catch (RuntimeException e) {
			createErrorDialog(e.getMessage(), EXIT);
			return;
		}
		assestFormList = getAssetFormList();
		copyForms(assestFormList);

		ImageButton gid = (ImageButton) findViewById(R.id.grid);
		gid.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		        // Do something in response to button click
				Collect.getInstance().getActivityLogger().logAction(this, "OpenClassicView", "click");
				Intent i = new Intent(getApplicationContext(),GeoODKClassicActivity.class);
				startActivity(i);
		    }
		});
		

//        ImageButton layers = (ImageButton) findViewById(R.id.geoodk_edit_butt);
//		layers.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
////				Collect.getInstance().getActivityLogger()
////						.logAction(this, "editSavedForm", "click");
////				Intent i = new Intent(getApplicationContext(),InstanceChooserList.class);
////				startActivity(i);
//			}
//		});
		ImageButton collect_button = (ImageButton) findViewById(R.id.collect_button);
		collect_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Collect.getInstance().getActivityLogger()
						.logAction(this, "FormChooserList", "click");
				Intent i = new Intent(getApplicationContext(),	FormChooserList.class);
				startActivity(i);
			}
		});
////
//		ImageButton gps = (ImageButton) findViewById(R.id.geoodk_settings_butt);
//		gps.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
////				Collect.getInstance()
////				.getActivityLogger()
////				.logAction(this,"Main_Settings","click");
////				Intent ig = new Intent( getApplicationContext(), MainSettingsActivity.class);
////				startActivity(ig);
//			}
//		});
		//End of Main activity
    }
	


	private String[] getAssetFormList() {
		AssetManager assetManager = getAssets();
		String[] formList = null;
		try {
			formList = assetManager.list("forms");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//assetManager.list(path);
		// TODO Auto-generated method stub
		return formList;
	}



	private void copyForms(String[] forms){
		AssetManager assetManager = getAssets();
		InputStream in = null;
		OutputStream out = null;
		for (int i=0; forms.length>i; i++) {
			String filename = forms[i];
			File form_file = new File(FORMS_PATH,filename);
			if (!form_file.exists()){
				try {
					in = assetManager.open("forms/"+filename);
					out = new FileOutputStream(FORMS_PATH+File.separator+filename);
					copyFile(in, out);
					in.close();
		            out.flush();
		            out.close();
		            in = null;
		            out = null;
					
				} catch (IOException e) {
					Log.e("tag", "Failed to copy asset file: " + FORMS_PATH+File.separator+forms[i], e);
			}
				
			}
			 System.out.println(forms[i]);
		}
		
	}
	
	private void copyFile(InputStream in, OutputStream out) throws IOException
	{
	      byte[] buffer = new byte[1024];
	      int read;
	      while((read = in.read(buffer)) != -1)
	      {
	            out.write(buffer, 0, read);
	      }
	}
	
	private void createErrorDialog(String errorMsg, final boolean shouldExit) {
		Collect.getInstance().getActivityLogger()
				.logAction(this, "createErrorDialog", "show");
		mAlertDialog = new AlertDialog.Builder(this).create();
		mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
		mAlertDialog.setMessage(errorMsg);
		DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int i) {
				switch (i) {
				case DialogInterface.BUTTON_POSITIVE:
					Collect.getInstance()
							.getActivityLogger()
							.logAction(this, "createErrorDialog",
									shouldExit ? "exitApplication" : "OK");
					if (shouldExit) {
						finish();
					}
					break;
				}
			}
		};
		mAlertDialog.setCancelable(false);
		mAlertDialog.setButton(getString(R.string.ok), errorListener);
		mAlertDialog.show();
	}
	


	
	
}
