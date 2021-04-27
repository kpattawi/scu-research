# energyOptTset ... expanding on energyOpt1 by solving for Tset

from cvxopt import matrix, solvers, sparse, spmatrix
from cvxopt.modeling import op, dot, variable
from decimal import Decimal
import time
import pandas as pd
import sys
#start_time=time.process_time()



# Parameters
heatorcool = 'heat'
comfortZone_upper = 24.0
comfortZone_lower = 20.0
timestep = 5*60
pricingmultfactor = 4.0
pricingoffset = 0.10

n=48
x=variable(n)

Output = matrix(0.00, (324,2))
J = matrix(1.00, (48,1))
cost =0
block = int(sys.argv[1]) +1 # block goes 1:6
temp_indoor_initial = float(sys.argv[2])

# Data from EnergyPlus
# temp_indoor_initial = 20.0

# Import coefficient matrix
df = pd.read_excel('CoefficientMatrix.xlsx', sheet_name='HP2') # sheet 3 before, 12nonzero
coeffmatrix=matrix(df.to_numpy())

# Input EnergyPlus data matrix
df = pd.read_excel('Jan15min.xlsx', sheet_name='Jan1thru7') #Sheet1_2
EPdata=matrix(df.to_numpy())
#print(EPdata[0,0])

# Input WholesalePrice data
df = pd.read_excel('WholesalePrice.xlsx', sheet_name='Jan1thru7') #Sheet1_2
wholesaleprice=matrix(df.to_numpy())
#print(EPdata[0,0])

M = matrix(0.00, (97,1))
M[0,0]=temp_indoor_initial
k=(block-1)*12 # used to be 0 before looping

i = 1
while i<96:
	M[i,0] = EPdata[k,0]
	M[i+1,0] = EPdata[k,1]
	
	k=k+1
	i=i+2
#print(M)

# c matrix is hourly cost per kWh of energy
#c = matrix(0.20, (48,1)) #324 before

c = matrix(0.00, (48,1))
i = 0
k=(block-1)*12
while i<48:
	c[i,0] = wholesaleprice[k,0]*pricingmultfactor/1000+pricingoffset
	k=k+1
	i = i+1
print(c)

# splitting coeff matrix into coeff of Tin, Tout, Qs and Eusedh, Eusedc
coeffmatrix1 = matrix(0.00, (48,97))
coeffmatrix2 = matrix(0.00, (48,48)) # (48,96)separate heating/cooling
i=1
itr = 0
itr2=1
j=0
while j<48:
	coeffmatrix1[j,0]=coeffmatrix[j,0]
	j = j+1
while i<145:
	j=1
	while j<4: # j<4 for separate heating/cooling
		if j==3: # j==3 for separate heating/cooling
			ii=0
			while ii<n:
				coeffmatrix2[ii,itr]=coeffmatrix[ii,i+j-1]
				ii=ii+1
			itr = itr+1
		else:
			ii=0
			while ii<n:
				coeffmatrix1[ii,itr2]=coeffmatrix[ii,i+j-1]
				ii=ii+1
			itr2 = itr2+1
		j = j+1
	i=i+3

#print(coeffmatrix1)
#print(coeffmatrix2)

## Added 12/11
heat_positive = matrix(0.0, (n,n))
i = 0
while i<n:
	heat_positive[i,i] = -1.0 # setting boundary condition: Energy used at each timestep must be greater than 0
	i +=1

cool_negative = matrix(0.0, (n,n))
i = 0
while i<n:
	cool_negative[i,i] = 1.0 # setting boundary condition: Energy used at each timestep must be less than 0
	i +=1


d = matrix(0.0, (n,1))
energyLimit = matrix(0.40, (n,1))

heatineq = (heat_positive*x<=d)
coolineq = (cool_negative*x<=d)
heatlimiteq = (cool_negative*x<=energyLimit)

	# time to solve for energy at each timestep
#ineq = (coeffmatrix2*x <= comfortZone_upper-coeffmatrix1*M)
#ineqlower = (-coeffmatrix2*x <= -comfortZone_lower+coeffmatrix1*M)

coeffmatrix3 = matrix([coeffmatrix2,-coeffmatrix2])
comfortMatrix = matrix([comfortZone_upper*J-coeffmatrix1*M, -comfortZone_lower*J+coeffmatrix1*M])
ineq = (coeffmatrix3*x <= comfortMatrix)

#print(len(coeffmatrix2))
#print(coeffmatrix3)

#with pd.ExcelWriter('OutdoorTemp.xlsx', mode ='a') as writer:
	#df = pd.DataFrame(M).T
	#df.to_excel(writer, sheet_name = 'Sheet7')
	#df = pd.DataFrame(coeffmatrix1).T
	#df.to_excel(writer, sheet_name = 'Sheet8')
	#df = pd.DataFrame(coeffmatrix2).T
	#df.to_excel(writer, sheet_name = 'Sheet9')
	#df = pd.DataFrame(coeffmatrix3).T
	#df.to_excel(writer, sheet_name = 'Sheet10')
	#df = pd.DataFrame(coeffmatrix).T
	#df.to_excel(writer, sheet_name = 'Sheet11')
	#df = pd.DataFrame(comfortMatrix).T
	#df.to_excel(writer, sheet_name = 'Sheet12')

if heatorcool == 'heat':
	lp2 = op(dot(c,x),ineq)
	op.addconstraint(lp2, heatineq)
	op.addconstraint(lp2, heatlimiteq)
	#op.addconstraint(lp2, ineqlower)
if heatorcool == 'cool':
	lp2 = op(dot(-c,x),ineq)
	op.addconstraint(lp2, coolineq)
	#op.addconstraint(lp2, ineqlower)
lp2.solve()
energy = x.value
#print(lp2.objective.value())
print('energy consumption')
# print(energy)
j=0
while j<13:
	print(energy[j])
	j = j+1
print('indoor temp prediction')
temp_indoor = matrix(0.0, (n,1))
ti1 = coeffmatrix1*M
ti2 = coeffmatrix2*energy
temp_indoor = ti1+ti2
# print(temp_indoor)
j=0
while j<13:
	print(temp_indoor[j])
	j = j+1
#temp_indoor[0,0] = temp_indoor_initial
# ti1 = coeffmatrix1*M
# ti2 = coeffmatrix2*energy
# temp_indoor = ti1+ti2
	#p = 1
	#while p<n:
	#	temp_indoor[p,0] = timestep*(c1*(temp_outdoor[p-1,0]-temp_indoor[p-1,0])+c2*energy[p-1,0]+c3*q_solar[p-1,0])+temp_indoor[p-1,0]
	#	p = p+1
	#Output[ii*12:ii*12+12,0] = energy[0:12,0]
	#Output[ii*12:ii*12+12,1] = temp_indoor[0:12,0]
	#cost = cost + lp2.objective.value()	
	#cost = cost + cc[0:12,0].trans()*energy[0:12,0]
	# print(ii)
	# print(cost)
	# print(Output)
#print(temp_indoor)
#	temp_indoor_initial= temp_indoor[12,0]
#	print(temp_indoor_initial)

# # solve for thermostat temperature at each timestep
# thermo = matrix(0.0, (n,1))
# i = 0
# while i<n:
# 	thermo[i,0] = (-d2*temp_indoor[i,0]-d3*temp_outdoor[i]-d4*q_solar[i]+energy[i]*1000/12)/d1
# 	i = i+1


# #print(Output)
# with pd.ExcelWriter('OutdoorTemp.xlsx', mode ='a') as writer:
# 	df = pd.DataFrame(energy).T
# 	df.to_excel(writer, sheet_name = 'Sheet3')
# 	df = pd.DataFrame(temp_indoor).T
# 	df.to_excel(writer, sheet_name = 'Sheet4')
#print("Total price =") 
#print(cost)
# print("Thermostat setup =") 
# print(thermo)
# print("Indoor Temperature =") 
# print(temp_indoor)
#print("--- %s seconds ---" % (time.process_time()-start_time))
