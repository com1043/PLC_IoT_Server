package kr.dja.plciot.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

class TaskThread extends Thread
{
	private List<ITaskCallback> taskList;
	private boolean threadSwitch;
	private int taskInterval;
	
	public TaskThread()
	{
		this(0);
	}
	
	public TaskThread(int taskInterval)
	{
		this.taskList = Collections.synchronizedList(new ArrayList<ITaskCallback>());
		this.threadSwitch = true;
		this.taskInterval = taskInterval;
	}
	
	public void addTask(ITaskCallback callback)
	{// �۾� �߰�.
		this.taskList.add(callback);
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
			Object[] taskArr;
			
			try
			{// ������ �ð���ŭ ���.
				Thread.sleep(this.taskInterval);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			
			if(this.taskList.size() == 0) continue;
			
			System.out.println("TASKSIZE: " + this.taskList.size());
			synchronized(this)
			{
				taskArr = this.taskList.toArray();
				this.taskList.clear();
			}
			
			for(Object callback : taskArr)
			{// �۾� ��� ����Ʈ�� �ִ� �۾� ��� ����.
				long taskStartTime = System.currentTimeMillis();
				
				((ITaskCallback)callback).task();
				
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
		}
	}
}
