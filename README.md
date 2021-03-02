# scu_research
Files related to Kaleb Pattawi's Master's thesis at Santa Clara University

This repository will serve the purpose of organizing work, tracking changes/updates, and storing/sharing files.  Most, if not all of the work, in this repository will be related to research related to developing a scalable method of simulating multiple residential homes.  Using a co-simulation platform (UCEF), multiple EnergyPlus models can be run simultaneously with different controls.  The goal is to optimize controls in homes by predicting energy consumption and using different pricing strategies to minimize cost.

## UCEF_projects:
This directory contains java files from projects made in UCEF:
- EPMultipleSims_v2: This project is the second attempt at making a federation that can run multiple Energy Plus simulations simultaneously. This version uses only one controller (thermostat) for all the Energy Plus simulations and also uses an improved interaction method that will have a single interaction to and from each socket.
- EPMultipleSims_v2_6sims: Has six socket federates for simulating six houses simultaneously
- EPMultipleSims_v3: This version removes the time delay that existed from communicating between federates and will also have one socket that can host multiple clients
- KalebsFed: This is a simple UCEF project that can be used to test if UCEF works.  It generates 2 random numbers and outputs the sum.
- randNum_noTimeDelay: This is also a random number generator but is a simple project used for testing whether or not there is a time delay between interactions.

## energyPlusIDF_config:
This directory contains a python file that will automatically change the timestep and run period for multiple .idf files.

## energyPrediction:
This branch contains files that were used to predict energy consumption using data generated with Energy Plus written in MATLAB. This code uses a linear algebra approach of predicting energy consumption.  In the most recent update, past energy consumption, past outdoor temperature, past indoor temperature, past solar radiation, and past temperature setpoint are used to predict future energy consumption 3 timesteps ahead.  I have also been working on shifting to python and inside the "notebooks" directory you will find some jupyter notebooks.  Some are tutorials that I did to learn more about numpy and pandas.  Eventually, there will also be some notebooks using machine learning algorithms in sci-kit learn/tensorflow to predict energy consumption.

## fmu_config
This directory contains the files needed to make an fmu (Windows).
