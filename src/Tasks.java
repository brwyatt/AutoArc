import java.util.ArrayList;
import java.util.Collections;

public class Tasks {
	private ArrayList<Task> tasks;
	
	public Tasks(){
	}
	public Task getTask(int x){
		return tasks.get(x);
	}
	public void addTask(Task t){
		int i=Collections.binarySearch(tasks, t);
		if(i<0){
			tasks.add((-i)-1,t);
		}else{
			tasks.add(i,t);
		}
		t.schedule();
		exportTasks();
	}
	public Task removeTask(int x){
		Task tmp=tasks.remove(x);
		tmp.unschedule();
		exportTasks();
		return tmp;
	}
	public boolean removeTask(Task t){
		boolean tmp=tasks.remove(t);
		t.unschedule();
		exportTasks();
		return tmp;
	}
	public void importTasks(){
		tasks=new ArrayList<Task>();
		String line="";
		int failCount=0;
		for(int x=0;failCount<=5;x++){
			line=Settings.getSetting("TASK"+x);
			if(line.length()<10){
				failCount++;
			}else{
				failCount=0;
				tasks.add(new Task(line));
			}
		}
		Collections.sort(tasks);
		exportTasks();
	}
	public void exportTasks(){
		for(int x=0;x<tasks.size();x++){
			Settings.addSetting("TASK"+x, tasks.get(x).getRawData());
		}
		for(int x=tasks.size();x<tasks.size()+5;x++){
			Settings.deleteSetting("TASK"+x);
		}
	}
	public int size(){
		return tasks.size();
	}
}
