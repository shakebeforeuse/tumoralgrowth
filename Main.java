public class Main
{
	public static void main(String[] args)
	{
		TumorAutomata ca = new TumorAutomata(4000);
		ca.tejido().set(500, 2500, true);
		ca.tejido().set(1500, 2500, true);
		ca.tejido().set(2500, 2500, true);
		ca.tejido().set(3000, 2500, true);
		//ca.nucleos(1);
		//~ ca.tejido().set(15, 14, true);
		//~ ca.tejido().set(16, 14, true);
		//~ ca.tejido().set(17, 14, true);
		
		//~ TumorAutomata ca = new TumorAutomata(10);
		//~ ca.tejido().set(5, 5, true);
		//~ ca.tejido().set(6, 5, true);
		
		ca.pm = .8;
		double tic = System.currentTimeMillis();
		ca.ejecutar(500);
		ca.terminar();
		double toc = System.currentTimeMillis();
		
		System.out.println((toc - tic) + " ms.");
		
		
		//~ for (int i = 0; i < 32; ++i)
		//~ {
			//~ for (int j = 0; j < 32; ++j)
				//~ if (ca.tejido().get(i, j))
					//~ System.out.print("#");
				//~ else
					//~ System.out.print("·");
			
			//~ System.out.println("");
		//~ }
		
		ca.tejido().guardarPuntos("graph");
	}
}
