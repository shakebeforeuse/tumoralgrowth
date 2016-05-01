import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.Image;

public class TumorAutomata implements Runnable
{
	public static final int MUERTA = 0, LATENTE = 1, VIVA = 2, NUEVA = 3, MIGRADA = 4;
	
	public static double ps;
	public static double pp;
	public static double pm;
	public static int    np;
	public static int    rho;
	
	private static int[][]  tejido_;
	private static int[][]  rhos_;
	private static byte[][] generacion_;
	private static int[][]  ph_;
	private static byte it_;
	private static int  tam_;
	
	//~ private static AtomicLong poblacion_;
	private int inicio_;
	private int fin_;
	private static int pasos_;
	
	private static int nucleos_;
	private ExecutorService threadPool_;
	private Runnable[] tareas_;
	private static CyclicBarrier barrera_;
	private static ReentrantLock cerrojo_;
	
	private Random random_;	
	
	public TumorAutomata(int tam)
	{
		tam_        = tam;
		tejido_     = new int[tam][tam];
		ph_         = new int[tam][tam];
		rhos_       = new int[tam][tam];
		generacion_ = new byte[tam][tam];
		random_     = new Random();
		cerrojo_    = new ReentrantLock();
	}
	
	private TumorAutomata(int inicio, int fin)
	{
		random_ = new Random();
		inicio_ = inicio;
		fin_    = fin;
	}
	
	public void terminar()
	{	
		while (threadPool_ != null && !threadPool_.isTerminated())
			threadPool_.shutdown();
	}
	
	public void nucleos(int n)
	{
		this.terminar();
		
		nucleos_    = n;
		threadPool_ = Executors.newFixedThreadPool(nucleos_);
		barrera_    = new CyclicBarrier(nucleos_ + 1);
		tareas_     = new Runnable[nucleos_];
		
		for (int i = 0; i < nucleos_; ++i)
		{
			int inicioIntervalo = i       * (tam_ / nucleos_);
			int finIntervalo    = (i + 1) * (tam_ / nucleos_);
			
			if ((i + 1) == nucleos_)
				finIntervalo = tam_;
			
			tareas_[i] = new TumorAutomata(inicioIntervalo, finIntervalo);
		}
	}
	
	public void ejecutar(int nGeneraciones)
	{
		//Si no hay pool de threads, ejecutar secuencialmente
		if (threadPool_ == null)
			for (int k = 0; k < nGeneraciones; ++k)
			{
				for (int i = 0; i < tam_; ++i)
					for (int j = 0; j < tam_; ++j)
						actualizarCelda(i, j);
				
				it_ = (byte)((it_ + 1) % 2);		
			}
		else
		{
			pasos_ = nGeneraciones;
			
			for (int i = 0; i < tareas_.length; ++i)
				threadPool_.execute(tareas_[i]);
				
			for (int k = 0; k < nGeneraciones; ++k)
			{
				try
				{					
					barrera_.await();
					it_ = (byte)((it_ + 1) % 2);
				}
				catch (BrokenBarrierException e)
				{
					System.err.println("BrokenBarrierException: " + e.getMessage());
				}
				catch (InterruptedException e)
				{
					System.err.println("InterruptedException: " + e.getMessage());
				}
			}
		}
	}
	
	public void run()
	{
		for (int k = 0; k < pasos_; ++k)
		{
			try
			{
				for (int i = inicio_; i < fin_; ++i)
					for (int j = 0; j < tam_; ++j)
						actualizarCelda(i, j);
			
				barrera_.await();
			}
			catch (BrokenBarrierException e)
			{
				System.err.println("BrokenBarrierException: " + e.getMessage());
			}
			catch (InterruptedException e)
			{
				System.err.println("InterruptedException: " + e.getMessage());
			}
		}
	}
	
	void reiniciar()
	{
		tejido_     = new int[tam_][tam_];
		ph_         = new int[tam_][tam_];
		rhos_       = new int[tam_][tam_];
		generacion_ = new byte[tam_][tam_];
		it_         = 0;
	}
	
	int verEstado(int x, int y)
	{
		if (0 <= x && x < tam_ && 0 <= y && y < tam_)
			return tejido_[x][y];

		return LATENTE;
	}
	
	void cambiarEstado(int x, int y, int v)
	{
		if (0 <= x && x < tam_ && 0 <= y && y < tam_)
			tejido_[x][y] = v;
	}
	
	public void actualizarCelda(int x, int y)
	{
		//Comprobar si está viva y debe procesarse (generacion actual)
		if (verEstado(x, y) != MUERTA && generacion_[x][y] == it_)
		{
			generacion_[x][y] = (byte)((it_ + 1) % 2);
			
			//Comprobar si sobrevive
			if (random_.nextFloat() < ps)
			{
				//Si está latente, no hacemos nada
				if (verEstado(x, y) != LATENTE)
				{
					//Para el color
					cambiarEstado(x, y, VIVA);
					
					//Comprobar si prolifera
					boolean prolifera = random_.nextFloat() < pp && ++ph_[x][y] >= np;
					
					//Si prolifera o migra
					if (prolifera || random_.nextFloat() < pm)
					{
						//Calcular dirección
						float denominador =  0;
						
						int[] n   = new int[8];
						float[] p = new float[8];
						
						if (x == inicio_ || x == fin_)
							cerrojo_.lock();
						
						//Calcular la cantidad de células vivas vecina			
						n[0] = verEstado(x - 1, y - 1) == MUERTA ? 1:0;
						n[1] = verEstado(x - 1, y)     == MUERTA ? 1:0;
						n[2] = verEstado(x - 1, y + 1) == MUERTA ? 1:0;
						n[3] = verEstado(x, y - 1)     == MUERTA ? 1:0;
						n[4] = verEstado(x, y + 1)     == MUERTA ? 1:0;
						n[5] = verEstado(x + 1, y - 1) == MUERTA ? 1:0;
						n[6] = verEstado(x + 1, y)     == MUERTA ? 1:0;
						n[7] = verEstado(x + 1, y + 1) == MUERTA ? 1:0;
						
						denominador = n[0] + n[1] + n[2] + n[3] + n[4] + n[5] + n[6] + n[7];
						
						
						//~ //Si el denominador es 0, implica que todas las
						//~ //células vecinas están vivas. En ese caso, la
						//~ //marcamos como LATENTE y no hacemos nada más.
						if (denominador == 0)
							cambiarEstado(x, y, LATENTE);
						else
						//~ if (denominador != 0)
						{
							//Calcular la probabilidad de proliferar o
							//migrar a cada celda vecina
							p[0] = n[0]/denominador;
							p[1] = n[1]/denominador + p[0];
							p[2] = n[2]/denominador + p[1];
							p[3] = n[3]/denominador + p[2];
							p[4] = n[4]/denominador + p[3];
							p[5] = n[5]/denominador + p[4];
							p[6] = n[6]/denominador + p[5];
							p[7] = n[7]/denominador + p[6];
							
							
							//Seleccionamos posición aleatoriamente
							float r = random_.nextFloat();
							
							int cont = 0;
							boolean continuar = true;
							for (int i = -1; i <= 1 && continuar; ++i)
								for (int j = -1; j <= 1 && continuar; ++j)
									if ((i != 0 || j != 0) && r < p[cont++])
									{
										if (x+i == inicio_ || x+i == fin_)
											cerrojo_.lock();
											
										//Proliferamos o migramos a la posición
										//seleccionada
										if (prolifera)
										{										
											cambiarEstado(x + i, y + j, NUEVA);
											
											rhos_[x+i][y+j] = rho;
											if (--rhos_[x][y] == 0)
												cambiarEstado(x, y, MUERTA);
										}
										else
										{
											cambiarEstado(x, y, MUERTA);
											cambiarEstado(x + i, y + j, MIGRADA);
											
											rhos_[x+i][y+j] = rhos_[x][y];
											rhos_[x][y]     = 0;
										}
										
										//Marcamos la posición seleccionada para ser
										//procesada en la siguiente generación
										generacion_[x+i][y+j] = (byte)((it_ + 1) % 2);
											
										continuar = false;
										
										if (x+i == inicio_ || x+i == fin_)
											cerrojo_.unlock();
									}
						}
						
						if (x == inicio_ || x == fin_)
							cerrojo_.unlock();
					}
				}
			}
			else
			{
				if (x == inicio_ || x == fin_)
					cerrojo_.lock();
							
				cambiarEstado(x, y, MUERTA);
				
				for (int i = -1; i <= 1; ++i)
					for (int j = -1; j <= 1; ++j)
						if ((i != 0 || j != 0) && verEstado(x+i, y+j) == LATENTE)
							cambiarEstado(x+i, y+j, VIVA);
				
				if (x == inicio_ || x == fin_)
					cerrojo_.unlock();
			}
		}
	}
}
