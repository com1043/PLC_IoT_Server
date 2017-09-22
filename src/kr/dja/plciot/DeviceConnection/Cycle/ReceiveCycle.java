package kr.dja.plciot.DeviceConnection.Cycle;

import java.util.Map;

import kr.dja.plciot.DeviceConnection.PacketProcess;
import kr.dja.plciot.DeviceConnection.PacketReceive.IPacketReceiveObservable;
import kr.dja.plciot.DeviceConnection.PacketReceive.IPacketReceiveObserver;
import kr.dja.plciot.DeviceConnection.PacketSend.IPacketSender;

public class ReceiveCycle implements Runnable, IPacketReceiveObserver
{
	private final IPacketSender sender;
	private final IPacketReceiveObservable receiver;
	private final IPacketCycleController deviceCallback;
	
	private final String uuid;
	private byte[] receivePacketHeader;
	private byte[] receivePacketData;
	private int resendCount;
	private boolean taskState;
	
	private Thread resiveTaskThread;
	
	public ReceiveCycle(IPacketSender sender, IPacketReceiveObservable receiver, byte[] data, IPacketCycleController deviceCallback)
	{
		this.resendCount = 0;
		this.taskState = false;
		
		this.sender = sender;
		this.receiver = receiver;
		this.deviceCallback = deviceCallback;
		
		this.ReceivePacket = data;
		this.uuid = PacketProcess.GetPacketFULLUID(data);
		
		// ���� �޴����� �ش� ����Ŭ�� ���ε� �մϴ�.
		this.receiver.addObserver(this.uuid, this);
		
		// �߽��ڷκ��� ��Ŷ �� ��ȯ�Ǿ� �ö����� ������ϴ�.
		this.resiveWaitTask();
		
		// �߽��ڿ��� ��Ŷ�� ��ȯ�մϴ�.
		this.reSendPhase();
	}
	
	@Override
	public void packetResive(byte[] resivePacket)
	{// ��Ŷ�� ���� �����϶�.
		this.ReceivePacket = resivePacket;
		this.resiveTaskThread.interrupt();
	}
	
	@Override
	public void run()
	{
		try
		{
			// ������ ���ͷ�Ʈ�� �ɸ� ������ ����մϴ�.
			// ���� ���ͷ�Ʈ�� �ɸ��� ������ �ð� �ʰ�.
			Thread.sleep(PacketProcess.TIMEOUT);
		}
		catch (InterruptedException e)
		{
			byte phase = PacketProcess.GetPacketPhase(this.ReceivePacket);
			
			if(phase == CycleProcess.PHASE_EXECUTE)
			{// ������ ���� ���� ����.
				this.taskState = true;
				this.endProcess();// ����Ŭ�� ���������� �Ϸ�Ǿ����ϴ�.
				return;
			}
			else if(phase == CycleProcess.PHASE_START)
			{// ������ �ִ� ����.
				if(this.resendCount < CycleProcess.MAX_RESEND)
				{// ��ġ���� ������ �ִٴ� ��ȣ�� ���� ���� - ������ �ʿ�.
					++this.resendCount;
					
					// �߽��ڷκ��� ��Ŷ �� ��ȯ�Ǿ� �ö����� ������ϴ�.
					this.resiveWaitTask();
					// �߽��ڿ��� ��Ŷ�� ��ȯ �մϴ�.
					this.reSendPhase();
					return;
				}
				else
				{// �߽��ڰ� ���� �������� �ʴ� ����. (������ ���� Ƚ�� �ʰ�)
					new Exception("Device is not responding").printStackTrace();
					this.endProcess();// task ERROR.
					return;
				}
			}
		}
		
		this.taskState = false;
		this.endProcess();// ����Ŭ �ð� ���� ����.
	}
	
	public boolean getResiveState()
	{
		return this.taskState;
	}
	
	private void resiveWaitTask()
	{
		this.resiveTaskThread = new Thread(this);
		this.resiveTaskThread.start();
	}
	
	private void reSendPhase()
	{// ������.
		this.sender.sendData(sendAddress, data);
	}
	
	private void endProcess()
	{
		String receiveName = PacketProcess.GetPacketName(this.ReceivePacket);
		
		// ��ġ���� ������ ������ �˸��ϴ�.
		this.deviceCallback.ReceiveData(receiveName, receiveData, this.taskState);
		
		// ���� �޴��� ���ε� ����.
		this.receiver.deleteObserver(this.uuid);
	}
}
