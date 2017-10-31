/**
    Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package otsuka;

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
import com.amazon.speech.ui.Card;
import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.SsmlOutputSpeech;

public class OtsukaSpeechlet implements Speechlet {
	private static final Logger log = LoggerFactory.getLogger(OtsukaSpeechlet.class);

	/**
	 * The slots defined in Intent.
	 */
	private static final String SLOT_GENDERTYPE = "gendertype";
	
	private static final String SLOT_SPONSOR = "sponsor";
	
	private static final String SLOT_STATE = "state";
	
	private static final String SLOT_STUDYTYPE = "studytype";
	
	private static final String SLOT_PHASE = "phase";

	private static final String SLOT_STATUS = "status";
	
	private static final String SLOT_CONDITION = "condition";


	private ConnectionUtil connUtil;

	@Override
	public void onSessionStarted(final SessionStartedRequest request, final Session session) throws SpeechletException {
		log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

		initializeComponents();
	}

	private void initializeComponents() {
		if (connUtil == null) {
			connUtil = new ConnectionUtil();
		}
	}

	@Override
	public SpeechletResponse onLaunch(final LaunchRequest request, final Session session) throws SpeechletException {
		log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

		String speechOutput = "Hello there! Welcome to the Clinical Trial Analytics. How can I help you?";

		String repromptText = " ";

		// Here we are prompting the user for input
		return newAskResponse(speechOutput, false, "<speak>" + repromptText + "</speak>", true, false);
	}

	@Override
	public SpeechletResponse onIntent(final IntentRequest request, final Session session) throws SpeechletException {
		log.info("onIntent requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

		initializeComponents();

		Intent intent = request.getIntent();
		String intentName = (intent != null) ? intent.getName() : null;
		
	   
		if ("enrollment".equals(intentName)){
			return getTotalEnrollmentResponse(intent, session);
		}else if ("gender".equals(intentName)){
			return getTotalTrialsGenderResponse(intent, session);
		}else if ("study".equals(intentName)){
			return getTotalTrialsResponse(intent, session);
		}else if ("TotalStudies".equals(intentName)) {
			return getTotalStudiesResponse(intent, session);
		} else if ("Recruitment".equals(intentName)) {
			return getRecruitmentResponse(intent, session);
		} else if ("conditions".equals(intentName)) {
			return getTotalTrialsConditionResponse(intent, session);
		} else if ("HearMore".equals(intentName)) {
			return getMoreHelp();
		} else if ("DontHearMore".equals(intentName)) {
			PlainTextOutputSpeech output = new PlainTextOutputSpeech();
			output.setText("Thanks, Goodbye");
			return SpeechletResponse.newTellResponse(output);
		} else if ("AMAZON.HelpIntent".equals(intentName)) {
			return getHelp();
		} else if ("AMAZON.StopIntent".equals(intentName)) {
			PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
			outputSpeech.setText("Bye,  Hope to see you soon!");
			return SpeechletResponse.newTellResponse(outputSpeech);
		} else if ("AMAZON.CancelIntent".equals(intentName)) {
			PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
			outputSpeech.setText("Goodbye! ");
			return SpeechletResponse.newTellResponse(outputSpeech);
		} else if (intentName==null){
			String speechOutput="Can you please repeat";
			
			String repromptText="You can ask things like, "
					+ "Give me the number of total studies in phase one <break time=\"0.2s\" /> "
					+ "  <break time=\"0.2s\" /> " + " <break time=\"1s\" /> ";
								
			return newAskResponse(speechOutput, false, "<speak>" + repromptText + "</speak>", true, false);			
		}else {
			// Reprompt the user.
			String speechOutput = "Can you please repeat "+ " <break time=\"0.8s\" /> ";

			String repromptText = "You can ask things like, "
					+ "Give me the number of total studies in phase one <break time=\"0.5s\" /> "
					+ "  <break time=\"0.5s\" /> " + " <break time=\"0.5s\" /> ";
					
			return newAskResponse(speechOutput, false, "<speak>" + repromptText + "</speak>", true, false);

		}
		   
	}

	@Override
	public void onSessionEnded(final SessionEndedRequest request, final Session session) throws SpeechletException {
		log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

	}
	
		
	/**
	 * Creates Dynamic Query for Total Enrollment Count based on sponsor
	 * 
	 * @param sponsor 
	 * 
	 * @return String totalEnrollmentCount
	 * 
	 */
	private String executeEnrollmentCountQuery(String sponsor, String state) {
		String totalEnrollmentQuery="";
		// base Query 
		if (state==null) totalEnrollmentQuery = "select count(*) from ClinicalTrials_Dataset where Sponsor_or_Collaborators like '%" + sponsor + "%'";
		else if (state!=null ) totalEnrollmentQuery = "select count(*) from ClinicalTrials_Dataset where Sponsor_or_Collaborators like '%" + sponsor + "%' and State ='"+state+"'";
		// execute the query on server connection
		String totalEnrollmentCount = connUtil.executeQuery(totalEnrollmentQuery);

		return totalEnrollmentCount;

	}
	/**
	 * Creates Dynamic Query for Total Trials based on gender
	 * 
	 * @param gendertype 
	 * 
	 * @return String totalTrialsCount
	 * 
	 */
	private String executeTotalTrialsGenderQuery(String gendertype) {

		// base Query 
		String totalTrialsGenderQuery = "select count(*) FROM ClinicalTrials_Dataset where Gender='" + gendertype + "'";

		// execute the query on server connection
		String totalTrialsCount = connUtil.executeQuery(totalTrialsGenderQuery);

		return totalTrialsCount;
		
		
	}
	
	private String executeTotalTrialsCondition(String sponsor, String condition) {
        String totalTrialsQuery="";
		// base Query
               
        if(sponsor!=null && condition!=null)
        totalTrialsQuery="select count(*) from ClinicalTrials_Dataset where Conditions like '%" + condition + "%' and Sponsor_or_Collaborators like '%" + sponsor + "%'";
       
        else if(sponsor==null && condition!=null)
        totalTrialsQuery="select count(*) from ClinicalTrials_Dataset where Conditions like '%" + condition + "%'";
		// execute the query on server connection
		String totalTrialsCount = connUtil.executeQuery(totalTrialsQuery);

		return totalTrialsCount;

	}
	
	/**
	 * Creates Dynamic Query for Total Trials based on study type
	 * 
	 * @param studytype 
	 * 
	 * @return String totalTrialsCount
	 * 
	 */
	private String executeTotalTrialsQuery(String studytype, String sponsor, String condition) {
        String totalTrialsQuery="";
		// base Query
        
        if(sponsor==null && condition==null && studytype!=null )
	    totalTrialsQuery = "select count(*) from ClinicalTrials_Dataset where Study_Types='" + studytype + "'";
        else if(sponsor!=null && studytype!=null && condition==null)
        totalTrialsQuery = "select count(*) from ClinicalTrials_Dataset where Study_Types='" + studytype + "' and Sponsor_or_Collaborators like '%" + sponsor + "%'";            
        else if(sponsor!=null && studytype!=null && condition!=null)
        totalTrialsQuery="select count(*) from ClinicalTrials_Dataset where Conditions like '%" + condition + "%' and Sponsor_or_Collaborators like '%" + sponsor + "%' and Study_types = '" + studytype + "'";
		// execute the query on server connection
		String totalTrialsCount = connUtil.executeQuery(totalTrialsQuery);

		return totalTrialsCount;

	}
	
	
	 
	 /**
	 * Creates Dynamic Query for Total Studies based on phase
	 * 
	 * @param phase 
	 * 
	 * @return String totalStudiesCount
	 * 
	 */
	private String executeTotalStudiesQuery(String phase, String sponsor) {
        String totalStudiesQuery="";
		// base Query 		
		if(sponsor==null)
		totalStudiesQuery = "select count(*) FROM ClinicalTrials_Dataset where Phases='" + phase + "'";

		else if(sponsor!=null)
			totalStudiesQuery = "select count(*) FROM ClinicalTrials_Dataset where Phases='" + phase + "' and Sponsor_or_Collaborators like '%" + sponsor + "%'";	
		                        
		// execute the query on server connectionrj
		String totalStudiesCount = connUtil.executeQuery(totalStudiesQuery);

		return totalStudiesCount;

	}

	/**
	 * Creates Dynamic Query for Recruitment based on status
	 * 
	 * @param status
	 * 
	 * @return String recruitmentCount
	 * 
	 */

	private String executeRecruitmentQuery(String status, String sponsor) {
		String recruitmentQuery="";
		// base Query
		if(sponsor!=null) recruitmentQuery = "select count(*) from ClinicalTrials_Dataset where Recruitment like '"+ status +"%' and Sponsor_or_Collaborators like '%" + sponsor + "%'";
		else if (sponsor==null) recruitmentQuery = "select count(*) from ClinicalTrials_Dataset where Recruitment like '"+ status +"%'";

		String recruitmentCount = connUtil.executeQuery(recruitmentQuery);

		return recruitmentCount;

	}
	
	
	
	/**
	 * Creates a Dynamic Query for Total Enrollment Count based on the Sponsor, executes
	 * the Query and returns the result.
	 *
	 * @param intent
	 *            - the intent for the request
	 * @param session
	 * @return SpeechletResponse spoken and visual response for the given intent
	 * @throws SpeechletExxception
	 * 
	 */
		
	private SpeechletResponse getTotalEnrollmentResponse(final Intent intent, final Session session)
			throws SpeechletException {
		// Simple Display Card
				SimpleCard card = new SimpleCard();
				card.setTitle(":: Enrollment ::");

				String sponsor = intent.getSlot(SLOT_SPONSOR) != null ? intent.getSlot(SLOT_SPONSOR).getValue() : "";
				String state=intent.getSlot(SLOT_STATE) != null ? intent.getSlot(SLOT_STATE).getValue() : "";
						
				if(sponsor!=null){
				String answer="";
				     if (sponsor!=null && state!=null) answer = "The count of clinical studies for " + sponsor +" in state "+state;
				     else if(sponsor!=null && state==null) answer = "The count of clinical studies for " + sponsor;
				     
				String finalSpeechOut = "";

				String finalCardOut = "";

				String totalEnrollmentCount = executeEnrollmentCountQuery(sponsor, state); // "203090661";

				finalSpeechOut = answer + " is <break time=\"0.2s\" /> <say-as interpret-as=\"cardinal\"> " + totalEnrollmentCount
						+ "</say-as>";

				finalCardOut = answer + " is " + totalEnrollmentCount;

				card.setContent(finalCardOut);

				String repromptText = " ";

				return newAskResponse("<speak>" + finalSpeechOut + "</speak>", true, "<speak>" + repromptText + "</speak>",
						true, card);
				}
				else return getHelp();
			}

	
	/**
	 * Creates a Dynamic Query for Total Trials based on the Gender, executes
	 * the Query and returns the result.
	 *
	 * @param intent
	 *            - the intent for the request
	 * @param session
	 * @return SpeechletResponse spoken and visual response for the given intent
	 * @throws SpeechletExxception
	 * 
	 */
		
	private SpeechletResponse getTotalTrialsGenderResponse(final Intent intent, final Session session)
			throws SpeechletException {
		// Simple Display Card
				SimpleCard card = new SimpleCard();
				card.setTitle(":: Gender ::");

				String gendertype = intent.getSlot(SLOT_GENDERTYPE) != null ? intent.getSlot(SLOT_GENDERTYPE).getValue() : "";

				if(gendertype!=null){
				String answer = "The Number of Total Trials for Gernder Type" + gendertype;

				String finalSpeechOut = "";

				String finalCardOut = "";

				String totalTrialsCount = executeTotalTrialsGenderQuery(gendertype); // "203090661";

				finalSpeechOut = answer + " is <break time=\"0.2s\" /> <say-as interpret-as=\"cardinal\"> " + totalTrialsCount
						+ "</say-as>";

				finalCardOut = answer + " is " + totalTrialsCount;

				card.setContent(finalCardOut);

				String repromptText = " ";

				return newAskResponse("<speak>" + finalSpeechOut + "</speak>", true, "<speak>" + repromptText + "</speak>",
						true, card);
				}
				
				else return getHelp();
			}


	private SpeechletResponse getTotalTrialsConditionResponse(final Intent intent, final Session session)
			throws SpeechletException {
		// Simple Display Card
				SimpleCard card = new SimpleCard();
				card.setTitle(":: Condition ::");
				
				String sponsor = intent.getSlot(SLOT_SPONSOR) != null ? intent.getSlot(SLOT_SPONSOR).getValue() : "";
				String condition = intent.getSlot(SLOT_CONDITION) != null ? intent.getSlot(SLOT_CONDITION).getValue() : "";
				
				if(sponsor!=null || condition !=null){
					
					String answer="";					
				  
				    if(sponsor!=null && condition!=null) answer = "The Number of Total Studies for " + condition + " and for " + sponsor;
				    else if(sponsor==null && condition!=null) answer = "The Number of Total Studies for " + condition; 
				    
				 String  finalSpeechOut = "";

				String finalCardOut = "";

				String totalTrialsCount = executeTotalTrialsCondition(sponsor, condition); // "203090661";

				finalSpeechOut = answer + " is <break time=\"0.2s\" /> <say-as interpret-as=\"cardinal\"> " + totalTrialsCount
						+ "</say-as>";

				finalCardOut = answer + " is " + totalTrialsCount;

				card.setContent(finalCardOut);

				String repromptText = "";

				return newAskResponse("<speak>" + finalSpeechOut + "</speak>", true, "<speak>" + repromptText + "</speak>",
						true, card);
				}
				else return getHelp();
			}
	/**
	 * Creates a Dynamic Query for Total Trials based on the Study Type, executes
	 * the Query and returns the result.
	 *
	 * @param intent
	 *            - the intent for the request
	 * @param session
	 * @return SpeechletResponse spoken and visual response for the given intent
	 * @throws SpeechletExxception
	 * 
	 */
	
	
	private SpeechletResponse getTotalTrialsResponse(final Intent intent, final Session session)
			throws SpeechletException {
		// Simple Display Card
				SimpleCard card = new SimpleCard();
				card.setTitle(":: Study Type ::");

				String studytype = intent.getSlot(SLOT_STUDYTYPE) != null ? intent.getSlot(SLOT_STUDYTYPE).getValue() : "";
				String sponsor = intent.getSlot(SLOT_SPONSOR) != null ? intent.getSlot(SLOT_SPONSOR).getValue() : "";
				String condition = intent.getSlot(SLOT_CONDITION) != null ? intent.getSlot(SLOT_CONDITION).getValue() : "";
				
				if(sponsor!=null || studytype!=null || condition !=null){
					
					String answer="";
					
				    if(sponsor==null && condition==null && studytype!=null ) answer = "The Number of Total Trials for " + studytype;
				    else if(sponsor!=null && studytype!=null && condition==null) answer = "The Number of Total Trials for " + studytype + " and for " + sponsor;				   
				    else if(sponsor!=null && studytype!=null && condition!=null) answer = "The Number of Total Studies for " + condition + "  for " + sponsor +" for " + studytype; 
				    
				 String  finalSpeechOut = "";

				String finalCardOut = "";

				String totalTrialsCount = executeTotalTrialsQuery(studytype, sponsor, condition); // "203090661";

				finalSpeechOut = answer + " is <break time=\"0.2s\" /> <say-as interpret-as=\"cardinal\"> " + totalTrialsCount
						+ "</say-as>";

				finalCardOut = answer + " is " + totalTrialsCount;

				card.setContent(finalCardOut);

				String repromptText = "";

				return newAskResponse("<speak>" + finalSpeechOut + "</speak>", true, "<speak>" + repromptText + "</speak>",
						true, card);
				}
				else return getHelp();
			}

	/**
	 * Creates a Dynamic Query for Total Studies based on the Phases, executes
	 * the Query and returns the result.
	 *
	 * @param intent
	 *            - the intent for the request
	 * @param session
	 * @return SpeechletResponse spoken and visual response for the given intent
	 * @throws SpeechletExxception
	 * 
	 */	
	
	private SpeechletResponse getTotalStudiesResponse(final Intent intent, final Session session)
			throws SpeechletException {

		// Simple Display Card
		SimpleCard card = new SimpleCard();
		card.setTitle(":: Total Studies ::");

		String phase = intent.getSlot(SLOT_PHASE) != null ? intent.getSlot(SLOT_PHASE).getValue() : "";
		String sponsor = intent.getSlot(SLOT_SPONSOR) != null ? intent.getSlot(SLOT_SPONSOR).getValue() : "";
       
		
        if(phase!=null){
			String answer="";
			
			if(sponsor!=null) answer = "The Number of Total Studies in " + phase +" and by "+sponsor;
			else if(sponsor==null) answer = "The Number of Total Studies in " + phase;

		String finalSpeechOut = "";

		String finalCardOut = "";

		String totalStudiesCount = executeTotalStudiesQuery(phase, sponsor); // "203090661";

		finalSpeechOut = answer + " is <break time=\"0.2s\" /> <say-as interpret-as=\"cardinal\"> " + totalStudiesCount
				+ "</say-as>";

		finalCardOut = answer + " is " + totalStudiesCount;

		card.setContent(finalCardOut);

		String repromptText = " ";

		return newAskResponse("<speak>" + finalSpeechOut + "</speak>", true, "<speak>" + repromptText + "</speak>",
				true, card);
		}
		
		else return getHelp();
	}

	/**
	 * Creates a Dynamic Query for Recruitment based on the
	 * Status(Withdrawn/Completed etc.. ), executes the Query and returns the
	 * result
	 *
	 * @param intent
	 *            the intent for the request
	 * @param session
	 * @return SpeechletResponse spoken and visual response for the given intent
	 * @throws SpeechletException
	 * 
	 */
	private SpeechletResponse getRecruitmentResponse(final Intent intent, final Session session)
			throws SpeechletException {

		// Simple Display Card
		SimpleCard card = new SimpleCard();
		card.setTitle(":: Recruitment Count :: ");

		String status = intent.getSlot(SLOT_STATUS) != null ? intent.getSlot(SLOT_STATUS).getValue() : "";
		String sponsor= intent.getSlot(SLOT_SPONSOR) != null ? intent.getSlot(SLOT_SPONSOR).getValue() : "";
	    
		if(status!=null){
			String answer="";
			
			if(sponsor==null) answer = "The number of " + status +" trials "  ;
			else if (sponsor!=null) answer = "The number of " + status +" trials for " + sponsor  ;

		String recruitmentCount = executeRecruitmentQuery(status, sponsor); // "2376586";

		String finalSpeechOut = answer  + " is <break time=\"0.8s\" /> <say-as interpret-as=\"cardinal\"> "
				+ recruitmentCount + "</say-as>";

		String finalCardOut = answer + " is " + recruitmentCount;

		card.setContent(finalCardOut);

		String repromptText = " ";
	    
		return newAskResponse("<speak>" + finalSpeechOut + "</speak>", true, "<speak>" + repromptText + "</speak>",
				true, card);
	    }
	    else
	    	return getHelp();
	    			
		}

	
	/**
	 * Instructs the user on how to interact with this skill.
	 */
	private SpeechletResponse getHelp() {

		String speechOutput = "Can you please repeat <break time=\"0.2s\" />";

		String repromptText = "Can you please repeat <break time=\"0.2s\" />";				

		return newAskResponse("<speak>" + speechOutput + "</speak>", true, "<speak>" + repromptText + "</speak>", true, false);
	}

	/**
	 * Provides more help on how to interact with this skill.
	 */
	private SpeechletResponse getMoreHelp() throws SpeechletException {

		String speechOutput = "Waiting for your query!";

		String repromptText = "Here is a samples question <break time=\"0.2s\" />"
				+ "Give me the number of active trials for a sponsored company <break time=\"0.3s\" /> ";
		
		

		// Here we are prompting the user for input
		return newAskResponse(speechOutput, false, "<speak>" + repromptText + "</speak>", true, false);
	}

	/**
	 * Wrapper for creating the Ask response from the input strings.
	 * 
	 * @param stringOutput
	 *            the output to be spoken
	 * @param isOutputSsml
	 *            whether the output text is of type SSML
	 * @param repromptText
	 *            the reprompt for if the user doesn't reply or is
	 *            misunderstood.
	 * @param isRepromptSsml
	 *            whether the reprompt text is of type SSML
	 * @param displayCard
	 *            the display text to be sent to device
	 * @return SpeechletResponse the speechlet response
	 */
	private SpeechletResponse newAskResponse(String stringOutput, boolean isOutputSsml, String repromptText,
			boolean isRepromptSsml, Card displayCard) {
		OutputSpeech outputSpeech, repromptOutputSpeech;
		if (isOutputSsml) {
			outputSpeech = new SsmlOutputSpeech();
			((SsmlOutputSpeech) outputSpeech).setSsml(stringOutput);
		} else {
			outputSpeech = new PlainTextOutputSpeech();
			((PlainTextOutputSpeech) outputSpeech).setText(stringOutput);
		}

		if (isRepromptSsml) {
			repromptOutputSpeech = new SsmlOutputSpeech();
			((SsmlOutputSpeech) repromptOutputSpeech).setSsml(repromptText);
		} else {
			repromptOutputSpeech = new PlainTextOutputSpeech();
			((PlainTextOutputSpeech) repromptOutputSpeech).setText(repromptText);
		}
		Reprompt reprompt = new Reprompt();
		reprompt.setOutputSpeech(repromptOutputSpeech);
		return SpeechletResponse.newAskResponse(outputSpeech, reprompt, displayCard);
	}

	/**
	 * Wrapper for creating the Ask response from the input strings.
	 * 
	 * @param stringOutput
	 *            the output to be spoken
	 * @param isOutputSsml
	 *            whether the output text is of type SSML
	 * @param repromptText
	 *            the reprompt for if the user doesn't reply or is
	 *            misunderstood.
	 * @param isRepromptSsml
	 *            whether the reprompt text is of type SSML
	 * @return SpeechletResponse the speechlet response
	 */
	private SpeechletResponse newAskResponse(String stringOutput, boolean isOutputSsml, String repromptText,
			boolean isRepromptSsml, boolean shouldEndSession) {
		OutputSpeech outputSpeech, repromptOutputSpeech;
		if (isOutputSsml) {
			outputSpeech = new SsmlOutputSpeech();
			((SsmlOutputSpeech) outputSpeech).setSsml(stringOutput);
		} else {
			outputSpeech = new PlainTextOutputSpeech();
			((PlainTextOutputSpeech) outputSpeech).setText(stringOutput);
		}

		if (isRepromptSsml) {
			repromptOutputSpeech = new SsmlOutputSpeech();
			((SsmlOutputSpeech) repromptOutputSpeech).setSsml(repromptText);
		} else {
			repromptOutputSpeech = new PlainTextOutputSpeech();
			((PlainTextOutputSpeech) repromptOutputSpeech).setText(repromptText);
		}
		Reprompt reprompt = new Reprompt();
		reprompt.setOutputSpeech(repromptOutputSpeech);
		
		//added code for Session Handling 
		SpeechletResponse response=new SpeechletResponse();
		  response.setShouldEndSession(shouldEndSession);
		  response.setOutputSpeech(outputSpeech);
		  response.setReprompt(reprompt);
		  return response;
		
		  //previous code
		//return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
	}
}
