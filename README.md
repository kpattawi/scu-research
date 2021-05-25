# scu_research
Kaleb Pattawi's Master's thesis at Santa Clara University

The work in this repository will be related to research on developing a scalable method of simulating multiple residential homes.  Using a co-simulation platform (UCEF), multiple EnergyPlus models can be run simultaneously with different controls.  The goal is to optimize controls in homes by predicting energy consumption and using different pricing strategies to minimize cost.

## ucef_projects:
This directory contains java files from projects made in UCEF:
- EPMultipleSims: This project has a base setup for running multiple energy plus simulations at the same time
- KalebsFed: This is a simple UCEF project that can be used to test if UCEF works.  It generates 2 random numbers and outputs the sum.
- randNum_noTimeDelay: This is also a random number generator but is a simple project used for testing whether or not there is a time delay between interactions.
Note: When adding more projects, remember to add the logs to the .gitignore file

## ucef_config
This directory contains a python file that will automatically change multiple Socket federates for the EPMultipleSims project

## idf_config:
This directory contains a python file that will automatically change the timestep and run period for multiple .idf files.

## fmu_config
This directory contains the files needed to make an fmu (Windows) that contains a .txt file for more easily changing the IP address and port number.
