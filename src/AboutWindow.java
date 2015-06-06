import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;


@SuppressWarnings("serial")
public class AboutWindow extends JFrame implements ActionListener{
	private JPanel main;
	private JLabel titlelbl=new JLabel("<html><body><h1 align=\"center\">About AutoArc</h1></body></html>");
	private JLabel versionlbl=new JLabel("Version: "+AutoArc.VERSION);
	private JLabel creatorlbl=new JLabel("Created by: Bryan Wyatt");
	private JLabel urllbl=new JLabel("URL:");
	private JEditorPane url=new JEditorPane("text/html","<html><body><a href=\"http://files.getdropbox.com/u/1471134/AutoArc/index.html\">http://files.getdropbox.com/u/1471134/AutoArc/index.html</a></body></html>");
	private JButton close=new JButton("Close Window");
	
	
	public AboutWindow(){
		super("About AutoArc "+AutoArc.VERSION);
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setIconImage(AutoArc.ICON_32);//set frame icon

		main=new JPanel(new SpringLayout());
		
		url.setEditable(false);
		url.setOpaque(false);
		final JFrame parent=this;
		url.addHyperlinkListener(new HyperlinkListener(){   
			public void hyperlinkUpdate(HyperlinkEvent hle) {
				if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())&&JOptionPane.YES_OPTION==JOptionPane.showConfirmDialog(parent, "This action will open a browser window to download the Crypto Library.\n\nDo you wish to continue?", "Continue?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)){
					try {
						Desktop d=Desktop.getDesktop();
						d.browse(hle.getURL().toURI());
					} catch (Exception e) {
						JOptionPane.showMessageDialog(parent, "Unable to open a browser window!");
					}
				}   
			}
		});
		
		main.add(titlelbl);
		main.add(versionlbl);
		main.add(creatorlbl);
		main.add(urllbl);
		main.add(url);
		close.addActionListener(this);
		main.add(close);
		
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.NORTH, titlelbl, 5, SpringLayout.NORTH, main);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.EAST, titlelbl, -5, SpringLayout.EAST, main);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, titlelbl, 5, SpringLayout.WEST, main);

		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.NORTH, versionlbl, 15, SpringLayout.SOUTH, titlelbl);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, versionlbl, 5, SpringLayout.WEST, main);

		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.NORTH, creatorlbl, 5, SpringLayout.SOUTH, versionlbl);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, creatorlbl, 5, SpringLayout.WEST, main);

		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.NORTH, urllbl, 5, SpringLayout.SOUTH, creatorlbl);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, urllbl, 5, SpringLayout.WEST, main);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.NORTH, url, -2, SpringLayout.SOUTH, creatorlbl);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, url, 5, SpringLayout.EAST, urllbl);
		
		add(main);
		
		setSize(400,200);
		setResizable(false);
		setLocationRelativeTo(AutoArc.mainWindow);
		
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.SOUTH, close, -5, SpringLayout.SOUTH, main);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.WEST, close, (this.getWidth()/2)-75, SpringLayout.WEST, main);
		((SpringLayout)main.getLayout()).putConstraint(SpringLayout.EAST, close, -((this.getWidth()/2)-75), SpringLayout.EAST, main);
		
		setVisible(true);
	}
	public void actionPerformed(ActionEvent arg0) {
		this.dispose();
	}
}
