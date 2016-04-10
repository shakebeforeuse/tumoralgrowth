import java.util.Random;
import java.util.Scanner;

public class Speedup
{
	private static int tam, maxTareas;
	private static double tic, tac;
	private static double[] tiempos;
	
	public static void main(String[] args) throws Exception
	{
		if (args.length == 2)
		{
			tam = Integer.parseInt(args[0]);
			maxTareas = Integer.parseInt(args[1]);
		}
		else
		{
			Scanner teclado = new Scanner(System.in);
			System.out.println("Introduce un tamaño válido");
			tam = teclado.nextInt();
			System.out.println("Introduce el máximo de tareas a ejecutar");
			maxTareas = teclado.nextInt();
			teclado.close();
		}
		
		tiempos = new double[maxTareas];
		
		TumorAutomata tumor = new TumorAutomata(tam);
		
		Random random = new Random();
		
		RejillaEntera matriz = tumor.tejido();
		
		for (int i = 0; i < tam; ++i)
			for (int j = 0; j < tam; ++j)
				matriz.set(i, j, random.nextInt(4));
		
		RejillaEntera copia = matriz.clone();
		
		System.out.println("Tareas\tSpeedup\tTiempo");
		
		for (int n = 1; n <= maxTareas; ++n)
		{
			tumor.tejido(copia.clone());
			
			tumor.nucleos(n);
			
			tic = System.currentTimeMillis();
			tumor.ejecutar(100);
			tac = System.currentTimeMillis();
			
			tiempos[n - 1] = (tac - tic) / 100;
			
			System.out.printf("%d\t%.2f\t%.2f%n", n, (tiempos[0] / tiempos[n-1]), tiempos[n-1]);
		}
		
		tumor.terminar();
	}
}
