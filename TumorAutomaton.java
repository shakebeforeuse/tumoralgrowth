package tumoralgrowthautomaton;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Modelo de simulación tumoral mediante autómata celular.
 * @author Manuel Francisco
 * @version 2.0 06/2016
 */
public class TumorAutomaton implements Runnable
{
	//Cell states
	/** Estado de una célula muerta. */
	public static final byte DEAD = 0;
	/** Estado de una célula latente. */
	public static final byte DORMANT = 1;
	/** Estado de una célula viva. */
	public static final byte ALIVE = 2;
	/** Estado de una célula recién creada. */
	public static final byte NEW = 3;
	/** Estado de una célula que acaba de migrar. */
	public static final byte MIGRATED = 4;
	
	//Tumoral growth simulation parameters
	/** Probabilidad de supervivencia. */
	public static double ps;
	/** Probabilidad de proliferación. */
	public static double pp;
	/** Probabilidad de migración. */
	public static double pm;
	/** Número de señales necesarias para proliferar. */
	public static byte    np;
	/** Número de veces que una célula puede proliferar sin morir. */
	public static byte    rho;
	
	//CA grid
        /** Dominio tisular. */
	private static byte[][] tissue_;
        /** Tamaño del dominio tisular. */
	private static int     size_;
	
	//No. of times each cell can proliferatete
        /** Número de veces que puede proliferar cada célula. */
	private static byte[][] rhos_;
	
	//No. of times each cell has been signaled for proliferatetion
        /** Número de señales de proliferación de cada célula. */
	private static byte[][] ph_;
	
	//To avoid processing a new cell in the same generation
        /** Control de generaciones. */
	private static byte[][] generation_;
        /** Etiqueta de la generación actual. */
	private        byte      it_;
	
	//Dynamic domain
        /** Inicio del dominio de ejecución. */
	volatile static int[] domainBegin_;
        
        /** Fin del dominio de ejecución. */
	volatile static int[] domainEnd_;
	
	//Paralelism
	//Partition
        /** Contador de índices de hilos. */
	private static AtomicInteger threadIndex_;
        
        /** Índice del hilo. */
	private        int index_;
	
	//No. of generations to compute
        /** Pasos de tiempo discreto a ejecutar. */
	private static int steps_;
	
	//Number of threads we will have and array of tasks.
        /** Número de tareas. */
	private static int              threads_;
        /** Colección de tareas. */
	private        TumorAutomaton[] tasks_;
	
	//Pool of threads
        /** Ejecutor. */
	private ExecutorService threadPool_;
	
	//Synchronization
        /** Barrera de sincronización. */
	private static CyclicBarrier barrier_;
        
        /** Colección de cerrojos. */
	private static ReentrantLock[] locks_;
	
	//Non-static random number generaton (to avoid thread-safety)
        /** Generador de números aleatorios. */
	private Random random_;	
	
	/**
	 * Crea un nuevo autómata celular con el tamaño especificado.
	 * @param size Tamaño del dominio tisular.
	 */
	public TumorAutomaton(int size)
	{
		size_       = size;
		tissue_     = new byte[size_][size_];
		ph_         = new byte[size_][size_];
		rhos_       = new byte[size_][size_];
		generation_ = new byte[size_][size_];
		random_     = new Random();
		
		threadIndex_ = new AtomicInteger(0);
		
		domainBegin_    = new int[2];
		domainEnd_      = new int[2];
		domainBegin_[0] = size;
		domainBegin_[1] = size;
		domainEnd_[0]   = 0;
		domainEnd_[1]   = 0;
	}
	
	/**
	 * Constructor de tareas.
	 */
	private TumorAutomaton()
	{
		random_ = new Random();
		index_  = threadIndex_.getAndIncrement();
	}
	
	/**
         * Intenta parar el ejecutor y espera hasta que este haya terminado.
	 */
	public void shutdown()
	{	
		while (threadPool_ != null && !threadPool_.isTerminated())
			threadPool_.shutdown();
	}
	
	/**
	 * Configura el autómata para ser ejecutado en paralelo.
         * Si se especifica 0, el autómata se ejecutará secuencialmente.
	 * @param n Número de tareas a ejecutar
	 */
	public void threads(int n)
	{
		//Wait until the tasks are finished, if there is any
		this.shutdown();
		
		//Set parameters and build a pool for these tasks
		threads_    = n;
		
		//If 0, set the CA to run sequentially
		if (n == 0)
			threadPool_ = null;
		else
		{
			threadPool_ = Executors.newFixedThreadPool(threads_);
			barrier_    = new CyclicBarrier(threads_ + 1);
			locks_      = new ReentrantLock[threads_ - 1];
			tasks_      = new TumorAutomaton[threads_];
			
			//Create tasks and locks
			for (int i = 0; i < threads_; ++i)
				tasks_[i] = new TumorAutomaton();
				
			for (int i = 0; i < locks_.length; ++i)
				locks_[i] = new ReentrantLock();
		}
	}
	
	/**
         * Ejecuta el autómata durante un número determinado de generaciones.
	 * @param nGenerations Número de generaciones a computar
	 */
	public void execute(int nGenerations)
	{
		//Run sequentially if there is no pool of threads
		if (threadPool_ == null)
			//Iterate k times over the whole grid, updating cells
			for (int k = 0; k < nGenerations; ++k)
			{
				//Change iteration direction, to avoid distortion
				if (it_ == 0)
					for (int i = domainBegin_[0]; i < domainEnd_[0]; ++i)
						for (int j = domainBegin_[1]; j < domainEnd_[1]; ++j)
							updateCell(i, j);
				else
					for (int i = domainEnd_[0] - 1; i >= domainBegin_[0]; --i)
						for (int j = domainEnd_[1] - 1; j >= domainBegin_[1]; --j)
							updateCell(i, j);
				
				it_ = (byte)((it_ + 1) % 2);
			}
		else
		{
			//Set number of iterations
			steps_ = nGenerations;
			
			//Run tasks
			for (int i = 0; i < tasks_.length; ++i)
				threadPool_.execute(tasks_[i]);
			
			//Wait until all threads are done, k times. Synchronization.
			for (int k = 0; k < nGenerations; ++k)
			{
				try
				{
					barrier_.await();
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
	
	/**
	 * Metodo ejecutable. Calcula el número de generaciones especificado
         * en el atributo 'steps_', dentro de la partición del hilo que lo
         * ejecute.
	 */
	public void run()
	{
		for (int k = 0; k < steps_; ++k)
		{
			int delta = (domainEnd_[0] - domainBegin_[0]) / threads_;
			
			int startX = domainBegin_[0] + index_       * delta;			
			int endX   = domainBegin_[0] + (index_ + 1) * delta;
			
			if (index_ + 1 == threads_)
				endX = domainEnd_[0];
			
			//Not the same than using domain{Begin|End}_ in the loop.
			//This avoid looping through just added cells
			int startY = domainBegin_[1];
			int endY   = domainEnd_[1];
			
			//Change iteration direction, to avoid distortion
			if (it_ == 0)
				for (int i = startX; i < endX; ++i)
				{
					if (index_ != 0 && i < startX + 2)
						locks_[index_ - 1].lock();
						
					if (index_ != threads_ - 1 && i >= endX - 2)
						locks_[index_].lock();
					
					try
					{
						for (int j = startY; j < endY; ++j)
								updateCell(i, j);
					}
					finally
					{
						if (index_ != 0 && i < startX + 2)
							locks_[index_ - 1].unlock();
						
						if (index_ != threads_ - 1 && i >= endX - 2)
							locks_[index_].unlock();
					}
				}
			else
				for (int i = endX - 1; i >= startX; --i)
				{
					if (index_ != 0 && i < startX + 2)
						locks_[index_ - 1].lock();
						
					if (index_ != threads_ - 1 && i >= endX - 2)
						locks_[index_].lock();
					
					try
					{
						for (int j = endY - 1; j >= startY; --j)
								updateCell(i, j);
					}
					finally
					{
						if (index_ != 0 && i < startX + 2)
							locks_[index_ - 1].unlock();
						
						if (index_ != threads_ - 1 && i >= endX - 2)
							locks_[index_].unlock();
					}
				}
			
			try
			{
				//Synchronization
				barrier_.await();
				it_ = (byte) ((it_ + 1) % 2);
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
	
	/**
	 * Reinicia el autómata celular.
	 */
	void reset()
	{
		tissue_     = new byte[size_][size_];
		ph_         = new byte[size_][size_];
		rhos_       = new byte[size_][size_];
		generation_ = new byte[size_][size_];
		
		domainBegin_[0] = size_;
		domainBegin_[1] = size_;
		domainEnd_[0]   = 0;
		domainEnd_[1]   = 0;
		
		threadIndex_.set(0);
		
		it_ = 0;
		for (int i = 0; tasks_ != null && i < tasks_.length; ++i)
			tasks_[i].it_ = 0;
	}	
	
	/**
         * Devuelve el estado de la célula (x, y).
	 * @param x Coordenada en el eje x.
	 * @param y Coordenada en el eje y.
	 * @return Estado de la célula (x, y).
	 */
	int cellState(int x, int y)
	{
		//Check if it is within bounds
		if (0 <= x && x < size_ && 0 <= y && y < size_)
			return tissue_[x][y];

		//If not, return ALIVE. CA won't proliferatete or migrate if it
		//thinks the cell is alive, and it does not alter CA behavior.
		return ALIVE;
	}
	
	/**
	 * Modifica el estado de la célula (x, y).
	 * @param x Coordenada en el eje x.
	 * @param y Coordenada en el eje y.
	 * @param v Nuevo estado de la célula (x, y).
	 */
	void cellState(int x, int y, byte v)
	{
		//Check if it is within bounds. Do nothing if not.
		if (0 <= x && x < size_ && 0 <= y && y < size_)
		{
			if (domainBegin_[0] > x)
				domainBegin_[0] = Math.max(x - 1, 0);
			if (domainBegin_[1] > y)
				domainBegin_[1] = Math.max(y - 1, 0);
				
			if (domainEnd_[0] <= x)
				domainEnd_[0] = Math.min(x + 1, size_);
			if (domainEnd_[1] <= y)
				domainEnd_[1] = Math.min(y + 1, size_);
			
			tissue_[x][y] = v;
		}
	}
	
	/**
         * Despierta a las celulas latentes de la vecindad de (x, y)
	 * @param x Coordenada en el eje x.
	 * @param y Coordenada en el eje y.
	 */
	void awakeNeighbourhood(int x, int y)
	{
		for (int i = -1; i <= 1; ++i)
			for (int j = -1; j <= 1; ++j)
				if (0 <= x+i && x+i < size_ && 0 <= y+j && y+j < size_)
					if ((i != 0 || j != 0) && cellState(x+i, y+j) == DORMANT)
					{
						cellState(x+i, y+j, ALIVE);
						generation_[x+i][y+j] = (byte)((it_ + 1) % 2);
					}
	}
	
	/**
	 * Ejecuta la regla del autómata para la célula (x, y).
	 * @param x Coordenada en el eje x.
	 * @param y Coordenada en el eje y.
	 */
	public void updateCell(int x, int y)
	{
		//Check if ALIVE and whether should be processed or not (current generation?)
		if (cellState(x, y) != DEAD && generation_[x][y] == it_)
		{
			//Mark to be computed in the next generation
			generation_[x][y] = (byte)((it_ + 1) % 2);
			
			//Check if survives
			if (random_.nextFloat() < ps)
			{
				//If DORMANT, do nothing. There is no room in the
				//neighbourhood
				if (cellState(x, y) != DORMANT)
				{
					//Change state to alive (just for correct color in GUI)
					cellState(x, y, ALIVE);
					
					//Check proliferatetion
					boolean proliferate = random_.nextFloat() < pp && ++ph_[x][y] >= np;
					
					//Check whether proliferates or migrates
					if (proliferate || random_.nextFloat() < pm)
					{
						//Compute next position
						float denom =  0;
						
						int[] n   = new int[8];
						float[] p = new float[8];
						
						//Compute no. of alive neighbours
						int count = 0;
						for (int i = -1; i <= 1; ++i)
							for (int j = -1; j <= 1; ++j)
								if (i != 0 || j != 0)
								{
									n[count] = cellState(x + i, y + j) <= DEAD ? 1:0;
									denom += n[count++];
								}
						
						
						//denom == 0 means that neighbourhood is full.
						//Mark as DORMANT and stop updating this cell.
						if (denom == 0)
							cellState(x, y, DORMANT);
						else
						{
							//Compute probability of selecting each
							//neighbour cell.
							p[0] = n[0]/denom;
							for (int i = 1; i < 8; ++i)
								p[i] = n[i]/denom + p[i-1];
							
							
							//Select position, randomly
							float r = random_.nextFloat();
							
							int cont = 0;
							boolean continueIt = true;
							for (int i = -1; i <= 1 && continueIt; ++i)
								for (int j = -1; j <= 1 && continueIt; ++j)
									if ((i != 0 || j != 0) && r < p[cont++])
									{
										//Proliferate (or migrate) to the specified cell
										if (proliferate)
										{
											//New cell				
											cellState(x + i, y + j, NEW);
											
											//Set proliferation signals to 0.
											ph_[x+i][y+j] = 0;
											
											//Reset no. of proliferations remaining until death.
											rhos_[x+i][y+j] = rho;
											
											//Kill the cell if it has reached the limit.
											if (--rhos_[x][y] == 0)
											{
												cellState(x, y, DEAD);
												awakeNeighbourhood(x, y);
											}
										}
										else
										{
											//Move to the selected position
											cellState(x, y, DEAD);
											cellState(x + i, y + j, MIGRATED);
											awakeNeighbourhood(x, y);
											
											//Move no. of proliferation signals
											ph_[x+i][y+j] = ph_[x][y];
											ph_[x][y]     = 0;
											
											//Move no. of proliferations remaining.
											rhos_[x+i][y+j] = rhos_[x][y];
											rhos_[x][y]     = 0;
										}
										
										//Mark to be processed in the next iteration
										generation_[x+i][y+j] = (byte)((it_ + 1) % 2);
										
										//Stop iteration (position already chosen!)
										continueIt = false;
									}
						}
					}
				}
			}
			else
			{
				//If the cell does not survive
				//Kill the cell
				cellState(x, y, DEAD);
				
				//Mark DORMANT neighbours as ALIVE, to be processed
				awakeNeighbourhood(x, y);
			}
		}
	}
        
        /**
         * Observador del tamaño del dominio tisular.
         * @return Tamaño del dominio tisular
         */
        public int size()
        {
            return size_;
        }
        
        /**
         * Coloca una célula stem en las coordenadas (x, y).
         * @param x Coordenada en el eje x.
         * @param y Coordenada en el eje y.
         */
        public void setStem(int x, int y)
        {
            cellState(x, y, TumorAutomaton.ALIVE);
        }
}
