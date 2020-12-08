package org.webgme.guest.socket;

import org.webgme.guest.socket.rti.*;

import org.cpswt.config.FederateConfig;
import org.cpswt.config.FederateConfigParser;
import org.cpswt.hla.InteractionRoot;
import org.cpswt.hla.base.AdvanceTimeRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Kaleb // import extra packages
import java.io.*;
import java.net.*;
// Kaleb //

// Define the Socket type of federate for the federation.

public class Socket extends SocketBase {
    private final static Logger log = LogManager.getLogger();

    private double currentTime = 0;
    
    // Kaleb // Define variables
    // START WITH simID as ZERO because java is zero indexed
    int simID = 0;   // Change simID based on socket number
    int numVars = 0;
    String eGSH=null, eGSC=null, setName=null, ePeople=null;
    String holder=null;
    String[] varNames=new String[]{"","","","","","","","","",""};
    String[] doubles= new String[]{"","","","","","","","","",""};
    int receivedID = 0, dummy=0;
    int j=0;
    boolean empty=true;

    String varNameSeparater = "@";
    String doubleSeparater = "$";
    String dataString = "";
    
    // Kaleb //

    public Socket(FederateConfig params) throws Exception {
        super(params);
    }

    private void checkReceivedSubscriptions() {
        InteractionRoot interaction = null;
        while ((interaction = getNextInteractionNoWait()) != null) {
            if (interaction instanceof Controller_Socket) {
                handleInteractionClass((Controller_Socket) interaction);
            }
            else {
                log.debug("unhandled interaction: {}", interaction.getClassName());
            }
        }
    }

    private void execute() throws Exception {
        if(super.isLateJoiner()) {
            log.info("turning off time regulation (late joiner)");
            currentTime = super.getLBTS() - super.getLookAhead();
            super.disableTimeRegulation();
        }

        /////////////////////////////////////////////
        // TODO perform basic initialization below //
        /////////////////////////////////////////////

        // Kaleb // Add socket here:
        InetAddress addr = InetAddress.getByName("192.168.1.111");  // the address needs to be changed
        ServerSocket welcomeSocket = new ServerSocket(6789, 50, addr);  // 6789 is port number. Can be changed
        java.net.Socket connectionSocket = welcomeSocket.accept(); // initial connection will be made at this point
        System.out.println("connection successful");
        log.info("connection successful");
     
        InputStreamReader inFromClient = new InputStreamReader(connectionSocket.getInputStream());
        BufferedReader buffDummy = new BufferedReader(inFromClient);
        DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
        // Kaleb // end socket
        
        AdvanceTimeRequest atr = new AdvanceTimeRequest(currentTime);
        putAdvanceTimeRequest(atr);

        if(!super.isLateJoiner()) {
            log.info("waiting on readyToPopulate...");
            readyToPopulate();
            log.info("...synchronized on readyToPopulate");
        }

        ///////////////////////////////////////////////////////////////////////
        // TODO perform initialization that depends on other federates below //
        ///////////////////////////////////////////////////////////////////////

        if(!super.isLateJoiner()) {
            log.info("waiting on readyToRun...");
            readyToRun();
            log.info("...synchronized on readyToRun");
        }

        startAdvanceTimeThread();
        log.info("started logical time progression");
        
        
        // Kaleb // Define variables
        
        String header, time="0", varName="", value="";        
        double varValue=0;
        String dataString ="";
        int size = 0;

        // Kaleb //
        
        while (!exitCondition) {
            atr.requestSyncStart();
            enteredTimeGrantedState();

            ////////////////////////////////////////////////////////////
            // TODO send interactions that must be sent every logical //
            // time step below                                        //
            ////////////////////////////////////////////////////////////

            // Set the interaction's parameters.
            //
            //    Socket_Controller vSocket_Controller = create_Socket_Controller();
            //    vSocket_Controller.set_actualLogicalGenerationTime( < YOUR VALUE HERE > );
            //    vSocket_Controller.set_dataString( < YOUR VALUE HERE > );
            //    vSocket_Controller.set_federateFilter( < YOUR VALUE HERE > );
            //    vSocket_Controller.set_originFed( < YOUR VALUE HERE > );
            //    vSocket_Controller.set_simID( < YOUR VALUE HERE > );
            //    vSocket_Controller.set_size( < YOUR VALUE HERE > );
            //    vSocket_Controller.set_sourceFed( < YOUR VALUE HERE > );
            //    vSocket_Controller.sendInteraction(getLRC(), currentTime + getLookAhead());

            checkReceivedSubscriptions();
            
            // Kaleb // 
            if((header = buffDummy.readLine()).equals("TERMINATE")){
            	exitCondition = true;
            			
            }

            time = buffDummy.readLine();
            System.out.println("in loop header=" + header + " t=" + time);
            
            while(!(varName = buffDummy.readLine()).isEmpty()) {
                value = buffDummy.readLine();
                System.out.println("Received: " + varName + " as " + value);
                
                // before @ is varName and before $ is value
                // varName first!!!

                if(varName.equals("epSendOutdoorAirTemp")){
              	  
                	dataString = dataString +varName+"@";
              	  	dataString = dataString +value+"$";  
              	  	size = size +1;
              	  
                }
                
                if(varName.equals("epSendZoneMeanAirTemp")){
            
                	dataString = dataString +varName+"@";
              	  	dataString = dataString +value+"$";  
              	  	size = size +1;
                	  
                }
                
                if(varName.equals("epSendZoneHumidity")){
              	  
              	  	dataString = dataString +varName+"@";
              	  	dataString = dataString +value+"$";  
              	  	size = size +1;
              	  
                }
                
            }

            System.out.println("dataString before removing last char: "+dataString);
            dataString = dataString.substring(0,dataString.length()-2);
            System.out.println("dataString after removing last char: "+dataString);

            Socket_Controller sendEPData = create_Socket_Controller();
            sendEPData.set_simID(simID);
            sendEPData.set_size(size);
            sendEPData.set_dataString(dataString);
            log.info("Sent sendEPData interaction from socket{} with {}", simID , dataString);
            sendEPData.sendInteraction(getLRC(), currentTime + getLookAhead());
            
            
            if (empty==true) {
                outToClient.writeBytes("NOUPDATE\r\n\r\n");
                } 
            else {
                outToClient.writeBytes("SET\r\n" + time + "\r\n"+ "epGetStartCooling\r\n" + eGSC + "\r\n" + "epGetStartHeating\r\n" + eGSH + "\r\n" + "\r\n");
                System.out.println("SET\r\n" + time +  "\r\n"+ "epGetStartCooling\r\n" + eGSC + "\r\n" + "epGetStartHeating\r\n" + eGSH + "\r\n" + "\r\n");
                }
            outToClient.flush();
            
            // Kaleb //
                
            
            ////////////////////////////////////////////////////////////////////
            // TODO break here if ready to resign and break out of while loop //
            ////////////////////////////////////////////////////////////////////

            if (!exitCondition) {
                currentTime += super.getStepSize();
                AdvanceTimeRequest newATR =
                    new AdvanceTimeRequest(currentTime);
                putAdvanceTimeRequest(newATR);
                atr.requestSyncEnd();
                atr = newATR;
            }
        }

        // call exitGracefully to shut down federate
        exitGracefully();

        //////////////////////////////////////////////////////////////////////
        // TODO Perform whatever cleanups are needed before exiting the app //
        //////////////////////////////////////////////////////////////////////
        
    }

    private void handleInteractionClass(Controller_Socket interaction) {
        ///////////////////////////////////////////////////////////////
        // TODO implement how to handle reception of the interaction //
        ///////////////////////////////////////////////////////////////
    	
    	// Kaleb // 
    	
    	receivedID = interaction.get_simID();
    	if(receivedID == simID){
    		numVars = interaction.get_size();
    		holder = interaction.get_dataString();
            System.out.println("holder = "+ holder );
    		// before @ is varName and before $ is value
            // varName first!!!
            
            // // ------------------------------ This method didnt work
            // for(int i=0; i<holder.length(); i++){

            //     if(holder.substring(i,i).equals(varNameSeparater)){
            //         varNames[j] = holder.substring(dummy,i-1);
            //         dummy =  i+1;
            //     }
            //     else if(holder.substring(i,i).equals(doubleSeparater)){
            //         doubles[j] = holder.substring(dummy,i-1);
            //         dummy =  i+1;
            //         j = j+1;
            //     }
            // }
            // // -------------------------------------------------

            String vars[] = holder.split("$");
            j=0;
            for( String token : vars){
                System.out.println("token before removing last char = " +token);
                token = token.substring(0,token.length()-2);
                System.out.println("token after removing last char = " +token);
                String token1[] = token.split("@");
                varNames[j] = token1[0].substring(0,token1[0].length()-2);
                doubles[j] = token1[1].substring(0,token1[1].length()-2);
                System.out.println("varNames = "+ varNames[j] );
                System.out.println("doubles = "+ doubles[j] );
                j = j+1;
            }

            

            String value = "20";
        	empty = false;

        	
    		for(int i =0; i<numVars; i++){

    			setName = varNames[i];
    			value = doubles[i];
	        	System.out.println("ReceivedData interaction " + setName + " as " + value);
	        	if(setName.equals("epGetStartHeating")){
	        		eGSH = value;
	        		System.out.println("Received Heating setpoint as" + setName + eGSH);
	        		log.info("Received Heating setpoint as {} = {}" , setName , eGSH);
	        	}
	        	if(setName.equals("epGetStartCooling")){
	        		eGSC = value;
	        		System.out.println("Received Cooling setpoint as" + setName + eGSC);
	        		log.info("Received Cooling setpoint as {} = {}" , setName , eGSC);
	        	}
	        	if(setName.equals("epGetPeople")){
	        		ePeople = value;
	        		System.out.println("Received People as" + setName + ePeople);
	        		log.info("Received People as {} = {}" , setName , ePeople);
	        	}
    		}

            
    	}
    	
    	
    	
    	// Kaleb //   	
    	
    }

    public static void main(String[] args) {
        try {
            FederateConfigParser federateConfigParser =
                new FederateConfigParser();
            FederateConfig federateConfig =
                federateConfigParser.parseArgs(args, FederateConfig.class);
            Socket federate =
                new Socket(federateConfig);
            federate.execute();
            log.info("Done.");
            System.exit(0);
        }
        catch (Exception e) {
            log.error(e);
            System.exit(1);
        }
    }
}
