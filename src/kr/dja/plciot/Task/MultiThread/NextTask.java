package kr.dja.plciot.Task.MultiThread;

public class NextTask
{// nextTask�� ĸ��ȭ �ϱ� ���� Ŭ����.
	
	private final MultiThreadTaskOperator oper;
	
	NextTask(MultiThreadTaskOperator oper)
	{
		this.oper = oper;
	}
	
	public void nextTask()
	{
		this.oper.nextTask();
	}
	
	public void error(Exception e, String message)
	{
		this.oper.error(e, message);
	}
}
