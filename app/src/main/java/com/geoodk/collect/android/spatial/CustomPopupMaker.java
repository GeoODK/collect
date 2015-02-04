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
 *
 * @author Jon Nordling (jonnordling@gmail.com)
 */
package com.geoodk.collect.android.spatial;



import org.osmdroid.bonuspack.overlays.MarkerInfoWindow;
import org.osmdroid.views.MapView;

import com.geoodk.collect.android.R;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class CustomPopupMaker extends MarkerInfoWindow{
		Uri instanceUrl;
         public CustomPopupMaker(MapView mapView,Uri instance_Url) {
                 super(R.layout.custom_popup, mapView);
                 instanceUrl = instance_Url;
                 
                 Button btn = (Button)(mView.findViewById(R.id.bubble_moreinfo));
                 
                 btn.setOnClickListener(new View.OnClickListener() {
                     public void onClick(View view) {
                    	 
                         if (instanceUrl != null){
                             //Intent myIntent = new Intent(Intent.ACTION_EDIT, instanceUrl);
                        	 view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, instanceUrl ));
                             //view.getContext().startActivity(myIntent);
                         } else {
                                 Toast.makeText(view.getContext(), "Button clicked", Toast.LENGTH_LONG).show();
                         }
                     }
                 });
         }
         public void onClick(View v) {
        	 mView.invalidate();
             // Override Marker's onClick behaviour here
        	 
          }
       
		private Button findViewById(int bubbleMoreinfo) {
			// TODO Auto-generated method stub
			return null;
		}

		
		

 }
 

