// Basic built in files
#include <stdio.h>	
#include <stdlib.h>
#include <string.h>
#include <math.h>
// Project linker files
#include "FMU_Header_Files/Joe_ep_fmu.h" // Need to be in same folder as fmi header files to link properly
#include "Parser_Files/xml_parser.h"  // Put in folder for organization

ModelDescription* md; //creates md from xml file. Requires Parse Files

// Commenting out winsock
//#include<winsock2.h>
//WSADATA wsa;
//SOCKET s;

// Trying to implement linux socket
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>

int sockfd;
int isWarmupFlag = 1;
int nextStringIndex = 0;
char nextString[2048];

char* getNextString(char myMsg[2048])
{
	//printf("\n===== In getNextString Function =====\n");
	int currentIndex = nextStringIndex;
	memset(nextString, '\0', 2048); // resets memory to null
	while (!(myMsg[currentIndex] == '\r' && myMsg[currentIndex + 1] == '\n'))
	{
		//printf("currentInd: %d\n", currentInd);
		nextString[currentIndex - nextStringIndex] = myMsg[currentIndex];
		currentIndex = currentIndex + 1;
	}
	//printf("diffInd %d\n", currentInd - nextStringIndex);
	if ((currentIndex - nextStringIndex) == 0)
	{
		nextStringIndex = 0;
		return '\0';
	}
	nextString[currentIndex - nextStringIndex] = '\0';
	//printf("nextString = %s\n", nextString1);
	//printf("<===== End of getNextString Function\n");
	nextStringIndex = currentIndex + 2;

	return nextString;
	//return currentInd;
}

// Commenting out winsock
int myLineReader(int s, char server_reply[2048], char buffer[2048])
{
	int currentBufferLocation = 0;
	int recievedReplyLength;
	while ((recievedReplyLength = read(s, server_reply, 2048, 0)) > 0)
	{
		server_reply[recievedReplyLength] = '\0';
		if (currentBufferLocation == 0)
		{
			sprintf(buffer, "%s", server_reply); // or else something happens
		}
		else
		{
			sprintf(buffer, "%s%s", buffer, server_reply);
		}
		currentBufferLocation = currentBufferLocation + recievedReplyLength;
		if (currentBufferLocation >= 2048)
		{
			fprintf(myOutputLog, "bufferSize too large. Truncated incoming data.\n");
			break;
		}
		if (server_reply[recievedReplyLength - 2] == '\r' && server_reply[recievedReplyLength - 1] == '\n')// && bufferSize >= 2
		{
			//fprintf(myOutputLog, "Recieved end packet. \n");
			break;
		}
	}
	return 1;
}

/*-----------------------------------------------------------------------------------------------*/
// FMI FUNCTIONS: Platform, Version, Logging ...
/*-----------------------------------------------------------------------------------------------*/
const char* fmiGetTypesPlatform()
{
	return fmiPlatform;
}

const char* fmiGetVersion()
{
	return fmiVersion;
}

fmiStatus fmiSetDebugLogging(fmiComponent c, fmiBoolean loggingOn)
{
	return fmiOK;
}

/*-----------------------------------------------------------------------------------------------*/
// FMI DATA EXCHANGE FUNCTIONS: fmiGet___()
/*-----------------------------------------------------------------------------------------------*/
fmiStatus fmiGetReal(fmiComponent c, const fmiValueReference vr[], size_t nvr, fmiReal value[])
{
	fprintf(myOutputLog, "\n========================= fmiGetReal =========================\n");
	fprintf(myOutputLog, "MASTER (E+) GETTING VARS FROM SLAVE (fmu)\n");

	unsigned int i;
	for (i = 0; i < nvr; ++i)
	{
		ScalarVariable* myInst = getVariable(md, vr[i], elm_Real); // pulls specific var from md
		const char* thisVarName = getName(myInst); // gets name of var from md

		fprintf(myOutputLog, "SENT var to E+\n");
		fprintf(myOutputLog, "	Name: ------------- %s \n", thisVarName);
		value[i] = my_values[vr[i]]; // replaces E+ value from FMI's stored values
		fprintf(myOutputLog, "	Value: ------------ %.2f \n", (float)value[i]);
		fprintf(myOutputLog, "	Value Reference --- '%d' \n", vr[i]);
	}
	fflush(myOutputLog);
	return fmiOK;
}

/*----------------------ONLY ABOVE USED ^^^--------------------------------------------------*/
fmiStatus fmiGetInteger(fmiComponent c, const fmiValueReference vr[],
	size_t nvr, fmiInteger value[])
{
	return fmiError;
}

fmiStatus fmiGetBoolean(fmiComponent c, const fmiValueReference vr[],
	size_t nvr, fmiBoolean value[])
{
	return fmiError;
}

fmiStatus fmiGetString(fmiComponent c, const fmiValueReference vr[],
	size_t nvr, fmiString  value[])
{
	return fmiError;
}

/*-----------------------------------------------------------------------------------------------*/
// FMI DATA EXCHANGE FUNCTIONS: fmiSet___()
/*-----------------------------------------------------------------------------------------------*/
fmiStatus fmiSetReal(fmiComponent c, const fmiValueReference vr[],
	size_t nvr, const fmiReal    value[])
{
	fprintf(myOutputLog, "\n========================= fmiSetReal =========================\n");
	fprintf(myOutputLog, "MASTER (E+) SENDING VARS TO SLAVE (fmu)\n");

	unsigned int i;
	for (i = 0; i < nvr; ++i)
	{
		ScalarVariable* myInst = getVariable(md, vr[i], elm_Real); // pulls specific var from md
		const char* thisVarName = getName(myInst); // gets name of var from md

		fprintf(myOutputLog, "RECIEVED var from E+\n");
		fprintf(myOutputLog, "	Name: ------------- %s\n", thisVarName);
		fprintf(myOutputLog, "	Value: ------------ %.2f\n", value[i]);
		fprintf(myOutputLog, "	Value Reference: -- '%d'\n", vr[i]);
		my_values[vr[i]] = (float)value[i]; // copys E+ values into the FMI's stored values
	}
	fflush(myOutputLog);
	return fmiOK;
}

/*----------------------ONLY ABOVE USED ^^^--------------------------------------------------*/
fmiStatus fmiSetInteger(fmiComponent c, const fmiValueReference vr[],
	size_t nvr, const fmiInteger value[])
{
	return fmiError;
}

fmiStatus fmiSetBoolean(fmiComponent c, const fmiValueReference vr[],
	size_t nvr, const fmiBoolean value[])
{
	return fmiError;
}

fmiStatus fmiSetString(fmiComponent c, const fmiValueReference vr[],
	size_t nvr, const fmiString  value[])
{
	return fmiError;
}

/*-----------------------------------------------------------------------------------------------*/
// Creation and destruction of slave instances and setting debug status
/*-----------------------------------------------------------------------------------------------*/
fmiComponent fmiInstantiateSlave(fmiString  instanceName, fmiString  fmuGUID,
	fmiString  fmuLocation, fmiString  mimeType, fmiReal timeout, fmiBoolean visible,
	fmiBoolean interactive, fmiCallbackFunctions functions, fmiBoolean loggingOn)
{
	myOutputLog = fopen("../myOutputLog.log", "w"); // creates log file to write
	fprintf(myOutputLog, "\n========================= fmiInstantiateSlave =========================\n");
	fprintf(myOutputLog, "FMU LOCATION: %s\n", fmuLocation);
	fflush(myOutputLog);

	size_t sizeOfFmuLocation = strlen(fmuLocation); // finds size of location string
	char * fmuFilePath = (char *)malloc((sizeOfFmuLocation + 1) * sizeof(char)); //allocates memory for file location string
	for (size_t i = 0; i < sizeOfFmuLocation; i++) // switches direction of backslashes to work properly
	{
		if (fmuLocation[i] == '\\') {
			fmuFilePath[i] = '/';
		}
		else {
			fmuFilePath[i] = fmuLocation[i];
		}
	}
	if (fmuFilePath[sizeOfFmuLocation - 1] == '/')  // removes final slash if there is one
	{
		fmuFilePath[sizeOfFmuLocation - 1] = '\0';
	}
	fmuFilePath[sizeOfFmuLocation] = '\0'; // assures there is an ending null character (OK to have two)

	char * xmlFileName = "modelDescription.xml"; // name of xml in fmu
	 //allocates memory for full file location
	char * xmlFilePath = (char *)malloc(sizeof(char)*(strlen(fmuFilePath) + 1 + strlen(xmlFileName) + 1));
	sprintf(xmlFilePath, "%s/%s", fmuFilePath, xmlFileName); // copys fmupath and xml file name for full xml path
	fprintf(myOutputLog, "XML LOCATION: %s\n", xmlFilePath);

	md = parse(xmlFilePath); // parses through xml to populate model description, md

	int myNumStates = getNumberOfStates(md);
	int myNumEventIndicators = getNumberOfEventIndicators(md);
	int myNumVars = md->n;
	fprintf(myOutputLog, "myNumStates: ----------- %d\n", myNumStates);
	fprintf(myOutputLog, "myNumEventIndicators: -- %d\n", myNumEventIndicators);
	fprintf(myOutputLog, "myNumVars: ------------- %d\n", myNumVars);
	fflush(myOutputLog);
	/* CODE TO PRINT CONTENTS OF XML FOR SANITY PURPOSE
	FILE * xmlFile = fopen(xmlFilePath, "r");
	if (xmlFile == NULL)
	{
	fprintf(myOutputLog, "ERROR: No XML file found in FMU\n");
	// logs error if no xml file in fmu
	}

	char nextCharacter;
	fprintf(myOutputLog, "\n========== XML Content ==========\n");
	while ((nextCharacter = getc(xmlFile)) != EOF) {
	fprintf(myOutputLog, "%c", nextCharacter);
	}
	fprintf(myOutputLog, "\n");
	fclose(xmlFile);
	/*END CODE TO PRINT CONTENTS OF XML*/

	// removes vars from memory
	free(xmlFilePath);
	free(fmuFilePath);
	fflush(myOutputLog);

	//TODO: restructure to find # vars based on modelDescription, md
	my_values = malloc(sizeof(float) * 25); // 10 represents # of variables transfered (should change appropriately)


	// TODO: HARD CODED -- WANT SOME WAY AROUND THIS
	my_values[17] = (float)0; //starting cooling Start
	my_values[18] = (float)0; //starting heating Start
	fprintf(myOutputLog, "NOTE:\n");
	fprintf(myOutputLog, "	- EnergyPlus cannot be one day duration (for warmup distinction).\n");
	fprintf(myOutputLog, "	- Should look into finding total \"input\" and \"output\" causalities.\n");
	fprintf(myOutputLog, "		-- Information will change allocation for my_values array.\n");
	fprintf(myOutputLog, "	- Inaccuracies exist when turning recieving string value to float.\n");
	fprintf(myOutputLog, "	- Initial conditions (first iteration) hardcoded here. \n");
	fprintf(myOutputLog, "		-- Will be replaced by socket before E+ reads it.\n");
	fflush(myOutputLog);



	return my_values; //Seems need to return allocated memory
} // End fmiInstantiateSlave()

fmiStatus fmiInitializeSlave(fmiComponent c, fmiReal tStart, fmiBoolean StopTimeDefined, fmiReal tStop)
{
	fprintf(myOutputLog, "\n========================= fmiInitializeSlave =========================\n");

	if (tStop - tStart > 86400)
	{
		isWarmupFlag = 0;
	}

	if (isWarmupFlag == 0)
	{
		// Read IP information from config
		FILE* fp;
		char buff[255];
		char IP_line[255];
		char Port_line[255];
		char* IPaddress_from_config;
		char* PortNumber_from_config;

		fp = fopen("tmp-fmus/Joe_ep_fmu.fmu_Joe_ep_fmu/ipconfig.txt ", "r");
		fgets(buff, 255, (FILE*)fp);
		fprintf(myOutputLog, "Ipaddress from config file\n");

		fgets(buff, 255, (FILE*)fp);
		strcpy(IP_line, buff);
		fprintf(myOutputLog, "buff = %s\n", buff);
		IPaddress_from_config = strtok(IP_line, ","); // find the first double quote
		IPaddress_from_config = strtok(NULL, ",");   // find the second double quote

		fprintf(myOutputLog, "IP address = %s\n", IPaddress_from_config);


		fgets(buff, 255, (FILE*)fp);
		strcpy(Port_line, buff);
		PortNumber_from_config = strtok(Port_line, ","); // find the first double quote
		PortNumber_from_config = strtok(NULL, ",");   // find the second double quote
		int portNumber = atoi(PortNumber_from_config);

		fprintf(myOutputLog, "Port Number = %i\n", portNumber);

		fclose(fp);

		fprintf(myOutputLog, "------ STARTING SOCKET CONNECTION ------\n");


		// THIS IS SOCKET STUFF

		//struct sockaddr_in server;
		int sockfd, portno, n;
		struct sockaddr_in serv_addr;
		char buffer[256];

		fprintf(myOutputLog, "Initializing socket...\n");
		sockfd = socket(AF_INET, SOCK_STREAM, 0);
		if (sockfd < 0)
			error("ERROR opening socket");
		
		bzero((char*)&serv_addr, sizeof(serv_addr));
		serv_addr.sin_family = AF_INET;
		bcopy(IPaddress_from_config,
			(char*)&serv_addr.sin_addr.s_addr,
			strlen(IPaddress_from_config);
		serv_addr.sin_port = htons(portNumber);
		if (connect(sockfd, (struct sockaddr*)&serv_addr, sizeof(serv_addr)) < 0)
			error("ERROR connecting");
		
		//close(sockfd);


		// Commenting out winsock
		//fprintf(myOutputLog, "Initializing Winsock...\n");
		//if (WSAStartup(MAKEWORD(2, 2), &wsa) != 0)
		//{
		//	fprintf(myOutputLog, "Failed. Error Code : %d", WSAGetLastError());
		//	//return 1;
		//}

		//fprintf(myOutputLog, "...Initialized.\n");

		//Create a socket


		//if ((s = socket(AF_INET, SOCK_STREAM, 0)) == INVALID_SOCKET)
		//{
		//	fprintf(myOutputLog, "Could not create socket : %d", WSAGetLastError());
		//}
		//fprintf(myOutputLog, "...Socket created.\n");

		
		//IP information

		//// Commenting out winsock
		//server.sin_addr.s_addr = inet_addr(IPaddress_from_config); //ip address
		//server.sin_family = AF_INET;
		//server.sin_port = htons(portNumber); //port #

		//Connect to remote server
		//if (connect(s, (struct sockaddr *)&server, sizeof(server)) < 0)
		//{
		//	fprintf(myOutputLog, "connect error\n");
		//	//return 1;
		//}
		//fprintf(myOutputLog, "...Connected\n");
	}
	return fmiOK;
} // End fmiInitializeSlave()

fmiStatus fmiTerminateSlave(fmiComponent c)
{
	return fmiOK;
}

fmiStatus fmiResetSlave(fmiComponent c)
{
	return fmiOK;
}

/*-------------------------------- fmiFreeSlaveInstance() ---------------------------------------*/
//  Master FMU is done with the Slave.
/*-----------------------------------------------------------------------------------------------*/
void fmiFreeSlaveInstance(fmiComponent c)
{
	fprintf(myOutputLog, "\n========================= fmiFreeSlaveInstance =========================\n");
	// frees remaining vars from memory
	free(my_values);
	freeElement(md); // frees modelDescription, md

	if (isWarmupFlag == 0)
	{
		printf("--- CLOSING CONNECTION ---\n");
		fprintf(myOutputLog, "--- CLOSING CONNECTION ---\n");

		char message[1024];
		sprintf(message, "TERMINATE\r\n\r\n");
		if (send(sockfd, message, strlen(message), 0) < 0)
		{
			fprintf(myOutputLog, "Send failed\n");
			//return 1;
		}
		fprintf(myOutputLog, "Data Sent: %s\n", message);
		close(sockfd);
		//closesocket(s); // frees socket
		//WSACleanup(); // cleans socket
	}

	fclose(myOutputLog);	//closes output file
}
/*----------------------------------------------------------------*/
// DERIVIATIVES 
/*-----------------------------------------------------------------------------------------------*/
fmiStatus fmiSetRealInputDerivatives(fmiComponent c, const  fmiValueReference vr[],
	size_t nvr, const  fmiInteger order[], const  fmiReal value[])
{
	return fmiError;
}

fmiStatus fmiGetRealOutputDerivatives(fmiComponent c, const   fmiValueReference vr[],
	size_t  nvr, const   fmiInteger order[], fmiReal value[])
{
	return fmiError;
}

/*------------------------------------------------------------------------- fmiCancelStep() -----*/
// STEPS
/*-----------------------------------------------------------------------------------------------*/
fmiStatus fmiCancelStep(fmiComponent c)
{
	return fmiError;
}

fmiStatus fmiDoStep(fmiComponent c, fmiReal currentCommunicationPoint,
	fmiReal communicationStepSize, fmiBoolean newStep)
{

	fprintf(myOutputLog, "\n========================= fmiDoStep (%.0f sec) =========================\n", currentCommunicationPoint);
	unsigned int i;
	for (i = 1; i<3; ++i)
	{
		ScalarVariable* myInst = getVariable(md, i, elm_Real); // pulls specific var from md
		const char* thisVarName = getName(myInst); // gets name of var from md

		fprintf(myOutputLog, "%d: %f ---------- %s\n", i, my_values[i], thisVarName);
		fflush(myOutputLog);
	}
	if (isWarmupFlag == 0)
	{
		// SOCKET CONNECTION VARS
		char mySendMsg[2048], server_reply[2048];
		int recv_size;
		char buffer[2048];

		// ========== Sending Data ==========
		fprintf(myOutputLog, "<---------- Sending Data ---------->\n");
		printf("<---------- Sending Data ---------->\n");

		// Loops through all output vars into string to send
		sprintf(mySendMsg, "UPDATE\r\n%.0f\r\n", currentCommunicationPoint); //sets steart of outgoing message with timestamp
		for (i = 1; i < 17; ++i) // TODO loop through all "input" causalities
		{
			ScalarVariable* myInst = getVariable(md, i, elm_Real); // pulls specific var from md
			const char* thisVarName = getName(myInst); // gets name of var from md
			fprintf(myOutputLog, "	Preparing to send %s as %f\n", thisVarName, my_values[i]);
			fflush(myOutputLog);
			printf("Preparing to send %s as %f\n", thisVarName, my_values[i]);
			sprintf(mySendMsg, "%s%s\r\n%f\r\n", mySendMsg, thisVarName, my_values[i]);
		}
		sprintf(mySendMsg, "%s\r\n", mySendMsg); // adds final \r\n to outgoing message
		// Send through socket command
		if (send(sockfd, mySendMsg, strlen(mySendMsg), 0) < 0)
		{
			fprintf(myOutputLog, "	Send failed\n");
			printf("Send failed\n");
			return fmiError;
		}
		fprintf(myOutputLog, "	Data Sent!\n");
		printf("Data Sent!\n");

		// ---------- Recieving Data ----------
		fprintf(myOutputLog, "----------> Recieving Data <----------\n");
		printf("----------> Recieving Data <----------\n");
		// trying to replace winSock
		if (myLineReader(sockfd, server_reply, buffer) < 0)
		{
			fprintf(myOutputLog, "	Could not read line properly\n");
			fflush(myOutputLog);
			return fmiError;
		}
		fprintf(myOutputLog, "	Data Recieved! \n");
		fflush(myOutputLog);
		printf("Data Recieved\n");

		// ----- Parsing recieved data string -----
		char* thisHeading;
		char* thisTimeString;
		char* thisVarName;
		char* thisValue;

		nextStringIndex = 0;

		//finds header -- first expected value
		thisHeading = getNextString(buffer);
		fprintf(myOutputLog, "	Recieved Header as %s\n", thisHeading);
		fflush(myOutputLog);
		printf("Recieved Header as %s\n", thisHeading);

		if (!strcmp(thisHeading, "SET"))
		{
			//finds time value -- second expected value
			thisTimeString = getNextString(buffer); 
			int thisRecievedTime = atoi(thisTimeString); //converted string to int
			fprintf(myOutputLog, "	Recieved Time as %d\n", thisRecievedTime);
			fflush(myOutputLog);
			printf("Recieved Time as %d\n", thisRecievedTime);

			while ( (thisVarName = getNextString(buffer)) != '\0') // until nothing is between \r\n\r\n
			{
				// Expected Variable Name
				// **update for thisVarName happens when testing while loop condition
				fprintf(myOutputLog, "	Recieved %s ", thisVarName);
				fflush(myOutputLog);
				printf("Recieved %s ", thisVarName);

				// gets value reference
				ScalarVariable* myInst = getVariableByName(md, thisHeading);
				int myValRef = getValueReference(myInst);

				// Expected Value
				thisValue = getNextString(buffer);
				float thisRecievedValue = atof(thisValue); //converted string to float
				fprintf(myOutputLog, "as %f\n", thisRecievedValue);
				fflush(myOutputLog);
				printf("as %f\n", thisRecievedValue);

				// Replacing values
				my_values[myValRef] = thisRecievedValue;
				fprintf(myOutputLog, "	 --New my_values[%d] = %f\n", myValRef, my_values[myValRef]);
				fflush(myOutputLog);
				//have condition if no matching vr
			}
		}
		else if (thisHeading == "NOUPDATE\0")
		{
			fprintf(myOutputLog, "NOUPDATE\n");
			printf("NOUPDATE\n");;
			fflush(myOutputLog);
		}
		else
		{
			fprintf(myOutputLog, "No expected headers found\n");;
			fflush(myOutputLog);
		}
	}
	return fmiOK;
} // End fmiDoStep()


  /*-----------------------------------------------------------------------------------------------*/
  // FMI STATUS FUNCTIONS
  /*-----------------------------------------------------------------------------------------------*/
fmiStatus fmiGetStatus(fmiComponent c, const fmiStatusKind s, fmiStatus* value)
{
	return fmiError;
}

fmiStatus fmiGetRealStatus(fmiComponent c, const fmiStatusKind s, fmiReal*    value)
{
	return fmiError;
}

fmiStatus fmiGetIntegerStatus(fmiComponent c, const fmiStatusKind s, fmiInteger* value)
{
	return fmiError;
}

fmiStatus fmiGetBooleanStatus(fmiComponent c, const fmiStatusKind s, fmiBoolean* value)
{
	return fmiError;
}

fmiStatus fmiGetStringStatus(fmiComponent c, const fmiStatusKind s, fmiString*  value)
{
	return fmiError;
}
