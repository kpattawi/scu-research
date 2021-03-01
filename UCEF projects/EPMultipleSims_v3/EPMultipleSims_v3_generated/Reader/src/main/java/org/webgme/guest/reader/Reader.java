package org.webgme.guest.reader;

import org.webgme.guest.reader.rti.*;

import org.cpswt.config.FederateConfig;
import org.cpswt.config.FederateConfigParser;
import org.cpswt.hla.base.AdvanceTimeRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Define the Reader type of federate for the federation.

public class Reader extends ReaderBase {
    private final static Logger log = LogManager.getLogger();

    private double currentTime = 0;

    public Reader(FederateConfig params) throws Exception {
        super(params);
    }

    // Change these variables based on what you need for market and what is sent from controller
    int numSockets = 1;  // probably better if you send this from controller so that you dont have to change it here too
    String[] varNames = new String[15];   // add more empty vals if sending more vars
    String[] doubles = new String[15];
    String[] dataStrings = new String[numSockets];
    String[] holder=new String[numSockets];
    double[] outTemps=new double[numSockets];
    double[] coolTemps= new double[numSockets]; 
    double[] heatTemps= new double[numSockets];
    double[] zoneTemps= new double[numSockets];
    double[] zoneRHs= new double[numSockets];
    double[] heatingEnergy= new double[numSockets];
    double[] coolingEnergy= new double[numSockets];
    double[] netEnergy= new double[numSockets];
    double[] energyPurchased= new double[numSockets];
    double[] energySurplus= new double[numSockets];
    double[] solarRadiation= new double[numSockets];
    double[] receivedHeatTemp= new double[numSockets];
    double[] receivedCoolTemp= new double[numSockets];
    double[] dayOfWeek= new double[numSockets];

    int[] numVars = new int[numSockets];
    String varNameSeparater = "@";
    String doubleSeparater = ",";

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

            ////////////////////////////////////////////////////////////
            // TODO send interactions that must be sent every logical //
            // time step below                                        //
            ////////////////////////////////////////////////////////////

            // write code here



            // send stuff to controller.java
            System.out.println("send from market to controller loop");
            for(int i=0;i<numSockets;i++){
                // This is an example of how to send stuff:
                // // simID = i;  I am leaving this here to remind myself that i is simID for each socket
                // dataStrings[i] = "epGetStartCooling"+varNameSeparater;
                // dataStrings[i] = dataStrings[i] + String.valueOf(coolTemps[i]) + doubleSeparater;
                // dataStrings[i] = dataStrings[i] + "epGetStartHeating"+varNameSeparater;
                // dataStrings[i] = dataStrings[i] + String.valueOf(heatTemps[i]) + doubleSeparater;
                // System.out.println("dataStrings[simID] = "+ dataStrings[i] );

                Reader_Controller sendReaderInfo = create_Reader_Controller();
                sendReaderInfo.set_dataString(dataStrings[i]);
                sendReaderInfo.set_simID(i);
                System.out.println("Send sendReaderInfo interaction: " + dataStrings[i] + " to socket #" + i);
                sendReaderInfo.sendInteraction(getLRC(), currentTime + getLookAhead());
            }
            // Set the interaction's parameters.
            //
            //    Reader_Controller vReader_Controller = create_Reader_Controller();
            //    vReader_Controller.set_actualLogicalGenerationTime( < YOUR VALUE HERE > );
            //    vReader_Controller.set_dataString( < YOUR VALUE HERE > );
            //    vReader_Controller.set_federateFilter( < YOUR VALUE HERE > );
            //    vReader_Controller.set_originFed( < YOUR VALUE HERE > );
            //    vReader_Controller.set_simID( < YOUR VALUE HERE > );
            //    vReader_Controller.set_sourceFed( < YOUR VALUE HERE > );
            //    vReader_Controller.sendInteraction(getLRC(), currentTime + getLookAhead());

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

    public static void main(String[] args) {
        try {
            FederateConfigParser federateConfigParser =
                new FederateConfigParser();
            FederateConfig federateConfig =
                federateConfigParser.parseArgs(args, FederateConfig.class);
            Reader federate =
                new Reader(federateConfig);
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
