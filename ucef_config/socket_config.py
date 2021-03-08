import sys

def Config(file,old,new):
    new_socket=""
    counter = 0
    with open(file) as openfile:
        for line in openfile:
            if searchExp in line:
                print (line)
                line.replace(old,new,1)

            new_socket += line

    socket = open(file,"w")
    socket.write(new_socket)
    socket.close()

#==============================================================================
#============================= Input Everything here ==========================
folderPath = "C:/Users\kpattawi\Desktop\Buildings\TestConfig"   # CANT HAVE \U for some reason so change \Users.. to /Users..
numberOfSockets = 6
for i in range(0,numberOfSockets):
    filename = folderPath+"\Socket"+i+""+i
    old_Value1 = "Socket"
    old_Value2 = "SocketBase"
    # old_Value3 = IP address and Port num... Might be easier to just replace whole line
    new_Value1 = "Socket"+i
    new_value2 = "SocketBase"+i
    # new_Value3 =

    Config(filename,old_Value1,new_Value1)
    Config(filename,old_Value2,new_Value2)
    # Config(filename,old_Value3,new_Value3)

#===============================================================================
#===============================================================================