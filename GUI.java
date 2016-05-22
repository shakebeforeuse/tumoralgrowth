import java.util.Scanner;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
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

public class GUI
{
	private static TumorAutomaton tumor;
	private static BufferedImage image;
	private static long it_;
	
	private static JFrame frame;
	private static JPanel panel;
	private static JLabel picLabel;
	private static JTextField size;
	private static JTextField it;
	private static JTextField ps;
	private static JTextField pp;
	private static JTextField pm;
	private static JTextField np;
	private static JTextField rho;
	
	private static JButton execute;
	private static JButton stop;
	
	private static SwingWorker worker;
	private static boolean terminate;
	
	private static int    fieldSize; 
	private static int    fieldIt; 
	private static double fieldPs; 
	private static double fieldPp; 
	private static double fieldPm; 
	private static int    fieldNP; 
	private static int    fieldRho; 
	
	private static void GUI()
	{
		//Create and set up the window.
		frame = new JFrame("Tumoral Growth CA");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		panel = new JPanel();
		JPanel parameters = new JPanel(new GridLayout(8, 2));
		panel.add(parameters);
		
		JLabel sizeText = new JLabel("Size ");
		JLabel itText  = new JLabel("Steps ");
		JLabel psText  = new JLabel("Ps ");
		JLabel ppText  = new JLabel("Pp ");
		JLabel pmText  = new JLabel("Pm ");
		JLabel npText  = new JLabel("NP ");
		JLabel rhoText = new JLabel("Rho Máx.");
		
		size = new JTextField("400");
		it   = new JTextField("1");
		ps   = new JTextField("0.99");
		pp   = new JTextField("0.4");
		pm   = new JTextField("0.2");
		np   = new JTextField("5");
		rho  = new JTextField("2");
		
		
		execute = new JButton("Run");
		stop    = new JButton("Stop");
		
		parameters.add(sizeText);
		parameters.add(size);
		
		parameters.add(itText);
		parameters.add(it);
		
		parameters.add(psText);
		parameters.add(ps);
		
		parameters.add(ppText);
		parameters.add(pp);
		
		parameters.add(pmText);
		parameters.add(pm);
		
		parameters.add(npText);
		parameters.add(np);
		
		parameters.add(rhoText);
		parameters.add(rho);
		
		parameters.add(execute);
		parameters.add(stop);

		// picLabel = new JLabel();
		// panel.add(picLabel);
		
		frame.getContentPane().add(panel);

		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}
	
	//Métodos para debug
	static BufferedImage imageColor()
	{
		BufferedImage image = new BufferedImage(fieldSize, fieldSize, BufferedImage.TYPE_INT_RGB);
		
		for (int i = 0; i < fieldSize; ++i)
		{
			for (int j = 0; j < fieldSize; ++j)
			{
				int color = Color.BLUE.getRGB();
				
				switch (tumor.cellState(i, j))
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
				
				image.setRGB(j, i, color);
			}
		}
		
		return image;
	}
	
	public static void main(String[] args) throws Exception
	{
		terminate = false;
		
		GUI();
		
		execute.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				while (worker != null && !worker.isDone())
					terminate = true;
				
				fieldSize = Integer.parseInt(size.getText());
				fieldIt   = Integer.parseInt(it.getText());
				fieldPs   = Double.parseDouble(ps.getText());
				fieldPp   = Double.parseDouble(pp.getText());
				fieldPm   = Double.parseDouble(pm.getText());
				fieldNP   = Integer.parseInt(np.getText());
				fieldRho  = Integer.parseInt(rho.getText());
				
				tumor     = new TumorAutomaton(fieldSize);
				tumor.ps  = (float)fieldPs;
				tumor.pp  = (float)fieldPp;
				tumor.pm  = (float)fieldPm;
				tumor.np  = fieldNP;
				tumor.rho = fieldRho;
				
				tumor.cellState(fieldSize / 2, fieldSize / 2, TumorAutomaton.ALIVE);
				it_ = 0;
				tumor.threads(Runtime.getRuntime().availableProcessors());
				
				if (picLabel == null)
				{
					picLabel = new JLabel(new ImageIcon(imageColor()));
					panel.add(picLabel);
				}
				else
					picLabel.setIcon(new ImageIcon(imageColor()));
					
				panel.revalidate();
				panel.repaint();
				frame.pack();
				
				terminate = false;
				
				worker = new SwingWorker<Void, Void>()
				{
					public Void doInBackground()
					{
						BufferedImage canvas = new BufferedImage(fieldSize, fieldSize, BufferedImage.TYPE_INT_RGB);
						
						while (!terminate)
						{
							tumor.execute(fieldIt);
							it_ += fieldIt;
							
							image = imageColor();
							
							Graphics2D g = canvas.createGraphics();
							g.drawImage(image, 0, 0, null);
							g.drawString(it_ + "", 0, 10);
							
							picLabel.setIcon(new ImageIcon(canvas));
						}
						
						return null;
					}
					
					protected void done()
					{
						terminate = false;
					}
				};
				worker.execute();
			}
		});
		
		stop.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				terminate = true;
			}
		});
	}
}
