package kr.dja.plciot.Device;

import java.util.HashMap;
import java.util.Map;

import kr.dja.plciot.PLC_IoT_Core;
import kr.dja.plciot.Device.Connection.IReceiveRegister;
import kr.dja.plciot.Device.Connection.PacketProcess;
import kr.dja.plciot.Device.Connection.ReceiveCycle;
import kr.dja.plciot.Device.Connection.PacketReceive.IPacketReceiveObservable;
import kr.dja.plciot.Device.Connection.PacketReceive.ReceiveController;
import kr.dja.plciot.Device.Connection.PacketReceive.ReceiveController.ReceiveControllerBuildManager;
import kr.dja.plciot.Device.Connection.PacketSend.SendController;
import kr.dja.plciot.Task.MultiThread.IMultiThreadTaskCallback;
import kr.dja.plciot.Task.MultiThread.NextTask;
import kr.dja.plciot.Task.MultiThread.TaskOption;

public class DeviceManager implements IReceiveRegister, IMultiThreadTaskCallback
{
	public static final int UDP_REC_PORT_START = 50000;
	public static final int UDP_REC_PORT_END = 50010;
	
	private ReceiveController receiveController;
	private SendController sendController;
	
	private final Map<String, Device> deviceList;
	
	public DeviceManager()
	{
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
			PLC_IoT_Core.CONS.push("��ϵ��� ���� ��ġ ����.");
		}
		
	}

	@Override
	public void executeTask(TaskOption option, NextTask nextTask)
	{// ��������ؼ� ��ġ��ϱܾ����
		if(option == TaskOption.START)
		{
			PLC_IoT_Core.CONS.push("��ġ ��� ������ �ε� ����.");
			ReceiveControllerBuildManager builder = new ReceiveControllerBuildManager(this);
			builder.createInstance(UDP_REC_PORT_START, UDP_REC_PORT_END, nextTask, (ReceiveController instance)->
			{
				this.receiveController = instance;
				PLC_IoT_Core.CONS.push("��ġ ��� ������ Ȱ��ȭ.");
			});
		}
		
	}


	
	
}
