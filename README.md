# scu_research
Kaleb Pattawi's Master's thesis at Santa Clara University

This repository will serve the purpose of organizing work, tracking changes/updates, and storing/sharing files.  Most, if not all, of the work in this repository will be related to research related to developing a scalable method of simulating multiple residential homes.  Using a co-simulation platform (UCEF), multiple EnergyPlus models can be run simultaneously with different controls.  The goal is to optimize controls in homes by predicting energy consumption and using different pricing strategies to minimize cost.

## ucef_projects:
This directory contains java files from projects made in UCEF:
- EPMultipleSims: This project has a base setup for running multiple energy plus simulations at the same time
- KalebsFed: This is a simple UCEF project that can be used to test if UCEF works.  It generates 2 random numbers and outputs the sum.
- randNum_noTimeDelay: This is also a random number generator but is a simple project used for testing whether or not there is a time delay between interactions.
Note: When adding more projects, remember to add the logs to the .gitignore file

## ucef_config
This directory contains a python file that will automatically change multiple Socket federates for the EPMultipleSims project

## energyPlusIDF_config:
This directory contains a python file that will automatically change the timestep and run period for multiple .idf files.

## energyPrediction:
This branch contains files that were used to predict energy consumption using data generated with Energy Plus written in MATLAB. This code uses a linear algebra approach of predicting energy consumption.  In the most recent update, past energy consumption, past outdoor temperature, past indoor temperature, past solar radiation, and past temperature setpoint are used to predict future energy consumption 3 timesteps ahead.  I have also been working on shifting to python and inside the "notebooks" directory you will find some jupyter notebooks.  Some are tutorials that I did to learn more about numpy and pandas.  Eventually, there will also be some notebooks using machine learning algorithms in sci-kit learn/tensorflow to predict energy consumption.

## fmu_config
This directory contains the files needed to make an fmu (Windows) that contains a .txt file for more easily changing the IP address and port number.
