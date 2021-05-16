package org.webgme.guest.socket;

import org.webgme.guest.socket.rti.*;

import org.cpswt.config.FederateConfig;
import org.cpswt.config.FederateConfigParser;
import org.cpswt.hla.InteractionRoot;
import org.cpswt.hla.base.AdvanceTimeRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Kaleb // import extra packages
import org.cpswt.utils.CpswtUtils;
import java.io.*;
import java.net.*;
// Kaleb //

// Define the Socket type of federate for the federation.

public class Socket extends SocketBase {
    private final static Logger log = LogManager.getLogger();

    private double currentTime = 0;
    
    // Kaleb // Config stuff TODO
    // START WITH simID as ZERO because java is zero indexed
    int simID = 0;   // Change simID based on socket number

    // Kaleb // Define global variables
    String[] varNames=new String[15];  // will have to add more empty strings based on how many strings we send/receive
    String[] doubles= new String[15];  // will have to add more empty strings based on how many strings we send/receive
    String varNameSeparater = "@";
    String doubleSeparater = ",";
    int numVars = 0;  
    String eGSH=null, eGSC=null, ePeople=null;  //values sent to EnergyPlus
    boolean empty=true;
    boolean receivedSimTime = false;    // this is for "received" interaction while loop
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

        
        // Read IP address and Port number from config.txt 
        log.info("create bufferedReader");
        File file= new File("/home/vagrant/Desktop/GitHub/scu_research/ucef_projects/EPMultipleSims_v4/EPMultipleSims_v4_generated/config.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        log.info("bufferedreader successful");
        String st = "";
        String ipAdd = "";
        int portNo = 0;
        while ((st = br.readLine())!=null){
            log.info(st);
            if(st.equals("ip_adress:")){
                ipAdd = br.readLine();
            }
            if(st.equals("port_number:")){
                portNo = Integer.valueOf(br.readLine());
            }
        }
        log.info(ipAdd);
        log.info(portNo);
        
        log.info("Waiting for EnergyPlus simulations to join...");
        // end test config.txt

        // Kaleb // Add socket here: 
        InetAddress addr = InetAddress.getByName(ipAdd);  // the address needs to be changed in config.txt
        ServerSocket welcomeSocket = new ServerSocket(portNo, 50, addr);  // Can also be changed in config.txt
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
        
        // Kaleb // Define variables for getting EP data
        String header, time="0", varName="", value="";        
        double varValue=0;
        String dataString ="";
        // Kaleb //
        
        while (!exitCondition) {
            atr.requestSyncStart();
            enteredTimeGrantedState();

            ////////////////////////////////////////////////////////////
            // TODO send interactions that must be sent every logical //
            // time step below                                        //
            ////////////////////////////////////////////////////////////
            
            // Kaleb // Getting values from  fmu
            if((header = buffDummy.readLine()).equals("TERMINATE")){
            	exitCondition = true;
            }
            time = buffDummy.readLine();
            System.out.println("in loop header=" + header + " t=" + time);
            
            while(!(varName = buffDummy.readLine()).isEmpty()) {
                value = buffDummy.readLine();
                System.out.println("Received: " + varName + " as " + value);
                // Add any variable that you want to get from EnergyPlus here...
                // Names have to match the modelDescription.xml file
                // before @ is varName and before , is value
                // varName first!!!
                if(varName.equals("epSendOutdoorAirTemp")){
                	dataString = dataString +varName+varNameSeparater;
              	  	dataString = dataString +value+doubleSeparater; 
                }
                if(varName.equals("epSendZoneMeanAirTemp")){
                	dataString = dataString +varName+varNameSeparater;
              	  	dataString = dataString +value+doubleSeparater; 
                }
                if(varName.equals("epSendZoneHumidity")){
              	  	dataString = dataString +varName+varNameSeparater;
              	  	dataString = dataString +value+doubleSeparater;  
                }
                if(varName.equals("epSendHeatingEnergy")){
              	  	dataString = dataString +varName+varNameSeparater;
              	  	dataString = dataString +value+doubleSeparater;
                }
                if(varName.equals("epSendCoolingEnergy")){
              	  	dataString = dataString +varName+varNameSeparater;
              	  	dataString = dataString +value+doubleSeparater;
                }
                if(varName.equals("epSendNetEnergy")){
              	  	dataString = dataString +varName+varNameSeparater;
              	  	dataString = dataString +value+doubleSeparater;  
                }
                if(varName.equals("epSendEnergyPurchased")){
              	  	dataString = dataString +varName+varNameSeparater;
              	  	dataString = dataString +value+doubleSeparater; 
                }
                if(varName.equals("epSendEnergySurplus")){
              	  	dataString = dataString +varName+varNameSeparater;
              	  	dataString = dataString +value+doubleSeparater;  
                }
                if(varName.equals("epSendDayOfWeek")){
              	  	dataString = dataString +varName+varNameSeparater;
              	  	dataString = dataString +value+doubleSeparater; 
                }
                if(varName.equals("epSendSolarRadiation")){
              	  	dataString = dataString +varName+varNameSeparater;
              	  	dataString = dataString +value+doubleSeparater;
                }
                if(varName.equals("epSendHeatingSetpoint")){
              	  	dataString = dataString +varName+varNameSeparater;
              	  	dataString = dataString +value+doubleSeparater;
                }
                if(varName.equals("epSendCoolingSetpoint")){
              	  	dataString = dataString +varName+varNameSeparater;
              	  	dataString = dataString +value+doubleSeparater;  
                }
            }
            // for checking timestep
            dataString = dataString+"timestep"+varNameSeparater+String.valueOf(currentTime)+doubleSeparater;

            // Send Socket_Controller interaction containing eplus data
            Socket_Controller sendEPData = create_Socket_Controller();
            sendEPData.set_simID(simID);
            sendEPData.set_dataString(dataString);
            log.info("Sent sendEPData interaction from socket{} with {}", simID , dataString);
            sendEPData.sendInteraction(getLRC());

            // Wait to receive Controller_Socket information containing setpoints that will be sent to eplus
            while (!receivedSimTime){
                log.info("waiting to receive SimTime...");
                synchronized(lrc){
                    lrc.tick();
                }
                checkReceivedSubscriptions();
                if(!receivedSimTime){
                    CpswtUtils.sleep(100);
                }
            }
            receivedSimTime = false;
            // 

            // Empty Data String for next time step
            dataString = "";
            
            // send eGSH and eGSC to eplus, if you want to send something else to EnergyPlus need to add here
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
        // exit while loop above waiting for Controller_Socket
        receivedSimTime = true;

        // epvalues are not empty
        empty = false;
    	
        // get dataString from Controller and separate into varNames and doubles
    	int receivedID = interaction.get_simID();
        String holder = null;
    	if(receivedID == simID){
    		holder = interaction.get_dataString();
            System.out.println("holder = "+ holder );
    		
            String vars[] = holder.split(doubleSeparater);
            System.out.println("vars[0] = "+vars[0]);
            int j=0;
            for( String token : vars){
                System.out.println("token = " +token);
                String token1[] = token.split(varNameSeparater);
                System.out.println("token1[0] = "+token1[0]);
                System.out.println("token1[1] = "+token1[1]);
                varNames[j] = token1[0];
                doubles[j] = token1[1];
                System.out.println("varNames[j] = "+ varNames[j] );
                System.out.println("doubles[j] = "+ doubles[j] );
                j = j+1;
            }

    		for(int i =0; i<j; i++){
	        	System.out.println("ReceivedData interaction " + varNames[i] + " as " + doubles[i]);
                // if you are receiving something else besides variables listed below, add another if()
	        	if(varNames[i].equals("epGetStartHeating")){
	        		eGSH = doubles[i];
	        		System.out.println("Received Heating setpoint as" + varNames[i] + eGSH);
	        		log.info("Received Heating setpoint as {} = {}" , varNames[i] , eGSH);
	        	}
	        	if(varNames[i].equals("epGetStartCooling")){
	        		eGSC = doubles[i];
	        		System.out.println("Received Cooling setpoint as" + varNames[i] + eGSC);
	        		log.info("Received Cooling setpoint as {} = {}" , varNames[i] , eGSC);
	        	}
	        	if(varNames[i].equals("epGetPeople")){
	        		ePeople = doubles[i];
	        		System.out.println("Received People as" + varNames[i] + ePeople);
	        		log.info("Received People as {} = {}" , varNames[i] , ePeople);
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
