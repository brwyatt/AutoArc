import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;

public class FileLog {
	private String path;
	private RandomAccessFile log;
	private static final int RECORD_SIZE=512;
	
	public FileLog(String filePath, String backupPath){//create from root-relative file path, and abs/rel logfolder
		path=filePath;
		File f=new File(backupPath+"/"+path+".his");
		File dirs=new File(f.getParent());
		try {
			if(!f.exists()){
				try{
					dirs.mkdirs();
					f.createNewFile();
				} catch (Exception e) {
					System.out.println("1:1: "+e.getMessage());
				}
			}
			log=new RandomAccessFile(backupPath+"/"+path+".his","rw");
		} catch (Exception e) {
			System.out.println("1:2: "+e.getMessage());
		}
	}
	public FileLog(String logPath){//create from log path
		File f=new File(logPath);
		File dirs=new File(f.getParent());
		try {
			if(!f.exists()){
				try{
					dirs.mkdirs();
					f.createNewFile();
				} catch (Exception e) {
					System.out.println("2:1: "+e.getMessage());
				}
			}
			log=new RandomAccessFile(logPath,"rw");
		} catch (Exception e) {
			System.out.println("2:2: "+e.getMessage());
		}
		path=getRecord(0).getPath();
	}
	public int getRecordCount(){
		try {
			return (int)(log.length()/RECORD_SIZE);
		} catch (IOException e) {
			return 0;
		}
	}
	public FileRecord getRecord(int x){
		try {
			if(getRecordCount() > x){
				log.seek(x*RECORD_SIZE);
				byte data[]=new byte[RECORD_SIZE];
				log.read(data);
				return new FileRecord(new String(data,"Unicode"));
			}
		} catch (Exception e) {
		}
		return null;
	}
	public FileRecord getLastRecord(){
		return getRecord(getRecordCount()-1);
	}
	public boolean addRecord(FileRecord f){
		try{
			String s=f.getTime().getTime()+":"+f.getPath()+":"+f.getAction()+":"+f.getHash()+":";
			byte[] a=s.getBytes("Unicode");//s.getBytes();
			byte[] b=new byte[RECORD_SIZE];
			if(a.length>RECORD_SIZE){
				System.err.println("RECORD TOO BIG!");
				return false;
			}
			for(int x=0;x<a.length;x++){
				b[x]=a[x];
			}
			log.seek(log.length());
			log.write(b);
		}catch(Exception e){
		}
		return false;
	}
	public FileRecord getRecordByTime(Date time){//Finds most recent record for the given date
		Long t=time.getTime();

		int low = 0;
        int high = getRecordCount()-1;
        int mid=0;
        while( low <= high )
        {
            mid = ( low + high ) / 2;

            if( getRecord(mid).getTime().getTime() < t )
                low = mid + 1;
            else if( getRecord(mid).getTime().getTime() > t )
                high = mid - 1;
            else
                return getRecord(mid);
        }
        FileRecord fr=getRecord(mid);
        if(fr.getTime().getTime()<=t){
        	return getRecord(mid);
        }else{
        	return null;
        }
	}
	/*//unfinished code segment
	public FileRecord removeRecord(int x){
		try {
			FileRecord fr=getRecord(x);
			if(getRecordCount()==x+1){//is last record
				log.setLength((getRecordCount()-1)*RECORD_SIZE);
			}else if(getRecordCount() > x+1){
				for(int y=x+1;y<getRecordCount();y++){
					FileRecord f=getRecord(y);
					String s=f.getTime().getTime()+":"+f.getPath()+":"+f.getAction()+":"+f.getHash()+":";
					byte[] a=s.getBytes("Unicode");
					byte[] b=new byte[RECORD_SIZE];
					for(int z=0;z<a.length;z++){
						b[z]=a[z];
					}
					log.seek((y-1)*RECORD_SIZE);
					log.write(b);
				}
			}
			return fr;
		} catch (Exception e) {
		}
		return null;
	}
	public FileRecord removeRecordByTime(Date time){//deletes the record for the given date
		Long t=time.getTime();

		int low = 0;
        int high = getRecordCount()-1;
        int mid=0;
        while( low <= high )
        {
            mid = ( low + high ) / 2;

            if( getRecord(mid).getTime().getTime() < t )
                low = mid + 1;
            else if( getRecord(mid).getTime().getTime() > t )
                high = mid - 1;
            else
                return getRecord(mid);
        }
        FileRecord fr=getRecord(mid);
        if(fr.getTime().getTime()==t){
        	return removeRecord(mid);
        }else{
        	return null;
        }
	}
	*/
	public boolean match(String file){
		if(file.charAt(0)=='/' || file.charAt(0)=='\\'){
			file=file.substring(1);
		}
		String p=path;
		if(p.charAt(0)=='/' || p.charAt(0)=='\\'){
			p=p.substring(1);
		}
		if(file.equals(p)){
			return true;
		}
		return false;
	}
	public void close(){
		try {
			log.close();
		} catch (Exception e) {
		}
	}
}