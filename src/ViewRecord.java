import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class ViewRecord extends JFrame implements ActionListener, WindowListener, MouseListener{
	private BackupLogs logs;
	private BackupRecord rec;
	private Task t;
	private boolean run=true;
	private boolean restoring=false;
	
	private JPanel main;
	private JLabel title=new JLabel();
	private DefaultListModel tableModel=new DefaultListModel();
	private JList table=new JList(tableModel);
	private JScrollPane tablePane=new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	private JButton restore=new JButton("Restore");
	private JLabel status=new JLabel("Loading...", SwingConstants.CENTER);
	private JButton close=new JButton("Close");

	public ViewRecord(BackupLogs backupLogs, BackupRecord record, Task task){
		this(backupLogs, record, task, null);
	}
	
	public ViewRecord(BackupLogs backupLogs, BackupRecord record, Task task, JFrame parent) {
		logs=backupLogs;
		rec=record;
		t=task;
		
		this.setTitle("Backups for: "+rec);
		title.setText("Backups for: "+rec);
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setIconImage(AutoArc.ICON_32);//set frame icon

		setMinimumSize(new Dimension(250,300));
		setSize(400,600);
		setLocationRelativeTo(parent);
		addWindowListener(this);
		table.addMouseListener(this);
		
		main=new JPanel(new SpringLayout());
		
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		restore.addActionListener(this);
		restore.setEnabled(false);
		close.addActionListener(this);
		
		main.add(title);
		main.add(tablePane);
		main.add(restore);
		main.add(status);
		main.add(close);
		
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.NORTH, title, 5, SpringLayout.NORTH, main);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, title, 5, SpringLayout.WEST, main);

		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.SOUTH, restore, -5, SpringLayout.SOUTH, main);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, restore, 5, SpringLayout.WEST, main);

		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.SOUTH, status, -5, SpringLayout.SOUTH, main);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.EAST, status, -5, SpringLayout.WEST, close);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, status, 5, SpringLayout.EAST, restore);
		
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.SOUTH, close, -5, SpringLayout.SOUTH, main);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.EAST, close, -5, SpringLayout.EAST, main);

		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.NORTH, tablePane, 5, SpringLayout.SOUTH, title);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, tablePane, 5, SpringLayout.WEST, main);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.EAST, tablePane, -5, SpringLayout.EAST, main);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.SOUTH, tablePane, -5, SpringLayout.NORTH, close);
		
		add(main);
		setVisible(true);
		
		(new Thread(){public void run(){loadBackups();}}).start();
		//loadBackups();
	}
	
	private void loadBackups(){
		FileLog fl=null;
		FileRecord fr=null;
		int count=0;
		for(int x=0;(fl=logs.getFileLog(x))!=null;x++){
			if(!run){
				return;
			}
			fr=fl.getRecordByTime(new Date(rec.getTime()));
			if(fr!=null){
				if(fr.getAction()!=BackupLogs.FILE_DELETED){
					final FileRecord tmp = fr;
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							tableModel.addElement(tmp);
						}
					});
					count++;
				}
			}
			fl.close();
		}
		status.setText("Found "+count+" Files");
		restore.setEnabled(true);
	}
	
	private void restore(){
		if(JOptionPane.showConfirmDialog(this, "Are you sure you wish to restore backup for "+rec+"?", "Restore?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)==JOptionPane.YES_OPTION){
			restoring=true;
			AutoArc.addToLog("Restoring \""+t.getName()+"\"...",1);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					restore.setEnabled(false);
					close.setEnabled(false);
					status.setText("Restoring...");
				}
			});
			int count=0;
			File targetFile=new File(t.getTarget());
			recursiveDelete(targetFile);
			if(!targetFile.mkdirs()){
				JOptionPane.showMessageDialog(this, "Could not create target folder. Files in the target may be in use.\nClose any programs that may using them and try again.", "Restore Error", JOptionPane.ERROR_MESSAGE);
				AutoArc.addToLog("\tRestoring \""+t.getName()+"\" failed: Could not create target folder.",0);
				restoring=false;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						restore.setEnabled(true);
						close.setEnabled(true);
						status.setText("Restore failed!");
					}
				});
				return;
			}
			
			for(int x=0;x<tableModel.getSize();x++){
				FileRecord fr=(FileRecord)tableModel.get(x);
				InputStream in=null;
				OutputStream out=null;
				ZipFile zf=null;
				File restoreFile=null;
				
				try {
					zf = new ZipFile(logs.getLogLocation().getParent()+"/"+fr.getTime().getTime()+".arc");
					restoreFile=new File(t.getTarget()+"/"+fr.getPath());
					restoreFile.getParentFile().mkdirs();
					restoreFile.createNewFile();
					
					in = zf.getInputStream(new ZipEntry(fr.getPath()));
					out=new FileOutputStream(restoreFile);
					
					byte[] buf=new byte[1024];
					int read=0;
					
					while((read=in.read(buf))!=-1){
						out.write(buf, 0, read);
					}
					count++;
				} catch (Exception e) {
					AutoArc.addToLog("\tRestoring \""+t.getName()+"\" failed: "+e.getClass().getName()+": "+e.getMessage(),0);
					break;
				}
				
				try{
					out.close();
					zf.close();
				}catch(Exception e){
				}
			}
			restoring=false;
			final int c=count;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					restore.setEnabled(true);
					close.setEnabled(true);
					status.setText("Restored "+c+" files");
				}
			});
			AutoArc.addToLog("\tRestore \""+t.getName()+"\" finished",1);
		}
	}
	
	private void recursiveDelete(File root){
		if(root.isDirectory()){//directory: delete contents, then delete
			File[] children=root.listFiles();
			for(int x=0; x<children.length; x++){
				recursiveDelete(children[x]);
			}
			root.delete();
			root.delete();
		}else{//file, delete it
			root.delete();
		}
	}
	
	private void close(){
		if(!restoring){
			run=false;
			this.dispose();
		}
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(arg0.getSource()==close){
			close();
		}else if(arg0.getSource()==restore){
			(new Thread(){public void run(){restore();}}).start();
		}
	}
	@Override
	public void windowActivated(WindowEvent arg0) {
	}
	@Override
	public void windowClosed(WindowEvent arg0) {
	}
	@Override
	public void windowClosing(WindowEvent arg0) {
		close();
	}
	@Override
	public void windowDeactivated(WindowEvent arg0) {
	}
	@Override
	public void windowDeiconified(WindowEvent arg0) {
	}
	@Override
	public void windowIconified(WindowEvent arg0) {
	}
	@Override
	public void windowOpened(WindowEvent arg0) {
	}
	@Override
	public void mouseClicked(MouseEvent me) {
		if(me.getButton()==MouseEvent.BUTTON1 && me.getClickCount()==2){
			table.setSelectedIndex(table.locationToIndex(me.getPoint()));
			if(!table.isSelectionEmpty()&&table.locationToIndex(me.getPoint())==table.getSelectedIndex()){
				if(JOptionPane.showConfirmDialog(this, "Are you sure you wish to open "+((FileRecord)table.getSelectedValue()).getPath()+"?", "Open?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)==JOptionPane.YES_OPTION){
					InputStream in=null;
					OutputStream out=null;
					ZipFile zf=null;
					File tmpFile=null;
					
					try {
						zf = new ZipFile(logs.getLogLocation().getParent()+"/"+((FileRecord)table.getSelectedValue()).getTime().getTime()+".arc");
						tmpFile=File.createTempFile("ARCFILE_", ((FileRecord)table.getSelectedValue()).getPath().substring(((FileRecord)table.getSelectedValue()).getPath().lastIndexOf('.')));
						
						in = zf.getInputStream(new ZipEntry(((FileRecord)table.getSelectedValue()).getPath()));
						out=new FileOutputStream(tmpFile);
						
						byte[] buf=new byte[1024];
						int read=0;
						
						while((read=in.read(buf))!=-1){
							out.write(buf, 0, read);
						}
						
						Desktop.getDesktop().open(tmpFile);
					} catch (Exception e) {
						JOptionPane.showMessageDialog(this, "Error opening file!", "Error", JOptionPane.ERROR_MESSAGE);
					}

					tmpFile.deleteOnExit();
					try{
						out.close();
						zf.close();
					}catch(Exception e){
					}
				}
			}
		}
	}
	@Override
	public void mouseEntered(MouseEvent arg0) {
	}
	@Override
	public void mouseExited(MouseEvent arg0) {
	}
	@Override
	public void mousePressed(MouseEvent arg0) {
	}
	@Override
	public void mouseReleased(MouseEvent arg0) {
	}
}