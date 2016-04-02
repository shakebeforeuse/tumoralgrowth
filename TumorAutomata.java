import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.Random;

public class TumorAutomata implements Runnable
{
	static public double ps;
	static public double pp;
	static public double pm;
	static public int    np;
	
	static private RejillaBinaria tejido_;
	static private int[][] ph_;
	static private int it_;
	static private int tam_;
	
	private AtomicLong poblacion_;
	private int inicio_;
	private int fin_;
	private static int pasos_;
	
	private static int nucleos_;
	private ExecutorService threadPool_;
	private Runnable[] tareas_;
	private static CyclicBarrier barrera_;
	
	private Random random_;

	public TumorAutomata(int tam, double ps, double pp, double pm, int np)
	{
		random_ = new Random();
		tejido_ = new RejillaBinaria(tam, tam);
		ph_     = new int[tam][tam];
		tam_    = tam;
		
		poblacion_ = new AtomicLong(0);
		
		this.ps = ps;
		this.pp = pp;
		this.pm = pm;
		this.np = np;
	}
	
	public TumorAutomata(int tam)
	{
		this(tam, 1, .25, .2, 1);
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
		
		for (int i = 0; i < nucleos_; ++i)
		{
			int inicioIntervalo = i       * (tam_ / nucleos_);
			int finIntervalo    = (i + 1) * (tam_ / nucleos_);
			
			if ((i + 1) == nucleos_)
				finIntervalo = tam_;
			
			tareas_[i] = new TumorAutomata(inicioIntervalo, finIntervalo);
		}
	}

	public void cambiarEstado(int x, int y, boolean v)
	{
		//~ tejido_[(it_ + 1) % 2].set(x, y, v);
		//Mirar comentario en siguienteGeneracion
		tejido_.set(x, y, v);
	}

	void actualizarCelda(int x, int y)
	{
		if (tejido_.get(x, y) )
		{
			if (comprobarSupervivencia())
			{
				cambiarEstado(x, y, true);
				
				//Sobrevive. Comprobar si prolifera.
				boolean pom = false;
				if (comprobarProliferacion(x, y))
					//Prolifera
					pom = true;
				else
					if (comprobarMigracion(x, y))
					{
						//Migra
						pom = true;
	
						//Dejar libre la posición actual
						cambiarEstado(x, y, false);
					}

				if (pom)
				{
					//Actualizar posiciones
					float denominador =  0;
					denominador += !tejido_.get(x-1, y) ? 1:0;
					denominador += !tejido_.get(x+1, y) ? 1:0;
					denominador += !tejido_.get(x, y-1) ? 1:0;
					denominador += !tejido_.get(x, y+1) ? 1:0;

					float p1 = !tejido_.get(x-1, y) ? (1/denominador) : 0;
					float p2 = !tejido_.get(x+1, y) ? (1/denominador) : 0;
					float p3 = !tejido_.get(x, y-1) ? (1/denominador) : 0;
					//~ float p4 = !tejido_.get(x, y+1) ? (1/denominador) : 0;

					float r = (float)random_.nextDouble();

					int vx = x, vy = y;
					if (r <= p1)
						vx = x - 1;
					else
						if (r <= p1 + p2)
							vx = x + 1;
						else
							if (r <= p1 + p2 + p3)
								vy = y - 1;
							else
								vy = y + 1;

					cambiarEstado(vx, vy, true);
				}
			}
			else
				//No sobrevive
				cambiarEstado(x, y, false);
		}
	}

	boolean comprobarSupervivencia()
	{
		return random_.nextDouble() < ps;
	}

	boolean comprobarVecindadLibre(int x, int y)
	{
		return !(tejido_.get(x-1, y) && tejido_.get(x+1, y)
			  && tejido_.get(x, y-1) && tejido_.get(x, y+1));
	}

	boolean comprobarProliferacion(int x, int y)
	{
		boolean prolifera = false;

		if (random_.nextDouble() < pp)
		{
			++ph_[x][y];

			//Si hay suficientes señales para proliferar y hay al menos un hueco
			//libre en la vecindad, se prolifera
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
	
	public RejillaBinaria tejido()
	{
		return tejido_;
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
		//it_ = (it_ + 1) % 2;
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
		tejido_ = new RejillaBinaria(tam_, tam_);
	}
}
