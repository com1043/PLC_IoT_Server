package kr.dja.plciot.Task.Serial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.SynchronousQueue;

public class SerializeTask extends Thread
{
	private ConcurrentLinkedQueue<ISerialTaskCallback> taskQueue;
	private boolean threadSwitch;
	private int taskInterval;
	
	public SerializeTask()
	{
		this(0);
	}
	
	public SerializeTask(int taskInterval)
	{
		this.taskQueue = new ConcurrentLinkedQueue<ISerialTaskCallback>();
		this.threadSwitch = true;
		this.taskInterval = taskInterval;
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
		boolean sleepFlag;
		while(this.threadSwitch)
		{
			sleepFlag = true;
			System.out.println("TASKSIZE: " + this.taskQueue.size());
			
			while(!this.taskQueue.isEmpty())
			{// �۾� ��� ����Ʈ�� �ִ� �۾� ��� ����.
				sleepFlag = false;
				long taskStartTime = System.currentTimeMillis();
				
				this.taskQueue.poll().task();
				
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
				System.out.println(taskTime + " sleep: " + (this.taskInterval - taskTime));
			}
			
			if(sleepFlag)
			{
				try
				{// ������ �ð���ŭ ���.
					Thread.sleep(this.taskInterval);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
