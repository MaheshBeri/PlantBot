/*
 * Copyright 2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amazonaws.services.iot.client.sample.pubSub;

import com.amazonaws.services.iot.client.AWSIotMqttClient;

import java.io.IOException;

import com.amazonaws.services.iot.client.AWSIotDevice;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTimeoutException;
import com.amazonaws.services.iot.client.AWSIotTopic;
import com.amazonaws.services.iot.client.sample.sampleUtil.CommandArguments;
import com.amazonaws.services.iot.client.sample.sampleUtil.SampleUtil;
import com.amazonaws.services.iot.client.sample.sampleUtil.SampleUtil.KeyStorePasswordPair;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plantbot.Thing;

/**
 * This is an example that uses {@link AWSIotMqttClient} to subscribe to a topic
 * and publish messages to it. Both blocking and non-blocking publishing are
 * demonstrated in this example.
 */
public class PublishSubscribeSample {

	public PublishSubscribeSample(String[] args) throws Exception {
		CommandArguments arguments = CommandArguments.parse(args);
		initClient(arguments);
		System.out.println("Trying to connect  ...");
		awsIotClient.connect();

	}

	 private static final String TestTopic = "sdkTest/config";
	//private static final String TestTopic = "acme/temp";

	private static final AWSIotQos TestTopicQos = AWSIotQos.QOS0;

	private AWSIotMqttClient awsIotClient;
	// int temperature;

	public Thing getDeviceState() throws Exception{
		String thingName = "HydraulicPump";
		AWSIotDevice device = new AWSIotDevice(thingName);

		awsIotClient.attach(device);

		// Get the entire shadow document
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		Thing thing = new Thing();

		String shadowState = device.get();
		thing = objectMapper.readValue(shadowState, Thing.class);

		System.out.println("DEVICE Temp state is ..." + thing.state.reported.temp);

		//return Integer.parseInt(thing.state.reported.temp);
		return thing;
	}

	public void setTemperature(int temperature1) throws Exception {
		String payload = "" + temperature1;
		awsIotClient.publish(TestTopic, payload);
		// temperature = temperature1;
		String thingName = "HydraulicPump";
		AWSIotDevice device = new AWSIotDevice(thingName);

		awsIotClient.attach(device);

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		Thing thing = new Thing();
		//thing.state.desired.temp= ""+temperature1;
		thing.state.reported.temp= ""+temperature1;

		String jsonState = objectMapper.writeValueAsString(thing);
		System.out.println("Setting Device Temperature "+ jsonState);
		// Send updated document to the shadow
		device.update(jsonState);
		System.out.println(System.currentTimeMillis() + ": >>> " + jsonState);
	}

	public void setClient(AWSIotMqttClient client) {
		awsIotClient = client;
	}

	private void initClient(CommandArguments arguments) {
		String clientEndpoint = arguments.getNotNull("clientEndpoint", SampleUtil.getConfig("clientEndpoint"));

		int Min = 1000;
		int Max = 9999;
		int clientId = Min + (int) (Math.random() * ((Max - Min) + 1));
		String clientIdS = "" + clientId;
		String certificateFile = arguments.get("certificateFile", SampleUtil.getConfig("certificateFile"));
		String privateKeyFile = arguments.get("privateKeyFile", SampleUtil.getConfig("privateKeyFile"));
		if (awsIotClient == null && certificateFile != null && privateKeyFile != null) {
			String algorithm = arguments.get("keyAlgorithm", SampleUtil.getConfig("keyAlgorithm"));
			KeyStorePasswordPair pair = SampleUtil.getKeyStorePasswordPair(certificateFile, privateKeyFile, algorithm);

			awsIotClient = new AWSIotMqttClient(clientEndpoint, clientIdS, pair.keyStore, pair.keyPassword);
		}

		if (awsIotClient == null) {
			String awsAccessKeyId = arguments.get("awsAccessKeyId", SampleUtil.getConfig("awsAccessKeyId"));
			String awsSecretAccessKey = arguments.get("awsSecretAccessKey", SampleUtil.getConfig("awsSecretAccessKey"));
			String sessionToken = arguments.get("sessionToken", SampleUtil.getConfig("sessionToken"));

			if (awsAccessKeyId != null && awsSecretAccessKey != null) {
				awsIotClient = new AWSIotMqttClient(clientEndpoint, clientIdS, awsAccessKeyId, awsSecretAccessKey,
						sessionToken);
			}
		}

		if (awsIotClient == null) {
			throw new IllegalArgumentException("Failed to construct client due to missing certificate or credentials.");
		}
	}

	/*public void main(String args[]) throws InterruptedException, AWSIotException, AWSIotTimeoutException, Exception,
	JsonMappingException, IOException {
		CommandArguments arguments = CommandArguments.parse(args);
		initClient(arguments);
		System.out.println("Trying to connect  ...");
		awsIotClient.connect();
		// String subTopicPump="$aws/things/HydraulicPump/shadow/get/accepted";
		// String pubTopicPump="$aws/things/HydraulicPump/shadow/get";
		// AWSIotTopic topic = new TestTopicListener(subTopicPump,
		// TestTopicQos);
		// awsIotClient.publish(pubTopicPump, "");
		String payload = "" + getTemperature();
		System.out.println("Trying to publish ...");
		awsIotClient.publish(TestTopic, payload);

		String thingName = "HydraulicPump"; // replace with your AWS IoT Thing
		// name

		AWSIotDevice device = new AWSIotDevice(thingName);

		awsIotClient.attach(device);

		// Get the entire shadow document
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		Thing thing = new Thing();

		String shadowState = device.get();
		thing = objectMapper.readValue(shadowState, Thing.class);

		System.out.println("DEVICE Temp state is ..." + thing.state.reported.temp);
		System.out.println("DEVICE Pressure state is ..." + thing.state.reported.pressure);
		// awsIotClient.disconnect();
	}*/

}
