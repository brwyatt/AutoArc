import java.util.Date;


public class TaskTimer extends Thread{
	private long timeTillRun=0L;
	private long runTime=0L;
	private TimedTask t;
	private boolean stopped=false;
	
	public TaskTimer(TimedTask task){
		t=task;
		runTime=task.getNextRun();
		timeTillRun=runTime-(new Date()).getTime();
	}
	public void run() {
		while(timeTillRun>(1*60*60*1000L) && !stopped){//while timeTillRun>1hour and the thread has not been stopped
			try{
				Thread.sleep(1*60*60*1000L);//sleep for 1 hour
			}catch (Exception e){//ignore exception
			}
			timeTillRun=runTime-(new Date()).getTime();
		}
		if(stopped){
			return;
		}
		try{
			Thread.sleep(timeTillRun);//sleep for remaining time
		}catch (Exception e){//since the exception happened "near" the run time, reschedule and exit
			t.schedule();
			return;
		}
		if(!stopped){
			t.runTask();
		}
	}
	public void stopTimer(){
		stopped=true;
	}
}
