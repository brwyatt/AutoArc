import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.Date;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

@SuppressWarnings("serial")
public class ViewBackup extends JFrame implements ActionListener, ListSelectionListener, MouseListener, WindowListener{
	private Task t;
	private boolean run=true;
	
	private JPanel main;
	private JLabel title=new JLabel();
	private DefaultListModel tableModel=new DefaultListModel();
	private JList table=new JList(tableModel);
	private JScrollPane tablePane=new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	private JButton view=new JButton("View Details");
	private JLabel status=new JLabel("Loading...", SwingConstants.CENTER);
	private JButton close=new JButton("Close");
	
	public ViewBackup(Task task, JFrame parent){
		t=task;
		
		this.setTitle("Backups for: "+t.getName());
		title.setText("Backups for: "+t.getName());
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setIconImage(AutoArc.ICON_32);//set frame icon

		setMinimumSize(new Dimension(250,300));
		setSize(250,300);
		setLocationRelativeTo(parent);
		addWindowListener(this);
		
		main=new JPanel(new SpringLayout());
		
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		table.addMouseListener(this);
		table.addListSelectionListener(this);
		view.addActionListener(this);
		close.addActionListener(this);
		view.setEnabled(false);
		
		main.add(title);
		main.add(tablePane);
		main.add(view);
		main.add(status);
		main.add(close);
		
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.NORTH, title, 5, SpringLayout.NORTH, main);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, title, 5, SpringLayout.WEST, main);

		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.SOUTH, view, -5, SpringLayout.SOUTH, main);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, view, 5, SpringLayout.WEST, main);

		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.SOUTH, status, -5, SpringLayout.SOUTH, main);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.EAST, status, -5, SpringLayout.WEST, close);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, status, 5, SpringLayout.EAST, view);
		
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
	public ViewBackup(Task task){
		this(task, null);
	}
	private void loadBackups(){
		File backuploc=new File(t.getBackup()+"/"+t.getName()+"/");
		if(backuploc.exists()){
			File children[]=backuploc.listFiles();
			for(int x=0;x<children.length;x++){
				if(children[x].getName().endsWith(".arc")){
					try{
						tableModel.addElement(new BackupRecord(new Date(Long.parseLong(children[x].getName().substring(0,children[x].getName().length()-4)))));
					}catch(NumberFormatException nfe){
					}
				}
			}
		}
		BackupLogs logs=new BackupLogs(new File(""+t.getBackup()+"/"+t.getName()+"/"));
		FileLog fl=null;
		FileRecord fr=null;
		for(int x=0;(fl=logs.getFileLog(x))!=null;x++){
			for(int y=0;(fr=fl.getRecord(y))!=null;y++){
				if(!run){
					fl.close();
					return;
				}
				if(fr.getAction()==BackupLogs.FILE_DELETED){
					int low = 0;
			        int high = tableModel.size()-1;
			        int mid=0;
			        boolean found=false;
			        while( low <= high ){
			        	if(!run){
							fl.close();
							return;
						}
			        	
			        	mid = ( low + high ) / 2;
			        	
			        	if(((BackupRecord)tableModel.get(mid)).getTime() < fr.getTime().getTime()){
			        		low = mid + 1;
			        	}else if(((BackupRecord)tableModel.get(mid)).getTime() > fr.getTime().getTime()){
			        		high = mid - 1;
			        	}else{
			        		found=true;
			        		break;
			        	}
			        }
			        if(!found){
			        	final FileRecord tmp = fr;
			        	final int index=mid;
						//SwingUtilities.invokeLater(new Runnable() {
						//	public void run() {
			        	//if(index<tableModel.getSize()-1){
							tableModel.add(index, new BackupRecord(new Date(tmp.getTime().getTime())));
			        	//}else{
			        	//	tableModel.addElement(new BackupRecord(new Date(tmp.getTime().getTime())));
			        	//}
						table.revalidate();
						//	}
						//});
			        }
				}
			}
			fl.close();
		}
		table.revalidate();
		status.setText("");
	}
	
	public void openRecordViewer(){
		new ViewRecord(new BackupLogs(new File(""+t.getBackup()+"/"+t.getName()+"/")), (BackupRecord)table.getSelectedValue(), t, this);
	}
	
	private void close(){
		run=false;
		this.dispose();
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(arg0.getSource()==close){
			close();
		}else if(arg0.getSource()==view){
			openRecordViewer();
		}
	}
	@Override
	public void valueChanged(ListSelectionEvent arg0) {
		if(table.getSelectedIndex()>=0){
			view.setEnabled(true);
		}else{
			view.setEnabled(false);
		}
	}
	@Override
	public void mouseClicked(MouseEvent arg0) {
		if(arg0.getClickCount()==2 && arg0.getButton()==MouseEvent.BUTTON1){
			openRecordViewer();
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
}