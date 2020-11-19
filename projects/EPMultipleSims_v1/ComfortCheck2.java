package org.webgme.guest.comfortcheck2;

import org.webgme.guest.comfortcheck2.rti.*;

import org.cpswt.config.FederateConfig;
import org.cpswt.config.FederateConfigParser;
import org.cpswt.hla.InteractionRoot;
import org.cpswt.hla.base.AdvanceTimeRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.Math;

// Define the ComfortCheck2 type of federate for the federation.

public class ComfortCheck2 extends ComfortCheck2Base {
    private final static Logger log = LogManager.getLogger();

    private double currentTime = 0;
    
    public ComfortCheck2(FederateConfig params) throws Exception {
        super(params);
    }
    
    double outTemp=23.0, zoneTemp=23.0, zoneHumidity=0,clo=1,tempDiff=5, optset=20;
    String varname; 
    double value;
    int Comfort=8;
    
    private void checkReceivedSubscriptions() {
        InteractionRoot interaction = null;
        while ((interaction = getNextInteractionNoWait()) != null) {
            if (interaction instanceof ReceiveEp2) {
                handleInteractionClass((ReceiveEp2) interaction);
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

        while (!exitCondition) {
            atr.requestSyncStart();
            enteredTimeGrantedState();

            checkReceivedSubscriptions();
            
            if (outTemp<10){
            	optset = 20.9;
            }else if(outTemp>33.5){
            	optset = 28.2;
            }else {
            	optset = 0.31*outTemp+17.8;
            }
            
            tempDiff = Math.abs(zoneTemp-optset);
            
            if (tempDiff<2){
            	Comfort = 0;
            }else if (tempDiff<3){
            	Comfort = 1;
            }else{
            	Comfort = 2;
            }
            
            // write to data to record comfort information
            
            try{
                  // Create new file
                  
                  String path="/home/vagrant/Desktop/EnergyPlusData/Adaptive_Test_Comfort2.txt";
                  File file = new File(path);

                  // If file doesn't exists, then create it
                  if (!file.exists()) {
                      file.createNewFile();
                  }

                  FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
                  BufferedWriter bw = new BufferedWriter(fw);

                  // Write in file
                  bw.write(currentTime + "\t" + Comfort +  "\n" );

                  // Close connection
                  bw.close();
              }
              catch(Exception e){
                  System.out.println(e);
              }
          //=====================================
            
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

    private void handleInteractionClass(ReceiveEp2 interaction) {
        ///////////////////////////////////////////////////////////////
        // TODO implement how to handle reception of the interaction //
        ///////////////////////////////////////////////////////////////
    	varname = interaction.get_varName();
    	value = interaction.get_value();
    	
        if(varname.equals("epSendOutdoorAirTemp")) {
            outTemp = value;
            System.out.println("Received Out Temp interaction as: " + varname + value);
        
          }
        if(varname.equals("epSendZoneMeanAirTemp")) {
            zoneTemp = value;
            System.out.println("Received Zone Temp interaction as: " + varname + value);
          }
        if(varname.equals("epSendZoneHumidity")) {
            zoneHumidity = value;
            System.out.println("Received  Humidity interaction as: " + varname + value);
          }
        if(varname.equals("epSendClo")) {
            clo = value;
            System.out.println("Received clo interaction as: " + varname + value);
          }
    }

    public static void main(String[] args) {
        try {
            FederateConfigParser federateConfigParser =
                new FederateConfigParser();
            FederateConfig federateConfig =
                federateConfigParser.parseArgs(args, FederateConfig.class);
            ComfortCheck2 federate =
                new ComfortCheck2(federateConfig);
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
