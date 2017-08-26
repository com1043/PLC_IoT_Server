package kr.dja.plciot.Device;

import java.util.HashMap;
import java.util.Map;

import kr.dja.plciot.Device.Connection.IReceiveRegister;
import kr.dja.plciot.Device.Connection.PacketProcess;
import kr.dja.plciot.Device.Connection.ReceiveCycle;
import kr.dja.plciot.Device.Connection.PacketReceive.IPacketReceiveObservable;
import kr.dja.plciot.Device.Connection.PacketReceive.ReceiveController;
import kr.dja.plciot.Device.Connection.PacketSend.SendController;
import kr.dja.plciot.Task.MultiThread.IMultiThreadTaskCallback;
import kr.dja.plciot.Task.MultiThread.NextTask;
import kr.dja.plciot.Task.MultiThread.TaskOption;

public class DeviceManager implements IReceiveRegister, IMultiThreadTaskCallback
{
	private final ReceiveController receiveController;
	private final SendController sendController;
	
	private final Map<String, Device> deviceList;
	
	public DeviceManager(ReceiveController receiveController, SendController sendController)
	{
		this.receiveController = receiveController;
		this.sendController = sendController;
		this.deviceList = new HashMap<String, Device>();
	}
	
	@Override
	public void registerReceive(IPacketReceiveObservable observable, byte[] data)
	{
		String macAddr = PacketProcess.GetpacketMacAddr(data);
		Device receiveTarget = this.deviceList.getOrDefault(macAddr, null);
		if(receiveTarget != null)
		{// �Ϲ����� ��� ����Ŭ�� �����մϴ�.
			ReceiveCycle receiveCycle = new ReceiveCycle(observable, data, receiveTarget);
		}
		else
		{// ��ġ ��� ����Ŭ�� �����մϴ�.
			
		}
		
	}

	@Override
	public void executeTask(TaskOption option, NextTask nextTask)
	{// ��������ؼ� ��ġ��ϱܾ����
		if(option == TaskOption.START)
		{
			nextTask.nextTask();
		}
		
	}


	
	
}
