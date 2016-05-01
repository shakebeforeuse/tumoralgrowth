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
	private static JTextField rho;
	
	private static JButton ejecutar;
	private static JButton parar;
	
	private static SwingWorker worker;
	private static boolean finalizar;
	
	private static int    campoTam; 
	private static int    campoIt; 
	private static double campoPs; 
	private static double campoPp; 
	private static double campoPm; 
	private static int    campoNP; 
	private static int    campoRho; 
	
	private static void GUI()
	{
		//Create and set up the window.
		frame = new JFrame("Tumoral Growth CA");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		panel = new JPanel();
		JPanel parametros = new JPanel(new GridLayout(8, 2));
		panel.add(parametros);
		
		JLabel tamText = new JLabel("Tamaño ");
		JLabel itText  = new JLabel("Pasos ");
		JLabel psText  = new JLabel("Ps ");
		JLabel ppText  = new JLabel("Pp ");
		JLabel pmText  = new JLabel("Pm ");
		JLabel npText  = new JLabel("NP ");
		JLabel rhoText = new JLabel("Rho Máx.");
		
		tam = new JTextField("400");
		it  = new JTextField("1");
		ps  = new JTextField("0.99");
		pp  = new JTextField("0.4");
		pm  = new JTextField("0.2");
		np  = new JTextField("5");
		rho = new JTextField("2");
		
		
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
		
		parametros.add(rhoText);
		parametros.add(rho);
		
		parametros.add(ejecutar);
		parametros.add(parar);

		// picLabel = new JLabel();
		// panel.add(picLabel);
		
		frame.getContentPane().add(panel);

		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}
	
	//Métodos para debug
	static BufferedImage imagenColor()
	{
		BufferedImage imagen = new BufferedImage(campoTam, campoTam, BufferedImage.TYPE_INT_RGB);
		
		for (int i = 0; i < campoTam; ++i)
		{
			for (int j = 0; j < campoTam; ++j)
			{
				int color = Color.BLUE.getRGB();
				
				switch (tumor.verEstado(i, j))
				{
					case 0:
						color = Color.GRAY.getRGB();
						break;
					
					case 1:
						color = Color.DARK_GRAY.getRGB();
						break;
						
					case 2:
						color = Color.BLACK.getRGB();
						break;
					
					case 3:
						color = Color.RED.getRGB();
						break;
						
					case 4:
						color = Color.GREEN.getRGB();
						break;
					
					default:
						color = Color.MAGENTA.getRGB();
				}
				
				imagen.setRGB(j, i, color);
			}
		}
		
		return imagen;
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
				
				campoTam = Integer.parseInt(tam.getText());
				campoIt  = Integer.parseInt(it.getText());
				campoPs  = Double.parseDouble(ps.getText());
				campoPp  = Double.parseDouble(pp.getText());
				campoPm  = Double.parseDouble(pm.getText());
				campoNP  = Integer.parseInt(np.getText());
				campoRho = Integer.parseInt(rho.getText());
				
				tumor = new TumorAutomata(campoTam);
				tumor.ps  = campoPs;
				tumor.pp  = campoPp;
				tumor.pm  = campoPm;
				tumor.np  = campoNP;
				tumor.rho = campoRho;
				
				tumor.cambiarEstado(campoTam / 2, campoTam / 2, TumorAutomata.VIVA);
				tumor.nucleos(Runtime.getRuntime().availableProcessors());
				
				if (picLabel == null)
				{
					picLabel = new JLabel(new ImageIcon(imagenColor()));
					panel.add(picLabel);
				}
				else
					picLabel.setIcon(new ImageIcon(imagenColor()));
					
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
							imagen = imagenColor();
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
