# SensorDataCompressionDemo

The demo is shown in 2017 14th Annual IEEE International Conference on Sensing, Communication, and Networking (SECON).
The link of the demo paper is here: [A WSN Testbed for Z-Order Encoding Based Multi-Modal Sensor Data Compression](https://ieeexplore.ieee.org/abstract/document/7964952)

##Demo procedure
The system procedure of the demo is look like this:
![procedure](https://github.com/cxfcdcpu/SensorDataCompressionDemo/blob/master/sys.PNG)
![layter](https://github.com/cxfcdcpu/SensorDataCompressionDemo/blob/master/layer.PNG)

##Topology of the network
The network is a tree style network with 1 sink nodes, many leaf nodes and some intermediate nodes. 
![topoloty](https://github.com/cxfcdcpu/SensorDataCompressionDemo/blob/master/toplogy.PNG)

![topo](https://github.com/cxfcdcpu/SensorDataCompressionDemo/blob/master/top.png)


The data sensing and compressing happens in each leaf nodes.
The data concatenating is in the intermediate nodes
The data decoding and visualization is in the PC.

##How to use
First, the program in folder controlGroup and expGroup are the sensor programs that need to flash to the control group and experimental group TinyOs sensors. I have write two version. One is for Iris sensors and one is for TeloSB sensors. I have implement five compressing algorithm, LEC, TinyPack and our Z-compression ....

Second, the UI contains the java visualization programs that decode and visualize the sening data. Some screen shoot look like below:
![cp](https://github.com/cxfcdcpu/SensorDataCompressionDemo/blob/master/cp.png)
![compare](https://github.com/cxfcdcpu/SensorDataCompressionDemo/blob/master/compare.PNG)

Note:
To run the UI, you have to have tinyos installed and serial forworder running. 
