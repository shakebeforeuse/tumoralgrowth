import java.util.Random;
import java.util.Scanner;

public class Speedup
{
	private static int size, maxTasks, stepTasks, steps;
	private static double tic, toc;
	private static double[] time;
	
	public static void main(String[] args) throws Exception
	{
		if (args.length == 4)
		{
			size      = Integer.parseInt(args[0]);
			maxTasks  = Integer.parseInt(args[1]);
			stepTasks = Integer.parseInt(args[2]);
			steps     = Integer.parseInt(args[3]);
		}
		else
		{
			Scanner teclado = new Scanner(System.in);
			System.out.println("Enter a valid size");
			size = teclado.nextInt();
			System.out.println("Enter maximum number of tasks to run");
			maxTasks = teclado.nextInt();
			System.out.println("Enter steps between number of tasks");
			stepTasks = teclado.nextInt();
			System.out.println("Enter number of generations to compute");
			steps = teclado.nextInt();
			teclado.close();
		}
		
		time = new double[maxTasks];
		
		TumorAutomaton tumor = new TumorAutomaton(size);
		tumor.ps  = 1;
		tumor.pp  = .8;
		tumor.pm  = .2;
		tumor.np  = 5;
		tumor.rho = 2;
		
		tumor.cellState(size/2, size/2, TumorAutomaton.ALIVE);
		
		System.out.println("Tasks\tSpeedup\tTime");
		tumor.threads(1);
		tic = System.nanoTime();
		tumor.execute(steps);
		toc = System.nanoTime();
		
		time[0] = (toc - tic) * 1e-9;// / steps;
		System.out.printf("%d\t%.2f\t%.2f%n", 1, 1.0, time[0]);
		
		
		for (int n = 2; n <= maxTasks; n += stepTasks)//++n)
		{
			tumor.reset();
			tumor.cellState(size/2, size/2, TumorAutomaton.ALIVE);
			
			tic = System.nanoTime();
			tumor.threads(n);
			tumor.execute(steps);
			toc = System.nanoTime();
			
			time[n - 1] = (toc - tic) * 1e-9;// / steps;
			
			System.out.printf("%d\t%.2f\t%.2f%n", n, (time[0] / time[n-1]), time[n-1]);
		}
		
		tumor.shutdown();
	}
}
