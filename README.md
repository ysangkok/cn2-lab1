Minimum API level 10.

Push "Start listener" on all phones to make them receive messages.
Push "Send ping" to send a message which will cause receiving phones to take pictures and light sensor readings.

We have tested with four phones: An HTC Desire, a Nexus 7, a Nexus 5 and a Samsung S4 Mini.

We couldn't use the Nexus 7 to broadcast because it doesn't have a flashlight.

Check out the folders `scenario1`, `scenario2`, `scenario3`, `scenario4` for more photos and raw light sensor readings. The captured images are available upon request.

Build instructions
---

This is an Android Studio project. Import it into Android Studio, and it will build with Gradle. It can also build with Gradle alone, but we didn't test that.

Scenario 1
---

In this scenario, we used a laptop to broadcast the signal.

Phones are right next to one another in a bright room. This is just to test the network.

Here are the measurements for the Nexus 7. The Nexus 5, the Samsung and the Desire have constant readings, so we assume that they malfunctioned.

http://youtu.be/hlDZt67k65Y

```
google-Nexus 7-Nexus 7	20.0	20150125_183030
google-Nexus 7-Nexus 7	24.0	20150125_183031
google-Nexus 7-Nexus 7	18.0	20150125_183032
google-Nexus 7-Nexus 7	12.0	20150125_183033
google-Nexus 7-Nexus 7	24.0	20150125_183034
google-Nexus 7-Nexus 7	14.0	20150125_183035
google-Nexus 7-Nexus 7	20.0	20150125_183036
google-Nexus 7-Nexus 7	24.0	20150125_183037
google-Nexus 7-Nexus 7	22.0	20150125_183038
google-Nexus 7-Nexus 7	22.0	20150125_183053
google-Nexus 7-Nexus 7	26.0	20150125_183054
google-Nexus 7-Nexus 7	18.0	20150125_183055
google-Nexus 7-Nexus 7	26.0	20150125_183056
google-Nexus 7-Nexus 7	20.0	20150125_183057
google-Nexus 7-Nexus 7	22.0	20150125_183059
google-Nexus 7-Nexus 7	20.0	20150125_183100
google-Nexus 7-Nexus 7	6.0	20150125_183101
google-Nexus 7-Nexus 7	20.0	20150125_183102
google-Nexus 7-Nexus 7	20.0	20150125_183102
```

Scenario 2
---

Broadcaster: S4 Mini.

Phones are right next to one another on a radiator. They are separated by about two feet.

![](http://i.imgur.com/c9wQt6O.jpg)

http://youtu.be/tqF30U-XNh8

Scenario 3
---

Broadcaster: S4 Mini.

The phones are on the floor, separated by about 4 feets distance.

![](http://i.imgur.com/KYNx284.jpg)

Measurements from the Nexus 7:

```
google-Nexus 7-Nexus 7	40.0	20150125_183525
google-Nexus 7-Nexus 7	46.0	20150125_183526
google-Nexus 7-Nexus 7	36.0	20150125_183527
google-Nexus 7-Nexus 7	38.0	20150125_183528
google-Nexus 7-Nexus 7	2.0   20150125_183552
google-Nexus 7-Nexus 7	24.0	20150125_183600
google-Nexus 7-Nexus 7	28.0	20150125_183600
google-Nexus 7-Nexus 7	28.0	20150125_183600
google-Nexus 7-Nexus 7	28.0	20150125_183600
```

You notice how the light sensor levels from after the fourth measurement. This is because the flashlight turns off.

http://youtu.be/U11iXTH8ruk

Scenario 4
---

Broadcaster: S4 Mini.

The phones are on a table, separated by two feets distance.

Images: http://imgur.com/BgHto70,p0n6bR0,fmnzww3

Measurements from the Nexus 7:

```
google-Nexus 7-Nexus 7	2.0	20150125_185409
google-Nexus 7-Nexus 7	2.0	20150125_185434
google-Nexus 7-Nexus 7	2.0	20150125_185436
google-Nexus 7-Nexus 7	4.0	20150125_185437
```

From the Nexus 5:
```
google-Nexus 5-Nexus 5	1.0	20150125_185416
google-Nexus 5-Nexus 5	1.0	20150125_185416
google-Nexus 5-Nexus 5	1.0	20150125_185416
google-Nexus 5-Nexus 5	1.0	20150125_185444
google-Nexus 5-Nexus 5	1.0	20150125_185444
google-Nexus 5-Nexus 5	1.0	20150125_185444
google-Nexus 5-Nexus 5	1.0	20150125_185444
```
