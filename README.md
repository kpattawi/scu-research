# scu_research
Files related to Kaleb Pattawi's Master's thesis at Santa Clara University

This repository will serve the purpose of organizing work, tracking changes/updates, and storing/sharing files.  Most, if not all of the work, in this repository will be related to research related to developing a scalable method of simulating multiple residential homes.  Using a co-simulation platform (UCEF), multiple EnergyPlus models can be run simultaneously with different controls.  The goal is to optimize controls in homes by predicting energy consumption and using different pricing strategies to minimize cost.


Energy Consumption Prediction Model:
This directory contains files that were used to predict energy consumption using data generated with Energy Plus written in MATLAB. This code uses a linear algebra approach of predicting energy consumption.  In the most recent update, past energy consumption, past outdoor temperature, past indoor temperature, past solar radiation, and past temperature setpoint are used to predict future energy consumption 3 timesteps ahead.  I have also been working on shifting to python and inside the "notebooks" directory you will find some jupyter notebooks.  Some are tutorials that I did to learn more about numpy and pandas.  Eventually, there will also be some notebooks using machine learning algorithms in sci-kit learn to predict energy consumption.

Java Codes for UCEF:
This directory contains files related to the way the simulation works. Currently, the codes have to do with sending/receiving data from multiple EnergyPlus simulations and processing the information in one federate.

