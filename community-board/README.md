## setup ##
setup Edison board on MacOSX : https://software.intel.com/en-us/get-started-edison-osx
IDE selected - [Intel XDK IoT edition](https://software.intel.com/en-us/getting-started-with-the-intel-xdk-iot-edition#launch)

language : nodeJS 


## howTo connect ##

```
# look for device
ls /dev/cu.usbs*

# connect to device
screen /dev/cu.usbserial-A903C3NV 115200 -L

# once connect, select password

# then configure wifi 
root@edison:~# configure_edison --wifi 
```

## sensor: enable and use ##
https://software.intel.com/en-us/adding-sensors-and-actuators-xdk

use code samples for each sensor

* https://software.intel.com/en-us/iot/hardware/sensors/grove-temperature-sensor
* https://software.intel.com/en-us/iot/hardware/sensors/grove-led-bar

## see also ##

[intel readme](README_intel.md)

https://github.com/muzzley/muzzley-intel-iot-led-strip
