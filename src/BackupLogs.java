import java.io.File;
import java.util.ArrayList;
import java.util.Date;


public class BackupLogs {
	public static final int FILE_CREATED=0;
	public static final int FILE_CHANGED=1;
	public static final int FILE_DELETED=2;
	
	private File folder;
	private ArrayList<FileLog> logs;
	
	public BackupLogs(File location){
		folder=new File(location.getAbsolutePath()+"/logs/");
		folder.mkdirs();
		logs=loadFileLogs();
	}
	private ArrayList<FileLog> loadFileLogs(){
		ArrayList<String> files=traverseFileTree(folder);
		ArrayList<FileLog> fl=new ArrayList<FileLog>();
		for(int x=0;x<files.size();x++){
			fl.add(new FileLog(files.get(x)));
		}
		return fl;
	}
	private ArrayList<String> traverseFileTree(File root){
		ArrayList<String> files=new ArrayList<String>();
		if(root.isFile()){
			files.add(root.getAbsolutePath());
			return files;
		}
		File[] children=root.listFiles();
		if(children==null){
			files.add(root.getAbsolutePath());
			return files;
		}
		for(int x=0;x<children.length;x++){
			if(children[x].isHidden()){
			}else if(children[x].isDirectory()){
				files.addAll(traverseFileTree(children[x]));
			}else if(children[x].getPath().endsWith(".his")){
				files.add(children[x].getAbsolutePath());
			}
		}
		return files;
	}
	public boolean addEntry(String path, String hash, Date time){//returns false if the file is the same
		FileLog fl=findLog(path);
		if(fl!=null){//log already exists
			FileRecord fr=fl.getLastRecord();
			if(fr.getHash().equals(hash)){
				fl.close();
				logs.remove(fl);
				return false;
			}
			if(fr.getAction()==FILE_DELETED){
				fl.addRecord(new FileRecord(time, path, FILE_CREATED, hash));
			}else{
				fl.addRecord(new FileRecord(time, path, FILE_CHANGED, hash));
			}
		}else{
			fl=new FileLog(path,folder.getAbsolutePath());
			fl.addRecord(new FileRecord(time, path, FILE_CREATED, hash));
		}
		fl.close();
		logs.remove(fl);
		return true;
	}
	private FileLog findLog(String path){
		for(int x=0;x<logs.size();x++){
			if(logs.get(x).match(path)){
				return logs.get(x);
			}
		}
		return null;
	}
	public FileLog getFileLog(int x){
		if(x<logs.size()){
			return logs.get(x);
		}
		return null;
	}
	public void markDeletedFiles(Date time){
		for(int x=0;x<logs.size();x++){
			if(logs.get(x).getLastRecord().getAction()!=FILE_DELETED){
				logs.get(x).addRecord(new FileRecord(time,logs.get(x).getLastRecord().getPath(),FILE_DELETED,"0"));
				logs.get(x).close();
			}
		}
	}
	public File getLogLocation(){
		return new File(folder.getAbsolutePath());
	}
}
