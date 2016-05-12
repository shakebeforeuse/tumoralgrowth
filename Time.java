import java.util.Random;
import java.util.Scanner;

public class Time
{
	public static void main(String[] args) throws Exception
	{
		if (args.length == 3)
		{
			int size  = Integer.parseInt(args[0]);
			int steps = Integer.parseInt(args[1]);
			int tasks = Integer.parseInt(args[2]);
			
			tasks = tasks == 1? 0:tasks;
			
			TumorAutomaton tumor = new TumorAutomaton(size);
			
			tumor.ps  = 1;
			tumor.pp  = .8;
			tumor.pm  = .2;
			tumor.np  = 5;
			tumor.rho = 2;
			
			tumor.cellState(size/2, size/2, TumorAutomaton.ALIVE);
			
			double tic = System.nanoTime();
			tumor.threads(tasks);
			tumor.execute(steps);
			double toc = System.nanoTime();
			System.out.print((toc-tic)*1e-9);
			
			tumor.shutdown();
		}
		else
			System.out.println("Usage: java Time <size> <steps> <tasks>");
	}
}
