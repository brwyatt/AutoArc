import java.io.File;
import java.util.Date;

public class SaveSettingsTask implements TimedTask{
	private long delay;
	private File prefs;
	private TaskTimer timer;
	private long lastrun=0L;
	
	public SaveSettingsTask(File prefsFile){
		this(prefsFile,(5*60*1000));//5 minute delay
	}
	public SaveSettingsTask(File prefsFile, long time){
		prefs=prefsFile;
		delay=time;
		runTask();
		schedule();
	}
	public long getNextRun() {
		return lastrun+delay;
	}
	public void runTask() {
		AutoArc.addToLog("Saving Settings...",2);
		try{
			Settings.exportSettings(prefs);
			lastrun=(new Date()).getTime();
			AutoArc.addToLog("\tSettings Saved",2);
		}catch(Exception e){
			AutoArc.addToLog("\t!Error saving Settings",0);
		}
		schedule();
	}
	public void schedule() {
		if(timer!=null){
			timer.stopTimer();
		}
		timer=new TaskTimer(this);
		timer.start();
	}
	public void unschedule() {//do nothing... this task should never be unscheduled
	}
}
