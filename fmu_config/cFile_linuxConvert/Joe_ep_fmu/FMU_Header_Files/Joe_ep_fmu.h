#ifndef Joe_ep_fmu_H
#define Joe_ep_fmu_H

//#define FMI_COSIMULATION
// define class name and unique id
#define MODEL_IDENTIFIER Joe_ep_fmu
#define MODEL_GUID "{818642F1-D7D4-4DC7-8549-554862454199}"
#include "fmiFunctions.h"
#include "fmiPlatformTypes.h"

//-----------My Added Stuff Below------------

FILE *myOutputLog;		//pointer to output file

float *my_values;	//pointer to set of memory allocated

#define LINELEN 256  /* == _MAX_FNAME */

#endif // Joe_ep_fmu


