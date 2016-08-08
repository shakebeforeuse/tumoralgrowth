package tumoralgrowthautomaton;

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
			
			tumor.setStem(size/2, size/2);
                        TumorAutomaton.ps  = 1;
                        TumorAutomaton.pp  = .8;
                        TumorAutomaton.pm  = .2;
                        TumorAutomaton.np  = 5;
                        TumorAutomaton.rho = 2;
			
			double tic = System.nanoTime();
			tumor.threads(tasks);
			tumor.execute(steps);
			double toc = System.nanoTime();
			System.out.print((toc-tic)*1e-9);
			
			tumor.shutdown();
		}
		else
			System.out.println("Usage: java -cp TumoralGrowth.jar tumoralgrowthautomaton.Time <size> <generations> <tasks>");
	}
}
