package kr.dja.plciot.DependManager;

public class DependencyTaskOperator
{
	private int taskIndex;
	
	private final TaskOption option;
	private final IDependencyTask[] callbackArr;
	
	public DependencyTaskOperator(TaskOption option, IDependencyTask[] callbackArr)
	{
		this.option = option;
		this.callbackArr = callbackArr;
		
		this.taskIndex = 0;
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
		System.out.println(taskIndex+"����:"+this.callbackArr[this.taskIndex].getClass());
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
