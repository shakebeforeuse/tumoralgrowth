import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import javax.imageio.ImageIO;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class RejillaEntera
{
	private int[][] rejilla_;
	private int[]   dimensiones_;
	
	private ReentrantLock cerrojo_;
	private Condition nadieLeyendo_;
	private Condition nadieEscribiendo_;
	
	private volatile int lectores_;
	private volatile boolean escribiendo_;

	RejillaEntera(int xtam, int ytam)
	{
		rejilla_     = new int[xtam][ytam];
		dimensiones_ = new int[]{xtam, ytam};
		
		lectores_    = 0;
		escribiendo_ = false;
		
		cerrojo_          = new ReentrantLock();
		nadieLeyendo_     = cerrojo_.newCondition();
		nadieEscribiendo_ = cerrojo_.newCondition();
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
		{
			empezarLectura();
			celda = rejilla_[x][y];
			terminarLectura();
		}

		return celda;
	}
	
	void set(int x, int y, int v)
	{
		if (0 <= x && x < dimensiones_[0]
		 && 0 <= y && y < dimensiones_[1])
		{
			empezarEscritura();
			rejilla_[x][y] = v;
			terminarEscritura();
		}
	}
	
	
	
	
	void empezarLectura()
	{
		cerrojo_.lock();
		
		try
		{
			while (escribiendo_)
				nadieEscribiendo_.await();
			
			++lectores_;
		}
		catch (InterruptedException e)
		{
			System.out.println("Intentando empezar lectura: " + e.getMessage());
		}
		finally
		{
			cerrojo_.unlock();
		}
	}
	
	void terminarLectura()
	{
		cerrojo_.lock();
		
		try
		{
			--lectores_;
			
			if (lectores_ == 0)
				nadieLeyendo_.signalAll();
		}
		finally
		{		
			cerrojo_.unlock();
		}
	}
	
	void empezarEscritura()
	{
		cerrojo_.lock();
		
		try
		{
			while (escribiendo_ || lectores_ != 0)
			{
				if (escribiendo_)
					nadieEscribiendo_.await();
				else
					nadieLeyendo_.await();
			}
			
			escribiendo_ = true;
		}
		catch (InterruptedException e)
		{
			System.out.println("Intentando empezar lectura: " + e.getMessage());
		}
		finally
		{	
			cerrojo_.unlock();
		}
	}
	
	void terminarEscritura()
	{
		cerrojo_.lock();
		
		try
		{
			escribiendo_ = false;
			nadieEscribiendo_.signalAll();
		}
		finally
		{
			cerrojo_.unlock();
		}
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
