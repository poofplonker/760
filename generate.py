import math
import random

POINTS = 1000

f = open('SinCosRand'+str(POINTS)+'.csv', 'w')

x = 0
step = (2*math.pi)/POINTS
for i in range(0, POINTS):
    x += step
    f.write(str(x) + "," + str(random.random()*2-1) + "," + str(math.sin(x)*math.cos(2*x))+"\n")

f.close()

