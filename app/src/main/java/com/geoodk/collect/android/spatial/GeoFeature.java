package com.geoodk.collect.android.spatial;

import java.util.ArrayList;

/**
 * Created by jnordling on 9/14/15.
 */


public class GeoFeature {



    public ArrayList<ArrayList<GeoObject>> geoFields = new ArrayList<ArrayList<GeoObject>>();
    public String instance_url;
    public String instance_form_id;
    public String instance_form_name;
    public String instance_form_status;
    public String instanceUriString;


    public ArrayList<ArrayList<GeoObject>> getGeoFields() {
        return geoFields;
    }
    public void setGeoFields(ArrayList<ArrayList<GeoObject>> geoFields) {
        this.geoFields = geoFields;
    }

    public String getInstance_url() {
        return instance_url;
    }

    public void setInstance_url(String instance_url) {
        this.instance_url = instance_url;
    }

    public String getInstance_form_id() {
        return instance_form_id;
    }

    public void setInstance_form_id(String instance_form_id) {
        this.instance_form_id = instance_form_id;
    }

    public String getInstance_form_name() {
        return instance_form_name;
    }

    public void setInstance_form_name(String instance_form_name) {
        this.instance_form_name = instance_form_name;
    }

    public String getInstance_form_status() {
        return instance_form_status;
    }

    public void setInstance_form_status(String instance_form_status) {
        this.instance_form_status = instance_form_status;
    }

    public String getInstanceUriString() {
        return instanceUriString;
    }

    public void setInstanceUriString(String instanceUriString) {
        this.instanceUriString = instanceUriString;
    }

}
