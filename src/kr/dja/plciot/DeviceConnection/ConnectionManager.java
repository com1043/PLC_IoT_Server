package kr.dja.plciot.DeviceConnection;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import kr.dja.plciot.PLC_IoT_Core;
import kr.dja.plciot.DeviceConnection.PacketReceive.ReceiveController;
import kr.dja.plciot.DeviceConnection.PacketReceive.ReceiveController.ReceiveControllerBuildManager;
import kr.dja.plciot.DeviceConnection.PacketSend.SendController;
import kr.dja.plciot.DeviceConnection.PacketSend.UDPRawSocketSender;
import kr.dja.plciot.Task.MultiThread.IMultiThreadTaskCallback;
import kr.dja.plciot.Task.MultiThread.NextTask;
import kr.dja.plciot.Task.MultiThread.TaskOption;

public class ConnectionManager
{
	private static final int UDP_RCV_PORT_START = 50000;
	private static final int UDP_RCV_PORT_END = 50010;
	private static final int UDP_SND_PORT_START = 50011;
	private static final int UDP_SND_PORT_END = 50020;
	
	// ����̽� ��Ÿ��.
	private ReceiveController receiveController;
	private SendController sendController;
	
	public ConnectionManager()
	{
		
	}
	
	public SendController getSendController()
	{
		return this.sendController;
	}
	
	public ReceiveController getReceiveController()
	{
		return this.receiveController;
	}
	
	public static class ConnectionManagerBuilder implements IMultiThreadTaskCallback
	{
		private final ConnectionManager instance;
		private final List<DatagramSocket> createdSocketList;
		private ReceiveControllerBuildManager rcvBuildManager;
		private IReceiveRegister receiveRegister;
		
		public ConnectionManagerBuilder(ConnectionManager instance)
		{
			this.instance = instance;
			this.createdSocketList = new ArrayList<DatagramSocket>();
		}

		@Override
		public void executeTask(TaskOption option, NextTask nextTask)
		{// ��������ؼ� ��ġ��ϱܾ����
			if(option == TaskOption.START)
			{
				this.createInstance(nextTask);
			}
			else if(option == TaskOption.SHUTDOWN)
			{
				this.disposInstance(nextTask);
			}
		}
		
		public void setReceiveRegister(IReceiveRegister receiveRegister)
		{
			this.receiveRegister = receiveRegister;
		}
		
		private void createInstance(NextTask nextTask)
		{
			PLC_IoT_Core.CONS.push("��ġ ��� ������ �ε� ����.");
			
			ReceiveController receiveController = new ReceiveController(this.receiveRegister);
			this.rcvBuildManager = new ReceiveControllerBuildManager(receiveController);
			SendController sendController = new SendController(this.createSocketList(UDP_SND_PORT_START, UDP_SND_PORT_END));
			this.rcvBuildManager.setDatagramSocket(this.createSocketList(UDP_RCV_PORT_START, UDP_RCV_PORT_END));
			
			this.instance.receiveController = receiveController;
			this.instance.sendController = sendController;
			
			nextTask.insertTask((TaskOption option, NextTask startNext)->
			{
				PLC_IoT_Core.CONS.push("��ġ ��� ������ Ȱ��ȭ.");
				startNext.nextTask();
				
			});
			
			nextTask.insertTask(this.rcvBuildManager);
			
			nextTask.nextTask();
		}
		
		private void disposInstance(NextTask nextTask)
		{
			nextTask.insertTask(this.rcvBuildManager);
			
			for(DatagramSocket disposSocket : this.createdSocketList)
			{
				PLC_IoT_Core.CONS.push("���� " + disposSocket.getLocalPort() + " �� ��Ʈ ��Ȱ��ȭ.");
				disposSocket.close();
			}
			
			nextTask.nextTask();
		}
		
		private List<DatagramSocket> createSocketList(int portStart, int portEnd)
		{
			List<DatagramSocket> socketList = new ArrayList<DatagramSocket>();
			for(int i = portStart; i <= portEnd; ++i)
			{
				try
				{
					DatagramSocket createSocket = new DatagramSocket(i);
					socketList.add(createSocket);
					this.createdSocketList.add(createSocket);
				}
				catch (SocketException e)
				{
					e.printStackTrace();
				}
			}
			return socketList;
		}
		
	}
}
