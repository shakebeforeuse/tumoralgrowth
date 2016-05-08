import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

class Position
{
	int x, y;
	
	public Position(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
}

class Cell
{
	public static final byte DEAD = 0, DORMANT = 1, ALIVE = 2, NEW = 3, MIGRATED = 4;
	byte state;
	
	int ph;
	int rho;
	boolean isStem;
	
	Position p;
	
	public Cell(Position p, byte state, int ph, int rho, boolean isStem)
	{
		this.state  = state;
		this.p      = p;
		this.ph     = ph;
		this.rho    = rho;
		this.isStem = isStem;
	}
}

public class TumorAutomaton
{
	public static int size;
	
	public static int rho;
	public static int np;
	
	public static float ps;
	public static float pp;
	public static float pm;
	
	static boolean[][] tissue_;
	private static ArrayList<Cell> cells_;
	private static ArrayList<Cell> cellsNextGen_;
	
	private static ArrayList<Position> whereTo_;
	private Random random_;
	
	public TumorAutomaton(int size)
	{
		this.size = size;
		
		rho = 10;
		np  = 5;
		ps  = .99f;
		pp  = 1.f/24;
		pm  = 10.f/24;
		
		tissue_ = new boolean[size][size];
		
		cells_        = new ArrayList<Cell>();
		cellsNextGen_ = new ArrayList<Cell>();
		whereTo_      = new ArrayList<Position>();
		
		for (int i = -1; i <= 1; ++i)
			for (int j = -1; j <= 1; ++j)
				if (i != 0 || j != 0)
					whereTo_.add(new Position(i, j));
		
		random_ = new Random();
	}
	
	public boolean tissue(Position p)
	{
		if (0 <= p.x && p.x < size && 0 <= p.y && p.y < size)
			return tissue_[p.x][p.y];
		
		//Cell alive, to avoid proliferation
		return true;
	}
	
	public boolean tissue(int x, int y)
	{
		if (0 <= x && x < size && 0 <= y && y < size)
			return tissue_[x][y];
		
		//Cell alive, to avoid proliferation
		return true;
	}
	
	public void tissue(Position p, boolean v)
	{
		if (0 <= p.x && p.x < size && 0 <= p.y && p.y < size)
			tissue_[p.x][p.y] = v;
	}
	
	public void tissue(int x, int y, boolean v)
	{
		if (0 <= x && x < size && 0 <= y && y < size)
			tissue_[x][y] = v;
	}
	
	public void setStemCell(int x, int y)
	{
		//Set steam cell
		if (!tissue(x, y))
		{
			Cell stem = new Cell(new Position(x, y), Cell.ALIVE, 0, rho, true);
			
			cells_.add(stem);
		}
	}
	
	public void execute(int steps)
	{
		for (int k = 0; k < steps; ++k)
		{
			Collections.shuffle(cells_);
			
			//While there are cells to process
			while (!cells_.isEmpty())
			{
				//Get cell and remove from the list
				Cell current = cells_.get(0);
				cells_.remove(0);
				
				//Check spontaneous death
				if (random_.nextFloat() > ps)
					tissue_[current.p.x][current.p.y] = false;
				else
				{
					//Get free, random position
					Position newPosition = new Position(current.p.x, current.p.y);
					Collections.shuffle(whereTo_);
					
					for (Position p : whereTo_)
					{
						int nX = current.p.x + p.x;
						int nY = current.p.y + p.y;
						
						if (0 <= nX && nX < size && 0 <= nY && nY < size && !tissue_[nX][nY])
						{
							newPosition.x = nX;
							newPosition.y = nY;
							break;
						}
					}
					
					//If exists a free position
					if (newPosition.x != current.p.x || newPosition.y != current.p.y)
					{
						//Check proliferation
						if (random_.nextFloat() < pp && ++current.ph >= np)
						{
							//New cell				
							Cell newCell = new Cell(new Position(newPosition.x, newPosition.y), Cell.NEW, 0, rho, false);
							cellsNextGen_.add(newCell);
							
							tissue_[newPosition.x][newPosition.y] = true;
							
							//Kill the cell if it has reached the limit.
							if (!current.isStem && --current.rho == 0)
							{
								tissue_[current.p.x][current.p.y] = false;
								current = null;
							}
						}
						else
							//Check migration
							if (random_.nextFloat() < pm)
							{
								//Migrate cell
								tissue_[current.p.x][current.p.y] = false;
								tissue_[newPosition.x][newPosition.y] = true;
								
								//Move to the selected position
								current.p = newPosition;
							}
					}
					
					//Add to be computed in the next generation
					if (current != null)
						cellsNextGen_.add(current);
				}
			}
			
			//Swap lists
			ArrayList<Cell> aux = cellsNextGen_;
			cellsNextGen_ = cells_;
			cells_ = aux;
		}
	}
}
