package util;

import util.socket.ThreadUtilServer;


public class TaskMake{
	TaskInterface taskResult = null;
	int threadType = ThreadUtil.DefaultThread;
	long time = 1000;
	int maxRetryCount = 5;	//最大重试次数
	String doName = "操作";
	Task task = null;
	public TaskMake(TaskInterface taskResult){
		this.taskResult = taskResult;
		this.task = new Task(this.taskResult, this.doName, this.time, this.maxRetryCount);
	}
	public TaskMake(TaskInterface taskResult, String doName){
		this.taskResult = taskResult;
		this.doName = doName;
		this.task = new Task(this.taskResult, this.doName, this.time, this.maxRetryCount);
	}
	public TaskMake(TaskInterface taskResult, String doName, long time, int maxRetryCount){
		this.taskResult = taskResult;
		this.doName = doName;
		this.time = time;
		this.maxRetryCount = maxRetryCount;
		this.task = new Task(this.taskResult, this.doName, this.time, this.maxRetryCount);
	}
	public void startTask(){
		ThreadUtilServer.execute(threadType,  task);
	}
	class Task implements Runnable{
		int count = 0;			//操作失败次数
		int maxRetryCount = 5;	//最大重试次数
		long time = 1000;
		String doName;
		TaskInterface result = null;
		public Task(TaskInterface taskResult, String doName, long time, int maxRetryCount){
			this.result = taskResult;
			this.doName = doName;
			this.time = time;
			this.maxRetryCount = maxRetryCount;
		} 
		@Override
		public void run() {
			try{
				if(count > 0 && count <= maxRetryCount){
					Tools.out("尝试重新" + doName, count);
					result.doTask();
					count = 0;
					result.onTrue();
				}else if(count > maxRetryCount){
					result.onFalse();
				}else{
					result.doTask();
					count = 0;
					result.onTrue();
				}
			}catch(Exception e){
//				e.printStackTrace();
				Tools.out(TaskMake.this.doName + "异常," + Tools.calcTime(time) + "后重新读取", count++, e.toString());
				ThreadUtil.sleep(time);
				TaskMake.this.startTask();
			}
		}
	}
}
