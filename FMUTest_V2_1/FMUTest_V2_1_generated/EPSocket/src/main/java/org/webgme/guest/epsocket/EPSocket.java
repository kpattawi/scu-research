package org.webgme.guest.epsocket;

import org.webgme.guest.epsocket.rti.*;

import org.cpswt.config.FederateConfig;
import org.cpswt.config.FederateConfigParser;
import org.cpswt.hla.InteractionRoot;
import org.cpswt.hla.base.AdvanceTimeRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.*;


// Define the EPSocket type of federate for the federation.

public class EPSocket extends EPSocketBase {
    private final static Logger log = LogManager.getLogger();

    private double currentTime = 0;

    boolean empty=false;
    String eGSH="0.0"; 
    String eGSC="0.0"; 
    String DataFromThermostat=null;
    String NumberofIncomeData=null;
    public String Ipaddress = "";
    public int PortNum = 0;
    

    public EPSocket(InputSourceConfig params) throws Exception {
        super(params);
        Ipaddress = params.IP_address;
        PortNum = params.Port_Number;
    }

    private void checkReceivedSubscriptions() {
        InteractionRoot interaction = null;
        while ((interaction = getNextInteractionNoWait()) != null) {
            if (interaction instanceof SendToEP) {
                handleInteractionClass((SendToEP) interaction);
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
        System.out.println("IP address is "+ Ipaddress);
        System.out.println("Port Number is "+ PortNum);
        
        InetAddress addr = InetAddress.getByName(Ipaddress);  // the address needs to be changed
        ServerSocket welcomeSocket = new ServerSocket(PortNum, 50, addr);  // 6789 is port number. Can be changed
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

        String header, time="0";
        String varname, value;

        double varValue;

     

        
        while (!exitCondition) {
            String data="";
            int numberOfData = 0;
            int simID = 1;

            atr.requestSyncStart();
            enteredTimeGrantedState();

            ////////////////////////////////////////////////////////////
            // TODO send interactions that must be sent every logical //
            // time step below                                        //
            ////////////////////////////////////////////////////////////

            // Set the interaction's parameters.
            //
            //    ReceiveFromEP receiveFromEP = create_ReceiveFromEP();
            //    receiveFromEP.set_Data( < YOUR VALUE HERE > );
            //    receiveFromEP.set_NumberOfVariable( < YOUR VALUE HERE > );
            //    receiveFromEP.set_SimID( < YOUR VALUE HERE > );
            //    receiveFromEP.set_actualLogicalGenerationTime( < YOUR VALUE HERE > );
            //    receiveFromEP.set_federateFilter( < YOUR VALUE HERE > );
            //    receiveFromEP.set_originFed( < YOUR VALUE HERE > );
            //    receiveFromEP.set_sourceFed( < YOUR VALUE HERE > );
            //    receiveFromEP.sendInteraction(getLRC(), currentTime + getLookAhead());

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
              data = data + varname + "@" + value + ",";
              numberOfData = numberOfData + 1;
            }
            ReceiveFromEP datatoThermostat = create_ReceiveFromEP();

            datatoThermostat.set_Data(data);
            datatoThermostat.set_NumberOfVariable(String.valueOf(numberOfData));
            datatoThermostat.set_SimID(simID);
            datatoThermostat.sendInteraction(getLRC(), currentTime + getLookAhead());
            System.out.println("Send out ==>"+data);

            if (empty==true) {
                outToClient.writeBytes("NOUPDATE\r\n\r\n");
            } else {
                outToClient.writeBytes("SET\r\n" + time + "\r\n"+ "fmuCOOL_SETP_DELTA\r\n" + eGSC + "\r\n" + "fmuHEAT_SETP_DELTA\r\n" + eGSH + "\r\n" + "\r\n");
                System.out.println("SET\r\n" + time +  "\r\n"+ "fmuCOOL_SETP_DELTA\r\n" + eGSC + "\r\n" + "fmuHEAT_SETP_DELTA\r\n" + eGSH + "\r\n" + "\r\n");
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

    private void handleInteractionClass(SendToEP interaction) {
        ///////////////////////////////////////////////////////////////
        // TODO implement how to handle reception of the interaction //
        ///////////////////////////////////////////////////////////////
        empty = false;
        DataFromThermostat = interaction.get_Data();
        NumberofIncomeData = interaction.get_NumberOfVariable();

        String vars[] = DataFromThermostat.split(",");
        ///////////////////////////////////////////////////////////////
        for(String token : vars) {
            System.out.println(token);
            String token1[]= token.split("@");
            if (token1[0].equals("HEAT_SETP_DELTA")){
                eGSH = token1[1];
            }
            if (token1[0].equals("COOL_SETP_DELTA")){
                eGSC = token1[1];
            }
            
        }
        ///////////////////////////////////////////////////////////////        
    }

    public static void main(String[] args) {
        try {
            FederateConfigParser federateConfigParser =
                new FederateConfigParser();
            // FederateConfig federateConfig =
            //     federateConfigParser.parseArgs(args, FederateConfig.class);
            InputSourceConfig federateConfig = federateConfigParser.parseArgs(args, InputSourceConfig.class);
            EPSocket federate =
                new EPSocket(federateConfig);
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
