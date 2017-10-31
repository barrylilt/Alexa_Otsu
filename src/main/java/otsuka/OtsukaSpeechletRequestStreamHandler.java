/**
    Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package otsuka;

import java.util.HashSet;
import java.util.Set;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

/**
 * This class is created by the Lambda environment when a request comes in. All calls will be
 * dispatched to the Speechlet passed into the super constructor.
 */
public final class OtsukaSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {
    private static final Set<String> supportedApplicationIds;

    static {
        /*
         * This Id can be found on https://developer.amazon.com/edw/home.html#/ "Edit" the relevant
         * Alexa Skill and put the relevant Application Ids in this Set.
         *
         *  arn:aws:lambda:us-east-1:511106519738:function:OtsukaDemo
         *  amzn1.ask.skill.c7f22ed7-ba9e-450a-a0d8-dff647c3a282
         *  
         */
    	
        supportedApplicationIds = new HashSet<String>();
        //amzn1.echo-sdk-ams.app.[]
         supportedApplicationIds.add("amzn1.ask.skill.c7f22ed7-ba9e-450a-a0d8-dff647c3a282");
    }

    public OtsukaSpeechletRequestStreamHandler() {
        super(new OtsukaSpeechlet(), supportedApplicationIds);
    }
}
