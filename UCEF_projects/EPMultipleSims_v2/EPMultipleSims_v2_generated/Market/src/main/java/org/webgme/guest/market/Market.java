package org.webgme.guest.market;

import org.webgme.guest.market.rti.*;

import org.cpswt.config.FederateConfig;
import org.cpswt.config.FederateConfigParser;
import org.cpswt.hla.InteractionRoot;
import org.cpswt.hla.base.AdvanceTimeRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Define the Market type of federate for the federation.

public class Market extends MarketBase {
    private final static Logger log = LogManager.getLogger();

    private double currentTime = 0;

    public Market(FederateConfig params) throws Exception {
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
    double[] predictedEnergy= new double[numSockets];
    double price = 10; // Choose default price

    int[] numVars = new int[numSockets];
    String varNameSeparater = "@";
    String doubleSeparater = ",";


    private void checkReceivedSubscriptions() {
        InteractionRoot interaction = null;
        while ((interaction = getNextInteractionNoWait()) != null) {
            if (interaction instanceof Controller_Market) {
                handleInteractionClass((Controller_Market) interaction);
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

            ////////////////////////////////////////////////////////////
            // TODO send interactions that must be sent every logical //
            // time step below                                        //
            ////////////////////////////////////////////////////////////

            // Set the interaction's parameters.
            //
            //    Market_Controller vMarket_Controller = create_Market_Controller();
            //    vMarket_Controller.set_actualLogicalGenerationTime( < YOUR VALUE HERE > );
            //    vMarket_Controller.set_dataString( < YOUR VALUE HERE > );
            //    vMarket_Controller.set_federateFilter( < YOUR VALUE HERE > );
            //    vMarket_Controller.set_originFed( < YOUR VALUE HERE > );
            //    vMarket_Controller.set_simID( < YOUR VALUE HERE > );
            //    vMarket_Controller.set_size( < YOUR VALUE HERE > );
            //    vMarket_Controller.set_sourceFed( < YOUR VALUE HERE > );
            //    vMarket_Controller.sendInteraction(getLRC(), currentTime + getLookAhead());

            checkReceivedSubscriptions();

            ////////////////////////////////////////////////////////////////////
            // TODO break here if ready to resign and break out of while loop //
            ////////////////////////////////////////////////////////////////////

            // write code here



            // send stuff to controller.java
            System.out.println("send from market to controller loop");
            int size = 0;

            dataStrings[0] = "price"+varNameSeparater + String.valueOf(price) + doubleSeparater;
            size = size +1;
            System.out.println("dataStrings[simID] = "+ dataStrings[0] );

            Market_Controller sendMarketInfo = create_Market_Controller();
            sendMarketInfo.set_dataString(dataStrings[i]);
            // sendMarketInfo.set_simID(i);
            sendMarketInfo.set_size(size);
            System.out.println("Send sendMarketInfo interaction: " + dataStrings[0]);
            sendMarketInfo.sendInteraction(getLRC(), currentTime + getLookAhead());
            

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

    private void handleInteractionClass(Controller_Market interaction) {
        ///////////////////////////////////////////////////////////////
        // TODO implement how to handle reception of the interaction //
        ///////////////////////////////////////////////////////////////

        // Kaleb // 
        // Could make global var that holds simIDs but it would just be 0,1,2,...
        int simID = interaction.get_simID();
    		numVars[simID] = interaction.get_size();  // not used for anything
        System.out.println("numVars[simID] = " + numVars[simID]);
    		holder[simID] = interaction.get_dataString();
        System.out.println("holder[simID] = "+ holder[simID] );

        System.out.println("handle interaction loop");

        // "varName{varNameSplitter}double{doubleSplitter}"!!!
        String vars[] = holder[simID].split(doubleSeparater);
        System.out.println("vars[0] = "+vars[0]);
        System.out.println("length of vars = " + vars.length);
        int j;
        j=0;
        for( String token : vars){
              System.out.println("j = "+j);
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

        for(int i=0; i<varNames.length;i++){
          System.out.println("i = "+i);
        // Example how to handle variables (depends on how they were sent from controller)
          if(varNames[i].equals("TODO predictedEnergy")){
            predictedEnergy[simID] = Double.valueOf(doubles[i]);
          }
        //   else if(varNames[i].equals("epSendOutdoorAirTemp")){
        //     outTemps[simID] = Double.valueOf(doubles[i]);
        //   }
          
        }

        // Kaleb //
    }

    public static void main(String[] args) {
        try {
            FederateConfigParser federateConfigParser =
                new FederateConfigParser();
            FederateConfig federateConfig =
                federateConfigParser.parseArgs(args, FederateConfig.class);
            Market federate =
                new Market(federateConfig);
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
