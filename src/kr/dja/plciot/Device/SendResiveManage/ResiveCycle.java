package kr.dja.plciot.Device.SendResiveManage;

import java.util.Observable;
import java.util.Observer;

import kr.dja.plciot.Device.PacketProcess;

public class ResiveCycle extends Thread implements IPacketObserver
{
	private final IPacketObservable observable;
	
	private byte[] resivePacket;
	private int resendCount;
	private boolean taskState;
	
	public ResiveCycle(IPacketObservable observable, byte[] data)
	{
		this.resendCount = 0;
		this.taskState = false;
		this.observable = observable;
		this.observable.addObserver(PacketProcess.GetpacketMacAddr(data), this);
		
		this.reSendPhase();
	}
	
	@Override
	public void packetResive(byte[] resivePacket)
	{// �ް� ������������ �ٽ� �»���
		this.resivePacket = resivePacket;
		this.interrupt();
	}
	
	@Override
	public void run()
	{
		try
		{
			Thread.sleep(PacketProcess.TIMEOUT);
		}
		catch (InterruptedException e)
		{
			//OK
			if(PacketProcess.GetPacketPhase(this.resivePacket) == PacketProcess.PHASE_EXECUTE)
			{// ������ ���� ���� ����.
				this.taskState = true;
				this.endProcess();// task OK.
				return;
			}
			else if(PacketProcess.GetPacketPhase(this.resivePacket) == PacketProcess.PHASE_SEND)
			{// ������ �ִ� ����.
				if(this.resendCount < PacketProcess.MAX_RESEND)
				{
					++this.resendCount;
					this.reSendPhase();
					this.start();// ��õ�.
					return;
				}
			}
		}
		
		this.taskState = false;
		this.endProcess();// timeout ERROR.
	}
	
	public boolean getResiveState()
	{
		return this.taskState;
	}
	
	private void reSendPhase()
	{// ������.
		
	}
	
	private void endProcess()
	{
		this.observable.deleteObserver(this);
	}
}
