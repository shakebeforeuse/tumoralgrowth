import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import javax.imageio.ImageIO;

public class RejillaEntera
{
	private int[][] rejilla_;
	private int[]   dimensiones_;

	RejillaEntera(int xtam, int ytam)
	{
		rejilla_     = new int[xtam][ytam];
		dimensiones_ = new int[]{xtam, ytam};
	}

	int get(int x, int y)
	{
		//Si no existe devolveremos verdadero. Que en la frontera se
		//asuman células tumorales no afecta al resultado, y de esta
		//forma se simplifican cálculos a la hora de proliferar en
		//la frontera.
		int celda = 1;
		if (0 <= x && x < dimensiones_[0]
		 && 0 <= y && y < dimensiones_[1])
			celda = rejilla_[x][y];

		return celda;
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
				int color = Color.BLUE.getRGB();
				
				switch (rejilla_[i][j])
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
	
	protected RejillaEntera clone()
	{
		RejillaEntera copia = new RejillaEntera(dimensiones_[0], dimensiones_[1]);
		
		for (int i = 0; i < dimensiones_[0]; ++i)
			for (int j = 0; j < dimensiones_[1]; ++j)
				copia.rejilla_[i][j] = rejilla_[i][j];
				
		copia.dimensiones_[0] = dimensiones_[0];
		copia.dimensiones_[1] = dimensiones_[1];
		
		return copia;
	}
}
