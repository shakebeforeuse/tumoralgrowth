import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import javax.imageio.ImageIO;

public class RejillaBinaria
{
	private int[][] rejilla_;
	private int[]   dimensiones_;

	RejillaBinaria(int xtam, int ytam)
	{
		rejilla_     = new int[xtam][ytam];
		dimensiones_ = new int[]{xtam, ytam};
	}

	boolean get(int x, int y)
	{
		//Si no existe devolveremos verdadero. Que en la frontera se
		//asuman células tumorales no afecta al resultado, y de esta
		//forma se simplifican cálculos a la hora de proliferar en
		//la frontera.
		int celda = 1;
		if (0 <= x && x < dimensiones_[0]
		 && 0 <= y && y < dimensiones_[1])
			celda = rejilla_[x][y];

		return celda > 0;
	}

	void set(int x, int y, boolean v)
	{
		set(x, y, v? 1:0);
	}
	
	void set(int x, int y, int v)
	{
		if (0 <= x && x < dimensiones_[0]
		 && 0 <= y && y < dimensiones_[1])
			rejilla_[x][y] = v;
	}
	
	BufferedImage imagen()
	{
		BufferedImage imagen = new BufferedImage(dimensiones_[1], dimensiones_[0], BufferedImage.TYPE_BYTE_BINARY);
		
		for (int i = 0; i < dimensiones_[0]; ++i)
			for (int j = 0; j < dimensiones_[1]; ++j)
				imagen.setRGB(j, i, rejilla_[i][j] > 0? 0:0xffffff);
		
		return imagen;
	}
	
	BufferedImage imagenColor()
	{
		BufferedImage imagen = new BufferedImage(dimensiones_[1], dimensiones_[0], BufferedImage.TYPE_INT_RGB);
		
		for (int i = 0; i < dimensiones_[0]; ++i)
		{
			for (int j = 0; j < dimensiones_[1]; ++j)
			{
				if (rejilla_[i][j] == 0)
					imagen.setRGB(j, i, Color.GRAY.getRGB());
				else
				{
					int color;
					
					switch (rejilla_[i][j])
					{
						case 2:
							color = Color.RED.getRGB();
							break;
						
						case 3:
							color = Color.GREEN.getRGB();
							break;
						
						default:
							color = Color.BLACK.getRGB();
					}
					
					imagen.setRGB(j, i, color);
					
				}
			}
		}
		
		return imagen;
	}
	
	void guardarImagen(String nombre)
	{
		BufferedImage imagen = imagen();
		
		for (int i = 0; i < dimensiones_[0]; ++i)
			for (int j = 0; j < dimensiones_[1]; ++j)
				imagen.setRGB(j, i, rejilla_[i][j] > 0? 0:0xffffff);
		
		File salida = new File(nombre + ".png");
		try
		{
			ImageIO.write(imagen, "png", salida);
		}
		catch (IOException e)
		{
			System.err.println("IOException: " + e.getMessage());
		}
	}
	
	void guardarPuntos(String nombre)
	{
		try
		{
			PrintWriter salida = new PrintWriter(nombre + ".tmp");
			
			salida.println("# x\ty");
			salida.println("0\t0\n" + dimensiones_[1] + "\t" + dimensiones_[0]);
			
			for (int i = 0; i < dimensiones_[0]; ++i)
				for (int j = 0; j < dimensiones_[1]; ++j)
					if (rejilla_[i][j] > 0)
						salida.println(j + "\t" + i);
			
			salida.close();
		}
		catch (FileNotFoundException e)
		{
			System.err.println("Error: " + nombre + ".tmp: El fichero no existe y no puede ser creado");
		}
	}
}
