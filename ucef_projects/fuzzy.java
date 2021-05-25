double fuzzy_heat = 0;  // NEEDS TO BE GLOBAL VAR outside of while loop
double fuzzy_cool = 0;  // NEEDS TO BE GLOBAL VAR outside of while loop

double max_cool_temp = 30.2; 
double min_heat_temp = 18.9; 
double OFFSET = 0.6; // need to change slightly higher/lower so E+ doesnt have issues

// I think if we set these as a band 
heatTemps[i]=Double.parseDouble(futureIndoorTemp[p])-0.5;
coolTemps[i]=Double.parseDouble(futureIndoorTemp[p])+0.5;
coolTemps[i] = 30.2 // do this for now to avoid turning on AC

// Determine minimum and maximum temperatures allowed (we can probably print this from optimization code too)
if (outTemps[i]<=10){
    min_heat_temp =18.9;
    max_cool_temp =22.9;
}else if (outTemps[i]>=33.5){
    min_heat_temp =26.2;
    max_cool_temp =30.2;
}else {
    min_heat_temp = 0.31*outTemps[i] + 17.8-2;
    max_cool_temp = 0.31*outTemps[i] + 17.8+2;
}

// Now set maximum cool and minimum heats:
if (coolTemps[i]>=max_cool_temp){
    coolTemps[i]=max_cool_temp;
    }
if (heatTemps[i]<=min_heat_temp){
    heatTemps[i]=min_heat_temp;
    }

// For Cooling 1 degree under Cooling setpoint:
if (zoneTemps[i] >= coolTemps[i]-.1){ // first check if going to exit maximum band
    fuzzy_cool = -1;
} else if (zoneTemps[i] <= coolTemps[i]-1.1){
    fuzzy_cool = 1;
}
coolTemps[i] = coolTemps[i] - 0.6 +fuzzy_cool*OFFSET;   // -0.6 so that oscillates 0.1-1.1 degree under cooling setpoint

// For Heating 1 degree under Heating setpoint:
if (zoneTemps[i] <= heatTemps[i]+.1){ // first check if going to exit minimum band
    fuzzy_heat = 1;
} else if (zoneTemps[i] >= heatTemps[i]+1.1){
    fuzzy_heat = -1;
}
heatTemps[i] = heatTemps[i] + 0.6 +fuzzy_heat*OFFSET;  // +0.6 so that oscillates 0.1-1.1 degree above heating setpoint