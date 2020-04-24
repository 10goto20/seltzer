package burp;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;

/**
 * Extension Name: seltzer
 * Description: Scans a target using the Burp Suite REST API.  Exports HTML and XML reports.  Closes Burp Suite when the scan is complete.
 * Author: Gabriel Merritt
 */

@SuppressWarnings({ "unchecked", "unused" })
public class BurpExtender implements IBurpExtender, IHttpListener {

	private PrintWriter stdout;
	private PrintWriter stderr;
	
	private IBurpExtenderCallbacks mycallbacks;
	private IExtensionHelpers helpers;
	
	// variable for cli arguments
	private String[] cli; 
	
	// variables to handle paused scans - typically indicates an error state
	private int pausedthreshold = 6;
	private int pausedcount = 1;
	
	// variable to handle API version
	private String apiversion = "v0.1";
	
	// format for all dates
	private SimpleDateFormat formatter = new SimpleDateFormat("'['yyyy-MM-dd HH:mm:ss z']'");
	private Date date = new Date(System.currentTimeMillis());;
	
	public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {

		// register the extension name
		callbacks.setExtensionName("seltzer");
		
		// init printwriters for stdout and stderr
		stdout = new PrintWriter(callbacks.getStdout(), true);
		stderr = new PrintWriter(callbacks.getStderr(), true);
				
		// register ourselves as an HTTP listener
		callbacks.registerHttpListener(this);
		
		// set mycallbacks to callbacks for use by other methods in our class
		mycallbacks = callbacks;
			
		// use callbacks to get Burp helpers for use by other methods in our class
		helpers = callbacks.getHelpers();
		
		// print to stdout
		date = new Date(System.currentTimeMillis());
		stdout.println("");
		stdout.println(formatter.format(date) + "[seltzer] " + "seltzer loaded");
		
		// Get command line arguments
		cli = mycallbacks.getCommandLineArguments();
		
		// Check for minimum command line arguments
		if (cli.length < 2) {
			date = new Date(System.currentTimeMillis());
			stdout.println(formatter.format(date) + "[seltzer] " + "inadequate command line arguments passed");			
		}
		
		else {
			
			// echo all cli arguments passed except API key
			for (int i=0; i < cli.length; i++)
			{
				date = new Date(System.currentTimeMillis());
				if( i == 0 ) {stdout.println(formatter.format(date) + "[seltzer] server: " + cli[i]);}
				else if( i == 1 ) { /* stdout.println(formatter.format(date) + "[seltzer] api key: " + cli[i]); */ }
				else if( i == 2 ) {stdout.println(formatter.format(date) + "[seltzer] target: " + cli[i]);}
				else if( i == 3 ) {stdout.println(formatter.format(date) + "[seltzer] report: " + cli[i]);}
				else if( i == 4 ) {stdout.println(formatter.format(date) + "[seltzer] username: " + (cli[i].equals("notprovided") ? "not provided" : cli[i]) );}
				else if( i == 5 ) {stdout.println(formatter.format(date) + "[seltzer] password: " + (cli[i].equals("notprovided") ? "not provided" : cli[i]) );}
				else if( i == 6 ) {stdout.println(formatter.format(date) + "[seltzer] scan configuration: " + (cli[i].equals("notprovided") ? "not provided" : cli[i]) );}
				else if( i == 7 ) {stdout.println(formatter.format(date) + "[seltzer] resource pool: " + (cli[i].equals("notprovided") ? "not provided" : cli[i]) );}
				else {stdout.println(formatter.format(date) + "[seltzer] other cli: " + cli[i]);}	
			}
			
			// wait for burp to load everything
			try {
				date = new Date(System.currentTimeMillis());
				stdout.println(formatter.format(date) + "[seltzer] waiting for REST API...");
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// call startScan
			String id = startScan(cli);
			
			// check for valid scan id
			// if no scan id, exit burp
			if(id.equalsIgnoreCase("0")) {
				date = new Date(System.currentTimeMillis());
				stdout.println(formatter.format(date) + "[seltzer] failed to start scan for " + cli[2]);
				date = new Date(System.currentTimeMillis());
				stdout.println(formatter.format(date) + "[seltzer] " + "exiting burp");
				mycallbacks.exitSuite(false);				
			}
			else {
				date = new Date(System.currentTimeMillis());
				stdout.println(formatter.format(date) + "[seltzer] " + "scan " + cli[2] + " started with id " + id);
				Boolean complete = false;
				// while scan is not in complete state, call checkStatus
				// when scan is in complete state, export reports and close Burp
				while(!complete) {
					complete = checkStatus(id, ((cli[1].equalsIgnoreCase("none")) ? cli[0] : cli[0] + "/" + cli[1]));
					if(complete) {
						date = new Date(System.currentTimeMillis());
						stdout.println(formatter.format(date) + "[seltzer] scan " + id + " complete");
						
						// export report
						date = new Date(System.currentTimeMillis());
						stdout.println(formatter.format(date) + "[seltzer] scan " + id + " exporting report " + cli[3]);
						File f = new File(cli[3] + ".html");
						mycallbacks.generateScanReport("HTML", mycallbacks.getScanIssues(null), f);
						f = new File(cli[3] + ".xml");
						mycallbacks.generateScanReport("XML", mycallbacks.getScanIssues(null), f);
						
						// exit burp
						date = new Date(System.currentTimeMillis());
						stdout.println(formatter.format(date) + "[seltzer] " + "exiting burp");
						mycallbacks.exitSuite(false);
					}
					else {
						// no-op
					}
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageinfo) {

		if (!messageIsRequest) {	
			
		}
		
		if (messageIsRequest) {

		}
	}
	
	 /**
	 * Method Name: startScan Starts a scan using the REST API.
	 * @param  cli  A String array containing all command line paramteres.	
	 * @return      The scan id for the scan if successful else returns 0.
	 */
	public String startScan(String[] cli) {
		HttpURLConnection con = null;
		SimpleDateFormat formatter= new SimpleDateFormat("'['yyyy-MM-dd HH:mm:ss z']'");
		try {
			URL url = null;
			if(cli[1].equalsIgnoreCase("none")) {
				url = new URL(cli[0] + "/v0.1/scan");
			}
			else {
				url = new URL(cli[0] 
						+ "/" 
						+ cli[1] 
						+ "/"
						+ apiversion
						+ "/scan"
						);
			}
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			
			// master json object
			JSONObject msg = new JSONObject();
			
			// add urls to master object
			JSONArray urls = new JSONArray();
			urls.add(cli[2]);
			msg.put("urls", urls);
			
			// add application_logins to master object
			if((!cli[4].equals("notprovided")) && (!cli[5].equals("notprovided")))
			{  
				JSONObject mypairs = new JSONObject();
				mypairs.put("password", cli[5]);
				mypairs.put("username", cli[4]);
				JSONArray myarray = new JSONArray();
				myarray.add(mypairs);
				msg.put("application_logins", myarray);
			}
			
			// add scan_configurations to master object
			if(!cli[6].equals("notprovided"))
			{ 
				JSONObject mypairs = new JSONObject();
				mypairs.put("name", cli[6]);
				mypairs.put("type", "NamedConfiguration");
				JSONArray myarray = new JSONArray();
				myarray.add(mypairs);
				msg.put("scan_configurations", myarray);
			}
			
			// add resource_pool to master object
			if(!cli[7].equals("notprovided"))
			{ 
				msg.put("resource_pool", cli[7]);
			}
			
			con.setDoOutput(true);
			DataOutputStream outputStream = new DataOutputStream(con.getOutputStream());
			outputStream.write(msg.toJSONString().getBytes());
			outputStream.flush();
			outputStream.close();
			
			int status = con.getResponseCode();
			String id = con.getHeaderField("Location");
		    
		    BufferedReader in = new BufferedReader(
		    new InputStreamReader(con.getInputStream()));
		    	String inputLine;
		    	StringBuffer content = new StringBuffer();
		    	while ((inputLine = in.readLine()) != null) {
		    	    content.append(inputLine);
		    	}
		    in.close();
		    stdout.println(content.toString());
		    return id;
		}
		catch (Exception e) {
			date = new Date(System.currentTimeMillis());
			stdout.println(formatter.format(date) + "[seltzer] " + "error starting scan");
			try {
				BufferedReader in = new BufferedReader(
				new InputStreamReader(con.getErrorStream()));
				String inputLine;
				StringBuffer content = new StringBuffer();
				while ((inputLine = in.readLine()) != null) {
				    content.append(inputLine);
				}
				in.close();

				date = new Date(System.currentTimeMillis());
				stdout.println(formatter.format(date) + "[seltzer] " + "error: " + content.toString());
			} 
			catch (Exception e1) {
				date = new Date(System.currentTimeMillis());
				stdout.println(formatter.format(date) + "[seltzer] " + "scan server could not be contacted - check server configuration");
			}
		}
		return "0";
	}
	
	 /**
	 * Method Name: checkStatus Check the status of a scan using the REST API
	 * @param  id     The ID of the status to check.
	 * @param  server The Burp REST API server.
	 * @return boolean
 	 * returns true if the scan is in a succeeded, failed or unknown state 
	 * returns false if the scan is in a initializing, auditing, crawling state or if the scan is in a paused state for more than 60 seconds 
	 */
	public boolean checkStatus(String id, String server) {
	    
		// establish connection with the Burp REST API
		HttpURLConnection con = null;
		try {
			URL url = new URL(server 
					+ "/"
					+ apiversion
					+ "/scan/" 
					+ id);
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			String msgBody = "";
			
		    int status = con.getResponseCode();
		    BufferedReader in = new BufferedReader(
		    new InputStreamReader(con.getInputStream()));
		    	String inputLine;
		    	StringBuffer content = new StringBuffer();
		    	while ((inputLine = in.readLine()) != null) {
		    	    content.append(inputLine);
		    	}
		    in.close();

			JSONParser parser = new JSONParser();
			Reader reader = new StringReader(content.toString());
			Object jsonObj;
			jsonObj = parser.parse(reader);
			JSONObject jsonObject = (JSONObject) jsonObj;
			String scanstatus = (String) jsonObject.get("scan_status");
			JSONObject scanmetrics = (JSONObject) jsonObject.get("scan_metrics");
			long progress = (long) scanmetrics.get("crawl_and_audit_progress");
			String caption = (String) scanmetrics.get("crawl_and_audit_caption");
			reader.close();

		    // swtich for most scan status values
		    switch (scanstatus) {
			    case "initializing":
			    	date = new Date(System.currentTimeMillis());
			    	stdout.println(formatter.format(date) + "[seltzer] scan " + id + " " + scanstatus);
				if(caption != null){ stdout.println(formatter.format(date) + "[seltzer] scan " + id + " " + caption); }
			    	return false;
			    case "paused":
			    	stdout.println(formatter.format(date) + "[seltzer] scan " + id + " " + scanstatus);		    
				if(caption != null){ stdout.println(formatter.format(date) + "[seltzer] scan " + id + " " + caption); }
			    	if (pausedcount <= pausedthreshold) {
				    	stdout.println(formatter.format(date) + "[seltzer] scan " + id + " retry count " + pausedcount);		    
			    		pausedcount += 1;
					date = new Date(System.currentTimeMillis());
			    		return false;
			    	}
			    	else {
				    	stdout.println(formatter.format(date) + "[seltzer] scan " + id + " exceeded retry count aborting...");
			    		pausedcount = 0;
					date = new Date(System.currentTimeMillis());
			    		return true;
			    	}
			    case "auditing":
				    date = new Date(System.currentTimeMillis());
				    if (progress >= 0) {
					    stdout.println(formatter.format(date) + "[seltzer] scan " + id + " " + scanstatus + " " + String.valueOf(progress) + "% complete");				    	
				    }
				    else {
					    stdout.println(formatter.format(date) + "[seltzer] scan " + id + " " + scanstatus);
				    }
				if(caption != null){ stdout.println(formatter.format(date) + "[seltzer] scan " + id + " " + caption); }
			    	return false;
			    case "crawling":
			    	date = new Date(System.currentTimeMillis());
			    	stdout.println(formatter.format(date) + "[seltzer] scan " + id + " " + scanstatus);
 			        if(caption != null){ stdout.println(formatter.format(date) + "[seltzer] scan " + id + " " + caption); }
			    	return false;
			    case "succeeded":
			    	date = new Date(System.currentTimeMillis());
			    	stdout.println(formatter.format(date) + "[seltzer] scan " + id + " " + scanstatus);
 			        if(caption != null){ stdout.println(formatter.format(date) + "[seltzer] scan " + id + " " + caption); }
			    	return true;
			    case "failed":
			    	date = new Date(System.currentTimeMillis());
			    	stdout.println(formatter.format(date) + "[seltzer] scan " + id + " " + scanstatus);
 			        if(caption != null){ stdout.println(formatter.format(date) + "[seltzer] scan " + id + " " + caption); }
			    	return true;
			    default:
			    	date = new Date(System.currentTimeMillis());
			    	stdout.println(formatter.format(date) + "[seltzer] scan " + id + " " + scanstatus);
 			        if(caption != null){ stdout.println(formatter.format(date) + "[seltzer] scan " + id + " " + caption); }
			    	return true;
		    }
		}
		// error handling if Burp REST API cannot be reached or otherwise the status check has failed
		catch (Exception e) {
			date = new Date(System.currentTimeMillis());
			stdout.println(formatter.format(date) + "[seltzer] " + "error getting scan status");
			try {
				BufferedReader in = new BufferedReader(
				new InputStreamReader(con.getErrorStream()));
				String inputLine;
				StringBuffer content = new StringBuffer();
				while ((inputLine = in.readLine()) != null) {
				    content.append(inputLine);
				}
				in.close();

				date = new Date(System.currentTimeMillis());
				stdout.println(formatter.format(date) + "[seltzer] " + "error: " + content.toString());
			} 
			catch (Exception e1) {
				date = new Date(System.currentTimeMillis());
				stdout.println(formatter.format(date) + "[seltzer] " + "scan server could not be contacted - check server configuration");
			}
		}
		return false;
	}
}
