package kr.dja.plciot.Task.MultiThread;

public class NextTask
{// nextTask�� ĸ��ȭ �ϱ� ���� Ŭ����.
	
	private final MultiThreadTaskOperator oper;
	private boolean taskFlag;
	
	NextTask(MultiThreadTaskOperator oper)
	{
		this.oper = oper;
		this.taskFlag = false;
	}
	
	public void nextTask()
	{
		if(!this.taskFlag)
		{
			this.oper.nextTask();
			this.taskFlag = true;
		}
		else
		{
			new Exception("Task Already Executed").printStackTrace();
		}
		
	}
	
	public void error(Exception e, String message)
	{
		this.oper.error(e, message);
	}
}
