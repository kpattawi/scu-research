import sys

def Config(idf,searchExp,replaceExp,numLines):
    new_idf=""
    counter = 0
    with open(idf) as openfile:
        for line in openfile:
            if counter>0 and counter<numLines:
                line = ""
                counter += 1
            if searchExp in line:
                print (line)
                # check = input("Replace this line to [" + replaceExp + "]? (Enter/No)")
                # if check == "":
                #     print ("Replaced")
                #     line=replaceExp + "\n"
                line = replaceExp + "\n"
                counter += 1

            new_idf += line

    idf2 = open(idf,"w")
    idf2.write(new_idf)
    idf2.close()

#==============================================================================
#============================= Input Everything here ==========================
folderPath = "C:/Users\kpattawi\Desktop\Buildings\TestConfig"   # CANT HAVE \U for some reason so change \Users.. to /Users..
idfname = "SF_California_San.Francisco.Intl.AP.724940_gasfurnace_crawlspace_IECC_2012.idf"
newTimestep = 12
newStartMonth = 1
newStartDay = 1
newEndMonth =  12
newEndDay = 31
numberOfIDFs = 3
#===============================================================================
#===============================================================================

Keyword1 = "Timestep,"
NewParameter1 = "Timestep,\n"+str(newTimestep)+";"
numLines1 = 2   # this is the number of lines that are in the "Timestep" class in EnergyPlus

Keyword2 = "RunPeriod,"
NewParameter2 = "RunPeriod,\nAnnual,\n"+str(newStartMonth)+",\n"+str(newStartDay)+",\n,\n"+str(newEndMonth)+",\n"+str(newEndDay)+",\n,\nUseWeatherFile,\nYes,\nYes,\nNo,\nYes,\nYes;"
numLines2 = 14   # this is number of lines that are in the "RunPeriod" class in EnergyPlus


for i in range(1,numberOfIDFs+1):
    Config(folderPath+ "/" +str(i) + "/" +idfname,Keyword1,NewParameter1,numLines1) # Change parameter here!
    Config(folderPath+ "/" +str(i) + "/" +idfname,Keyword2,NewParameter2,numLines2) # Change parameter here!
