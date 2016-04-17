import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.Random;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

enum Estado
{
	MUERTA, LATENTE, VIVA, NUEVA, MIGRADA;
	
	public static Estado fromInteger(int i)
	{
		Estado e = MUERTA;
		
		switch (i)
		{
			case 1:
				e = LATENTE;
				break;
			
			case 2:
				e = VIVA;
				break;
			
			case 3:
				e = NUEVA;
				break;
				
			case 4:
				e = MIGRADA;
				break;
		}
		
		return e;
	}
}

public class TumorAutomata implements Runnable
{
	static public double ps;
	static public double pp;
	static public double pm;
	static public int    np;
	static public int    rho;
	
	static private RejillaEntera tejido_;
	static private int[][]  rhos_;
	static private byte[][] generacion_;
	static private int[][]  ph_;
	static private byte it_;
	static private int tam_;
	
	private static AtomicLong poblacion_;
	private int inicio_;
	private int fin_;
	private static int pasos_;
	
	private static int nucleos_;
	private ExecutorService threadPool_;
	private Runnable[] tareas_;
	private static CyclicBarrier barrera_;
	
	private Random random_;
	
	private static int[] limites_;
	private static ReentrantLock[] cerrojos_;
	private static Condition[] nadieLeyendo_;
	private static Condition[] nadieEscribiendo_;
	private static volatile int[] lectores_;
	private static volatile boolean[] escribiendo_;

	public TumorAutomata(int tam, double ps, double pp, double pm, int np, int rho)
	{
		random_     = new Random();
		tejido_     = new RejillaEntera(tam, tam);
		rhos_       = new int[tam][tam];
		generacion_ = new byte[tam][tam];
		ph_         = new int[tam][tam];
		tam_        = tam;
		this.rho    = rho;
		
		poblacion_ = new AtomicLong(0);
		
		this.ps = ps;
		this.pp = pp;
		this.pm = pm;
		this.np = np;
	}
	
	public TumorAutomata(int tam)
	{
		this(tam, 1, .25, .2, 1, 5);
	}
	
	private TumorAutomata(int inicio, int fin)
	{
		inicio_ = inicio;
		fin_    = fin;
		random_ = new Random();
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
		
		cerrojos_         = new ReentrantLock[n];
		nadieEscribiendo_ = new Condition[n];
		nadieLeyendo_     = new Condition[n];
		lectores_         = new int[n];
		escribiendo_      = new boolean[n];
		limites_          = new int[n];
		
		for (int i = 0; i < nucleos_; ++i)
		{
			int inicioIntervalo = i       * (tam_ / nucleos_);
			int finIntervalo    = (i + 1) * (tam_ / nucleos_);
			
			if ((i + 1) == nucleos_)
				finIntervalo = tam_;
			
			tareas_[i]  = new TumorAutomata(inicioIntervalo, finIntervalo);
			limites_[i] = finIntervalo;
			
			cerrojos_[i]         = new ReentrantLock();
			nadieEscribiendo_[i] = cerrojos_[i].newCondition();
			nadieLeyendo_[i]     = cerrojos_[i].newCondition();
		}
	}
	
	int empezarLectura(int x)
	{
		int particion = -1;
		
		if (limites_ != null)
		{
			for (int i = 0; i < nucleos_ && particion == -1; ++i)
				if (x < limites_[i])
					particion = i;
			
			//Comprobar que la coordenada esté dentro de los limites
			if (particion != -1)
			{
				cerrojos_[particion].lock();
				
				try
				{
					while (escribiendo_[particion])
						nadieEscribiendo_[particion].await();
					
					++lectores_[particion];
				}
				catch (InterruptedException e)
				{
					System.err.println("Intentando empezar lectura: " + e.getMessage());
				}
				finally
				{
					cerrojos_[particion].unlock();
				}
			}
		}
		
		return particion;
	}
	
	void terminarLectura(int particion)
	{
		if (particion != -1)
		{
			cerrojos_[particion].lock();
			
			try
			{
				--lectores_[particion];
				
				if (lectores_[particion] == 0)
					nadieLeyendo_[particion].signalAll();
			}
			finally
			{
				cerrojos_[particion].unlock();
			}
		}
	}
	
	int empezarEscritura(int x)
	{
		int particion = -1;
		
		if (limites_ != null)
		{
			for (int i = 0; i < nucleos_ && particion == -1; ++i)
				if (x < limites_[i])
					particion = i;
			
			if (particion != -1)
			{
				cerrojos_[particion].lock();
				
				try
				{
					while (escribiendo_[particion] || lectores_[particion] != 0)
						if (escribiendo_[particion])
							nadieEscribiendo_[particion].await();
						else
							nadieLeyendo_[particion].await();
					
					escribiendo_[particion] = true;
				}
				catch (InterruptedException e)
				{
					System.err.println("Intentando empezar escritura: " + e.getMessage());
				}
				finally
				{
					cerrojos_[particion].unlock();
				}
			}
		}
		
		return particion;
	}
	
	void terminarEscritura(int particion)
	{
		if (particion != -1)
		{
			cerrojos_[particion].lock();
			
			try
			{
				escribiendo_[particion] = false;
				
				nadieEscribiendo_[particion].signalAll();
			}
			finally
			{
				cerrojos_[particion].unlock();
			}
		}
	}

	public void cambiarEstado(int x, int y, Estado e)
	{
		int p = empezarEscritura(x);
		
		tejido_.set(x, y, e.ordinal());
		
		terminarEscritura(p);
	}
	
	public Estado verEstado(int x, int y)
	{
		int p = empezarLectura(x);
		
		try
		{
			return Estado.fromInteger(tejido_.get(x, y));
		}
		finally
		{
			terminarLectura(p);
		}
	}
	
	public void apoptosis(int x, int y)
	{
		if (verEstado(x, y) != Estado.MUERTA)
		{
			poblacion_.getAndDecrement();
			
			//Notificar a las células vecinas latentes
			for (int i = -1; i <= 1; ++i)
				for (int j = -1; j <= 1; ++j)
					if (i != 0 || j != 0)
						if (verEstado(x+i, y+j) == Estado.LATENTE)
							cambiarEstado(x+i, y+j, Estado.VIVA);
			
			cambiarEstado(x, y, Estado.MUERTA);
			rhos_[x][y] = 0;
		}
	}
	
	public void revivir(int x, int y)
	{
		if (verEstado(x, y) == Estado.MUERTA)
		{
			rhos_[x][y] = rho;
			poblacion_.getAndIncrement();
		}
		
		
		//Se contempla la posibilidad de revivir incluso estando ya viva
		//por si acaso estaba LATENTE, MIGRADA o NUEVA
		cambiarEstado(x, y, Estado.VIVA);
	}
	
	public void proliferar(int x1, int y1, int x2, int y2)
	{
		if (verEstado(x2, y2) == Estado.MUERTA)
		{
			poblacion_.getAndIncrement();
			cambiarEstado(x2, y2, Estado.NUEVA);
			generacion_[x2][y2] = (byte)((it_ + 1) % 2);
			
			if (--rhos_[x1][y1] <= 0)
				cambiarEstado(x1, y1, Estado.MUERTA);
			
			rhos_[x2][y2] = rho;
		}
	}
	
	public void migrar(int x1, int y1, int x2, int y2)
	{
		cambiarEstado(x1, y1, Estado.MUERTA);
		cambiarEstado(x2, y2, Estado.MIGRADA);
		generacion_[x2][y2] = (byte)((it_ + 1) % 2);
		
		rhos_[x2][y2] = rhos_[x1][y1];
		rhos_[x1][y1] = 0;
	}

	void actualizarCelda(int x, int y)
	{
		Estado estadoActual = verEstado(x, y);
		
		//Si está muerta no hacemos nada. Si está latente, comprobamos
		//únicamente si muere espontáneamente, pero nada más
		//Si están recién creadas, tampoco deberían procesarse, pero sí
		//aumentar su edad .
		if (estadoActual != Estado.MUERTA)
		{			
			if (generacion_[x][y] == it_)
			{
				//Comprobar si sobrevive
				if (comprobarSupervivencia())
				{
					//Sobrevive.
					if (estadoActual != Estado.LATENTE)
					{
						//Solo tiene sentido si se trabaja con dos matrices para
						//no mezclar generaciones, o si se usa la interfaz
						//multicolor.
						revivir(x, y);
						
						//No está latente. Comprobar si prolifera o migra.
						boolean prolifera = comprobarProliferacion(x, y);

						if (prolifera || comprobarMigracion(x, y))
						{
							//Actualizar posiciones
							float denominador =  0;
							int cont  = 0;
							int[] n   = new int[8];
							float[] p = new float[8];
							
							//Calcular la cantidad de células vivas vecina
							for (int i = -1; i <= 1; ++i)
								for (int j = -1; j <= 1; ++j)
									if (i != 0 || j != 0)
									{
										//n[cont] = !tejido_.get(x+i, y+j) ? 1:0;
										n[cont] = verEstado(x+i, y+j) == Estado.MUERTA ? 1:0;
										denominador += n[cont++];
										//System.out.println(verEstado(x+i, y+j).ordinal());
									}
							
							//Si el denominador es 0, implica que todas las
							//células vecinas están vivas. En ese caso, la
							//marcamos como LATENTE y no hacemos nada más.
							if (denominador == 0)
								cambiarEstado(x, y, Estado.LATENTE);
							else
							{						
								//Calcular la probabilidad de proliferar o
								//migrar a cada celda vecina
								p[0] = n[0]/denominador;
								for (int i = 1; i < 8; ++i)
									p[i] = n[i]/denominador + p[i-1];
								
								//Seleccionamos posición aleatoriamente
								float r = random_.nextFloat();
								
								cont = 0;
								boolean continuar = true;
								for (int i = -1; i <= 1 && continuar; ++i)
									for (int j = -1; j <= 1 && continuar; ++j)
									{
										if ((i != 0 || j != 0) && r < p[cont++])
										{
											//Proliferamos o migramos a la posición
											//seleccionada
											if (prolifera)
												proliferar(x, y, x + i, y + j);
											else
												migrar(x, y, x + i, y + j);
																					
											continuar = false;
										}
										//System.out.println(p[cont-1]);
									}
							}
						}
					}
				}
				else
				{
					//No sobrevive
					apoptosis(x, y);
				}
			}
			else
				generacion_[x][y] = (byte)((it_ + 1) % 2);
		}
	}

	boolean comprobarSupervivencia()
	{
		return random_.nextDouble() < ps;
	}

	boolean comprobarVecindadLibre(int x, int y)
	{
		return verEstado(x, y) != Estado.LATENTE;
	}

	boolean comprobarProliferacion(int x, int y)
	{
		boolean prolifera = false;

		if (random_.nextDouble() < pp)
		{
			++ph_[x][y];

			//Si hay suficientes señales para proliferar y hay al menos
			//un hueco libre en la vecindad, se prolifera
			prolifera = ph_[x][y] >= np && comprobarVecindadLibre(x, y);
		}

		return prolifera;
	}

	boolean comprobarMigracion(int x, int y)
	{
		return random_.nextDouble() < pm && comprobarVecindadLibre(x, y);
	}

	public int size()
	{
		return tam_;
	}
	
	public RejillaEntera tejido()
	{
		return tejido_;
	}
	
	public void tejido(RejillaEntera tejido)
	{
		tejido_ = tejido;
	}
	
	public void siguienteGeneracion()
	{
		for (int i = 0; i < tam_; ++i)
			for (int j = 0; j < tam_; ++j)
				actualizarCelda(i, j);
		/*
		 * ¿Tiene sentido realmente no sobreescribir la matriz que
		 * leemos? Si la célula vive o no, no depende de la vecindad 
		 * sino de probabilidades. La proliferación y la migración
		 * dependen de los huecos. Si una célula decide proliferar a una
		 * celda vecina, una célula aledaña no podrá migrar allí. Para
		 * ello hay que contemplar los cambios que se están haciendo en
		 * tiempo real
		 */
		it_ = (byte)((it_ + 1) % 2);
	}
	
	public void ejecutar(int nGeneraciones)
	{
		if (threadPool_ == null)
			for (int i = 0; i < nGeneraciones; ++i)
				siguienteGeneracion();
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
	
	protected void finalize()
	{
		this.terminar();
	}
	
	public void reiniciar()
	{
		tejido_ = new RejillaEntera(tam_, tam_);
	}
}
