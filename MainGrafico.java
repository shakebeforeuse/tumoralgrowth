import java.util.Scanner;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class MainGrafico
{
	private static TumorAutomata tumor;
	private static BufferedImage imagen;
	
	private static JFrame frame;
	private static JPanel panel;
	private static JLabel picLabel;
	private static JTextField tam; 
	private static JTextField it;  
	private static JTextField ps;  
	private static JTextField pp;  
	private static JTextField pm;  
	private static JTextField np;  
	
	private static JButton ejecutar;
	private static JButton parar;
	
	private static SwingWorker worker;
	private static boolean finalizar;
	
	private static void GUI()
	{
		//Create and set up the window.
		frame = new JFrame("Tumoral Growth CA");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		panel = new JPanel();
		JPanel parametros = new JPanel(new GridLayout(7, 2));
		panel.add(parametros);
		
		JLabel tamText = new JLabel("Tamaño ");
		JLabel itText  = new JLabel("Pasos ");
		JLabel psText  = new JLabel("Ps ");
		JLabel ppText  = new JLabel("Pp ");
		JLabel pmText  = new JLabel("Pm ");
		JLabel npText  = new JLabel("NP ");
		
		tam = new JTextField("400");
		it  = new JTextField("25");
		ps  = new JTextField("1.0");
		pp  = new JTextField("0.25");
		pm  = new JTextField("0.2");
		np  = new JTextField("1");
		
		ejecutar = new JButton("Ejecutar");
		parar    = new JButton("Parar");
		
		parametros.add(tamText);
		parametros.add(tam);
		
		parametros.add(itText);
		parametros.add(it);
		
		parametros.add(psText);
		parametros.add(ps);
		
		parametros.add(ppText);
		parametros.add(pp);
		
		parametros.add(pmText);
		parametros.add(pm);
		
		parametros.add(npText);
		parametros.add(np);
		
		parametros.add(ejecutar);
		parametros.add(parar);

		// picLabel = new JLabel();
		// panel.add(picLabel);
		
		frame.getContentPane().add(panel);

		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}
	
	public static void main(String[] args) throws Exception
	{
		finalizar = false;
		
		GUI();
		
		ejecutar.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				while (worker != null && !worker.isDone())
					finalizar = true;
				
				int    campoTam = Integer.parseInt(tam.getText());
				int    campoIt  = Integer.parseInt(it.getText());
				double campoPs  = Double.parseDouble(ps.getText());
				double campoPp  = Double.parseDouble(pp.getText());
				double campoPm  = Double.parseDouble(pm.getText());
				int    campoNP  = Integer.parseInt(np.getText());
				
				tumor = new TumorAutomata(campoTam, campoPs, campoPp, campoPm, campoNP);
				tumor.tejido().set(campoTam / 2, campoTam / 2, true);
				tumor.nucleos(Runtime.getRuntime().availableProcessors());
				
				if (picLabel == null)
				{
					picLabel = new JLabel(new ImageIcon(tumor.tejido().imagenColor()));
					panel.add(picLabel);
				}
				else
					picLabel.setIcon(new ImageIcon(tumor.tejido().imagenColor()));
					
				panel.revalidate();
				panel.repaint();
				frame.pack();
				
				finalizar = false;
				
				worker = new SwingWorker<Void, Void>()
				{
					public Void doInBackground()
					{
						while (!finalizar)
						{
							imagen = tumor.tejido().imagenColor();
							picLabel.setIcon(new ImageIcon(imagen));
							
							tumor.ejecutar(campoIt);
						}
						
						return null;
					}
					
					protected void done()
					{
						finalizar = false;
					}
				};
				worker.execute();
			}
		});
		
		parar.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				finalizar = true;
			}
		});
	}
}
