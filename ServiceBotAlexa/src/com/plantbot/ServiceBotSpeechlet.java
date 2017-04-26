/**
    Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.plantbot;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.Image;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.amazon.speech.ui.StandardCard;
import com.amazonaws.services.iot.client.sample.pubSub.PublishSubscribeSample;

/**
 * This sample shows how to create a simple speechlet for handling speechlet requests.
 */
public class ServiceBotSpeechlet implements Speechlet {
	//private static final Logger log = LoggerFactory.getLogger(ServiceBotSpeechlet.class);
	private static String TITLE="Plant Bot";

	@Override
	public void onSessionStarted(final SessionStartedRequest request, final Session session)
			throws SpeechletException {
		//log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
			//	session.getSessionId());
		// any initialization logic goes here
	}

	@Override
	public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
			throws SpeechletException {
		//log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
			//	session.getSessionId());
		return getWelcomeResponse();
	}

	@Override
	public SpeechletResponse onIntent(final IntentRequest request, final Session session)
			throws SpeechletException {
		//log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
			//	session.getSessionId());

		Intent intent = request.getIntent();
		String intentName = (intent != null) ? intent.getName() : null;

		 if ("AMAZON.HelpIntent".equals(intentName)) {
			return getHelpResponse();
		} else if ("SetAlertTemperatureIntent".equals(intentName)) {

			String sNewAlertTemperature=request.getIntent().getSlot("newAlertTemperature").getValue();
			return getConfigAlertTemperatureResponse(sNewAlertTemperature);
		} else if ("SelectSupplierIntent".equals(intentName)) {
			String sSelectedSupplier=request.getIntent().getSlot("selectedSupplier").getValue();
			System.out.println("Selected supplier"+sSelectedSupplier);
			return getSupplierNameResponse(sSelectedSupplier);
		}else if ("AMAZON.StopIntent".equals(intentName)||"AMAZON.NoIntent".equals(intentName)) {
			return getStopResponse();
		}else if ("AMAZON.YesIntent".equals(intentName)) {			
			return getPartsOrderConfirmationResponse();
		}		
		else {
			throw new SpeechletException("Invalid Intent");
		}
	}
	private SpeechletResponse getSupplierNameResponse(String supplier) {
		String plainText="";
		boolean error=false;
		if(supplier==null || 
				!(supplier.equalsIgnoreCase("Aqua Press")||supplier.equalsIgnoreCase("Fluid Tech"))){
			plainText="Order from Aqua Press or Fluid Tech" ;
			error=true;
		}else{
			plainText = "Pump replacement order has been sucessfully placed on supplier "+supplier;
		}
		SsmlOutputSpeech objSsmlOutputSpeech = new SsmlOutputSpeech();
		String speechText="<p>"+plainText+"</p>";	 
		speechText="<speak>"+speechText+"</speak>";
		objSsmlOutputSpeech.setSsml(speechText);
		SimpleCard card = new SimpleCard();
		card.setTitle("Part order confirmation");
		card.setContent(plainText);
		if(error){
			Reprompt reprompt = new Reprompt();
			reprompt.setOutputSpeech(objSsmlOutputSpeech);
			return SpeechletResponse.newAskResponse(objSsmlOutputSpeech, reprompt, card);
			
		}else	
			return SpeechletResponse.newTellResponse(objSsmlOutputSpeech,card);
		
	}
	private SpeechletResponse getPartsOrderConfirmationResponse() {	
		String plainText1 = "There are two alternate parts matching pump specifications.";
		String plainText2 = "First supplier is Aqua Press .";
		String plainText3 = "Second supplier is Fluid Tech .";
		String plainText4 = "Which supplier do you wish to order part from ?";
		
		SsmlOutputSpeech objSsmlOutputSpeech = new SsmlOutputSpeech();
		String speechText="<p>"+plainText1+"</p>"+plainText2+plainText3+"<p>"+plainText4+"</p>";
		speechText="<speak>"+speechText+"</speak>";
		objSsmlOutputSpeech.setSsml(speechText);		
				
		Image objImage= new Image();
		objImage.setLargeImageUrl("https://s3.amazonaws.com/servicebot.valueinnovation.co.in/vendor112.jpeg");
		objImage.setSmallImageUrl("https://s3.amazonaws.com/servicebot.valueinnovation.co.in/vendor112.jpeg");
		
		String plainText=plainText1+plainText2+plainText3+plainText4;
		StandardCard card = new StandardCard();
		card.setTitle("Select Supplier Part");
		card.setText(plainText);
		card.setImage(objImage);
		Reprompt reprompt = new Reprompt();
		reprompt.setOutputSpeech(objSsmlOutputSpeech);
		return SpeechletResponse.newAskResponse(objSsmlOutputSpeech, reprompt, card);
		
	}
	
	private SpeechletResponse getStopResponse() {
		String speechText = "Goodbye";
		PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
		speech.setText(speechText);
		return SpeechletResponse.newTellResponse(speech);
	}
	@Override
	public void onSessionEnded(final SessionEndedRequest request, final Session session)
			throws SpeechletException {
		//log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
			//	session.getSessionId());
	}

	/**
	 * Creates and returns a {@code SpeechletResponse} with a welcome message.
	 *
	 * @return SpeechletResponse spoken and visual response for the given intent
	 */
	private SpeechletResponse getWelcomeResponse() {
		String speechText ="";
		String plainText ="";
		String[] args=new String[0];
		SsmlOutputSpeech objSsmlOutputSpeech = new SsmlOutputSpeech();
		objSsmlOutputSpeech.setSsml(speechText);
		boolean alert=false;
		try {
			PublishSubscribeSample objPublishSubscribeSample=new PublishSubscribeSample(args);
			Thing deviceState=objPublishSubscribeSample.getDeviceState();
			String sAlert=deviceState.state.reported.alert;
			String sOilTemperature=deviceState.state.reported.oil_temp;
			String sAlertTemperature=deviceState.state.reported.temp;
			String sPlainText1="There was a high temperature alert on device KJ109.";
			String sPlainText2="Oil temperature recorded was "+sOilTemperature;
			
			String sPlainText3="There are no pending alerts.";
			String sPlainText4="Current alert temperature is "+sAlertTemperature;
			String sPlainText5="You can update device configuration.";
			String sPlainText6="For example you can say set alert temperature as 42";
			
			if(sAlert!=null && sAlert.equals("true")){
				speechText ="<p>"+sPlainText1+"</p>"+ "<p>"+sPlainText2+"</p>"+ "<p>"+sPlainText5+"</p>"+ "<p>"+sPlainText6+"</p>";;
				plainText=sPlainText1+sPlainText2+sPlainText5+sPlainText6;
				alert=true;
			}else{
				speechText="<p>"+sPlainText3+"</p>"+ "<p>"+sPlainText4+"</p>"+ "<p>"+sPlainText5+"</p>"+ "<p>"+sPlainText6+"</p>";
				plainText=sPlainText3+sPlainText4+sPlainText5+sPlainText6;
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			speechText="Sorry, error in getting alert  temperature" ;
		}
		//        $aws/things/HydraulicPump/shadow/get
		// Create the Simple card content.
		SimpleCard card = new SimpleCard();
		card.setTitle("Alert Status");
		card.setContent(plainText);

		// Create the plain text output.
		//PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
		//speech.setText(speechText);
		 
		objSsmlOutputSpeech.setSsml("<speak>" + speechText + "</speak>");
		//if(alert){
			// Create reprompt
			Reprompt reprompt = new Reprompt();
			reprompt.setOutputSpeech(objSsmlOutputSpeech);
			//reprompt.setOutputSpeech(speech);
			return SpeechletResponse.newAskResponse(objSsmlOutputSpeech, reprompt, card);
			//return SpeechletResponse.newAskResponse(speech, reprompt, card);
		//}else
		//	return SpeechletResponse.newTellResponse(objSsmlOutputSpeech);
		
		
	}
//
	private SpeechletResponse getConfigAlertTemperatureResponse(String sNewAlertTemperature) {
		String plainText1="Alert temperature has been updated to "+sNewAlertTemperature+".";
		String plainText2="Based on the trend in temperature, the oil pump needs to be replaced.";
		String plainText3="Do you wish to proceed with parts order?";
		String speechText = "<p>"+plainText1+"</p>";
		speechText +=("<break time=\"1s\"/>"+plainText2);
		speechText +=("<break time=\"1s\"/>"+plainText3);
		
		// Create the Simple card content.
		SimpleCard card = new SimpleCard();
		card.setTitle(TITLE);
		
		card.setContent(plainText1+plainText2+plainText3);
		if(sNewAlertTemperature==null || sNewAlertTemperature.equals("null")||
				!StringUtils.isNumeric(sNewAlertTemperature)){
			speechText="Please set alert temperature as number" ;
			
		}else
		{
			int alertTemp=Integer.parseInt(sNewAlertTemperature);		

			String[] args=new String[0];

			try {
				PublishSubscribeSample objPublishSubscribeSample=new PublishSubscribeSample(args);
				objPublishSubscribeSample.setTemperature(alertTemp);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				speechText="Sorry, error in updating temperature. Please try again" ;
			}
			
		}
		SsmlOutputSpeech objSsmlOutputSpeech = new SsmlOutputSpeech();
		objSsmlOutputSpeech.setSsml("<speak>" + speechText + "</speak>");
		Reprompt reprompt = new Reprompt();
		reprompt.setOutputSpeech(objSsmlOutputSpeech);
		
		return SpeechletResponse.newAskResponse(objSsmlOutputSpeech, reprompt, card);
		
	}
	
	/**
	 * Creates a {@code SpeechletResponse} for the help intent.
	 *
	 * @return SpeechletResponse spoken and visual response for the given intent
	 */
	private SpeechletResponse getHelpResponse() {
		String speechText = "You can set alert temperature";

		// Create the Simple card content.
		SimpleCard card = new SimpleCard();
		card.setTitle(TITLE);
		card.setContent(speechText);

		// Create the plain text output.
		PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
		speech.setText(speechText);

		// Create reprompt
		Reprompt reprompt = new Reprompt();
		reprompt.setOutputSpeech(speech);

		return SpeechletResponse.newAskResponse(speech, reprompt, card);
	}
}
