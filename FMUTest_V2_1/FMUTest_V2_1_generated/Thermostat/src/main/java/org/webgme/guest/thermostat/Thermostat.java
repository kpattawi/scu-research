package org.webgme.guest.thermostat;

import org.webgme.guest.thermostat.rti.*;

import org.cpswt.config.FederateConfig;
import org.cpswt.config.FederateConfigParser;
import org.cpswt.hla.InteractionRoot;
import org.cpswt.hla.base.AdvanceTimeRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



// Define the Thermostat type of federate for the federation.

public class Thermostat extends ThermostatBase {
    private final static Logger log = LogManager.getLogger();

    private double currentTime = 0;

    public Thermostat(FederateConfig params) throws Exception {
        super(params);
    }

    double outTemp=23.0, coolTemp=0, heatTemp=0, zoneTemp=23.0, zoneHumidity=0,clo=1;
    String varname; 
    String eGSH=null, eGSC=null;
    double value;

    private void checkReceivedSubscriptions() {
        InteractionRoot interaction = null;
        while ((interaction = getNextInteractionNoWait()) != null) {
            if (interaction instanceof ReceiveFromEP) {
                handleInteractionClass((ReceiveFromEP) interaction);
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
            //    SendToEP sendToEP = create_SendToEP();
            //    sendToEP.set_Data( < YOUR VALUE HERE > );
            //    sendToEP.set_NumberOfVariable( < YOUR VALUE HERE > );
            //    sendToEP.set_SimID( < YOUR VALUE HERE > );
            //    sendToEP.set_actualLogicalGenerationTime( < YOUR VALUE HERE > );
            //    sendToEP.set_federateFilter( < YOUR VALUE HERE > );
            //    sendToEP.set_originFed( < YOUR VALUE HERE > );
            //    sendToEP.set_sourceFed( < YOUR VALUE HERE > );
            //    sendToEP.sendInteraction(getLRC(), currentTime + getLookAhead());

            checkReceivedSubscriptions();

            SendToEP DataToSocket = create_SendToEP();
            String Data = "COOL_SETP_DELTA@"+String.valueOf(coolTemp)+",HEAT_SETP_DELTA@"+String.valueOf(heatTemp);
            DataToSocket.set_Data(Data);
            System.out.println("Send out==>"+ Data);
            DataToSocket.sendInteraction(getLRC(), currentTime + getLookAhead());
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

    private void handleInteractionClass(ReceiveFromEP interaction) {
        ///////////////////////////////////////////////////////////////
        // TODO implement how to handle reception of the interaction //
        ///////////////////////////////////////////////////////////////
        String DataFromSocket = interaction.get_Data();
        String NumberofIncomeData = interaction.get_NumberOfVariable();

        String vars[] = DataFromSocket.split(",");
        ///////////////////////////////////////////////////////////////
        for(String token : vars) {
            System.out.println(token);
            String token1[]= token.split("@");
            if (token1[0].equals("HEAT_SETP_DELTA")){
                eGSH = token1[1];
            }
            if (token1[0].equals("COOL_SETP_DELTA")){
                eGSH = token1[1];
            }
            
        }
    }

    public static void main(String[] args) {
        try {
            FederateConfigParser federateConfigParser =
                new FederateConfigParser();
            FederateConfig federateConfig =
                federateConfigParser.parseArgs(args, FederateConfig.class);
            Thermostat federate =
                new Thermostat(federateConfig);
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
