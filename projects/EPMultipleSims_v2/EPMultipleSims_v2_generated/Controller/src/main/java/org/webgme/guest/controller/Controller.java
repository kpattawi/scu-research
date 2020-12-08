package org.webgme.guest.controller;

import org.webgme.guest.controller.rti.*;

import org.cpswt.config.FederateConfig;
import org.cpswt.config.FederateConfigParser;
import org.cpswt.hla.InteractionRoot;
import org.cpswt.hla.base.AdvanceTimeRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Kaleb // Import packages

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Random;

// Kaleb //

// Define the Controller type of federate for the federation.

public class Controller extends ControllerBase {
    private final static Logger log = LogManager.getLogger();

    private double currentTime = 0;

    public Controller(FederateConfig params) throws Exception {
        super(params);
    }
    
    // Kaleb // defining variables
    int numSockets = 1;
    String[] varNames = new String[]{"","",""};   // Right now zoneTemp, outTemp, zoneRH... add more empty vals if sending more vars
    String[] doubles = new String[]{"","",""};
    String[] dataStrings = new String[]{""};
    String[] holder=new String[]{""};
    double[] outTemps=new double[]{23};
    double[] coolTemps= new double[]{23}; 
    double[] heatTemps= new double[]{21};
    double[] zoneTemps= new double[]{23};
    double[] zoneRHs= new double[]{0};
    int[] numVars = new int[]{0};
    String varNameSeparater = "@";
    String doubleSeparater = ",";
    


    int hour=0, nexthour=0, quarter=0, fivemin=0, onemin=0, simulatetime=0;
    double r1 =0.0;
    double Preset_cool=23.0, Preset_heat=21.0;
    double event_p=0.0, duration_p=1.0, duration_q=0.0, nextevent_p=0.0;
    int occupancy = 2, check = 0, p=0, r2=0;
    

    String varname="";
    double value=0.0;
    double Last_cool=23.0, Last_heat=21.0;
    
    // Kaleb //
    
    private void checkReceivedSubscriptions() {
        InteractionRoot interaction = null;
        while ((interaction = getNextInteractionNoWait()) != null) {
            if (interaction instanceof Socket_Controller) {
                handleInteractionClass((Socket_Controller) interaction);
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

        
        // Kaleb // Adding probability for occupancy
        double[] pro; 
        // allocating memory for 24 doubles. 
        pro = new double[24]; 
        // initialize the  elements of the probability array 
        pro[0] = 0.875;           
        pro[1] = 0.817; 
        pro[2] = 0.867; 
        pro[3] = 0.783; 
        pro[4] = 0.8; 
        pro[5] = 0.708; 
        pro[6] = 0.767; 
        pro[7] = 0.683; 
        pro[8] = 0.658; 
        pro[9] = 0.5; 
        pro[10] = 0.517;           
        pro[11] = 0.358; 
        pro[12] = 0.225; 
        pro[13] = 0.167; 
        pro[14] = 0.192; 
        pro[15] = 0.25; 
        pro[16] = 0.542; 
        pro[17] = 0.642; 
        pro[18] = 0.608; 
        pro[19] = 0.692; 
        pro[20] = 0.85;   
        pro[21] = 0.858; 
        pro[22] = 0.883; 
        pro[23] = 0.85;
        // Kaleb //
        
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
          //    Controller_Socket vController_Socket = create_Controller_Socket();
          //    vController_Socket.set_actualLogicalGenerationTime( < YOUR VALUE HERE > );
          //    vController_Socket.set_dataString( < YOUR VALUE HERE > );
          //    vController_Socket.set_federateFilter( < YOUR VALUE HERE > );
          //    vController_Socket.set_originFed( < YOUR VALUE HERE > );
          //    vController_Socket.set_simID( < YOUR VALUE HERE > );
          //    vController_Socket.set_size( < YOUR VALUE HERE > );
          //    vController_Socket.set_sourceFed( < YOUR VALUE HERE > );
          //    vController_Socket.sendInteraction(getLRC(), currentTime + getLookAhead());

          checkReceivedSubscriptions();

          
          // Kaleb //  Occupancy stuff
          System.out.println("Logical Time is = " + currentTime);
          // quarter = (int) (currentTime%96);
          // fivemin = (int) (currentTime%288);
          onemin = (int) (currentTime%1440);
          hour = (int) (onemin/60);
          System.out.println("Hour is = " + hour);
          
          // Start Occupancy
          if (hour==simulatetime){
            System.out.println("Simulate at this time step!!!!!");
            event_p=pro[hour];
            System.out.println("Occupied Probability: "+ event_p);
            //  Occupancy Duration Simulator Based on Monte Carlo Method
            while (check ==0){
            //  r2 = (int) (Math.random()*24+1);
              r2=1;
              System.out.println("Random Number(Duration): "+ r2 );
              for(int i = 0; i < r2; i++){
                p=hour+i;
                if (p>23){
                  p=p-24;
                }
                event_p = pro[p];
                duration_p = duration_p * event_p ;
                duration_q = duration_q *(1 - event_p) ;
              }
              System.out.println("The probability of OCCUPIED for "+ r2 +" hours is "+ duration_p );
              System.out.println("The probability of NOT OCCUPIED for "+ r2 +" hours is "+ duration_q ); 
              r1 = (double) (Math.random());
              System.out.println("Random Number(Event): "+ r1 );
              if (r1 < duration_p){
                System.out.println("Continuously Occupied for "+r2+ " Hours Accepted!!!" );
                check = 1;
                simulatetime = simulatetime + r2;
                occupancy =1;
                if (simulatetime>23){
                  simulatetime=simulatetime-24;
                } 
                System.out.println("Next simulating time at hour = "+ simulatetime );
              } 
              else if(r1 > (1-duration_q)){
                System.out.println("Continuously Not Occupied for "+r2+ " Hours Accepted!!!" );
                check = 1;
                simulatetime = simulatetime + r2;
                occupancy=0;
                if (simulatetime>23){
                  simulatetime=simulatetime-24;
                } 
                System.out.println("Next simulating time at hour = "+ simulatetime );
              } 
              else {
                System.out.println("Event Rejected!!!" );
              }
              duration_p=1;
              duration_q=1;
            }
            check =0;
          }
          else{
            System.out.println("Keep the same Occupancy information as previous timestamp.");
          }
          // End determine Occupancy



          // Kaleb //
          // determine heating and cooling setpoints for each simID
          // will eventually change this part for transactive energy
          System.out.println("determine setpoints loop");
          
          double clo=1.0; // was here from old code... idk what for
          
          int Fuzzycool=0,Fuzzyheat=0;
          for(int i=0;i<numSockets;i++){
            System.out.println("outTemps[i] = "+ outTemps[i] );
            zoneTemps[i] = zoneTemps[i];
            System.out.println("zoneTemps[i] = "+ zoneTemps[i] );
            // zoneRHs[i] can add this but need to check FMU file and also edit socket.java

            // Adaptive Control
            // Always-On
            if (outTemps[i]<=10){
              heatTemps[i]=18.9;
              coolTemps[i]=22.9;
            }else if (outTemps[i]>=33.5){
              heatTemps[i]=26.2;
              coolTemps[i]=30.2;
            }else {
              heatTemps[i] = 0.31*outTemps[i] + 17.8-2;
              coolTemps[i] = 0.31*outTemps[i] + 17.8+2;
            }

            // // Occupancy-Driven Control
            // if (occupancy == 0 ){
            //    coolTemps[i] = 32;
            //    heatTemps[i] = 12;
            // }
            
            //0.5 degree fuzzy control 
            double offset=0.6;
            if (zoneTemps[i]>=coolTemps[i]+0.5-offset){
            Fuzzyheat = -1;
            Fuzzycool = -1;
            }else if (zoneTemps[i]>=coolTemps[i]-0.5-offset){
            Fuzzyheat = -1;
            }else if (zoneTemps[i]>=heatTemps[i]+0.5+offset){
              Fuzzyheat = -1;
            Fuzzycool = 1;
            }else if (zoneTemps[i]>=heatTemps[i]-0.5+offset){
            Fuzzycool = 1;
            }else{
            Fuzzyheat = 1;
            Fuzzycool = 1;
            }
            coolTemps[i] = coolTemps[i] -offset+ Fuzzycool*offset;
            heatTemps[i] = heatTemps[i] +offset+ Fuzzyheat*offset;

            //  //================================
            //   // write to data to record occupancy information
            //   try{
            //         // Create new file
            //         String path="/home/vagrant/Desktop/EnergyPlusData/Adaptive_Test_Occupancy.txt";
            //         File file = new File(path);
            //         // If file doesn't exists, then create it
            //         if (!file.exists()) {
            //             file.createNewFile();
            //         }
            //         FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
            //         BufferedWriter bw = new BufferedWriter(fw);
            //         // Write in file
            //         bw.write(currentTime + "\t" + occupancy +  "\n" );
            //         // Close connection
            //         bw.close();
            //     }
            //     catch(Exception e){
            //         System.out.println(e);
            //     }
            // //=====================================

            System.out.println("heatTemps[i] = "+ heatTemps[i] );
            System.out.println("coolTemps[i] = "+ coolTemps[i] );
          }
          // Kaleb //

          ////////////////////////////////////////////////////////////
          // TODO send interactions that must be sent every logical //
          // time step below                                        //
          ////////////////////////////////////////////////////////////

          // Send the Cooling Setpoint interaction's.

          // Kaleb //
          // varName first!!!

          System.out.println("send interactions loop");
          int size = 0;
          for(int i=0;i<numSockets;i++){
            // simID = i;  I am leaving this here to remind myself that i is simID for each socket
            size = 0;
            dataStrings[i] = "epGetStartCooling"+varNameSeparater;
            dataStrings[i] = dataStrings[i] + String.valueOf(coolTemps[i]) + doubleSeparater;
            size = size +1;
            dataStrings[i] = dataStrings[i] + "epGetStartHeating"+varNameSeparater;
            dataStrings[i] = dataStrings[i] + String.valueOf(heatTemps[i]) + doubleSeparater;
            size = size +1;
            System.out.println("dataStrings[simID] = "+ dataStrings[i] );

            Controller_Socket sendControls = create_Controller_Socket();
            sendControls.set_dataString(dataStrings[i]);
            sendControls.set_simID(i);
            sendControls.set_size(size);
            System.out.println("Send sendControls interaction: " + coolTemps[i] + " to socket #" + i);
            sendControls.sendInteraction(getLRC(), currentTime + getLookAhead());
            
            // Empty Data String and size
            dataStrings[i]="";
            size = 0;
          }

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

      private void handleInteractionClass(Socket_Controller interaction) {
        ///////////////////////////////////////////////////////////////
        // TODO implement how to handle reception of the interaction //
        ///////////////////////////////////////////////////////////////

        // Kaleb // 
        // Could make global var that holds simIDs but it would just be 0,1,2,...
        int simID;
        simID = interaction.get_simID();
    		numVars[simID] = interaction.get_size();
        System.out.println("numVars[simID] = " + numVars[simID]);
    		holder[simID] = interaction.get_dataString();
        System.out.println("holder[simID] = "+ holder[simID] );

        // varName first!!!
        System.out.println("handle interaction loop");

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
          if(varNames[i].equals("epSendZoneMeanAirTemp")){
            zoneTemps[simID] = Double.valueOf(doubles[i]);
          }
          else if(varNames[i].equals("epSendOutdoorAirTemp")){
            outTemps[simID] = Double.valueOf(doubles[i]);
          }
          else if(varNames[i].equals("epSendZoneHumidity")){
            zoneRHs[simID] = Double.valueOf(doubles[i]);
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
            Controller federate =
                new Controller(federateConfig);
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