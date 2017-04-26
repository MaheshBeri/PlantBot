package com.plantbot;

public class Thing {
	

	    public State state = new State();

	    public static class State {
	        public Document reported = new Document();
	        public Document desired = new Document();
	    }

	    public static class Document {
	        public String  temp = null;
	        public String  oil_temp = null;
	        public String  pressure = null;
	        public String  alert = null;
	    }	
}
