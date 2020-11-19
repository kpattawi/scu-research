import sys

def Config(idf,searchExp,replaceExp):
    new_idf=""
    with open(idf) as openfile:
        for line in openfile:
            if searchExp in line:
                print (line)
                check = input("Replace this line to [" + replaceExp + "]? (Enter/No)")
                if check == "":
                    print ("Replaced")
                    line=replaceExp + "\n"
            new_idf += line

    idf2 = open(idf,"w")
    idf2.write(new_idf)
    idf2.close()

#==============================================================================
#============================= Input Everything here ==========================
idfname = "1.idf"
Keyword = "Timestep,"
NewParameter = "Timestep,6;"
#===============================================================================
#===============================================================================

for i in range(1,4):
    Config("./"+ str(i) + "/" +idfname,Keyword,NewParameter) # Change parameter here! 
