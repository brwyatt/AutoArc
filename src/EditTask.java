import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;


@SuppressWarnings("serial")
public class EditTask extends JDialog implements ActionListener{
	private Task t;
	private boolean newTask;
	private long lastRun=0;
	
	//main window components
	private JPanel main;
	private JLabel nameLbl=new JLabel("Task Name:");
	private JTextField name=new JTextField();
	private JLabel targetLbl=new JLabel("Directory to Backup and Archive");
	private JTextField target=new JTextField();
	private JButton targetBtn=new JButton("Browse...");
	private JLabel backupLbl=new JLabel("Directory to save Archives");
	private JTextField backup=new JTextField();
	private JButton backupBtn=new JButton("Browse...");
	private JLabel freqLbl=new JLabel("Frequency: ");
	private JComboBox freq=new JComboBox(new String[]{"Daily","Weekly","Monthly"});
	private JLabel dOMonthLbl=new JLabel("Day of the Month:");
	private JComboBox dOMonth=new JComboBox(new String[]{"01","02","03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31"});
	private JLabel dOWeekLbl=new JLabel("Day of the Week:");
	private JComboBox dOWeek=new JComboBox(new String[]{"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"});
	private JLabel timeLbl=new JLabel("Time:");
	private JComboBox timeHours=new JComboBox(new String[]{"00","01","02","03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23"});
	private JComboBox timeMins=new JComboBox(new String[]{"00","01","02","03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31","32","33","34","35","36","37","38","39","40","41","42","43","44","45","46","47","48","49","50","51","52","53","54","55","56","57","58","59"});
	private JCheckBox skip=new JCheckBox("Skip hidden files");
	//private JCheckBox delete=new JCheckBox("Delete when archived  (potentially unsafe!)");
	private JButton save=new JButton("Save Task");
	private JButton cancel=new JButton("Cancel");
	
	public EditTask(){
		t=new Task();
		newTask=true;
		
		name.setText("Untitled Task");
		backup.setText(AutoArc.defaultBackupDir.getAbsolutePath());
		
		initWindow();
	}
	public EditTask(Task task){
		t=task;
		newTask=false;
		
		String parts[]=t.getRawData().split(";");
		name.setText(parts[0]);
		target.setText(parts[1]);
		backup.setText(parts[2]);
		lastRun=Long.parseLong(parts[3]);
		freq.setSelectedIndex(Integer.parseInt(parts[4]));
		int runTime=Integer.parseInt(parts[5]);
		timeHours.setSelectedIndex(runTime/100);
		timeMins.setSelectedIndex(runTime-((runTime/100)*100));
		int runDay=Integer.parseInt(parts[6]);
		if(freq.getSelectedIndex()==1){
			dOWeek.setSelectedIndex(runDay);
		}else if(freq.getSelectedIndex()==2){
			dOMonth.setSelectedIndex(runDay-1);
		}
		skip.setSelected(false);
		int tmp=Integer.parseInt(parts[7]);
		if(tmp==1){
			skip.setSelected(true);
		}
		//delete.setSelected(false);
		//tmp=Integer.parseInt(parts[8]);
		//if(tmp==1){
		//	delete.setSelected(true);
		//}
		
		initWindow();
	}
	private void initWindow(){
		setTitle("Task Editor");
		setIconImage(AutoArc.ICON_32);//set frame icon
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		target.setEditable(false);
		backup.setEditable(false);
		dOWeek.setEnabled(false);
		dOMonth.setEnabled(false);
		
		if(freq.getSelectedIndex()==1){
			dOWeek.setEnabled(true);
		}else if(freq.getSelectedIndex()==2){
			dOMonth.setEnabled(true);
		}
		
		main=new JPanel(new SpringLayout());
		
		main.add(nameLbl);
		main.add(name);
		main.add(targetLbl);
		main.add(target);
		main.add(targetBtn);
		targetBtn.addActionListener(this);
		main.add(backupLbl);
		main.add(backup);
		main.add(backupBtn);
		backupBtn.addActionListener(this);
		main.add(freqLbl);
		main.add(freq);
		freq.addActionListener(this);
		main.add(dOWeekLbl);
		main.add(dOWeek);
		main.add(dOMonthLbl);
		main.add(dOMonth);
		main.add(timeLbl);
		main.add(timeHours);
		main.add(timeMins);
		main.add(skip);
		//main.add(delete);
		main.add(save);
		save.addActionListener(this);
		main.add(cancel);
		cancel.addActionListener(this);
		
		
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.NORTH, nameLbl, 10, SpringLayout.NORTH, main);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, nameLbl, 5, SpringLayout.WEST, main);
		
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.NORTH, name, 0, SpringLayout.NORTH, nameLbl);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, name, 5, SpringLayout.EAST, nameLbl);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.EAST, name, -5, SpringLayout.EAST, main);
		
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.NORTH, targetLbl, 10, SpringLayout.SOUTH, nameLbl);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, targetLbl, 5, SpringLayout.WEST, main);
		
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.EAST, targetBtn, -5, SpringLayout.EAST, main);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.NORTH, targetBtn, 4, SpringLayout.SOUTH, targetLbl);
		
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.NORTH, target, 2, SpringLayout.NORTH, targetBtn);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.EAST, target, -5, SpringLayout.WEST, targetBtn);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, target, 10, SpringLayout.WEST, targetLbl);
		
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.NORTH, backupLbl, 10, SpringLayout.SOUTH, targetBtn);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, backupLbl, 5, SpringLayout.WEST, main);
		
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.EAST, backupBtn, -5, SpringLayout.EAST, main);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.NORTH, backupBtn, 4, SpringLayout.SOUTH, backupLbl);
		
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.NORTH, backup, 2, SpringLayout.NORTH, backupBtn);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.EAST, backup, -5, SpringLayout.WEST, backupBtn);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, backup, 10, SpringLayout.WEST, backupLbl);

		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.NORTH, freqLbl, 13, SpringLayout.SOUTH, backupBtn);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, freqLbl, 5, SpringLayout.WEST, main);

		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.NORTH, freq, -3, SpringLayout.NORTH, freqLbl);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, freq, 5, SpringLayout.EAST, freqLbl);

		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.NORTH, dOWeekLbl, 16, SpringLayout.SOUTH, freqLbl);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, dOWeekLbl, 5, SpringLayout.WEST, main);

		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.NORTH, dOWeek, -3, SpringLayout.NORTH, dOWeekLbl);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, dOWeek, 5, SpringLayout.EAST, dOWeekLbl);

		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.NORTH, dOMonthLbl, 16, SpringLayout.SOUTH, dOWeekLbl);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, dOMonthLbl, 5, SpringLayout.WEST, main);

		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.NORTH, dOMonth, -3, SpringLayout.NORTH, dOMonthLbl);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, dOMonth, 5, SpringLayout.EAST, dOMonthLbl);

		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.NORTH, timeLbl, 16, SpringLayout.SOUTH, dOMonthLbl);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, timeLbl, 5, SpringLayout.WEST, main);

		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.NORTH, timeHours, -3, SpringLayout.NORTH, timeLbl);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, timeHours, 5, SpringLayout.EAST, timeLbl);

		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.NORTH, timeMins, -3, SpringLayout.NORTH, timeLbl);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, timeMins, 0, SpringLayout.EAST, timeHours);

		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.NORTH, skip, 10, SpringLayout.SOUTH, timeMins);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, skip, 5, SpringLayout.WEST, main);
		
		//((SpringLayout)main.getLayout()).putConstraint(SpringLayout.NORTH, delete, 5, SpringLayout.SOUTH, skip);
		//((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, delete, 5, SpringLayout.WEST, main);

		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.SOUTH, save, -5, SpringLayout.SOUTH, main);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, save, 85, SpringLayout.WEST, main);

		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.NORTH, cancel, 0, SpringLayout.NORTH, save);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.EAST, cancel, -85, SpringLayout.EAST, main);
		
		add(main);
		
		setSize(350,400);
		setLocationRelativeTo(null);
		setModal(true);
		setResizable(false);
		
		setVisible(true);
	}
	public void actionPerformed(ActionEvent arg0) {
		if(arg0.getSource()==freq){
			switch(freq.getSelectedIndex()){
			case 0:dOWeek.setEnabled(false);dOMonth.setEnabled(false);break;
			case 1:dOWeek.setEnabled(true);dOMonth.setEnabled(false);break;
			case 2:dOWeek.setEnabled(false);dOMonth.setEnabled(true);break;
			}
		}else if(arg0.getSource()==save){
			if(name.getText().length()==0||target.getText().length()==0||backup.getText().length()==0){
				JOptionPane.showMessageDialog(this, "All fields must be filled in.", "Oops!", JOptionPane.WARNING_MESSAGE);
				return;
			}else if(name.getText().contains(";")){
				JOptionPane.showMessageDialog(this, "The task name is invalid.\nThe name cannot include the ';' character.", "Oops!", JOptionPane.WARNING_MESSAGE);
				return;
			}
			int f=freq.getSelectedIndex();
			int runDay=0;
			if(f==1){
				runDay=dOWeek.getSelectedIndex();
			}else if(f==2){
				runDay=dOMonth.getSelectedIndex()+1;
			}
			int tmp=0;
			if(skip.isSelected()){
				tmp=1;
			}
			int tmp2=0;
			//if(delete.isSelected()){
			//	tmp2=1;
			//}
			t.setAll(name.getText()+";"+target.getText()+";"+backup.getText()+";"+lastRun+";"+f+";"+((timeHours.getSelectedIndex()*100)+timeMins.getSelectedIndex())+";"+runDay+";"+tmp+";"+tmp2+";");
			if(newTask){
				AutoArc.tasks.addTask(t);
				AutoArc.addToLog("Created task \""+t.getName()+"\"",1);
			}else{
				AutoArc.addToLog("Changed task \""+t.getName()+"\"",1);
			}
			dispose();
		}else if(arg0.getSource()==cancel){
			dispose();
		}else if(arg0.getSource()==targetBtn){
			JFileChooser fc=new JFileChooser((new File(target.getText())));
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if(fc.showDialog(this, "Select Target Folder")==JFileChooser.APPROVE_OPTION){
				target.setText(fc.getSelectedFile().getAbsolutePath());
			}
		}else if(arg0.getSource()==backupBtn){
			JFileChooser fc=new JFileChooser((new File(backup.getText())));
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if(fc.showDialog(this, "Select Backup Folder")==JFileChooser.APPROVE_OPTION){
				backup.setText(fc.getSelectedFile().getAbsolutePath());
			}
		}
	}
	
}
