import java.awt.image.BufferedImage;
import java.awt.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import javax.imageio.ImageIO;

public class RejillaBinaria
{
	private boolean[][] rejilla_;
	private int[] dimensiones_;

	RejillaBinaria(int xtam, int ytam)
	{
		rejilla_     = new boolean[xtam][ytam];
		dimensiones_ = new int[]{xtam, ytam};
	}

	boolean get(int x, int y)
	{
		//Si no existe devolveremos falso. Que la frontera se asuman células
		//tumorales no afecta al resultado, y de esta forma se simplifican
		//cálculos a la hora de proliferar en la frontera.
		boolean celda = true;
		if (0 <= x && x < dimensiones_[0]
		 && 0 <= y && y < dimensiones_[1])
			celda = rejilla_[x][y];

		return celda;
	}

	void set(int x, int y, boolean v)
	{
		if (0 <= x && x < dimensiones_[0]
		 && 0 <= y && y < dimensiones_[1])
			rejilla_[x][y] = v;
	}
	
	void guardarImagen(String nombre)
	{
		BufferedImage imagen = new BufferedImage(dimensiones_[1], dimensiones_[0], BufferedImage.TYPE_BYTE_BINARY);
		
		for (int i = 0; i < dimensiones_[0]; ++i)
			for (int j = 0; j < dimensiones_[1]; ++j)
				imagen.setRGB(j, i, rejilla_[i][j]? 0:0xffffff);
		
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
					if (rejilla_[i][j])
						salida.println(j + "\t" + i);
			
			salida.close();
		}
		catch (FileNotFoundException e)
		{
			System.err.println("Error: " + nombre + ".tmp: El fichero no existe y no puede ser creado");
		}
	}
}
