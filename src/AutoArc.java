import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;

import javax.swing.JOptionPane;
import javax.swing.UIManager;


public class AutoArc {
	public static final String VERSION="0.9u1";
	public static final Image ICON_32 = Toolkit.getDefaultToolkit().getImage(AutoArc.class.getResource("Icon_32.png"));
	
	private static File prefsFile;
	public static final File defaultBackupDir=new File("Backups");;
	
	public static final Tasks tasks=new Tasks();
	public static MainWindow mainWindow;
	private static boolean logLocked=false;
	public static boolean startIconified=false;
	public static boolean noTray=false;
	
	public static void main(String[] args) {
    	addToLog("+Program Start - AutoArc version "+VERSION,0);
    	parseArgs(args);
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}catch(Exception e){
		}
		addToLog("Importing Settings",0);
		prefsFile=new File("Settings.conf");
    	if(!prefsFile.exists()){//if the prefs file cannot be found
    		try {
				prefsFile.createNewFile();
			}catch (Exception e) {
				addToLog("\t!Could not create new Settings file: "+e.getMessage(),0);
				JOptionPane.showMessageDialog(null, "Could not create Prefences file.\nThe program will now quit.", "Fatal Error", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
    	}
    	try{
    		Settings.importSettings(prefsFile);
    	}catch(Exception e){
			addToLog("\t!Could not read Settings file: "+e.getMessage(),0);
			JOptionPane.showMessageDialog(null, "Could not read Prefences file.\nThe program will now quit.", "Fatal Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
    	}
    	addToLog("Importing tasks...",0);
    	tasks.importTasks();
    	addToLog("\t"+tasks.size()+" tasks imported",0);
    	addToLog("Starting Save Settings Task",0);
    	new SaveSettingsTask(prefsFile);
    	
    	if(!defaultBackupDir.exists()||defaultBackupDir.isFile()){
    		if(defaultBackupDir.isFile()){
    			try{
    				defaultBackupDir.delete();
    			}catch(Exception e){
    			}
    		}
    		try {
    			defaultBackupDir.mkdir();
			} catch (Exception e) {
			}
    	}
    	addToLog("Launching interface",0);
    	mainWindow=new MainWindow();
	}
	public static void addToLog(String event){
		addToLog(event,0);
	}
	@SuppressWarnings("deprecation")
	public static void addToLog(String event, int level){
		try{
			if(level>Integer.parseInt(Settings.getSetting("LogLevel"))){//if the LogLevel requires a higher priority
				return;
			}
		}catch(NumberFormatException e){//was not set to a number, or no setting exists, assumes value is 0
			if(level>0){
				return;
			}
		}
		Date n=new Date();
		while(logLocked){
			try{
				Thread.sleep(500);//wait half a second
			}catch(Exception e){
			}
		}
		logLocked=true;//lock log
		File l=new File("eventLog.log");
		try{
			if(!l.exists()){
				l.createNewFile();
			}
	        BufferedWriter out = new BufferedWriter(new FileWriter(l,true));
	        out.write((n.getYear()+1900)+"-"+((n.getMonth()+1)<10?"0"+(n.getMonth()+1):(n.getMonth()+1))+"-"+(n.getDate()<10?"0"+n.getDate():n.getDate())+" "+(n.getHours()<10 ? "0"+n.getHours() : n.getHours())+":"+(n.getMinutes()<10 ? "0"+n.getMinutes() : n.getMinutes())+":"+(n.getSeconds()<10 ? "0"+n.getSeconds() : n.getSeconds())+"\t"+event);
	        out.newLine();
	        out.close();
		}catch(Exception e){
		}
		logLocked=false;//unlock
	}
	public static void close() {
		addToLog("Program exiting...",1);
		tasks.exportTasks();
		addToLog("\tSaving Settings...",1);
		try{
			Settings.exportSettings(prefsFile);
			addToLog("\t\tSettings Saved",1);
		}catch(Exception e){
			addToLog("\t\t!Error saving Settings",0);
		}
		addToLog("-Program exit",0);
    	System.exit(0);
	}
	private static void parseArgs(String args[]){
		for(int x=0;x<args.length;x++){
			if(args[x].equalsIgnoreCase("-iconified")){
				startIconified=true;
			}else if(args[x].equalsIgnoreCase("-notray")){
				noTray=true;
			}
		}
	}
}
