package org.webgme.guest.numgen;

import org.webgme.guest.numgen.rti.*;

import org.cpswt.config.FederateConfig;
import org.cpswt.config.FederateConfigParser;
import org.cpswt.hla.InteractionRoot;
import org.cpswt.hla.base.AdvanceTimeRequest;
import org.cpswt.utils.CpswtUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Define the NumGen type of federate for the federation.

public class NumGen extends NumGenBase {
    private final static Logger log = LogManager.getLogger();

    private double currentTime = 0;

    public NumGen(FederateConfig params) throws Exception {
        super(params);
    }

    boolean receivedSimTime = false;
    boolean firstInteraction= true;
    double sum = 0;
    int timeRec = 0;

    private void checkReceivedSubscriptions() {
        InteractionRoot interaction = null;
        while ((interaction = getNextInteractionNoWait()) != null) {
            if (interaction instanceof Receiver_NumGen) {
                handleInteractionClass((Receiver_NumGen) interaction);
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
            if(firstInteraction){
                receivedSimTime = true;
                firstInteraction = false;
            }
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

            double random_double1 = Math.random()*10;
            log.info("random number1: ",random_double1);
            System.out.println("random number1: "+random_double1);

            double random_double2 = Math.random()*10;
            log.info("random number2: ",random_double2);
            System.out.println("random 2: "+random_double2);
            
            double random_double3 = Math.random()*10;
            log.info("random number3: ",random_double3);
            System.out.println("random number3: "+random_double3);

            int timestep = (int)currentTime;
            log.info("timestep: ",timestep);
            System.out.println("timestep: "+timestep);

            NumGen_Receiver timeInteraction1 = create_NumGen_Receiver();
            timeInteraction1.set_randNum(random_double1);
            timeInteraction1.set_timestep(timestep);
            timeInteraction1.sendInteraction(getLRC());

            NumGen_Receiver timeInteraction2 = create_NumGen_Receiver();
            timeInteraction2.set_randNum(random_double2);
            timeInteraction2.set_timestep(timestep);
            timeInteraction2.sendInteraction(getLRC());

            NumGen_Receiver timeInteraction3 = create_NumGen_Receiver();
            timeInteraction3.set_randNum(random_double3);
            timeInteraction3.set_timestep(timestep);
            timeInteraction3.sendInteraction(getLRC());

            Simtime vSimtime = create_Simtime();
            vSimtime.set_timestep(timestep);
            vSimtime.sendInteraction(getLRC());

            // Set the interaction's parameters.
            //
            //    Simtime vSimtime = create_Simtime();
            //    vSimtime.set_actualLogicalGenerationTime( < YOUR VALUE HERE > );
            //    vSimtime.set_federateFilter( < YOUR VALUE HERE > );
            //    vSimtime.set_originFed( < YOUR VALUE HERE > );
            //    vSimtime.set_sourceFed( < YOUR VALUE HERE > );
            //    vSimtime.set_timestep( < YOUR VALUE HERE > );
            //    vSimtime.sendInteraction(getLRC(), currentTime + getLookAhead());
            //    NumGen_Receiver vNumGen_Receiver = create_NumGen_Receiver();
            //    vNumGen_Receiver.set_actualLogicalGenerationTime( < YOUR VALUE HERE > );
            //    vNumGen_Receiver.set_dataString( < YOUR VALUE HERE > );
            //    vNumGen_Receiver.set_federateFilter( < YOUR VALUE HERE > );
            //    vNumGen_Receiver.set_originFed( < YOUR VALUE HERE > );
            //    vNumGen_Receiver.set_randNum( < YOUR VALUE HERE > );
            //    vNumGen_Receiver.set_sourceFed( < YOUR VALUE HERE > );
            //    vNumGen_Receiver.set_timestep( < YOUR VALUE HERE > );
            //    vNumGen_Receiver.sendInteraction(getLRC(), currentTime + getLookAhead());

            checkReceivedSubscriptions();

            /////////////////////////////////+///////////////////////////////////
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

    private void handleInteractionClass(Receiver_NumGen interaction) {
        ///////////////////////////////////////////////////////////////
        // TODO implement how to handle reception of the interaction //
        ///////////////////////////////////////////////////////////////
        sum = interaction.get_sum();
        timeRec = interaction.get_timestep();
        receivedSimTime = true;
    }

    public static void main(String[] args) {
        try {
            FederateConfigParser federateConfigParser =
                new FederateConfigParser();
            FederateConfig federateConfig =
                federateConfigParser.parseArgs(args, FederateConfig.class);
            NumGen federate =
                new NumGen(federateConfig);
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
