package com.geoodk.collect.android.spatial;

import java.io.File;

import android.app.ListActivity;
import android.database.Cursor;

import com.geoodk.collect.android.provider.FormsProviderAPI.FormsColumns;

public class XmlGeopointHelper extends ListActivity {
	
	//The string passed with be the ID name for the form that
	//The purpose of this function is to find the field associated with the geopoint for mapping
    //public static void main(String[] args) {
    	
    //}
	public String getGeopointDBField(String form_name){
		String field = null;
		
		String sortOrder = FormsColumns.DISPLAY_NAME + " ASC, " + FormsColumns.JR_VERSION + " DESC";
        Cursor c = managedQuery(FormsColumns.CONTENT_URI, null, null, null, sortOrder);
        String x = "sdgkd";
        //String[] data = new String[] {
                //FormsColumns.DISPLAY_NAME, FormsColumns.DISPLAY_SUBTEXT, FormsColumns.JR_VERSION
        //};
		
		//Get the cursor fo the forms
		//Identify the correct form.
		//Find the geopoint field
		//Get db value and return it
		
		return form_name;
		
	}
	

}