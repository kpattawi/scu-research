package org.webgme.guest.socket;

import org.webgme.guest.socket.rti.*;

import org.cpswt.config.FederateConfig;
import org.cpswt.config.FederateConfigParser;
import org.cpswt.hla.InteractionRoot;
import org.cpswt.hla.base.AdvanceTimeRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.*;

// Define the Socket type of federate for the federation.

public class Socket extends SocketBase {
    private final static Logger log = LogManager.getLogger();

    private double currentTime = 0;

    String eGSH=null, eGSC=null, setName=null, ePeople=null;
    boolean empty=true;
    
    public Socket(FederateConfig params) throws Exception {
        super(params);
    }

    private void checkReceivedSubscriptions() {
        InteractionRoot interaction = null;
        while ((interaction = getNextInteractionNoWait()) != null) {
            if (interaction instanceof SendEP) {
                handleInteractionClass((SendEP) interaction);                
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
        
        ///////////////////////////////////////Socket
        InetAddress addr = InetAddress.getByName("192.168.56.101");  // the address needs to be changed
        ServerSocket welcomeSocket = new ServerSocket(6789, 50, addr);  // 6789 is port number. Can be changed
        java.net.Socket connectionSocket = welcomeSocket.accept(); // initial connection will be made at this point
        System.out.println("connection successful");
        log.info("connection successful");
     
        InputStreamReader inFromClient = new InputStreamReader(connectionSocket.getInputStream());
        BufferedReader buffDummy = new BufferedReader(inFromClient);
        DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
        ///////////////////////////////////////Socket

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
        
        String header, time="0", varname, value;
        double varValue;

     

        

        while (!exitCondition) {
            atr.requestSyncStart();
            enteredTimeGrantedState();

            checkReceivedSubscriptions();
            
            
            if((header = buffDummy.readLine()).equals("TERMINATE")){
            	exitCondition = true;
            			
            }
            
            time = buffDummy.readLine();
            System.out.println("in loop header=" + header + " t=" + time);
           
            while(!(varname = buffDummy.readLine()).isEmpty()) {
              value = buffDummy.readLine();
              System.out.println("Received: " + varname + " as " + value);
              varValue = Double.parseDouble(value);
              
              if(varname.equals("epSendOutdoorAirTemp")){
            	  ReceiveEP sendOutTemp = create_ReceiveEP();
            	  
            	  sendOutTemp.set_varName( varname );
            	  sendOutTemp.set_value( varValue );
            	  
            	  log.info("Sent Out Temp interaction as {} = {}" , varname , varValue);
            	  
            	  sendOutTemp.sendInteraction(getLRC(), currentTime + getLookAhead());
              }
              
              if(varname.equals("epSendZoneMeanAirTemp")){
            	  ReceiveEP sendZoneTemp = create_ReceiveEP();
            	  
            	  sendZoneTemp.set_varName( varname );
            	  sendZoneTemp.set_value( varValue );
            	  
            	  log.info("Sent Zone Mean Temp interaction as {} = {}" , varname , varValue);
            	  
            	  sendZoneTemp.sendInteraction(getLRC(), currentTime + getLookAhead());
              }
              
              if(varname.equals("epSendZoneHumidity")){
            	  ReceiveEP sendRH = create_ReceiveEP();
            	  
            	  sendRH.set_varName( varname );
            	  sendRH.set_value( varValue );
            	  
            	  log.info("Sent Humidity interaction as {} = {}" , varname , varValue);
            	  
            	  sendRH.sendInteraction(getLRC(), currentTime + getLookAhead());
              }
            ////////////////////////////////////////////////////////////
            // TODO send interactions that must be sent every logical //
            // time step below                                        //
            ////////////////////////////////////////////////////////////

            // Set the interaction's parameters.
            //
            //    ReceiveEP vReceiveEP = create_ReceiveEP();
            //    vReceiveEP.set_actualLogicalGenerationTime( < YOUR VALUE HERE > );
            //    vReceiveEP.set_federateFilter( < YOUR VALUE HERE > );
            //    vReceiveEP.set_originFed( < YOUR VALUE HERE > );
            //    vReceiveEP.set_sourceFed( < YOUR VALUE HERE > );
            //    vReceiveEP.set_unit( < YOUR VALUE HERE > );
            //    vReceiveEP.set_value( < YOUR VALUE HERE > );
            //    vReceiveEP.set_varName( < YOUR VALUE HERE > );
            //    vReceiveEP.sendInteraction(getLRC(), currentTime + getLookAhead());

            }
            if (empty==true) {
                outToClient.writeBytes("NOUPDATE\r\n\r\n");
              } else {
                outToClient.writeBytes("SET\r\n" + time + "\r\n"+ "epGetStartCooling\r\n" + eGSC + "\r\n" + "epGetStartHeating\r\n" + eGSH + "\r\n" + "\r\n");
                System.out.println("SET\r\n" + time +  "\r\n"+ "epGetStartCooling\r\n" + eGSC + "\r\n" + "epGetStartHeating\r\n" + eGSH + "\r\n" + "\r\n");
              }
              outToClient.flush();

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

    private void handleInteractionClass(SendEP interaction) {
        ///////////////////////////////////////////////////////////////
        // TODO implement how to handle reception of the interaction //
        ///////////////////////////////////////////////////////////////
       
        
        double value = 20;
    	empty = false;
    	setName = interaction.get_varName();
    	System.out.println("Received Data " + setName);
    	if(setName.equals("epGetStartHeating")){
    		value = interaction.get_value();
    		eGSH = String.valueOf(value);
    		System.out.println("Received Heating setpoint interaction as" + setName + eGSH);
    		log.info("Received Heating setpoint interaction as {} = {}" , setName , eGSH);
    	}
    	if(setName.equals("epGetStartCooling")){
    		value = interaction.get_value();
    		eGSC = String.valueOf(value);
    		System.out.println("Received Heating setpoint interaction as" + setName + eGSC);
    		log.info("Received Cooling setpoint interaction as {} = {}" , setName , eGSC);
    	}
    	if(setName.equals("epGetPeople")){
    		value = interaction.get_value();
    		ePeople = String.valueOf(value);
    		System.out.println("Received Heating setpoint interaction as" + setName + ePeople);
    		log.info("Received People interaction as {} = {}" , setName , ePeople);
    	}
    	
    	
    	
    	
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
