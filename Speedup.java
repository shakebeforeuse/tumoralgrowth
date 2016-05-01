import java.util.Random;
import java.util.Scanner;

public class Speedup
{
	private static int tam, maxTareas, generaciones;
	private static double tic, tac;
	private static double[] tiempos;
	
	public static void main(String[] args) throws Exception
	{
		if (args.length == 3)
		{
			tam = Integer.parseInt(args[0]);
			maxTareas = Integer.parseInt(args[1]);
			generaciones = Integer.parseInt(args[2]);
		}
		else
		{
			Scanner teclado = new Scanner(System.in);
			System.out.println("Introduce un tamaño válido");
			tam = teclado.nextInt();
			System.out.println("Introduce el máximo de tareas a ejecutar");
			maxTareas = teclado.nextInt();
			System.out.println("Introduce el número de generaciones");
			generaciones = teclado.nextInt();
			teclado.close();
		}
		
		tiempos = new double[maxTareas];
		
		TumorAutomata tumor = new TumorAutomata(tam);
		tumor.ps  = 1;
		tumor.pp  = .8;
		tumor.pm  = .2;
		tumor.np  = 5;
		tumor.rho = 2;
		
		tumor.cambiarEstado(tam/2, tam/2, TumorAutomata.VIVA);
		
		System.out.println("Tareas\tSpeedup\tTiempo");
		tic = System.nanoTime();
		tumor.ejecutar(generaciones);
		tac = System.nanoTime();
		
		tiempos[0] = (tac - tic) * 1e-9;// / generaciones;
		System.out.printf("%d\t%.2f\t%.2f%n", 1, 1.0, tiempos[0]);
		
		
		for (int n = 2; n <= maxTareas; n+=2)//++n)
		{
			tumor.reiniciar();
			tumor.cambiarEstado(tam/2, tam/2, TumorAutomata.VIVA);
			
			tic = System.nanoTime();
			tumor.nucleos(n);
			tumor.ejecutar(generaciones);
			tac = System.nanoTime();
			
			tiempos[n - 1] = (tac - tic) * 1e-9;// / generaciones;
			
			System.out.printf("%d\t%.2f\t%.2f%n", n, (tiempos[0] / tiempos[n-1]), tiempos[n-1]);
		}
		
		tumor.terminar();
	}
}
