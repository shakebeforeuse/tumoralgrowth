public class TumorAutomata
{
	static final double PS = 1;
	static final double PP = .25;
	static final double PM = .8;
	static final int    NP = 1;

	private RejillaBinaria tejido_[];
	private int[][] ph_;
	private int it_;
	private int tam_;

	public TumorAutomata(int tam)
	{
		tejido_ = new RejillaBinaria[]{new RejillaBinaria(tam, tam), new RejillaBinaria(tam, tam)};
		ph_     = new int[tam][tam];
		tam_    = tam;
	}

	public void cambiarEstado(int x, int y, boolean v)
	{
		//~ tejido_[(it_ + 1) % 2].set(x, y, v);
		//Mirar comentario en siguienteGeneracion
		tejido_[it_ ].set(x, y, v);
	}

	void actualizarCelda(int x, int y)
	{
		if (tejido_[it_].get(x, y) )
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
					denominador += !tejido_[it_].get(x-1, y) ? 1:0;
					denominador += !tejido_[it_].get(x+1, y) ? 1:0;
					denominador += !tejido_[it_].get(x, y-1) ? 1:0;
					denominador += !tejido_[it_].get(x, y+1) ? 1:0;

					float p1 = !tejido_[it_].get(x-1, y) ? (1/denominador) : 0;
					float p2 = !tejido_[it_].get(x+1, y) ? (1/denominador) : 0;
					float p3 = !tejido_[it_].get(x, y-1) ? (1/denominador) : 0;
					//~ float p4 = !tejido_[it_].get(x, y+1) ? (1/denominador) : 0;

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
		return Math.random() < TumorAutomata.PS;
	}

	boolean comprobarVecindadLibre(int x, int y)
	{
		return !(tejido_[it_].get(x-1, y) && tejido_[it_].get(x+1, y)
			  && tejido_[it_].get(x, y-1) && tejido_[it_].get(x, y+1));
	}

	boolean comprobarProliferacion(int x, int y)
	{
		boolean prolifera = false;

		if (Math.random() < TumorAutomata.PP)
		{
			++ph_[x][y];

			//Si hay suficientes señales para proliferar y hay al menos un hueco
			//libre en la vecindad, se prolifera
			prolifera = ph_[x][y] >= TumorAutomata.NP && comprobarVecindadLibre(x, y);
		}

		return prolifera;
	}

	boolean comprobarMigracion(int x, int y)
	{
		return Math.random() < TumorAutomata.PM && comprobarVecindadLibre(x, y);
	}

	public int size()
	{
		return tam_;
	}
	
	public RejillaBinaria tejido()
	{
		return tejido_[it_];
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
}
