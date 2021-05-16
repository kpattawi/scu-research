package org.webgme.guest.controller;

import org.webgme.guest.controller.rti.*;

import org.cpswt.config.FederateConfig;
import org.cpswt.config.FederateConfigParser;
import org.cpswt.hla.InteractionRoot;
import org.cpswt.hla.base.AdvanceTimeRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Importin gother packages
import java.io.*;
import java.net.*;
import org.cpswt.utils.CpswtUtils;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Random;    // random num generator
import java.lang.*;

// Define the Controller type of federate for the federation.

public class Controller extends ControllerBase {
    private final static Logger log = LogManager.getLogger();

    private double currentTime = 0;

    public Controller(FederateConfig params) throws Exception {
        super(params);
    }

    // Kaleb // defining  global variables
    double fuzzy_heat = 0;  // NEEDS TO BE GLOBAL VAR outside of while loop
    double fuzzy_cool = 0;  // NEEDS TO BE GLOBAL VAR outside of while loop

    int numSockets = 1;  // Change this
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
    double price = 10; // Set a default price here
    int[] numVars = new int[numSockets];
    String[] futureIndoorTemp = new String[12];

    String varNameSeparater = "@";
    String doubleSeparater = ",";
    String optDataString = "";
    int day = 0;

    int hour=0, nexthour=0, quarter=0, fivemin=0, onemin=0, simulatetime=0;
    double r1 =0.0;
    double Preset_cool=23.0, Preset_heat=20.0; // changed preset cool from 21.0 - PJ
    double event_p=0.0, duration_p=1.0, duration_q=0.0, nextevent_p=0.0;
    int occupancy = 2, check = 0, p=0, r2=0;

    String varname="";
    double value=0.0;
    double Last_cool=23.0, Last_heat=20.0; // changed Last_heat from 21.0 - PJ

    boolean receivedSocket = false;
    boolean receivedMarket = false;
    boolean receivedReader = false;
    
    String timestep_Socket = "";
    String timestep_Reader = "";
    String timestep_Market = "";
    String timestep_Controller = "";

    // REMOVE NEXT TWO LINES!! this was for testing preloading weather
    // double[] outTemperature = new double[]{7.2,6.7,6.1,4.4,4.4,6.1,5,7.8,8.9,9.4,10,10.6,11.1,13.9,13.9,11.1,11.1,10.6,10.6,8.9,9.4,7.8,6.7,8.9,6.1,5.6,3.9,5.6,7.2,7.8,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10};
    // int yeet = 0;
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
            //    vController_Socket.set_sourceFed( < YOUR VALUE HERE > );
            //    vController_Socket.sendInteraction(getLRC(), currentTime + getLookAhead());

            System.out.println("timestep before receiving Socket/Reader: "+ currentTime);
            log.info("timestep before receiving Socket/Reader: ",currentTime);
            // waiting to receive Socket_Controller and Reader_Controller
            while (!(receivedSocket)){

                //while ((!(receivedSocket) || !(receivedReader))){
                log.info("waiting to receive Socket_Controller interaction...");
                synchronized(lrc){
                    lrc.tick();
                }
                checkReceivedSubscriptions();
                if(!receivedSocket){
                    CpswtUtils.sleep(100);
                }
                // }else if(!receivedReader){
                //   log.info("waiting on Reader_Controller...");
                //   CpswtUtils.sleep(100);
                // }
            }
          receivedSocket = false;
          receivedReader = false;
          System.out.println("timestep after receiving Socket/Reader and before sending to Market: "+ currentTime);
          
        //   // TODO send Controller_Market here! vvvvvvvv
        //   log.info("sending Controller_Market interaction");
        //   Controller_Market sendMarket = create_Controller_Market();
        //   sendMarket.set_dataString("");
        //   System.out.println("Send controller_market and Reader_Controller interaction:");
        //   sendMarket.sendInteraction(getLRC());

          
        //   log.info("waiting for Market_controller interaction...");
        //   // Wait to receive price from market  
        //   while (!receivedMarket){
        //       log.info("waiting to receive Market_Controller interaction...");
        //       synchronized(lrc){
        //           lrc.tick();
        //       }
        //       checkReceivedSubscriptions();
        //       if(!receivedMarket){
        //           log.info("waiting on Market_Controller...");
        //           CpswtUtils.sleep(100);
        //       }
        //   }
        //   receivedMarket = false;
        //   log.info("received Market_controller interaction!");
        //   System.out.println("timestep after receiving Market: "+ currentTime);


            // PJ's optimization
            double hour = (double) ((currentTime%288) / 12);
            log.info("hour is: ",hour);
            System.out.println("hour is:"+hour);
            String s = null;
            String dataStringOpt = "";
            String dataStringOptT = "";
            String dataStringOptP = "";
            String dataStringOptO = "";
            String dataStringOptS = "";
            String sblock = null;
            String sday = null;
            String separatorOpt = ",";
            boolean	startSavingE = false;
            boolean startSavingT = false;
            boolean startSavingP = false;
            boolean startSavingO = false;
            boolean startSavingS = false;

            if (hour == 0){
                day = day+1;
            }
            
            if (hour%1 == 0){
                try {
                    sblock= String.valueOf((int)hour);
                    sday = String.valueOf(day);
                    dataStringOpt = sblock;
                    dataStringOptT = sblock;
                    dataStringOptP = sblock;
                    dataStringOptO = sblock;
                    dataStringOptS = sblock;
                    System.out.println("sblock:" +sblock);
                    System.out.println("sday:" +sday);
                    System.out.println("zonetemp string" +String.valueOf(zoneTemps[0]));

                    // Process p = Runtime.getRuntime().exec("python ./energyOpt.py " +sday +" "+sblock +" "+ String.valueOf(zoneTemps[0])); // 4 hr block method
                    Process p = Runtime.getRuntime().exec("python ./energyOptTset2hr.py " +sday +" " +sblock +" "+ String.valueOf(zoneTemps[0])); // 1 timestep method

    
                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    System.out.println("Here is the result");

                    // T is indoor Temp, P is price, O is outdoor T, S is solar rad
            
                    while ((s = stdInput.readLine()) != null) {
                        System.out.println(s);
                        if (startSavingE == true) {
                            dataStringOpt = dataStringOpt + separatorOpt + s;
                        }		
                        if (startSavingT == true) {
                            dataStringOptT = dataStringOptT + separatorOpt + s;
                        }
                        if (startSavingP == true) {
                            dataStringOptP = dataStringOptP + separatorOpt + s;
                        }
                        if (startSavingO == true) {
                            dataStringOptO = dataStringOptO + separatorOpt + s;
                        }
                        if (startSavingS == true) {
                            dataStringOptS = dataStringOptS + separatorOpt + s;
                        }
                        if (s .equals("energy consumption")){
                            startSavingE = true;
                        }
                        if (s .equals("indoor temp prediction")){
                            startSavingT = true;
                        }
                        if (s .equals("pricing per timestep")){
                            startSavingP = true;
                        }
                        if (s .equals("outdoor temp")){
                            startSavingO = true;
                        }
                        if (s .equals("solar radiation")){
                            startSavingS = true;
                        }
                        // System.out.println(dataString);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String vars[] = dataStringOpt.split(separatorOpt);
                String varsT[] = dataStringOptT.split(separatorOpt);
                String varsP[] = dataStringOptP.split(separatorOpt);
                String varsO[] = dataStringOptO.split(separatorOpt);
                String varsS[] = dataStringOptS.split(separatorOpt);

                // Take out of try catch
                for (int in =1;in<13;in++) {
                        futureIndoorTemp[in-1]=varsT[in];
                    }
    
                // Writing data to file
                try{
                    // Create new file
                    
                    String path="/home/vagrant/Desktop/GitHub/scu_research/ucef_projects/pjs_projects/EnergyPlusOpt2Fed/EnergyPlusOpt2Fed_generated/DataSummary.txt";
                    File file = new File(path);
    
                    // If file doesn't exists, then create it
                    if (!file.exists()) {
                        file.createNewFile();
                    }
    
                    FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
                    BufferedWriter bw = new BufferedWriter(fw);
    
                    // Write in file
                    for (int in =1;in<13;in++) {
                        bw.write(vars[in]+"\t"+varsT[in]+"\t"+varsP[in]+"\t"+varsO[in]+"\t"+varsS[in]+"\n");
                        // futureIndoorTemp[in-1]=varsT[in];  // put outside of try catch
                    }
    
                    // Close connection
                    bw.close();
                }
                catch(Exception e){
                    System.out.println(e);
                }
    
                // resetting 
                startSavingE = false;
                startSavingT = false;
                startSavingP = false;
                startSavingO = false;
                startSavingS = false;
                dataStringOpt = "";
            }

            // Setting setpoint temp for next hour 
            System.out.println("determine setpoints loop1");
            if (hour%1 == 0){
                p=0;
                System.out.println("p"+String.valueOf(p));
            }
            heatTemps[0]=Double.parseDouble(futureIndoorTemp[p]);
            System.out.println("heatTemp"+String.valueOf(heatTemps[0]));
            coolTemps[0]=30.2;
            System.out.println("coolTemp: "+String.valueOf(coolTemps[0]));
            p=p+1;
            System.out.println("p"+String.valueOf(p));
            
            //-------------------------------------------------------------------------------------------------
            // Now figure out all stuff that needs to be sent to socket...
        
            // determine heating and cooling setpoints for each simID
            // will eventually change this part for transactive energy
            System.out.println("determine setpoints loop2");

            int Fuzzycool=0;
            int Fuzzyheat=0;
            // 0.5 degree fuzzy control (this oscillates indoor temp)
            double offset=0.6;
            if (zoneTemps[0]>=coolTemps[0]+0.5-offset){
            Fuzzyheat = -1;
            Fuzzycool = -1;
            }else if (zoneTemps[0]>=coolTemps[0]-0.5-offset){
            Fuzzyheat = -1;
            }else if (zoneTemps[0]>=heatTemps[0]+0.5+offset){
                Fuzzyheat = -1;
            Fuzzycool = 1;
            }else if (zoneTemps[0]>=heatTemps[0]-0.5+offset){
            Fuzzycool = 1;
            }else{
            Fuzzyheat = 1;
            Fuzzycool = 1;
            }
            coolTemps[0] = coolTemps[0] -offset+ Fuzzycool*offset;
            heatTemps[0] = heatTemps[0] +offset+ Fuzzyheat*offset;

            System.out.println("heatTemps[0] = "+heatTemps[0] );
            System.out.println("coolTemps[0] = "+coolTemps[0] );

        //   // use the following loop to solve for heating/cooling setpts for each EnergyPlus simulation
        //   // if you only have one EnergyPlus simulation still use the loop so that it is easy to add more
        //   // currently, adaptive setpoint control is implemented with 0.5 "fuzzy control"
        //   for(int i=0;i<numSockets;i++){
        //     System.out.println("outTemps[i] = "+ outTemps[i] );
        //     zoneTemps[i] = zoneTemps[i];
        //     System.out.println("zoneTemps[i] = "+ zoneTemps[i] );
        //     // zoneRHs[i] can add this but need to check FMU file and also edit socket.java

        //     // Adaptive Setpoint Control:
        //     if (outTemps[i]<=10){
        //       heatTemps[i]=18.9;
        //       coolTemps[i]=22.9;
        //     }else if (outTemps[i]>=33.5){
        //       heatTemps[i]=26.2;
        //       coolTemps[i]=30.2;
        //     }else {
        //       heatTemps[i] = 0.31*outTemps[i] + 17.8-2;
        //       coolTemps[i] = 0.31*outTemps[i] + 17.8+2;
        //     }
        //     // End Adaptive Setpoint Control

            // // 0.5 degree fuzzy control (this oscillates indoor temp)
            // double offset=0.6;
            // if (zoneTemps[i]>=coolTemps[i]+0.5-offset){
            // Fuzzyheat = -1;
            // Fuzzycool = -1;
            // }else if (zoneTemps[i]>=coolTemps[i]-0.5-offset){
            // Fuzzyheat = -1;
            // }else if (zoneTemps[i]>=heatTemps[i]+0.5+offset){
            //   Fuzzyheat = -1;
            // Fuzzycool = 1;
            // }else if (zoneTemps[i]>=heatTemps[i]-0.5+offset){
            // Fuzzycool = 1;
            // }else{
            // Fuzzyheat = 1;
            // Fuzzycool = 1;
            // }
            // coolTemps[i] = coolTemps[i] -offset+ Fuzzycool*offset;
            // heatTemps[i] = heatTemps[i] +offset+ Fuzzyheat*offset;

            // System.out.println("heatTemps[i] = "+i+ heatTemps[i] );
            // System.out.println("coolTemps[i] = "+i+ coolTemps[i] );
        //   }
        //   // End fuzzy control
        //   //-------------------------------------------------------------------------------------------------


          // Send values to each socket federate
          System.out.println("send to sockets interactions loop");
          for(int i=0;i<numSockets;i++){
            // simID = i;  I am leaving this here to remind myself that i is simID for each socket
            
            dataStrings[i] = "epGetStartCooling"+varNameSeparater;
            dataStrings[i] = dataStrings[i] + String.valueOf(coolTemps[i]) + doubleSeparater;
            
            dataStrings[i] = dataStrings[i] + "epGetStartHeating"+varNameSeparater;
            dataStrings[i] = dataStrings[i] + String.valueOf(heatTemps[i]) + doubleSeparater;
            
            System.out.println("dataStrings[simID] = "+ dataStrings[i] );

            // SendModel vSendModel = create_SendModel();
            // vSendModel.set_dataString(dataString);
            // log.info("Sent sendModel interaction with {}", dataString);
            // vSendModel.sendInteraction(getLRC());

            Controller_Socket sendControls = create_Controller_Socket();
            sendControls.set_dataString(dataStrings[i]);
            sendControls.set_simID(i);
            System.out.println("Send sendControls interaction: " + coolTemps[i] + " to socket #" + i);
            sendControls.sendInteraction(getLRC());
            // SendModel vSendModel = create_SendModel();
            // vSendModel.set_dataString(dataStrings[i]);
            // System.out.println("Send SendModel interaction: " + coolTemps[i] + " to socket #" + i);
            // vSendModel.sendInteraction(getLRC());

            dataStrings[i] = "";
          }

          System.out.println("timestep after sending Socket... should advance after this: "+ currentTime);



            // System.out.println(currentTime);
            // System.out.println(dataString);

            // Writing data to file
            try{
                // Create new file
                
                String path="/home/vagrant/Desktop/GitHub/scu_research/ucef_projects/pjs_projects/EnergyPlusOpt2Fed/EnergyPlusOpt2Fed_deployment/DataSummary.txt";
                File file = new File(path);

                // If file doesn't exists, then create it
                if (!file.exists()) {
                    file.createNewFile();
                }

                FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
                BufferedWriter bw = new BufferedWriter(fw);

                // Write in file
                bw.write(currentTime+"\t"+hour+"\t"+ zoneTemps[0]+"\t"+ outTemps[0]+"\t"+ solarRadiation[0]+"\t"+ heatingEnergy[0]+"\t"+ coolingEnergy[0] + "\t"+ receivedHeatTemp[0]+"\t"+ receivedCoolTemp[0]+"\t"+heatTemps[0]+"\t"+coolTemps[0]+"\n");
               
                // Close connection
                bw.close();
            }
            catch(Exception e){
                System.out.println(e);
            }



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

        // can now exit while loop waiting for this interaction
        log.info("received RCModel_Controller interaction");
        receivedSocket = true;

        // Could make global var that holds simIDs but it would just be 0,1,2,...
        // int simID = 0;
        int simID = interaction.get_simID();
        System.out.println("numVars[simID] = " + numVars[simID]);
    	holder[simID] = interaction.get_dataString();
        System.out.println("holder[simID] = "+ holder[simID] );

        System.out.println("handle interaction loop");

        // "varName{varNameSplitter}double{doubleSplitter}"!!!
        String vars[] = holder[simID].split(doubleSeparater);
        System.out.println("vars[0] = "+vars[0]);
        System.out.println("length of vars = " + vars.length);
        int j=0;
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

        // organize varNames and doubles into vectors of values
        for(int i=0; i<j;i++){
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
          else if(varNames[i].equals("epSendHeatingEnergy")){
            heatingEnergy[simID] = Double.valueOf(doubles[i]);
          }
          else if(varNames[i].equals("epSendCoolingEnergy")){
            coolingEnergy[simID] = Double.valueOf(doubles[i]);
          }
          else if(varNames[i].equals("epSendNetEnergy")){
            netEnergy[simID] = Double.valueOf(doubles[i]);
          }
          else if(varNames[i].equals("epSendEnergyPurchased")){
            energyPurchased[simID] = Double.valueOf(doubles[i]);
          }
          else if(varNames[i].equals("epSendEnergySurplus")){
            energySurplus[simID] = Double.valueOf(doubles[i]);
          }
          else if(varNames[i].equals("epSendDayOfWeek")){
            dayOfWeek[simID] = Double.valueOf(doubles[i]);
          }
          else if(varNames[i].equals("epSendSolarRadiation")){
            solarRadiation[simID] = Double.valueOf(doubles[i]);
          }
          else if(varNames[i].equals("epSendHeatingSetpoint")){
            receivedHeatTemp[simID] = Double.valueOf(doubles[i]);
          }
          else if(varNames[i].equals("epSendCoolingSetpoint")){
            receivedCoolTemp[simID] = Double.valueOf(doubles[i]);
          }
          else if(varNames[i].equals("price")){
            price = Double.valueOf(doubles[i]);
          }
          // checking timesteps:
          else if(varNames[i].equals("timestep")){
            timestep_Socket = doubles[i];
          }
        }
      
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
