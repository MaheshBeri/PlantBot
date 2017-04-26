package com.plantbot;

import com.amazonaws.services.iot.client.AWSIotDevice;
import com.amazonaws.services.iot.client.AWSIotDeviceProperty;

public class PumpDevice extends AWSIotDevice {
	 public PumpDevice(String thingName) {
	        super(thingName);
	    }

	    @AWSIotDeviceProperty
	    private String temp;

	    public String getTemp() {
	        // read from the physical device
	    	//super.get();
	    	return "";
	    }

	    public void setTemp(String newValue) {
	        // write to the physical device
	    }
}
