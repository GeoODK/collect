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
 

