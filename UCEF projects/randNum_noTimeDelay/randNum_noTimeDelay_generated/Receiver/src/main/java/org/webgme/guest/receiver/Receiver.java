package org.webgme.guest.receiver;

import org.webgme.guest.receiver.rti.*;

import org.cpswt.config.FederateConfig;
import org.cpswt.config.FederateConfigParser;
import org.cpswt.hla.InteractionRoot;
import org.cpswt.hla.base.AdvanceTimeRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cpswt.utils.CpswtUtils;

// Define the Receiver type of federate for the federation.

public class Receiver extends ReceiverBase {
    private final static Logger log = LogManager.getLogger();

    private double currentTime = 0;

    public Receiver(FederateConfig params) throws Exception {
        super(params);
    }

    boolean receivedSimTime = false;
    double rand1 = -1;
    double rand2 = -1;
    double rand3 = -1;
    
    private void checkReceivedSubscriptions() {
        InteractionRoot interaction = null;
        while ((interaction = getNextInteractionNoWait()) != null) {
            if (interaction instanceof NumGen_Receiver) {
                handleInteractionClass((NumGen_Receiver) interaction);
            }
            else if (interaction instanceof Simtime) {
                handleInteractionClass((Simtime) interaction);
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

            // removing time delay...
            receivedSimTime = false;
            while (!receivedSimTime){
                log.info("waiting to receive SimTime...");
                synchronized(lrc){
                    lrc.tick();
                }
                checkReceivedSubscriptions();
                if(!receivedSimTime){
                    CpswtUtils.sleep(1000);
                }
            }
            //

            ////////////////////////////////////////////////////////////
            // TODO send interactions that must be sent every logical //
            // time step below                                        //
            ////////////////////////////////////////////////////////////

            double sum = rand1 + rand2 + rand3;
            log.info("sum: ",sum);
            System.out.println("sum: "+sum);

            Receiver_NumGen vReceiver_NumGen = create_Receiver_NumGen();
            vReceiver_NumGen.set_sum(sum);
            vReceiver_NumGen.sendInteraction(getLRC());

            // Set the interaction's parameters.
            //
            //    Receiver_NumGen vReceiver_NumGen = create_Receiver_NumGen();
            //    vReceiver_NumGen.set_actualLogicalGenerationTime( < YOUR VALUE HERE > );
            //    vReceiver_NumGen.set_dataString( < YOUR VALUE HERE > );
            //    vReceiver_NumGen.set_federateFilter( < YOUR VALUE HERE > );
            //    vReceiver_NumGen.set_originFed( < YOUR VALUE HERE > );
            //    vReceiver_NumGen.set_sourceFed( < YOUR VALUE HERE > );
            //    vReceiver_NumGen.set_sum( < YOUR VALUE HERE > );
            //    vReceiver_NumGen.set_timestep( < YOUR VALUE HERE > );
            //    vReceiver_NumGen.sendInteraction(getLRC(), currentTime + getLookAhead());

            checkReceivedSubscriptions();

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

    private void handleInteractionClass(NumGen_Receiver interaction) {
        ///////////////////////////////////////////////////////////////
        // TODO implement how to handle reception of the interaction //
        ///////////////////////////////////////////////////////////////

        if(rand1 == -1){
            rand1= interaction.get_randNum();
        }
        else if(rand2 ==-1){
            rand2 = interaction.get_randNum();
        }
        else if(rand3 ==-1){
            rand3 = interaction.get_randNum();
        }
    }

    private void handleInteractionClass(Simtime interaction) {
        ///////////////////////////////////////////////////////////////
        // TODO implement how to handle reception of the interaction //
        ///////////////////////////////////////////////////////////////
        receivedSimTime = true;
    }

    public static void main(String[] args) {
        try {
            FederateConfigParser federateConfigParser =
                new FederateConfigParser();
            FederateConfig federateConfig =
                federateConfigParser.parseArgs(args, FederateConfig.class);
            Receiver federate =
                new Receiver(federateConfig);
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
