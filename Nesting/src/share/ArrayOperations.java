package share;

/*
 *  @author Dr Eunice López-Camacho
 * Tecnológico de Monterrey, Campus Monterrey
 * 
 * Please report any bugs to Eunice López-Camacho at eunice.lopez@itesm.mx
 * or lopezeunice@gmail.com
 */

public class ArrayOperations {

	public ArrayOperations(){}
	
    public static void zeros(int[][] A, int y, int x){
    	for(int i=0;i<y;i++)
    		for(int j=0;j<x;j++)
    			A[i][j]=0;
    }
    
	
    // Largest
    public static int Mayor (int[] numeros){
		int mayor = numeros[0];
		for(int i=0; i<numeros.length; i++)
			if(numeros[i] > mayor)
				mayor = numeros[i];
		return mayor;	
	}
	
    //Smallest
    public static int Menor (int[] numeros){
		int menor;
		menor = numeros[0];
		for(int i=0; i<numeros.length; i++)
			if(numeros[i] < menor)
				menor = numeros[i];
		return menor;	
	}
	
	
	//Computes Dfunction for a point (xp, yp) and a oriented segment
	//from (x1, y1) to (x2, y2).
	//See tutorial from Bennell, 2006.
	public static int dFunction (int xp, int yp, int x1, int y1, int x2, int y2)
	{
		int D;
		D = (x1 - x2)*(y1 - yp) - (y1 - y2)*(x1 - xp);
		return D;
	}
	
	

}
