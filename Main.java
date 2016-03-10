public class Main
{
	public static void main(String[] args)
	{
		TumorAutomata ca = new TumorAutomata(32);
		ca.tejido().set(14, 14, true);
		//~ ca.tejido().set(15, 14, true);
		//~ ca.tejido().set(16, 14, true);
		//~ ca.tejido().set(17, 14, true);
		
		//~ TumorAutomata ca = new TumorAutomata(10);
		//~ ca.tejido().set(5, 5, true);
		//~ ca.tejido().set(6, 5, true);
		
		ca.siguienteGeneracion();
		ca.siguienteGeneracion();
		ca.siguienteGeneracion();
		ca.siguienteGeneracion();
		ca.siguienteGeneracion();
		
		ca.siguienteGeneracion();
		ca.siguienteGeneracion();
		ca.siguienteGeneracion();
		ca.siguienteGeneracion();
		ca.siguienteGeneracion();
		
		ca.siguienteGeneracion();
		ca.siguienteGeneracion();
		ca.siguienteGeneracion();
		ca.siguienteGeneracion();
		ca.siguienteGeneracion();
		
		ca.siguienteGeneracion();
		ca.siguienteGeneracion();
		ca.siguienteGeneracion();
		ca.siguienteGeneracion();
		ca.siguienteGeneracion();
		
		ca.siguienteGeneracion();
		ca.siguienteGeneracion();
		ca.siguienteGeneracion();
		ca.siguienteGeneracion();
		ca.siguienteGeneracion();
		
		ca.siguienteGeneracion();
		ca.siguienteGeneracion();
		ca.siguienteGeneracion();
		ca.siguienteGeneracion();
		ca.siguienteGeneracion();
		
		//~ ca.siguienteGeneracion();
		//~ ca.siguienteGeneracion();
		//~ ca.siguienteGeneracion();
		//~ ca.siguienteGeneracion();
		//~ ca.siguienteGeneracion();

		
		for (int i = 0; i < 32; ++i)
		{
			for (int j = 0; j < 32; ++j)
				if (ca.tejido().get(i, j))
					System.out.print("#");
				else
					System.out.print("·");
			
			System.out.println("");
		}
		
		ca.tejido().guardarImagen("");
	}
}
