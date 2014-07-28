package com.geoodk.collect.android.activities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.geoodk.collect.android.R;
import com.geoodk.collect.android.application.Collect;
import com.geoodk.collect.android.preferences.AdminPreferencesActivity;
import com.geoodk.collect.android.preferences.MapSettings;
import com.geoodk.collect.android.preferences.PreferencesActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class GeoODK extends Activity {
	private static final String t = "GeoODK";
	private static boolean EXIT = true;
	private AlertDialog mAlertDialog;
	private String[] assestFormList;

	
    public static final String FORMS_PATH = Collect.ODK_ROOT + File.separator + "forms";
	

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.geoodk_layout);
        
        //Create the files and directorys
        
        Log.i(t, "Starting up, creating directories");
		try {
			Collect.createODKDirs();
		} catch (RuntimeException e) {
			createErrorDialog(e.getMessage(), EXIT);
			return;
		}
        
		// Copy Forms from assest to SD Cards
		//String forms[] = { "Agriculture_demo.xml", "Inbal_Ukraine_crop.xml", "Pak_Training.xml" };
		String forms[] = { "Agriculture_demo.xml" };
		assestFormList = getAssetFormList();
		copyForms(assestFormList);
		
		
		ImageButton geoodk_collect_button = (ImageButton) findViewById(R.id.geoodk_collect_butt);
        geoodk_collect_button.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		        // Do something in response to button click
				Collect.getInstance().getActivityLogger().logAction(this, "fillBlankForm", "click");
				Intent i = new Intent(getApplicationContext(),FormChooserList.class);
				startActivity(i);
		    }
		});
       
        ImageButton geoodk_manage_but = (ImageButton) findViewById(R.id.geoodk_edit_butt);
		geoodk_manage_but.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Collect.getInstance().getActivityLogger()
						.logAction(this, "editSavedForm", "click");
				Intent i = new Intent(getApplicationContext(),
						InstanceChooserList.class);
				startActivity(i);
			}
		});
		ImageButton geoodk_map_but = (ImageButton) findViewById(R.id.geoodk_map_butt);
		geoodk_map_but.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Collect.getInstance().getActivityLogger()
						.logAction(this, "map_data", "click");
				Intent i = new Intent(getApplicationContext(),
						OSM_Map.class);
				startActivity(i);
			}
		});
		
		ImageButton geoodk_settings_but = (ImageButton) findViewById(R.id.geoodk_settings_butt);
		geoodk_settings_but.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Collect.getInstance()
				.getActivityLogger()
				.logAction(this,"Main_Settings","click");
				Intent ig = new Intent( getApplicationContext(), MainSettingsActivity.class);
						startActivity(ig);
			}
		});
		
		ImageButton geoodk_send_but = (ImageButton) findViewById(R.id.geoodk_send_data_butt);
		geoodk_send_but.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Collect.getInstance().getActivityLogger()
				.logAction(this, "uploadForms", "click");
					Intent i = new Intent(getApplicationContext(),
							InstanceUploaderList.class);
					startActivity(i);
			}
		});
		ImageButton geoodk_delete_but = (ImageButton) findViewById(R.id.geoodk_delete_data_butt);
		geoodk_delete_but.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Collect.getInstance().getActivityLogger()
						.logAction(this, "deleteSavedForms", "click");
				Intent i = new Intent(getApplicationContext(),
						FileManagerTabs.class);
				startActivity(i);
			}
		});
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
