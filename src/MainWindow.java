import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

@SuppressWarnings("serial")
public class MainWindow extends JFrame implements ActionListener, WindowListener, MouseListener, ListSelectionListener, WindowStateListener, ComponentListener{
	private boolean traySupported=true;
	private SysTray tray=null;
	
	//main window components
	private JPanel main;
	private JLabel taskslbl=new JLabel("Tasks");
	private DefaultListModel tasksModel=new DefaultListModel();
	private JList tasksList=new JList(tasksModel);
	private JScrollPane tasksPane=new JScrollPane(tasksList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	private JLabel taskInfolbl=new JLabel("Task Info");
	private JLabel taskInfo=new JLabel("No Task Selected");
	private JButton addTask=new JButton("Add Task");
	private JButton about=new JButton("About AutoArc");
	
	//popup
	private JPopupMenu popup;
	private JMenuItem runTask=new JMenuItem("Run Now");
	private JMenuItem viewBackups=new JMenuItem("View Backups");
	private JMenuItem editTask=new JMenuItem("Edit");
	private JMenuItem delTask=new JMenuItem("Delete");

	public MainWindow(){
		super("AutoArc "+AutoArc.VERSION);
		if(!AutoArc.noTray){
			try{
				tray=new SysTray(this);
			}catch(Exception e){
				traySupported=false;
			}
		}else{
			traySupported=false;
		}
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setIconImage(AutoArc.ICON_32);//set frame icon
		
		setSize();
		setLocation();
		
		main=new JPanel(new SpringLayout());
		
		tasksList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		tasksList.addMouseListener(this);
		tasksList.addListSelectionListener(this);
		addTask.addActionListener(this);
		about.addActionListener(this);
		taskInfo.setEnabled(false);
		
		main.add(taskslbl);
		main.add(tasksPane);
		main.add(taskInfolbl);
		main.add(taskInfo);
		main.add(addTask);
		main.add(about);

		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.NORTH, taskslbl, 5, SpringLayout.NORTH, main);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, taskslbl, 5, SpringLayout.WEST, main);

		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, addTask, 5, SpringLayout.WEST, main);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.SOUTH, addTask, -5, SpringLayout.SOUTH, main);
		
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.EAST, about, -5, SpringLayout.EAST, main);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.SOUTH, about, -5, SpringLayout.SOUTH, main);
		
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.NORTH, tasksPane, 5, SpringLayout.SOUTH, taskslbl);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, tasksPane, 5, SpringLayout.WEST, main);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.SOUTH, tasksPane, -5, SpringLayout.NORTH, addTask);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.EAST, tasksPane, 300, SpringLayout.WEST, tasksPane);

		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.NORTH, taskInfolbl, 5, SpringLayout.NORTH, main);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, taskInfolbl, 5, SpringLayout.EAST, tasksPane);

		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, taskInfo, 5, SpringLayout.WEST, taskInfolbl);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.NORTH, taskInfo, 5, SpringLayout.SOUTH, taskInfolbl);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.EAST, taskInfo, -5, SpringLayout.EAST, main);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.SOUTH, taskInfo, 0, SpringLayout.SOUTH, tasksPane);
		
		add(main);
		
		popup=new JPopupMenu();
		runTask.addActionListener(this);
		viewBackups.addActionListener(this);
		editTask.addActionListener(this);
		delTask.addActionListener(this);
		popup.add(runTask);
		popup.add(viewBackups);
		popup.add(editTask);
		popup.add(delTask);
		
		updateTasksList();

		addWindowListener(this);
		addWindowStateListener(this);
		addComponentListener(this);
		
		setVisible(true);
		if(AutoArc.startIconified){
			if(traySupported){
				toggleWindow();
			}else{
				this.setState(JFrame.ICONIFIED);
			}
		}
	}
	private void setSize(){
		setMinimumSize(new Dimension(600,400));
		int w=600;
		int h=400;
		try{
			w=Integer.parseInt(Settings.getSetting("LAST_WIDTH"));
		}catch(NumberFormatException e){
		}
		try{
			h=Integer.parseInt(Settings.getSetting("LAST_HEIGHT"));
		}catch(NumberFormatException e){
		}
		setSize(w,h);
		if(Settings.getSetting("MAXIMIZED").equalsIgnoreCase("true")){
			this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		}
	}
	private void setLocation(){
		int x=-1;
		int y=-1;
		try{
			x=Integer.parseInt(Settings.getSetting("LAST_XPOS"));
		}catch(NumberFormatException e){
		}
		try{
			y=Integer.parseInt(Settings.getSetting("LAST_YPOS"));
		}catch(NumberFormatException e){
		}
		Dimension screensize=Toolkit.getDefaultToolkit().getScreenSize();
		if(x<0){
			x=((int)screensize.getWidth()/2)-(this.getWidth()/2);
		}
		if((x+this.getWidth())>screensize.getWidth()){
			x=(int)screensize.getWidth()-this.getWidth();
		}
		if(y<0){
			y=((int)screensize.getHeight()/2)-(this.getHeight()/2);
		}
		if((y+this.getHeight())>screensize.getHeight()){
			y=(int)screensize.getHeight()-this.getHeight();
		}
		setLocation(x,y);
	}
	public void toggleWindow() {
		if(isVisible()){
			setVisible(false);
		}else{
			setVisible(true);
			this.setState(JFrame.NORMAL);
		}
		if(traySupported){
			tray.checkWindowVisible();
		}
	}
	public void close() {
		if(JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?", "Exit AutoArc?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)==JOptionPane.YES_OPTION){
			
			if(traySupported){
				tray.dispose();
			}
			this.dispose();
			AutoArc.close();
		}

	}
	public boolean visible() {
		return isVisible();
	}
	public void updateTasksList(){
		tasksModel.clear();
		for(int x=0;x<AutoArc.tasks.size();x++){
			tasksModel.addElement(AutoArc.tasks.getTask(x));
		}
		updateTaskInfo();
	}
	public void updateTaskInfo(){
		Task t=(Task)tasksList.getSelectedValue();
		if(t==null){
			taskInfo.setEnabled(false);
			taskInfo.setText("No Task Selected");
		}else{
			taskInfo.setEnabled(true);
			String text="<b><u>Name:</u></b> "+t.getName()+"<br><br><b><u>Target:</u></b> "+t.getTarget()+"<br><br><b><u>Backup:</u></b> "+t.getBackup()+"<br><br><b><u>Last Run:</u></b> "+t.getLastRun()+"<br><br><b><u>Runs On:</u></b> "+t.getRunRule()+"<br><br><b><u>Next Run:</u></b> "+t.getParsedNextRun()+"<br><br><b><u>Skip if hidden:</u></b> "+t.skipIfHidden();
			taskInfo.setText("<html><body>"+text+"</body></html>");
		}
		taskInfo.repaint();
		taskInfo.revalidate();
	}
	public void windowActivated(WindowEvent arg0) {
	}
	public void windowClosed(WindowEvent arg0) {
	}
	public void windowClosing(WindowEvent arg0) {
		close();
	}
	public void windowDeactivated(WindowEvent arg0) {
	}
	public void windowDeiconified(WindowEvent arg0) {
	}
	public void windowIconified(WindowEvent arg0) {
		if(traySupported){
			//this.setState(JFrame.NORMAL);
			toggleWindow();
		}
	}
	public void windowOpened(WindowEvent arg0) {
	}
	public void actionPerformed(ActionEvent arg0) {
		if(arg0.getSource()==addTask){
			new EditTask();
			updateTasksList();
		}else if(arg0.getSource()==runTask){
			if(((Task)tasksList.getSelectedValue()).getLastRun().equals("Running...")){//if task is running
				((Task)tasksList.getSelectedValue()).cancelRunTask();
			}else{
				((Task)tasksList.getSelectedValue()).runTask();
			}
			
			
		}else if(arg0.getSource()==viewBackups){
			new ViewBackup((Task)tasksList.getSelectedValue());
		}else if(arg0.getSource()==editTask){
			new EditTask((Task)tasksList.getSelectedValue());
			updateTaskInfo();
		}else if(arg0.getSource()==delTask){
			if(JOptionPane.showConfirmDialog(this, "Are you sure you want to delete task \""+tasksList.getSelectedValue()+"\"?", "Delete task?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)==JOptionPane.YES_OPTION){
				Task t=(Task)tasksList.getSelectedValue();
				AutoArc.tasks.removeTask(t);
				AutoArc.addToLog("Task \""+t.getName()+"\" Deleted",1);
			}
			updateTasksList();
		}else if(arg0.getSource()==about){
			new AboutWindow();
		}
	}
	public void mouseClicked(MouseEvent me) {
		if(SwingUtilities.isRightMouseButton(me)){
			tasksList.setSelectedIndex(tasksList.locationToIndex(me.getPoint()));
			updateTaskInfo();
			if(!tasksList.isSelectionEmpty()&&tasksList.locationToIndex(me.getPoint())==tasksList.getSelectedIndex()){
				if(((Task)tasksList.getSelectedValue()).getLastRun().equals("Running...")){//if task is running
					runTask.setText("Stop Task");
				}else{
					runTask.setText("Run Now");
				}
				popup.show(tasksList, me.getX(), me.getY());
			}
		}
	}
	public void mouseEntered(MouseEvent arg0) {
	}
	public void mouseExited(MouseEvent arg0) {
	}
	public void mousePressed(MouseEvent arg0) {
	}
	public void mouseReleased(MouseEvent arg0) {
	}
	public void valueChanged(ListSelectionEvent arg0) {
		updateTaskInfo();
	}
	public void windowStateChanged(WindowEvent arg0) {
		if(arg0.getNewState()==JFrame.MAXIMIZED_BOTH){
			Settings.addSetting("MAXIMIZED", "true");
		}else if(((JFrame)arg0.getComponent()).getExtendedState()!=JFrame.MAXIMIZED_BOTH){
			Settings.addSetting("MAXIMIZED", "false");
		}
	}
	public void componentHidden(ComponentEvent arg0) {
	}
	public void componentMoved(ComponentEvent arg0) {
		Settings.addSetting("LAST_XPOS", ""+((JFrame)arg0.getSource()).getX());
		Settings.addSetting("LAST_YPOS", ""+((JFrame)arg0.getSource()).getY());
	}
	public void componentResized(ComponentEvent arg0) {
		if(this.getExtendedState()!=JFrame.MAXIMIZED_BOTH){
			Settings.addSetting("LAST_WIDTH",""+((JFrame)arg0.getSource()).getWidth());
			Settings.addSetting("LAST_HEIGHT", ""+((JFrame)arg0.getSource()).getHeight());
			Settings.addSetting("LAST_XPOS", ""+((JFrame)arg0.getSource()).getX());
			Settings.addSetting("LAST_YPOS", ""+((JFrame)arg0.getSource()).getY());
		}
	}
	public void componentShown(ComponentEvent arg0) {
	}
}
