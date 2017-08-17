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
			// ���� �ð� ����
			long taskStartTime = System.currentTimeMillis();
			
			Iterator<ITaskCallback> itr;
			synchronized(this)
			{
				itr = this.taskList.iterator();
				this.taskList.clear();// �۾� ��� ����Ʈ �����.
			}
			
			while(itr.hasNext())
			{// �۾� ��� ����Ʈ�� �ִ� �۾� ��� ����.
				itr.next().task();
			}
			
			long taskTime = System.currentTimeMillis() - taskStartTime;
			if(taskTime >= this.taskInterval)
			{
				// �۾� �ð��� �ʹ� ���� �ɷ�����.
				// taskTime�� �ִ�ġ�� �����ؼ� �����尡 0ms��ŭ ����ϵ��� �Ѵ�.
				taskTime = this.taskInterval;
			}
			
			try
			{// ������ �ð���ŭ ���.
				Thread.sleep(this.taskInterval - taskTime);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
}
