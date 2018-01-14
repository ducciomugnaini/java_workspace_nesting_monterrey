package share;

import java.io.File;
import java.util.Vector;
import java.util.List;
import java.util.LinkedList;

/*
 *  @author Dr Eunice López-Camacho
 * Tecnológico de Monterrey, Campus Monterrey
 * 
 * Please report any bugs to Eunice López-Camacho at eunice.lopez@itesm.mx
 * or lopezeunice@gmail.com
 */

public class Instance
{
	private List<Sheet> listaObjetos = new LinkedList<Sheet>(); 
    List<Piece> listapiezas = new LinkedList<Piece>();       //method Ejecuta_Accion will decrease the list
    List<Piece> listapiezasFijas = new LinkedList<Piece>();  //permanent list
    private int xObjeto, yObjeto; //Object size
    private int numpiezas;
    private int noPzasAcomodar;
    private int Totalpiezas;
 	public ResultVisual[] nuevo;
 	
	public Instance(int indi)   
	{
		 nuevo = new ResultVisual[indi];
	}
	
	
	public void obtenerProblema(File archivoEntrada)
	{
		RWfiles rw = new RWfiles();
		int[][] matriz = null;
		try{  
			matriz = rw.obtenerMatriz(archivoEntrada, 37);  
		}
		catch (Exception e){
			System.err.println("Error al leer el archivo : "+archivoEntrada);
		}
		
	    Totalpiezas = 0;  
	    numpiezas = matriz[0][0]; 
        
        //Pone las piezas en el arreglo de piezas no acomodadas
	   	for(int m=0; m<numpiezas; m++)
	   	{
	   		int numLados = matriz[0][m+2];
   		 	int[] vertices = new int[numLados*2];
   		 	for(int i=0;i<numLados*2;i+=2){
   		 		vertices[i] = matriz[i+1][m+2];
   		 		vertices[i+1] = matriz[i+2][m+2];
   		 	}
   		 	Piece pzaprueba = new Piece(vertices);
		    pzaprueba.setnumber(m); 
 			this.Totalpiezas+=pzaprueba.getTotalSize();
   			this.listapiezas.add(pzaprueba);
   			this.listapiezasFijas.add(pzaprueba);
      	}
      	 // Abre el primer contenedor para inicializar
   	    if(listaObjetos.size()>0)
   	    	listaObjetos.clear(); //Limpiar el contenedor
   	    listaObjetos.add(new Sheet(matriz[0][1], matriz[1][1], 0));
   	    xObjeto = matriz[0][1];
   	    yObjeto = matriz[1][1];   
   	    noPzasAcomodar = (int)(listapiezas.size());    
   	    System.out.println("Pieces to place: " + noPzasAcomodar+ " into objects of size "+xObjeto+" x "+yObjeto);  
	}
	
	public double ejecutaAccion (int action, int indi, boolean graficar){
		double aptitud;
		ControlHeuristics control = new ControlHeuristics();
		do
		{
			control.ejecutaHeuristica(listapiezas, listaObjetos, action);
		}while(listapiezas.size()>0);
		for(int i=0; i<listaObjetos.size(); i++)
		{
			Sheet objk = (Sheet)listaObjetos.get(i);
			List<Piece> Lista2 = objk.getPzasInside();
			if(Lista2.size()==0)
				listaObjetos.remove(i);
		}
		aptitud=control.calcularAptitud(listaObjetos);
		int ax=(int) (aptitud*1000.0);
		aptitud=(double) ax/1000.0;
		
		//Get graph of the results:  
		if (graficar){
		 Vector<Sheet> listita = new Vector<Sheet>();
		 for(int i=0; i<listaObjetos.size(); i++)
		 {
			listita.add((Sheet)(listaObjetos.get(i)));
		 }
		 nuevo[indi] = new ResultVisual(listita);
		 nuevo[indi].setSize(700, 650);
       	 nuevo[indi].setVisible(true); 
       	}
		
		return aptitud;
	}
	
	
	public int numeroObjetos()
	{
		return listaObjetos.size();
	}
	
	
}