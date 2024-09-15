# ConVaI_ABSS Project

This project is a simulator for misinformation diffusion using Repast Simphony, including a tool for the management of the results in a database. In this file we give context to each folder. The instructions for installation and use are in the manuals of the [manuales](https://github.com/Kasdeyael/SimulatorFN/blob/main/manuales.pdf) file (only in Spanish). This file corresponds with a previous version of the tool and there are some differences, although the important information for installation and general usage are the same. 

## Simulator Tool
Contained in this folder we have the simulator with the implemented Textual Content Neutral-Vaccinated-Infected (**CoNVaI**) ABSS model. Simply run the *.bat* or *.command* file in accordance with the OS you have. This model might need some additional files (such as the user network), which can be found in the *Input_Simulator* folder. 

## Database
The database schema is included in the *mysql/init* subfolder and the full database (350MB zip file) can be downloaded from this [Google Drive link](https://drive.google.com/file/d/13W5rERaGbCK8PMqLe001Sv6T-Dx9Xjyd/view?usp=drive_link). 
This folder contains a docker-compose file to deploy the container in Docker. The folder mysql/init contains only the schema due to the size of the full database.

Before running the database, it's recommended to change the passwords of the *docker-compose.yml* file. After this configuration, simply run the command `docker compose up -d` from the directory with the *docker-compose.yml* file and let it load until it has initialized.


## ManagerSim

The source code for this tool is provided in the subfolder *ManagerSim*. It can be run by using the *.bat* or *.command* files provided within the *Executable* subfolder. It uses the *db.properties* file for configuration purposes. The database user and password needs to be updated here, based on the configuration set for the database. 

The last subfolder, *source_diff_db*, contains the information from the test set within the dataset used for this project. It contains the diffusion that is loaded in the database for the simulation comparison and error computation.


## Notebooks

This folder contains the zipped Jupyter Notebooks for the project, from the processing of the data, to the analysis and input/output of the models used within **CoNVaI**.


## Regression Data

This folder contains the links for the outputs of the MultiAzterTool and Empath, used within the model. 