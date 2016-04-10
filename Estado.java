public enum Estado
{
	MUERTA, LATENTE, VIVA, NUEVA, MIGRADA;
	
	public static Estado fromInteger(int i)
	{
		Estado e = MUERTA;
		
		switch (i)
		{
			case 1:
				e = LATENTE;
				break;
			
			case 2:
				e = VIVA;
				break;
			
			case 3:
				e = NUEVA;
				break;
				
			case 4:
				e = MIGRADA;
				break;
		}
		
		return e;
	}
}
