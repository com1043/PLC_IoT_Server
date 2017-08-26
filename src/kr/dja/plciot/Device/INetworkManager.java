package kr.dja.plciot.Device;

import java.util.Map;

public interface INetworkManager
{
	public void sendData(String name, Map<String, String> sendData);
	public void addReceiveObserver(String name, IDevicePacketReceiveObserver observer);
}
