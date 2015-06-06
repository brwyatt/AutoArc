import java.util.Date;

public class BackupRecord{
	Date t;
	public BackupRecord(Date time){
		t=time;
	}
	public long getTime(){
		return t.getTime();
	}
	@SuppressWarnings("deprecation")
	public String toString(){
		String year=""+(t.getYear()+1900);
		if(year.length()<2) year="0"+year;
		String month=""+(t.getMonth()+1);
		if(month.length()<2) month="0"+month;
		String day=""+t.getDate();
		if(day.length()<2) day="0"+day;
		String hours=""+t.getHours();
		if(hours.length()<2) hours="0"+hours;
		String min=""+t.getMinutes();
		if(min.length()<2) min="0"+min;
		String sec=""+t.getSeconds();
		if(sec.length()<2) sec="0"+sec;
		
		return year+"/"+month+"/"+day+" "+hours+":"+min+":"+sec;
	}
}
