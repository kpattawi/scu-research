# EPMultipleSims_v4
Below is information about where to add/change code for running simulations.

## config.txt
- Change the IP address and Port Number to match the virtual box
- Kaleb: Still working on changing this for multiple sockets

## Socket:
- If you have more than one Socket federate, you will need to change the simID for each additional federate.  The first Socket should have a simID=0, the 2nd should have a simID=1,... (If you only have one Socket, the simID=0)
    - This is around line 30
- Make sure to change the IP Address and Port Number in config.txt
- Add any other variables you would like to receive from Energy Plus (Make sure this matches the modelDescription.xml)
    - This is around line 150
- Add any other variables you would like to send to Energy Plus (Make sure this also matches FMU code)
    - This is around line 235
- Add any other variables you would like to receive from the Controller (also need to add them in Controller.java)
    - This is around line 300

## Controller:
- change numSockets to the number of EnergyPlus simulations.  numSockets=1 for just one EnergyPlus simulation (Kaleb: add to config.txt)
- change the code that determines information that gets sent back to the Socket (i.e. Heating and Cooling setpoints).  If you only have one EnergyPlus simulation, you should still use the loop so that it is easily scalable
    - This is around line 200
- Add any other values you would like to send to the socket
    - This is around line 250
- If you added any new variables to send from the Socket, Reader, or Market, add them to the handleInteractions


## Market:
- Kaleb: add this

## Reader:
- Kaleb: add this