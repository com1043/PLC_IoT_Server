package kr.dja.plciot.Device.AbsDevice;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kr.dja.plciot.PLC_IoT_Core;
import kr.dja.plciot.Device.DeviceManager;
import kr.dja.plciot.Device.IDeviceEventObserver;
import kr.dja.plciot.LowLevelConnection.ISendCycleStarter;
import kr.dja.plciot.LowLevelConnection.Cycle.IPacketCycleUser;

public abstract class AbsDevice implements IPacketCycleUser
{
	public static final String DEVICE_POWER_CHANGE = "powerchange";
	public static final String ON = "on";
	public static final String OFF = "off";
	public final String macAddr;
	protected final ISendCycleStarter sendManager;
	private boolean active;
	protected InetAddress addr;
	protected final IDeviceEventObserver eventObserver;
	
	public AbsDevice(String macAddr, ISendCycleStarter sendManager, IDeviceEventObserver eventObserver)
	{
		this.macAddr = macAddr;
		this.sendManager = sendManager;
		this.active = false;
		this.eventObserver = eventObserver;
	}
	
	public boolean isActivation()
	{
		return this.active;
	}

	@Override
	public abstract void packetSendCallback(boolean success, String name, String data);

	@Override
	public void packetReceiveCallback(InetAddress addr, String macAddr, String name, String data)
	{
		PLC_IoT_Core.CONS.push(this.macAddr + " ��� ���� ���� " + data);
		this.addr = addr;
		switch(name)
		{
		case DeviceManager.DEVICE_REGISTER:
			this.deviceReConnection(addr);
			break;
		case DEVICE_POWER_CHANGE:
			if(data == ON) this.setPower(true);
			else if(data == OFF) this.setPower(false);
			break;
		}
		
		return;
	}
	
	private void deviceReConnection(InetAddress addr)
	{
		PLC_IoT_Core.CONS.push(this.macAddr + ": ��� ������ Ȯ��.");
		this.sendManager.startSendCycle(addr, DeviceManager.DEFAULT_DEVICE_PORT, macAddr, DeviceManager.DEVICE_REGISTER_OK, "", this);
	}
	
	public void setPower(boolean onoff)
	{
		String power;
		if(onoff) power = ON;
		else power = OFF;
		PLC_IoT_Core.CONS.push(this.macAddr + ": ��� ��������("+power+")");
		this.eventObserver.deviceEvent(this, DEVICE_POWER_CHANGE, power);
		this.sendManager.startSendCycle(this.addr, DeviceManager.DEFAULT_DEVICE_PORT, macAddr, DEVICE_POWER_CHANGE, power, this);
	}
}
