import sys
#TODO add other config stuff like simID
def Config(file,newIP,newPort):
    new_socket=""
    counter = 0
    with open(file) as openfile:
        for line in openfile:
            if "public String IP_address" in line:
                print(line)
                line = "public String IP_address = " + newIP
            if "public String Port_Number" in line:
                print(line)
                line = "public String Port_Number = " + newPort

            new_socket += line

    socket = open(file,"w")
    socket.write(new_socket)
    socket.close()

#==============================================================================
#============================= Input Everything here ==========================
# make the folderPath go to the {projectname}_generated folder
folderPath = "C:/Users\kpattawi\Desktop\Buildings\TestConfig_generated"   # CANT HAVE \U for some reason so change \Users.. to /Users..
numberOfSockets = 6
newIP = ""
portNumbers= [6789,6790,6791,6792,6793,6794]

# Use Socket naming convention: Socket0, Socket1, Socket2,...,SocketN-1
for i in range(0,numberOfSockets):
    filename = folderPath+"\Socket"+i+"\src\main\java\org\webgme\guest\socket"+i+"\InputSourceConfig.java"
    Config(filename,newIP,portNumbers[i])

#===============================================================================
#===============================================================================