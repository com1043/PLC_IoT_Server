package kr.dja.plciot.Task.Serial;

import java.util.concurrent.ConcurrentLinkedQueue;

import kr.dja.plciot.Task.Lockable;

public class SerializeTaskOperator extends Lockable implements Runnable
{
	private ConcurrentLinkedQueue<ISerialTaskCallback> taskQueue;
	private boolean threadSwitch;
	private int taskInterval;
	private Thread taskThread;
	
	public SerializeTaskOperator()
	{
		this(0);
	}
	
	public SerializeTaskOperator(int taskInterval)
	{
		this.taskQueue = new ConcurrentLinkedQueue<ISerialTaskCallback>();
		this.threadSwitch = true;
		this.taskInterval = taskInterval;
		
		this.taskThread = new Thread(this);
		this.taskThread.run();
	}
	
	public void addTask(ISerialTaskCallback callback)
	{// �۾� �߰�.
		this.taskQueue.add(callback);
	}
	
	public void threadOff()
	{// ������ ����.
		this.threadSwitch = false;
	}
	
	@Override
	public void run()
	{
		while(this.threadSwitch)
		{
			long taskStartTime = System.currentTimeMillis();
			System.out.println("TASKSIZE: " + this.taskQueue.size());
			
			if(!this.isLock() && !this.taskQueue.isEmpty())
			{// ����� �ʾҰ�, �׽�ũ ť ������.
				this.taskQueue.poll().executeTask();
			}
			
			long taskTime = System.currentTimeMillis() - taskStartTime;
			
			try
			{
				if(taskTime >= this.taskInterval)
				{
					// �۾� �ð��� �ʹ� ���� �ɷ�����.
					// taskTime�� �ִ�ġ�� �����ؼ� �����尡 0ms��ŭ ����ϵ��� �Ѵ�.
					taskTime = this.taskInterval;
				}
				
				Thread.sleep(this.taskInterval - taskTime);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
}
