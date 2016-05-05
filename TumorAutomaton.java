import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Cellular Automaton that models tumoral growth.
 * @author Manuel Francisco
 * @version 1.0 04/2016
 */
public class TumorAutomaton implements Runnable
{
	//Cell states
	/** State of a dead cell. */
	public static final int DEAD = 0;
	/** State of a dormant cell. */
	public static final int DORMANT = 1;
	/** State of an alive cell. */
	public static final int ALIVE = 2;
	/** State of a recently proliferated cell. */
	public static final int NEW = 3;
	/** State of a recently migrated cell. */
	public static final int MIGRATED = 4;
	
	//Tumoral growth simulation parameters
	/** Survival probability. */
	public static double ps;
	/** Proliferation probability. */
	public static double pp;
	/** Migration probability. */
	public static double pm;
	/** Proliferation signals needed to proliferate. */
	public static int    np;
	/** Maximum number of times a cell can proliferate without dying. */
	public static int    rho;
	
	//CA grid
	private static int[][]  tissue_;
	private static int  size_;
	
	//No. of times each cell can proliferatete
	private static int[][]  rhos_;
	
	//No. of times each cell has been signaled for proliferatetion
	private static int[][]  ph_;
	
	//To avoid processing a new cell in the same generation
	private static byte[][] generation_;
	private static byte it_;
	
	//Paralelism
	//Partition
	private int begin_;
	private int end_;
	
	//No. of generations to compute
	private static int steps_;
	
	//Number of threads we will have and array of tasks.
	private static int threads_;
	private Runnable[] tasks_;
	
	//Pool of threads
	private ExecutorService threadPool_;
	
	//Synchronization
	private static CyclicBarrier barrier_;
	private static final ReentrantLock lock_ = new ReentrantLock();
	
	//Non-static random number generaton (to avoid thread-safety)
	private Random random_;	
	
	/**
	 * Creates a new CA with the given size.
	 * @param size Size of the squared grid.
	 */
	public TumorAutomaton(int size)
	{
		size_       = size;
		tissue_     = new int[size_][size_];
		ph_         = new int[size_][size_];
		rhos_       = new int[size_][size_];
		generation_ = new byte[size_][size_];
		random_     = new Random();
	}
	
	/**
	 * Intended to create the tasks.
	 * @param begin Begin of the partition.
	 * @param end End of the partition.
	 */
	private TumorAutomaton(int begin, int end)
	{
		random_ = new Random();
		begin_  = begin;
		end_    = end;
	}
	
	/**
	 * Tries to shutdown the pool of threads, and wait until is
	 * terminated.
	 */
	public void shutdown()
	{	
		while (threadPool_ != null && !threadPool_.isTerminated())
			threadPool_.shutdown();
	}
	
	/**
	 * Set the CA to run in parallel. If 0 is given, set the CA to run
	 * sequentially.
	 * @param n No. of tasks to run in parallel or 0 to run sequentially
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
			tasks_      = new Runnable[threads_];
			
			//Distribute the grid between the tasks that are being created
			for (int i = 0; i < threads_; ++i)
			{
				int beginInterval = i       * (size_ / threads_);
				int endInterval   = (i + 1) * (size_ / threads_);
				
				if ((i + 1) == threads_)
					endInterval = size_;
				
				tasks_[i] = new TumorAutomaton(beginInterval, endInterval);
			}
		}
	}
	
	/**
	 * Runs the CA for a given number of generations.
	 * @param nGenerations Number of generations to compute.
	 */
	public void execute(int nGenerations)
	{
		//Run sequentially if there is no pool of threads
		if (threadPool_ == null)
			//Iterate k times over the whole grid, updating cells
			for (int k = 0; k < nGenerations; ++k)
			{
				for (int i = 0; i < size_; ++i)
					for (int j = 0; j < size_; ++j)
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
					//~ Thread.currentThread().sleep(1000);
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
	 * Runnable method. Compute the next 'steps_' generations of the
	 * cells in the range [begin_, end_).
	 */
	public void run()
	{
		for (int k = 0; k < steps_; ++k)
		{
			try
			{
				for (int i = begin_; i < end_; ++i)
					for (int j = 0; j < size_; ++j)
						updateCell(i, j);
			
				barrier_.await();
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
	 * Reset CA.
	 */
	void reset()
	{
		tissue_     = new int[size_][size_];
		ph_         = new int[size_][size_];
		rhos_       = new int[size_][size_];
		generation_ = new byte[size_][size_];
		it_         = 0;
	}
	
	
	/**
	 * Return the state of the cell (x, y).
	 * @param x Coordinate in the x axis.
	 * @param y Coordinate in the y axis.
	 * @return State of the cell (x, y).
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
	 * Modifies the state of the cell (x, y).
	 * @param x Coordinate in the x axis.
	 * @param y Coorindate in the y axis.
	 * @param v New state of the cell (x, y).
	 */
	void cellState(int x, int y, int v)
	{
		//Check if it is within bounds. Do nothing if not.
		if (0 <= x && x < size_ && 0 <= y && y < size_)
			tissue_[x][y] = v;
	}
	
	/**
	 * Awake dormant cells around (x, y).
	 * @param x Coordinate in the x axis.
	 * @param y Coordinate in the y axis.
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
	 * Run the CA rule for the cell (x, y).
	 * @param x Coordinate in the x axis.
	 * @param y Coordinate in the y axis.
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
						
						lock_.lock();
						
						//Compute no. of alive neighbours
						int count = 0;
						for (int i = -1; i <= 1; ++i)
							for (int j = -1; j <= 1; ++j)
								if (i != 0 || j != 0)
								{
									n[count] = cellState(x + i, y + j) == DEAD ? 1:0;
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
						
						lock_.unlock();
					}
				}
			}
			else
			{
				//If the cell do not survive
				lock_.lock();
				
				//Kill the cell
				cellState(x, y, DEAD);
				
				//Mark DORMANT neighbours as ALIVE, to be processed
				awakeNeighbourhood(x, y);
				
				lock_.unlock();
			}
		}
	}
}
