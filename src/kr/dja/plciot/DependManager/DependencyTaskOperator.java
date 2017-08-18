package kr.dja.plciot.DependManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kr.dja.plciot.Task.ITaskCallback;

public class DependencyTaskOperator
{
	private final TaskOption option;
	private List<IDependencyTask> taskList;
	
	public DependencyTaskOperator()
	{
		
	}
	
	public DependencyTaskOperator(TaskOption option, IDependencyTask[] callbackArr)
	{
		this.option = option;
		this.taskList = Collections.synchronizedList(new ArrayList<IDependencyTask>());
		
		for(IDependencyTask task : callbackArr)
		{
			this.taskList.add(task);
		}
	}
	
	public void nextTask()
	{// ������ �������� �� �޼ҵ� ȣ��.
		try
		{
			Thread.sleep(100);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		new Thread(()->
		{
			try
			{
				Thread.sleep(500);
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.callbackArr[this.taskIndex++].executeTask(this.option, this);
		}).start();
		System.out.println("����Ϸ�");
	}
	
	public void error(Exception e, String message)
	{
		System.out.println("���� �۾��� ���� �߻�: " + this.taskIndex + "�� ����");
		System.out.println(message);
		e.printStackTrace();
	}
}
