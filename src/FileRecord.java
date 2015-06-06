import java.util.Date;

public class FileRecord {
	private Date time;
	private String path;
	private int action;
	private String hash;
	
	public FileRecord(String recordData){
		String[] parts=recordData.split(":");
		time=new Date(Long.parseLong(parts[0]));
		path=parts[1];
		action=Integer.parseInt(parts[2]);
		hash=parts[3];
	}
	public FileRecord(Date t, String p, int a, String h){
		time=t;
		path=p;
		action=a;
		hash=h;
	}
	public Date getTime(){
		return time;
	}
	public String getPath(){
		return path;
	}
	public int getAction(){
		return action;
	}
	public String getHash(){
		return hash;
	}
	@SuppressWarnings("deprecation")
	public String toString(){
		String year=""+(time.getYear()+1900);
		if(year.length()<2) year="0"+year;
		String month=""+(time.getMonth()+1);
		if(month.length()<2) month="0"+month;
		String day=""+time.getDate();
		if(day.length()<2) day="0"+day;
		String hours=""+time.getHours();
		if(hours.length()<2) hours="0"+hours;
		String min=""+time.getMinutes();
		if(min.length()<2) min="0"+min;
		String sec=""+time.getSeconds();
		if(sec.length()<2) sec="0"+sec;
		
		return path+" - "+year+"/"+month+"/"+day+" "+hours+":"+min+":"+sec;
	}
}
