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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.geoodk.collect.android.R;
import com.geoodk.collect.android.adapters.CustomListAdapter;

public class GeoODKOptionActivity extends Activity {
	ListView list;
	String[] itemname ={
			"Collect Data",
			"Edit Data",
			"Send Data",
			"Delete Data",
			"Settings",
			"About GeoODK"
	};
	String[] itemDescription = {
			"Complete a new survey",
			"Edit an existing survey",
			"Send completed forms",
			"Remove complete and blank forms",
			"Configure forms, maps and others",
			"Learn more about GeoODK"
	};

	Integer[] imgid={
			R.drawable.geoodk_notes,
			R.drawable.edit_data,
			R.drawable.data_upload_blue,
			R.drawable.delete_data,
			R.drawable.settings,
			R.drawable.info_button
	};

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.geoodk_option_layout);
		CustomListAdapter adapter=new CustomListAdapter(this, itemname, itemDescription, imgid);
		list=(ListView)findViewById(R.id.list);
		list.setAdapter(adapter);

		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				// TODO Auto-generated method stub
				Intent i =null;
				switch (position){
					case 0:
						//Collect Data
						i = new Intent(getApplicationContext(),FormChooserList.class);
						startActivity(i);
						break;
					case 1:
						//Edit Data
						i = new Intent(getApplicationContext(),InstanceChooserList.class);
						startActivity(i);
						break;
					case 2:
						//Send Data
						i = new Intent( getApplicationContext(), InstanceUploaderList.class);
						startActivity(i);
						break;
					case 3:
						// Delete Data
						i = new Intent( getApplicationContext(), FileManagerTabs.class);
						startActivity(i);
						break;
					case 4:
						//Settings
						i = new Intent( getApplicationContext(), MainSettingsActivity.class);
						startActivity(i);
						break;
					case 5:
						String Slecteditem= itemname[+position];
						Toast.makeText(getApplicationContext(), Slecteditem, Toast.LENGTH_SHORT).show();

				}
				if(i != null) {

				}

			}
		});
    }

}
