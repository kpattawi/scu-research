package org.webgme.guest.market;

import org.webgme.guest.market.rti.*;

import org.cpswt.config.FederateConfig;
import org.cpswt.config.FederateConfigParser;
import org.cpswt.hla.InteractionRoot;
import org.cpswt.hla.base.AdvanceTimeRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Import other packages needed
import org.cpswt.utils.CpswtUtils;  // need to remove time delay
// for getting and sending to fmu
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Random;    // random num generator

// Define the Market type of federate for the federation.

public class Market extends MarketBase {
    private final static Logger log = LogManager.getLogger();

    private double currentTime = 0;

    public Market(FederateConfig params) throws Exception {
        super(params);
    }

    boolean receivedController = false;

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

            // waiting to receive Controller_Market
            while (!receivedController){
                log.info("waiting to receive Controller_Market interaction...");
                synchronized(lrc){
                    lrc.tick();
                }
                checkReceivedSubscriptions();
                if(!receivedController){
                    CpswtUtils.sleep(100);
                }
            }
            receivedController = false;

            // TODO determine price using info from controller vvvvvv

            // TODO determine price using info from controller ^^^^^^

            // Send price back to controller 
            // aka send Market_Controller interaction
            Market_Controller sendPrice = create_Market_Controller();
            sendPrice.set_dataString(String.valueOf(currentTime));
            sendPrice.sendInteraction(getLRC());
            
            // Set the interaction's parameters.
            //
            //    Market_Controller vMarket_Controller = create_Market_Controller();
            //    vMarket_Controller.set_actualLogicalGenerationTime( < YOUR VALUE HERE > );
            //    vMarket_Controller.set_dataString( < YOUR VALUE HERE > );
            //    vMarket_Controller.set_federateFilter( < YOUR VALUE HERE > );
            //    vMarket_Controller.set_originFed( < YOUR VALUE HERE > );
            //    vMarket_Controller.set_simID( < YOUR VALUE HERE > );
            //    vMarket_Controller.set_sourceFed( < YOUR VALUE HERE > );
            //    vMarket_Controller.sendInteraction(getLRC(), currentTime + getLookAhead());


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

    private void handleInteractionClass(Controller_Market interaction) {
        ///////////////////////////////////////////////////////////////
        // TODO implement how to handle reception of the interaction //
        ///////////////////////////////////////////////////////////////
        // exit while loop waiting for this interaction
        receivedController = true;
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
