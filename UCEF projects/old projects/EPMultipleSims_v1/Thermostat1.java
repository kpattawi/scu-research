package org.webgme.guest.thermostat1;

import org.webgme.guest.thermostat1.rti.*;

import org.cpswt.config.FederateConfig;
import org.cpswt.config.FederateConfigParser;
import org.cpswt.hla.InteractionRoot;
import org.cpswt.hla.base.AdvanceTimeRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Random;

// Define the Thermostat1 type of federate for the federation.

public class Thermostat1 extends Thermostat1Base {
    private final static Logger log = LogManager.getLogger();

    private double currentTime = 0;

    public Thermostat1(FederateConfig params) throws Exception {
        super(params);
    }

    int hour=0, nexthour=0, quarter=0, fivemin=0, onemin=0, simulatetime=0;
    
    double r1 =0;
    double Preset_cool=23, Preset_heat=21;
    double event_p=0, duration_p=1, duration_q=0, nextevent_p=0;
    int occupancy = 2, check = 0, p=0, r2=0;
    double outTemp=23.0, coolTemp=23, heatTemp=21, zoneTemp=23.0, zoneHumidity=0,clo=1;
    String varname; 
    double value;
    
    double Last_cool=23, Last_heat=21;
    int Fuzzycool=0,Fuzzyheat=0;
    
    
    private void checkReceivedSubscriptions() {
        InteractionRoot interaction = null;
        while ((interaction = getNextInteractionNoWait()) != null) {
            if (interaction instanceof ReceiveEP1) {
                handleInteractionClass((ReceiveEP1) interaction);
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

            
            System.out.println("Logical Time is = " + currentTime);
//          quarter = (int) (currentTime%96);
//          fivemin = (int) (currentTime%288);
            onemin = (int) (currentTime%1440);


          
            hour = (int) (onemin/60);
            System.out.println("Hour is = " + hour);
            
            if (hour==simulatetime){
                System.out.println("Simulate at this time step!!!!!");
                event_p=pro[hour];
                System.out.println("Occupied Probability: "+ event_p);
                
                  
                //  Occupancy Duration Simulator Based on Monte Carlo Method
                while (check ==0){
//                  r2 = (int) (Math.random()*24+1);
                  r2=1;
                  System.out.println("Random Number(Duration): "+ r2 );
                  for (int i = 0; i < r2; i++){
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
                  
                  } else if(r1 > (1-duration_q)){
                    System.out.println("Continuously Not Occupied for "+r2+ " Hours Accepted!!!" );
                    check = 1;
                    simulatetime = simulatetime + r2;
                    occupancy=0;
                    if (simulatetime>23){
                      simulatetime=simulatetime-24;
                    } 
                    System.out.println("Next simulating time at hour = "+ simulatetime );
                    
                  
                    
                  } else {
                    System.out.println("Event Rejected!!!" );
                  }
                  duration_p=1;
                  duration_q=1;
                  
                  
                }
                check =0;
                
                }else{
                  System.out.println("Keep the same Occupancy information as previous timestamp.");
                }
            
            // Adaptive Control
            // Always-On
            if (outTemp<=10){
                heatTemp=18.9;
                coolTemp=22.9;
              }else if (outTemp>=33.5){
                heatTemp=26.2;
                coolTemp=30.2;
              }else {
                heatTemp = 0.31*outTemp + 17.8-2;
                coolTemp = 0.31*outTemp + 17.8+2;
              }

	    // For making a narrow upper band 
//	    heatTemp = heatTemp+2;
	    // For making a narrow upper-middle band
//	    heatTemp = heatTemp+1.5;
//	    coolTemp = coolTemp-0.5;
	    // For making a narrow middle band
//	    heatTemp = heatTemp+1;
//	    coolTemp = coolTemp-1;
	    // For making a narrow middle-lower band
//	    heatTemp = heatTemp+0.5;
//	    coolTemp = coolTemp-1.5;

	    // For making a narrow lower band
//	    coolTemp = coolTemp-2;


//	    coolTemp = 32;
//	    heatTemp = 12;
	    // For HEATER ONLY
//	    coolTemp =100;
	    // For AC ONLY
//	    heatTemp =-60;
	    

            // Occupancy-Driven Control
//            if (occupancy == 0 ){
//
//
//              coolTemp = 32;
//              heatTemp = 12;
//            }
            
           //0.5 degree fuzzy control 
            double offset=0.6;

               
            if (zoneTemp>=coolTemp+0.5-offset){
             Fuzzyheat = -1;
             Fuzzycool = -1;
            }else if (zoneTemp>=coolTemp-0.5-offset){
             Fuzzyheat = -1;
            }else if (zoneTemp>=heatTemp+0.5+offset){
              Fuzzyheat = -1;
             Fuzzycool = 1;
            }else if (zoneTemp>=heatTemp-0.5+offset){

             Fuzzycool = 1;
            }else{
             Fuzzyheat = 1;
             Fuzzycool = 1;
            }
              coolTemp = coolTemp -offset+ Fuzzycool*offset;
              heatTemp = heatTemp +offset+ Fuzzyheat*offset;

              // write to data to record occupancy information
              
              try{
                    // Create new file
                    
                    String path="/home/vagrant/Desktop/EnergyPlusData/Adaptive_Test_Occupancy.txt";
                    File file = new File(path);

                    // If file doesn't exists, then create it
                    if (!file.exists()) {
                        file.createNewFile();
                    }

                    FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
                    BufferedWriter bw = new BufferedWriter(fw);

                    // Write in file
                    bw.write(currentTime + "\t" + occupancy +  "\n" );

                    // Close connection
                    bw.close();
                }
                catch(Exception e){
                    System.out.println(e);
                }
            //=====================================

            
            
            ////////////////////////////////////////////////////////////
            // TODO send interactions that must be sent every logical //
            // time step below                                        //
            ////////////////////////////////////////////////////////////

              	// Send the Cooling Setpoint interaction's.
            
                SendEP1 coolT = create_SendEP1();                
                coolT.set_varName("epGetStartCooling");
                coolT.set_value(coolTemp);
                System.out.println("Send coolTemp interaction as epGetStartCooling: " + coolTemp);
                coolT.sendInteraction(getLRC(), currentTime + getLookAhead());

                // Send the Heating Setpoint interaction's.
                
                SendEP1 heatT = create_SendEP1();                
                heatT.set_varName("epGetStartHeating");
                heatT.set_value(heatTemp);
                System.out.println("Send heatTemp interaction as epGetStartCooling: " + heatTemp);
                heatT.sendInteraction(getLRC(), currentTime + getLookAhead());

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

    private void handleInteractionClass(ReceiveEP1 interaction) {
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
            Thermostat1 federate =
                new Thermostat1(federateConfig);
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
