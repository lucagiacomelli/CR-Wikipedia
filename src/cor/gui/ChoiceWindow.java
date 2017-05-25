package cor.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import cor.main.Main;

@SuppressWarnings("serial")
public class ChoiceWindow extends JFrame{

	private static JFrame frame;
	private JButton button1, button2;
	
	public ChoiceWindow(){
		super("Coreference Resolution");
		frame = new JFrame("Coreference Resolution");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		button1 = new JButton("Evaluate from Dump");
		button1.addActionListener(new Action1());

		button2 = new JButton("Coreference a new Wikipedia page");
		button2.addActionListener (new Action2()); 

		JPanel panel = new JPanel();
		panel.add(button1);
		panel.add(button2);
		
	    frame.setSize(500, 300);
		frame.add(panel);

	    frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	
	static class Action1 implements ActionListener {        
		public void actionPerformed (ActionEvent e) {
			int choice = JOptionPane.showConfirmDialog(null,"Are you sure? It takes long time!");
			if(choice == JOptionPane.YES_OPTION){
				frame.dispose();
				Main.EvaluateFromDump();
			}			
		 }
	}   

	static class Action2 implements ActionListener {        
		public void actionPerformed (ActionEvent e) {
			String s = JOptionPane.showInputDialog("Insert the title of the Wikipedia page");
		    frame.dispose();
		    Main.CoreferenceNewWikiPage(s);
		}
	}

	
	
}
