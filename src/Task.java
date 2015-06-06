import java.util.Date;

public class Task implements Comparable<Task>, TimedTask{
	public static final int FREQUENCY_DAILY=0;
	public static final int FREQUENCY_WEEKLY=1;
	public static final int FREQUENCY_MONTHLY=2;
	
	private String name="Unnamed Task";
	private String target="";
	private String backup="";
	private Date lastRun=new Date(0);
	private int runFrequency=0;//how often... see Constants
	private int runTime=0;//time of day
	private int runDay=0;//only used for weekly (Sun[0]-Sat[6]) or monthly (day)
	private boolean skipIfHidden=false;
	//private boolean deleteWhenArchived=false;
	private TaskTimer timer;
	private RunTask runningTask;
	
	public Task(){//blank task
		
	}
	public Task(String taskData){
		setAll(taskData);
	}
	
	public String getName(){
		return name;
	}
	public String getTarget(){
		return target;
	}
	public String getBackup(){
		return backup;
	}
	@SuppressWarnings("deprecation")
	public String getLastRun(){
		if(lastRun.getTime()==0L){
			return "Never";
		}else if(lastRun.getTime()==-1L){
			return "Running...";
		}else if(lastRun.getTime()==-2L){
			return "Last run interrupted or failed!";
		}
		return (lastRun.getYear()+1900)+"-"+((lastRun.getMonth()+1)<10?"0"+(lastRun.getMonth()+1):(lastRun.getMonth()+1))+"-"+(lastRun.getDate()<10?"0"+lastRun.getDate():lastRun.getDate())+" "+(lastRun.getHours()<10?"0"+lastRun.getHours():lastRun.getHours())+":"+(lastRun.getMinutes()<10?"0"+lastRun.getMinutes():lastRun.getMinutes())+":"+(lastRun.getSeconds()<10?"0"+lastRun.getSeconds():lastRun.getSeconds());
	}
	public String getRunRule(){
		String run="";
		int runHour=runTime/100;
		int runMinute=runTime-(runHour*100);
		if(runFrequency==0){
			run+="Daily";
		}else if(runFrequency==1){
			run+="Weekly on ";
			switch(runDay){
			case 0:run+="Sunday";break;
			case 1:run+="Monday";break;
			case 2:run+="Tuesday";break;
			case 3:run+="Wednesday";break;
			case 4:run+="Thursday";break;
			case 5:run+="Friday";break;
			case 6:run+="Saturday";break;
			}
		}else if(runFrequency==2){
			run+="Monthly on the "+runDay;
			switch(runDay){
			case 31:
			case 21:
			case 1:run+="st";break;
			case 22:
			case 2:run+="nd";break;
			case 23:
			case 3:run+="rd";break;
			default:run+="th";
			}
		}
		
		return run+" at "+(runHour<10?"0"+runHour:runHour)+":"+(runMinute<10?"0"+runMinute:runMinute);
	}
	@SuppressWarnings("deprecation")
	public String getParsedNextRun(){
		Date next=new Date(getNextRun());
		return (next.getYear()+1900)+"-"+((next.getMonth()+1)<10?"0"+(next.getMonth()+1):(next.getMonth()+1))+"-"+(next.getDate()<10?"0"+next.getDate():next.getDate())+" "+(next.getHours()<10?"0"+next.getHours():next.getHours())+":"+(next.getMinutes()<10?"0"+next.getMinutes():next.getMinutes())+":"+(next.getSeconds()<10?"0"+next.getSeconds():next.getSeconds());
	}
	@SuppressWarnings("deprecation")
	public long getNextRun(){//returns time till next run
		int runHour=runTime/100;
		int runMinute=runTime-(runHour*100);
		long oneDay=24L*60L*60L*1000L;
		long msecRun=(((long)runHour*60L*60L)+((long)runMinute*60L))*1000L;
		Date now=new Date();
		long msecNow=(((long)now.getHours()*60L*60L)+((long)now.getMinutes()*60L)+(long)now.getSeconds())*1000L;
		if(runFrequency==0){//daily
			if(now.getHours()>runHour || (now.getHours()==runHour && now.getMinutes()>=runMinute)){//time is tomorrow
				return (new Date(now.getTime()+oneDay+(msecRun-msecNow)).getTime());
			}else{//time is today
				return (new Date(now.getTime()+(msecRun-msecNow)).getTime());
			}
		}else if(runFrequency==1){//weekly
			if(now.getDay()==runDay){//same day of week as today
				if(now.getHours()>runHour || (now.getHours()==runHour && now.getMinutes()>=runMinute)){//time is next week
					return (new Date(now.getTime()+(oneDay*7)+(msecRun-msecNow)).getTime());
				}else{//time is today
					return (new Date(now.getTime()+(msecRun-msecNow)).getTime());
				}
			}else{//other day
				int dist=runDay-now.getDay();
				if(dist<0){
					dist=7+dist;
				}
				if(now.getHours()>runHour || (now.getHours()==runHour && now.getMinutes()>=runMinute)){//time is one day further
					return (new Date(now.getTime()+(oneDay*(dist+1))+(msecRun-msecNow)).getTime());
				}else{//time is on the day
					return (new Date(now.getTime()+(oneDay*dist)+(msecRun-msecNow)).getTime());
				}
				
			}
		}else if(runFrequency==2){//monthly
			if(now.getDate()==runDay){//same day of month as today
				if(now.getHours()>runHour || (now.getHours()==runHour && now.getMinutes()>=runMinute)){//time is next month
					long inc=oneDay*31;
					int nextMonth=(now.getMonth()<11?now.getMonth()+1:0);
					while((new Date(now.getTime()+inc+(msecRun-msecNow)).getMonth())>nextMonth){//overshot the month
						inc-=oneDay;
					}
					while((new Date(now.getTime()+inc+(msecRun-msecNow)).getDate())>runDay){//overshot the day
						inc-=oneDay;
					}
					return (new Date(now.getTime()+inc+(msecRun-msecNow)).getTime());
				}else{//time is today
					return (new Date(now.getTime()+(msecRun-msecNow)).getTime());
				}
			}else{//other day
				if(runDay>now.getDate()){//comes later in this month
					int dist=runDay-now.getDate();
					return (new Date(now.getTime()+(oneDay*dist)+(msecRun-msecNow)).getTime());
				}else{//comes earlier in the month
					long inc=oneDay*31;
					int nextMonth=(now.getMonth()<11?now.getMonth()+1:0);
					while((new Date(now.getTime()+inc+(msecRun-msecNow)).getMonth())>nextMonth){//overshot the month
						inc-=oneDay;
					}
					while((new Date(now.getTime()+inc+(msecRun-msecNow)).getDate())>runDay){//overshot the day
						inc-=oneDay;
					}
					return (new Date(now.getTime()+inc+(msecRun-msecNow)).getTime());
				}
			}
		}
		
		return 0L;
	}
	public boolean skipIfHidden(){
		return skipIfHidden;
	}
	//public boolean getDeleteWhenArchived(){
	//	return deleteWhenArchived;
	//}
	public void setAll(String taskData){
		String parts[]=taskData.split(";");
		name=parts[0];
		target=parts[1];
		backup=parts[2];
		lastRun=new Date(Long.parseLong(parts[3]));
		runFrequency=Integer.parseInt(parts[4]);
		runTime=Integer.parseInt(parts[5]);
		runDay=Integer.parseInt(parts[6]);
		int tmp=Integer.parseInt(parts[7]);
		if(tmp==1){
			skipIfHidden=true;
		}else{
			skipIfHidden=false;
		}
		tmp=Integer.parseInt(parts[8]);
		//if(tmp==1){
		//	deleteWhenArchived=true;
		//}
		unschedule();
		schedule();
	}
	public String getRawData(){
		int tmp=0;
		if(skipIfHidden){
			tmp=1;
		}
		int tmp2=0;
		//if(deleteWhenArchived){
		//	tmp2=1;
		//}
		return name+";"+target+";"+backup+";"+lastRun.getTime()+";"+runFrequency+";"+runTime+";"+runDay+";"+tmp+";"+tmp2+";";
	}
	public void setLastRun(Long time){
		lastRun=new Date(time);
		AutoArc.tasks.exportTasks();
	}
	public void schedule(){
		unschedule();
		timer=new TaskTimer(this);
		timer.start();
	}
	public void unschedule(){
		if(timer!=null){
			timer.stopTimer();
		}
	}
	public void runTask(){
		runningTask=new RunTask(this);
		(new Thread(runningTask)).start();
		setLastRun(-1L);
		AutoArc.mainWindow.updateTaskInfo();
		schedule();
	}
	public void cancelRunTask(){
		runningTask.cancelRun();
	}
	public String toString(){
		return name;
	}
	public int compareTo(Task t) {
		return name.compareTo(t.name);
	}
}
