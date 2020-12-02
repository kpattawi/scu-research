# scu_research
Files related to Kaleb Pattawi's Master's thesis at Santa Clara University

This repository will serve the purpose of organizing work, tracking changes/updates, and storing/sharing files.  Most, if not all of the work, in this repository will be related to research related to developing a scalable method of simulating multiple residential homes.  Using a co-simulation platform (UCEF), multiple EnergyPlus models can be run simultaneously with different controls.  The goal is to optimize controls in homes by predicting energy consumption and using different pricing strategies to minimize cost.


## energyPrediction:
This directory contains files that were used to predict energy consumption using data generated with Energy Plus written in MATLAB. This code uses a linear algebra approach of predicting energy consumption.  In the most recent update, past energy consumption, past outdoor temperature, past indoor temperature, past solar radiation, and past temperature setpoint are used to predict future energy consumption 3 timesteps ahead.  I have also been working on shifting to python and inside the "notebooks" directory you will find some jupyter notebooks.  Some are tutorials that I did to learn more about numpy and pandas.  Eventually, there will also be some notebooks using machine learning algorithms in sci-kit learn to predict energy consumption.

## projects:
This directory contains java files from projects made in UCEF:
- EnergyPlusPMV: This is the original project used for the occupancy drive/ schedule based/ always on simulations in different cities.
- EPMultipleSims_v1: This is the first attempt at making a federation that can run multiple Energy Plus simulations.  This was successful but the number of federates/interactions can be reduced making the structure more scalable.
- EPMultipleSims_v2: This project is the second attempt at making a federation that can run multiple Energy Plus simulations simultaneously. This version uses only one controller (thermostat) for all the Energy Plus simulations and also uses an improved interaction method that will have a single interaction to and from each socket.

## energyPlus_config:
This directory contains a python file that will automatically change the timestep for multiple .idf files