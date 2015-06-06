import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;

@SuppressWarnings("serial")
class SysTray extends JFrame{
	TrayIcon trayIcon=null;//tray icon
	private MenuItem exitItem = new MenuItem("Exit");//exit menu item
	private MenuItem toggleItem = new MenuItem("Hide Window");//Toggle menu item
	private MainWindow window;
	private PopupMenu popup;
	
	public SysTray(MainWindow win) throws Exception{
		window=win;
		Image image=AutoArc.ICON_32;
		if(true){
			//throw new Exception("TEST ERROR");
		}
		if (SystemTray.isSupported()&&image!=null){//if there is a system tray available and image loaded correctly
			SystemTray tray = SystemTray.getSystemTray();//retrieve systemTray reference
			MouseListener mouseListener = new MouseListener(){//mouse listeners
				public void mouseClicked(MouseEvent e){   
					if(e.getClickCount()==2&&e.getButton()==MouseEvent.BUTTON1){
						window.toggleWindow();
					}
	        	}
				public void mouseEntered(MouseEvent e){                
	        	}
	        	public void mouseExited(MouseEvent e){                
	        	}
	        	public void mousePressed(MouseEvent e){               
	        	}
	        	public void mouseReleased(MouseEvent e){               
	        	}
			};
			ActionListener actions = new ActionListener(){//exit listener
				public void actionPerformed(ActionEvent e){
					Object source=e.getSource();
					if(source==exitItem){//if exit item is clicked
						window.close();
					}else if(source==toggleItem){
						window.toggleWindow();
					}
				}
			};     
			popup = new PopupMenu();//create new popup menu
			exitItem.addActionListener(actions);//add listener for toggle
			toggleItem.addActionListener(actions);//add listener for toggle
			popup.add(toggleItem);//add to menu
			popup.addSeparator();
			popup.add(exitItem);//add to menu
			trayIcon = new TrayIcon(image, "AutoArc", popup);//create tray icon with image, tooltip and popup menu
			trayIcon.setImageAutoSize(true);//auto size image
	    	trayIcon.addActionListener(actions);//add action listener
	    	trayIcon.addMouseListener(mouseListener);//add mouse listener
	    	try {
	        	tray.add(trayIcon);//add tray icon to tray
	    	}catch (AWTException e){//error adding icon
	    		throw new Exception("System Tray Not Supported.");
	    	}
		}else{//System Tray is not supported
			throw new Exception("System Tray Not Supported.");
		}
	}
	public void checkWindowVisible(){
		if(window.visible()){
			toggleItem.setLabel("Hide Window");
		}else{
			toggleItem.setLabel("Show Window");
		}
	}
}