import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class RunTask implements Runnable{
	private Task t;
	private boolean keepRunning=true;
	
	public RunTask(Task task){
		t=task;
	}

	public void run() {
		AutoArc.addToLog("Running task \""+t.getName()+"\"...",1);
		byte[] buff=new byte[64];
		Date now=new Date();
		
		File location=new File(""+t.getBackup()+"/"+t.getName()+"/");
		location.mkdirs();
		BackupLogs logs=new BackupLogs(location);
		
		String outFilename=""+t.getBackup()+"/"+t.getName()+"/"+now.getTime()+".arc";

		try{
			ZipOutputStream out=null;
			ArrayList<String> filenames=traverseFileTree(new File(t.getTarget()));
			
			for(int i=0; i<filenames.size()&&keepRunning; i++){
				MessageDigest md = MessageDigest.getInstance("MD5");
				InputStream in=new FileInputStream(filenames.get(i));
				in=new DigestInputStream(in, md);
				
				try{//this segment will check that the file is not locked, if it is not locked, it creates an entry in the archive, and write the test byte
					byte[] tmp=new byte[1];
					in.read(tmp);
				}catch(IOException e){
					if(e.getMessage().startsWith("The process cannot access the file")){
						AutoArc.addToLog("\t!The file \""+RelativePath.getRelativePath(new URI(t.getTarget().replaceAll("\\\\", "/").replaceAll(" ","%20")), new URI(filenames.get(i).replaceAll("\\\\", "/").replaceAll(" ","%20")))+"\" for task \""+t.getName()+"\" was skipped because it was locked.",0);
					}else{
						throw e;
					}
				}
				
				int len;
				while((len=in.read(buff))>0&&keepRunning){
				}
				
				byte[] hash=md.digest();
				
				if(logs.addEntry(RelativePath.getRelativePath(new URI(t.getTarget().replaceAll("\\\\", "/").replaceAll(" ","%20")), new URI(filenames.get(i).replaceAll("\\\\", "/").replaceAll(" ","%20"))), (new BigInteger(1,hash)).toString(16), now)){
					in.close();
					in=new FileInputStream(filenames.get(i));
					if(out==null){
						out=new ZipOutputStream(new FileOutputStream(outFilename));
					}
					out.putNextEntry(new ZipEntry(RelativePath.getRelativePath(new URI(t.getTarget().replaceAll("\\\\", "/").replaceAll(" ","%20")), new URI(filenames.get(i).replaceAll("\\\\", "/").replaceAll(" ","%20")))));
					while((len=in.read(buff))>0&&keepRunning){
						out.write(buff,0,len);
					}
					out.closeEntry();
				}
				in.close();
			}
			if(out!=null){
				out.close();
			}
			if(keepRunning){
				logs.markDeletedFiles(now);
				t.setLastRun((new Date()).getTime());
				AutoArc.addToLog("\tTask \""+t.getName()+"\" completed successfully",1);
			}else{
				t.setLastRun(-2L);
				AutoArc.addToLog("\tTask \""+t.getName()+"\" canceled by user",0);
				try{
					(new File(outFilename)).delete();//delete file
				}catch(Exception e){
				}
			}
			
		}catch(Exception e){
			t.setLastRun(-2L);
			AutoArc.addToLog("\t!Task \""+t.getName()+"\" failed to complete: "+e.getClass().getName()+": "+e.getMessage(),0);
			try{
				(new File(outFilename)).delete();
			}catch(Exception e1){
			}
		}
		AutoArc.mainWindow.updateTaskInfo();
	}
	public void cancelRun(){
		keepRunning=false;
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
			if(children[x].isHidden() && t.skipIfHidden()){
			}else if(children[x].isDirectory()){
				files.addAll(traverseFileTree(children[x]));
			}else{
				files.add(children[x].getAbsolutePath());
			}
		}
		return files;
	}
}
