public class TumorAutomata
{
	public double ps;
	public double pp;
	public double pm;
	public int    np;

	private RejillaBinaria tejido_;
	private int[][] ph_;
	private int it_;
	private int tam_;

	public TumorAutomata(int tam, double ps, double pp, double pm, int np)
	{
		tejido_ = new RejillaBinaria(tam, tam);
		ph_     = new int[tam][tam];
		tam_    = tam;
		
		this.ps = ps;
		this.pp = pp;
		this.pm = pm;
		this.np = np;
	}
	
	public TumorAutomata(int tam)
	{
		this(tam, 1, .25, .2, 1);
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

					float r = (float)Math.random();

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
		return Math.random() < ps;
	}

	boolean comprobarVecindadLibre(int x, int y)
	{
		return !(tejido_.get(x-1, y) && tejido_.get(x+1, y)
			  && tejido_.get(x, y-1) && tejido_.get(x, y+1));
	}

	boolean comprobarProliferacion(int x, int y)
	{
		boolean prolifera = false;

		if (Math.random() < pp)
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
		return Math.random() < pm && comprobarVecindadLibre(x, y);
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
		for (int i = 0; i < nGeneraciones; ++i)
			siguienteGeneracion();
	}
}
