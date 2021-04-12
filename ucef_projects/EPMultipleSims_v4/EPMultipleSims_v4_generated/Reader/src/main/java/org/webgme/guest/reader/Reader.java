package org.webgme.guest.reader;

import org.webgme.guest.reader.rti.*;

import org.cpswt.config.FederateConfig;
import org.cpswt.config.FederateConfigParser;
import org.cpswt.hla.base.AdvanceTimeRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Import other packages needed
import org.cpswt.utils.CpswtUtils;  // need to remove time delay

// Define the Reader type of federate for the federation.

public class Reader extends ReaderBase {
    private final static Logger log = LogManager.getLogger();

    private double currentTime = 0;

    public Reader(FederateConfig params) throws Exception {
        super(params);
    }

    boolean receivedController = false;

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

            // send info to controller
            Reader_Controller vReader_Controller = create_Reader_Controller();
            vReader_Controller.set_dataString(String.valueOf(currentTime));
            vReader_Controller.sendInteraction(getLRC());
            log.info("sent reader_controller interaction");


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
