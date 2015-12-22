package fr.rsommerard.wifidirectplayground;

public class Device {

    String address;
    String port;

    public Device() { }

    @Override
    public boolean equals(Object obj) {
        Device device = (Device) obj;

        if (device.address.equals(this.address)) {
            return true;
        }

        return false;
    }

    public Device(String address, String port) {
        this.address = address;
        this.port = port;
    }


}