package share;

import java.util.List;

/*
 *  @author Dr Eunice López-Camacho
 * Tecnológico de Monterrey, Campus Monterrey
 * 
 * Please report any bugs to Eunice López-Camacho at eunice.lopez@itesm.mx
 * or lopezeunice@gmail.com
 */

// Selection heuristics.  

public class Heuristics
{
	static Sheet nextObjeto;
	static Piece pza;
	static int ObjetosMaximo = 20;  //maximum number of objects that a solution could have.
	static int PiezasMaximo = 60;  //maximum number of pieces that an instance could have.
	static boolean getOut = false;  //a flag to tell heuristic DJD to get out of the function.
	//Initialization of the following 3 variables is needed when running a hyper-heuristic and DJD does not start a problem.
	private int[][] puedeAcomodar  = new int [ObjetosMaximo][PiezasMaximo];  
	private int[][][] puedeAcomodar2  = new int [ObjetosMaximo][PiezasMaximo][PiezasMaximo];
	private int[][][][] puedeAcomodar3  = new int [ObjetosMaximo][PiezasMaximo][PiezasMaximo][PiezasMaximo];

	
	// Para c/pieza, de mayor a menor, busca el primer objeto abierto donde quepa y la mete.
	// Si no encuentra objeto, busca para la siguiente pieza.
	// Si no encuentra objeto para ninguna pieza, aplica FFD (es decir, para la pza mayor,
	// busca objeto y como no hallará, abre un objeto nuevo).  
	public void Filler(List<Piece> listapiezas, List<Sheet> listaObjetos, int xObjeto, int yObjeto, String H_acomodo)   
	{
		boolean acomodopieza = false;
		listapiezas = OrdenaPiezas(listapiezas, 1);  //descending order

		for(int i = 0; i<listapiezas.size(); i++)  //pieces in descending order
		{
			if (acomodopieza)
			{
				break; 
			}
			pza = (Piece)listapiezas.get(i);

			//For the next two rows: comment one and uncomment one.
			//for (int j = 0; j < listaObjetos.size(); j++)   // For hyper-heuristics
			for (int j = listaObjetos.size()-1; j < listaObjetos.size(); j++) 
			{
				nextObjeto = (Sheet)listaObjetos.get(j);
				if (pza.getTotalSize() <= nextObjeto.getFreeArea())
				{
					pza.desRotar();
					HeuristicsPlacement acomodo = new HeuristicsPlacement();
					acomodopieza = acomodo.HAcomodo(nextObjeto, pza, H_acomodo);
					if (acomodopieza)
					{
						nextObjeto.addPieza(pza);
						listapiezas.remove(pza);
						break;   
					}	    
				}
			}
		}

		if (acomodopieza == false)
		{
			First_Fit_Decreasing(listapiezas, listaObjetos, xObjeto, yObjeto, H_acomodo); 
		}

		// For hyper-heuristics (combining heuristics), it is important to leave the pieces in their original order.
		listapiezas = AcomodoOriginalPiezas(listapiezas);
	}



	// First_Fit: acomoda la primera pieza en el primer objeto que quepa, si no cabe en ninguno, abre un objeto nuevo 
	// y acomoda la pieza.
	// Considera todos los objetos parcialmente llenos como posibles para acomodar la pza.
	// Tiene desrotar :)  Antes de cada intento de acomodo, pone la pieza
	// en su posicion original (para llevar la bien la cuenta de las rotaciones).
	public static void First_Fit(List<Piece> listapiezas, List<Sheet> listaObjetos, int xObjeto, int yObjeto, String H_acomodo)
	{
		boolean acomodopieza = false;
		boolean encontroObjeto = false;
		HeuristicsPlacement acomodo = new HeuristicsPlacement();
		pza = (Piece)listapiezas.get(0);
		//Search for an object to place the piece
		int n = listaObjetos.size();
		for (int j = 0; j < n; j++) 
		{
			nextObjeto = (Sheet)listaObjetos.get(j);
			if (pza.getTotalSize() <= nextObjeto.getFreeArea())
			{
				pza.desRotar();
				acomodopieza = acomodo.HAcomodo(nextObjeto, pza, H_acomodo);
				if (acomodopieza)
				{			            	
					nextObjeto.addPieza(pza);
					listapiezas.remove(pza);
					encontroObjeto = true;
					break;
				}
			}
		}


		if (!encontroObjeto)
		{
			//Abre un nuevo Objeto vacio
			if(listapiezas.size()>0)
				nextObjeto=abreNuevoObjeto(listaObjetos, xObjeto, yObjeto);
			pza.desRotar();
			acomodopieza = acomodo.HAcomodo(nextObjeto, pza, H_acomodo);
			if (acomodopieza)  //el objeto es nuevo, siempre debería poder acomodar la pza.
			{
				nextObjeto.addPieza(pza);
				listapiezas.remove(pza);
			}
		}
	} // fin de la heuristica



	// First_Fit_Decreasing: acomoda la pieza más grande en el primer objeto que quepa, si no cabe en ninguno, abre un objeto nuevo 
	public void First_Fit_Decreasing(List<Piece> listapiezas, List<Sheet> listaObjetos, int xObjeto, int yObjeto, String H_acomodo)
	{
		boolean acomodopieza = false;
		boolean encontroObjeto = false;
		HeuristicsPlacement acomodo = new HeuristicsPlacement();
		pza = SearchGreatest(listapiezas);
		for (int j = 0; j < listaObjetos.size(); j++) 
		{
			nextObjeto = (Sheet)listaObjetos.get(j);
			if (pza.getTotalSize() <= nextObjeto.getFreeArea())
			{
				pza.desRotar();
				acomodopieza = acomodo.HAcomodo(nextObjeto, pza, H_acomodo);
				if (acomodopieza)
				{
					nextObjeto.addPieza(pza);
					listapiezas.remove(pza);
					encontroObjeto = true;
					break;
				}
			}
		}


		if (!encontroObjeto)
		{
			//Abre un nuevo Objeto vacio
			nextObjeto=abreNuevoObjeto(listaObjetos, xObjeto, yObjeto);
			pza.desRotar();
			acomodopieza = acomodo.HAcomodo(nextObjeto, pza, H_acomodo);
			if (acomodopieza)  //el objeto es nuevo, siempre debería poder acomodar la pza.
			{
				nextObjeto.addPieza(pza);
				listapiezas.remove(pza);
			}
		}
	} // fin de la heuristica



	public static void First_Fit_Increasing(List<Piece> listapiezas, List<Sheet> listaObjetos, int xObjeto, int yObjeto, String H_acomodo)
	{
		boolean acomodopieza = false;
		boolean encontroObjeto = false;
		HeuristicsPlacement acomodo = new HeuristicsPlacement();
		
		pza = SearchSmallest(listapiezas);
		//Busca por un objeto en el cual quepa la pieza
		for (int j = 0; j < listaObjetos.size(); j++)
		{
			nextObjeto = (Sheet)listaObjetos.get(j);
			if (pza.getTotalSize() <= nextObjeto.getFreeArea())
			{
				pza.desRotar();
				acomodopieza = acomodo.HAcomodo(nextObjeto, pza, H_acomodo);
				if (acomodopieza)
				{
					nextObjeto.addPieza(pza);
					listapiezas.remove(pza);
					encontroObjeto = true;
					break;
				}
			}
		}


		if (!encontroObjeto)
		{
			//Abre un nuevo Objeto vacio
			nextObjeto=abreNuevoObjeto(listaObjetos, xObjeto, yObjeto);
			pza.desRotar();
			acomodopieza = acomodo.HAcomodo(nextObjeto, pza, H_acomodo);
			if (acomodopieza)  //el objeto es nuevo, siempre debería poder acomodar la pza.
			{
				nextObjeto.addPieza(pza);
				listapiezas.remove(pza);
			}
		}
	} // fin de la heuristica


	// Determina si la instancia es 1D o 2D para saber si usar rutina de reducción de tiempos o no.
	public void Djang_and_Finch(List<Piece> listapiezas, List<Sheet> listaObjetos, int xObjeto, int yObjeto, String H_acomodo, double CapInicial)
	{ 	
		int ancho, anchoPza0;
		int numPiezas = listapiezas.size();
		boolean iguales = true;
		Piece pza;
		Sheet obj;
		
		pza = (Piece)listapiezas.get(0);
		anchoPza0 = pza.getxsize();
		
		for(int i=1; i<numPiezas; i++)	//pzas restantes.
		{
			pza = (Piece)listapiezas.get(i);
	   		ancho = pza.getxsize();
	   		if(ancho != anchoPza0)
	   		{
	   			iguales = false;
	   			break;
	   		}
	
		}
		if(iguales)   //pzas ya acomodadas en objetos.
			for(int i=0; i<listaObjetos.size(); i++)
			{
				obj = (Sheet)listaObjetos.get(i);
				List<Piece> listapzas = obj.getPzasInside();
				numPiezas = listapzas.size();
				for(int j=0; j<numPiezas; j++)	//pzas restantes.
				{
					pza = (Piece)listapzas.get(j);
			   		ancho = pza.getxsize();
			   		if(ancho != anchoPza0)
			   		{
			   			iguales = false;
			   			break;
			   		}
				}
				if(iguales==false)
				{
					break;
				}
			}

		if(iguales)
		{
			Djang_and_Finch_1D(listapiezas, listaObjetos, xObjeto, yObjeto, H_acomodo, CapInicial);
		}else
		{
			Djang_and_Finch_2D(listapiezas, listaObjetos, xObjeto, yObjeto, H_acomodo, CapInicial);
		}
	}


	// Si un objeto está lleno a menos de 1/3, mete una pieza ahí (de mayor a menor).
	// Si ninguna cabe, se pasa al siguiente objeto.
	// Si todos los objetos están llenos al menos a 1/3 (o no mete ninguna pza), entonces:
	//     Dado un desperdicio máximo permitido w, trata de colocar 1, 2 o 3 
	//     piezas (probando todas las combinaciones de piezas).
	//     Si no puede, aumenta el desperdicio.  
	//     Si no puede, se pasa al siguiente objeto.
	//     Si nunca puede, abre un objeto nuevo y mete ahí la pieza mayor.
	// Observación: si solo se efectúa esta heurística de principio a fin en la solución
	// del problema, no tendría caso revisar objetos anteriores al último pues no debería
	// caber ninguna pieza ahí.
	private void Djang_and_Finch_2D(List<Piece> listapiezas, List<Sheet> listaObjetos, int xObjeto, int yObjeto, String H_acomodo, double CapInicial)
	{ 	
      
		HeuristicsPlacement acomodo = new HeuristicsPlacement();
		boolean acomodopieza = false;
		int incremento = ((Sheet)listaObjetos.get(0)).gettotalsize() / 20; 
		int w = 0; //allowed waste
		listapiezas = OrdenaPiezas(listapiezas, 1);  //descending order
		boolean terminar = false;  // decides when to open a new object.
		getOut = false;

		//For the next two rows: comment one and uncomment one.
		//for (int j = 0; j < listaObjetos.size(); j++)   // for Hyper-heuristics
		for (int j = listaObjetos.size()-1; j < listaObjetos.size(); j++) 
		{
			nextObjeto = (Sheet)listaObjetos.get(j);
			if(nextObjeto.getUsedArea() < nextObjeto.gettotalsize()*CapInicial)  
			{
				for (int i=0; i<listapiezas.size(); i++)   //decreasing order of size
				{
					pza = (Piece)listapiezas.get(i);
					if (pza.getTotalSize() <= nextObjeto.getFreeArea() )
					{
						pza.desRotar();
						acomodopieza = acomodo.HAcomodo(nextObjeto, pza, H_acomodo);
						if (!acomodopieza)
						{
							puedeAcomodar[j][i] = 1;   // keeps a record that the piece does not fit into the object
						}
						if (acomodopieza)
						{
							nextObjeto.addPieza(pza);
							listapiezas.remove(pza);
							listapiezas = AcomodoOriginalPiezas(listapiezas);    
							return;
						}
					}
				}
			}
		}


		//For the next two rows: comment one and uncomment one.
		//for (int j = 0; j < listaObjetos.size(); j++) // For hyper-heuristics.
		for (int j = listaObjetos.size()-1; j < listaObjetos.size(); j++) 
		{
			nextObjeto = (Sheet)listaObjetos.get(j);
			w = 0;
			terminar = false;

			if( verificador(listapiezas, nextObjeto.getFreeArea()) )
			{
				continue;  //si por area libre, ya no cabe ninguna pieza, se pasa al otro objeto.
			}

			do
			{
				unapieza(listapiezas, nextObjeto, H_acomodo, w); 
				if(getOut)
				{
					listapiezas = AcomodoOriginalPiezas(listapiezas);
					return;
				}
				if(listapiezas.size()>1)
				{
					dospiezas(listapiezas, nextObjeto, H_acomodo, w); 
					if(getOut)
					{
						listapiezas = AcomodoOriginalPiezas(listapiezas);
						return;
					}
				}
				if(listapiezas.size()>2)
				{
					trespiezas(listapiezas, nextObjeto, H_acomodo, w);
					if(getOut)
					{
						listapiezas = AcomodoOriginalPiezas(listapiezas);
						return;
					}
				}

				if(w > nextObjeto.getFreeArea() )
				{terminar = true;}
				w+= incremento;  //w podría pasarse del área libre, pero se intenta dado que w>1. 
				//Suponer que el área libre es de 10999 y el incremento = 1000;
				//conviene intentar w=11000 después de w=10000 por si hay una 
				//pieza o combinación de piezas con área de hasta 999 que quepan.

			}while(!terminar);
		}


		nextObjeto=abreNuevoObjeto(listaObjetos, xObjeto, yObjeto);
		pza = SearchGreatest(listapiezas);
		pza.desRotar();
		acomodopieza = acomodo.HAcomodo(nextObjeto, pza, H_acomodo);
		if (acomodopieza)  //el objeto es nuevo, siempre debería poder acomodar la pza.
		{
			nextObjeto.addPieza(pza);
			listapiezas.remove(pza);
		}
		// For hyper-heuristics (combining heuristics), it is important to leave the pieces in their original order.
		listapiezas = AcomodoOriginalPiezas(listapiezas);
	}


	public static void Djang_and_Finch_1D(List<Piece> listapiezas, List<Sheet> listaObjetos, int xObjeto, int yObjeto, String H_acomodo, double CapInicial)
	{ 	
		HeuristicsPlacement acomodo = new HeuristicsPlacement();
		boolean acomodopieza = false;
		int incremento = ((Sheet)listaObjetos.get(0)).gettotalsize() / 20;   
		int w = 0; //allowed waste
		listapiezas = OrdenaPiezas(listapiezas, 1);  //decreasing order of size
		boolean terminar = false;  // decides when to open a new object
		getOut = false;
		

		//For the next two rows: comment one and uncomment one.
		//for (int j = 0; j < listaObjetos.size(); j++)   // For hyper-heuristics
        for (int j = listaObjetos.size()-1; j < listaObjetos.size(); j++) 
		{
			nextObjeto = (Sheet)listaObjetos.get(j);
			if(nextObjeto.getUsedArea() < nextObjeto.gettotalsize()*CapInicial)  
			{
				for (int i=0; i<listapiezas.size(); i++)   //decreasing order of size
				{
					pza = (Piece)listapiezas.get(i);
					if (pza.getTotalSize() <= nextObjeto.getFreeArea() )
					{
						pza.desRotar();
						acomodopieza = acomodo.HAcomodo(nextObjeto, pza, H_acomodo);
						if (acomodopieza)
						{
							nextObjeto.addPieza(pza);
							listapiezas.remove(pza);
							listapiezas = AcomodoOriginalPiezas(listapiezas);    
							return;
						}
					}
				}
			}
		}


        //For the next two rows: comment one and uncomment one.
		//for (int j = 0; j < listaObjetos.size(); j++) // For Hyper-heuristics
		for (int j = listaObjetos.size()-1; j < listaObjetos.size(); j++) 
		{
			nextObjeto = (Sheet)listaObjetos.get(j);
			w = 0;
			terminar = false;

			if( verificador(listapiezas, nextObjeto.getFreeArea()) )
			{
				continue;  //si por area libre, ya no cabe ninguna pieza, se pasa al otro objeto.
			}

			do
			{
				unapieza_1D(listapiezas, nextObjeto, H_acomodo, w);  
				if(getOut)
				{
					listapiezas = AcomodoOriginalPiezas(listapiezas);
					return;
				}
				if(listapiezas.size()>1)
				{
					dospiezas_1D(listapiezas, nextObjeto, H_acomodo, w);  
					if(getOut)
					{
						listapiezas = AcomodoOriginalPiezas(listapiezas);
						return;
					}
				}
				if(listapiezas.size()>2)
				{
					trespiezas_1D(listapiezas, nextObjeto, H_acomodo, w);
					if(getOut)
					{
						listapiezas = AcomodoOriginalPiezas(listapiezas);
						return;
					}
				}

				if(w > nextObjeto.getFreeArea() )
				{terminar = true;}
				w+= incremento;  //w podría pasarse del área libre, pero se intenta dado que w>1. 
				//Suponer que el área libre es de 10999 y el incremento = 1000;
				//conviene intentar w=11000 después de w=10000 por si hay una 
				//pieza o combinación de piezas con área de hasta 999 que quepan.

			}while(!terminar);
		}


		nextObjeto=abreNuevoObjeto(listaObjetos, xObjeto, yObjeto);
		pza = SearchGreatest(listapiezas);
		pza.desRotar();
		acomodopieza = acomodo.HAcomodo(nextObjeto, pza, H_acomodo);
		if (acomodopieza)  //el objeto es nuevo, siempre debería poder acomodar la pza.
		{
			nextObjeto.addPieza(pza);
			listapiezas.remove(pza);
		}
		// For hyper-heuristics (combining heuristics), it is important to leave the pieces in their original order.
		listapiezas = AcomodoOriginalPiezas(listapiezas);
	}


	//Receives an ordered list by piece size (decreasing order)
	private static boolean verificador(List<Piece> listapiezas1, int freearea)
	{
		Piece pza1;
		for(int i=listapiezas1.size()-1; i>=0; i--)  //como se entrega la lista ordenada de mayor a menor,
		{											 //si se empieza a buscar desde el último (pza + chica)
			pza1 = (Piece)listapiezas1.get(i);		 //devuelve un 'false' con menos comparaciones.
			if(pza1.getTotalSize() <= freearea)
			{
				return false;   
			}		
		}
		return true;
	}


	// Indica si puede o no poner una pieza en el objeto, dejando un máximo de desperdicio w.
	// Devuelve la matriz 'puedeAcomodar' actualizada con los intentos de acomodo realizados.
	// Si puede acomodar pieza pone un 2 en la posición [0][0] para terminar la heurística.
	// RECEIVES PIECES IN DESCENDING ORDER OF SIZE.
	private void unapieza(List<Piece> listapiezas1, Sheet nextObjeto1, String H_acomodo1, int w)    
	{
		HeuristicsPlacement acomodoP = new HeuristicsPlacement();
		Piece pza1;
		boolean acomodo = false;
		int arealibre;
		int numObj;  
		arealibre = nextObjeto1.getFreeArea();
		numObj = nextObjeto1.getNumObjeto(); 

		
		for(int i=0; i<listapiezas1.size(); i++)
		{
			pza1 = (Piece)listapiezas1.get(i);
			if( (arealibre-pza1.getTotalSize()) > w )
			{
				break;  // si con una pieza deja más desperdicio que w, con las demás también lo hará (dado q están ordenadas)
			}
			if( pza1.getTotalSize() > arealibre 
					|| (puedeAcomodar[numObj][i] == 1 ) )  
			{
				continue;  // ya probó que no puede meter esa pieza ahí.
			}

																								   
			//Trata de Acomodarlo en Bin con Heuristica de Acomodo														   
			pza1.desRotar();
			acomodo = acomodoP.HAcomodo(nextObjeto1, pza1, H_acomodo1);
			if (!acomodo)
			{
				puedeAcomodar[numObj][i] = 1;   //registra q esa pza no cabe en ese objeto.
			}
			if (acomodo)
			{
				nextObjeto1.addPieza(pza1);
				listapiezas1.remove(pza1);
				getOut = true;  // indica que ya acomodó pieza.
				return;
			}
		}
		return;
	}


	
	// Indica si puede o no poner una pieza en el objeto, dejando un máximo de desperdicio w.
	// Devuelve la matriz 'puedeAcomodar' actualizada con los intentos de acomodo realizados.
	// Si puede acomodar pieza pone un 2 en la posición [0][0] para terminar la heurística.
	// RECEIVES PIECES IN DESCENDING ORDER OF SIZE.
	private static void unapieza_1D(List<Piece> listapiezas1, Sheet nextObjeto1, String H_acomodo1, int w)    
	{
		HeuristicsPlacement acomodoP = new HeuristicsPlacement();
		Piece pza1;
		boolean acomodo = false;
		int arealibre;
		arealibre = nextObjeto1.getFreeArea();
		
		for(int i=0; i<listapiezas1.size(); i++)
		{
			pza1 = (Piece)listapiezas1.get(i);
			if( (arealibre-pza1.getTotalSize()) > w )
			{
				break;  // si con una pieza deja más desperdicio que w, con las demás también lo hará (dado q están ordenadas)
			}

			pza1.desRotar();
			acomodo = acomodoP.HAcomodo(nextObjeto1, pza1, H_acomodo1);
			if (acomodo)
			{
				nextObjeto1.addPieza(pza1);
				listapiezas1.remove(pza1);
				getOut = true;  // indica que ya acomodó pieza.
				return;
			}
		}
		return;
	}

	
	private void dospiezas(List<Piece> listapiezas1, Sheet nextObjeto1, String H_acomodo1, int w1)
	{
		HeuristicsPlacement acomodo = new HeuristicsPlacement();
		Piece pza1, pza2;
		boolean acomodo1 = false, acomodo2 = false;
		int area0, area1;  //guardará el área de las 2 piezas más grandes.
		int areaU;		   //guardará el área de la pieza más pequeña.
		int arealibre;
		int numObj;  
		pza1 = (Piece)listapiezas1.get(listapiezas1.size()-1);  
		areaU = pza1.getTotalSize();
		arealibre = nextObjeto1.getFreeArea();
		numObj = nextObjeto1.getNumObjeto();  
		
		//verificando si cabrían 2 piezas con ese desperdicio máximo permitido.
		// RECEIVES PIECES IN DESCENDING ORDER OF SIZE.
		pza1 = (Piece)listapiezas1.get(0);
		pza2 = (Piece)listapiezas1.get(1);
		area0 = pza1.getTotalSize();
		area1 = pza2.getTotalSize();
		if( (arealibre-area0-area1) > w1)
		{
			return;
		}

		for(int i=0; i<listapiezas1.size(); i++)
		{
			acomodo1 = false;
			acomodo2 = false;
			pza1 = (Piece)listapiezas1.get(i);

			if(arealibre - pza1.getTotalSize()-area0 > w1)
			{
				break;  // con pza1 y la más grande dejan más w, ya no tiene caso probar + pzas1.
			}

			if(pza1.getTotalSize()+areaU > arealibre  
					 || (puedeAcomodar[numObj][i] == 1 ) )  
			{
				continue;  	// a la sig. pza 1. Pza1 + la mas chica se pasarían del área disponible.
			}				// o bien, ya se sabe que pza 1 no se puede acomodar.

			pza1.desRotar();
			acomodo1 = acomodo.HAcomodo(nextObjeto1, pza1, H_acomodo1);
			if (acomodo1)
			{
				nextObjeto1.addPreliminarPieza(pza1);  //se añade pza como 'borrador'
				//no altera el FreeArea de objeto.

				// si puede acomodar pza1, prueba con cuál pza2 entra simultáneamente.
				for(int j=0; j<listapiezas1.size(); j++)
				{
					pza2 = (Piece)listapiezas1.get(j);

					if ( (arealibre-pza1.getTotalSize()-pza2.getTotalSize()) >  w1)
					{
						break;  // si con pza2 elegida se deja + w, con las sig. pzas2 también lo haría.
					}

					if ( (pza1.getTotalSize() + pza2.getTotalSize()) > arealibre 
							 || i == j  || puedeAcomodar[numObj][j] == 1
							 || puedeAcomodar2[numObj][i][j] == 1 )   
					{	//ADVERTENCIA: Aquí no debería verse si pza2 ya fue probada y no cupo, ya que con pza1 en el 
						//objeto, ahora podría caber (se enlistan más posiciones,
						//pza1, podría ser un 'puentecito' en BL, etc).  Pero sería muy improbable.	En BL es más probable
						//que suceda (se vio experimentalmente), por eso, en esa H de acomodo no se considera.
						continue;   // a la sig. pza 2.
					}

					pza2.desRotar();
					acomodo2 = acomodo.HAcomodo(nextObjeto1, pza2, H_acomodo1);
					if (acomodo2)
					{
						nextObjeto1.removePreliminarPieza(pza1);  //se borra el pegado preliminar.
						nextObjeto1.addPieza(pza1);  // se añade definitivamente.
						nextObjeto1.addPieza(pza2);
						listapiezas1.remove(pza1);
						listapiezas1.remove(pza2);
						getOut = true;  // indica que ya acomodó 2 piezas.
						return;
					}else{ 
						puedeAcomodar2[numObj][i][j] = 1;  //pieces i & j cannot be placed in the object.
					}
				} //termina de revisar posibles pzas 2.

				nextObjeto1.removePreliminarPieza(pza1);    //Ninguna pza2 entró con la posible pza1.  
				//Se borra el preliminar de pza1
			} else{  
				puedeAcomodar[numObj][i] = 1;   //registra q esa pza no cabe en ese objeto.
			}
		}  //termina de revisar posibles pzas 1.

		return;
	}

	
	private static void dospiezas_1D(List<Piece> listapiezas1, Sheet nextObjeto1, String H_acomodo1, int w1)
	{
		HeuristicsPlacement acomodo = new HeuristicsPlacement();
		Piece pza1, pza2;
		boolean acomodo1 = false, acomodo2 = false;
		int area0, area1;  //guardará el área de las 2 piezas más grandes.
		int areaU;		   //guardará el área de la pieza más pequeña.
		int arealibre;
		pza1 = (Piece)listapiezas1.get(listapiezas1.size()-1);  
		areaU = pza1.getTotalSize();
		arealibre = nextObjeto1.getFreeArea();
		
		//verificando si cabrían 2 piezas con ese desperdicio máximo permitido.
		// RECEIVES PIECES IN DESCENDING ORDER OF SIZE.
		pza1 = (Piece)listapiezas1.get(0);
		pza2 = (Piece)listapiezas1.get(1);
		area0 = pza1.getTotalSize();
		area1 = pza2.getTotalSize();
		if( (arealibre-area0-area1) > w1)
		{
			return;
		}

		for(int i=0; i<listapiezas1.size(); i++)
		{
			acomodo1 = false;
			acomodo2 = false;
			pza1 = (Piece)listapiezas1.get(i);

			if(arealibre - pza1.getTotalSize()-area0 > w1)
			{
				break;  // con pza1 y la más grande dejan más w, ya no tiene caso probar + pzas1.
			}

			if(pza1.getTotalSize()+areaU > arealibre )  
			{
				continue;  	// a la sig. pza 1. Pza1 + la mas chica se pasarían del área disponible.
			}				// o bien, ya se sabe que pza 1 no se puede acomodar.

			pza1.desRotar();
			acomodo1 = acomodo.HAcomodo(nextObjeto1, pza1, H_acomodo1);
			if (acomodo1)
			{
				nextObjeto1.addPreliminarPieza(pza1);  //se añade pza como 'borrador'
				//no altera el FreeArea de objeto.

				// si puede acomodar pza1, prueba con cuál pza2 entra simultáneamente.
				for(int j=0; j<listapiezas1.size(); j++)
				{
					pza2 = (Piece)listapiezas1.get(j);

					if ( (arealibre-pza1.getTotalSize()-pza2.getTotalSize()) >  w1)
					{
						break;  // si con pza2 elegida se deja + w, con las sig. pzas2 también lo haría.
					}
					
					if ( (pza1.getTotalSize() + pza2.getTotalSize()) > arealibre || i==j)  
					{	//ADVERTENCIA: Aquí no debería verse si pza2 ya fue probada y no cupo, ya que con pza1 en el 
						//objeto, ahora podría caber (se enlistan más posiciones,
						//pza1, podría ser un 'puentecito' en BL, etc).  Pero sería muy improbable.	En BL es más probable
						//que suceda (se vio experimentalmente), por eso, en esa H de acomodo no se considera.
						continue;   // a la sig. pza 2.
					}

					pza2.desRotar();
					acomodo2 = acomodo.HAcomodo(nextObjeto1, pza2, H_acomodo1);
					if (acomodo2)
					{
						nextObjeto1.removePreliminarPieza(pza1);  //se borra el pegado preliminar.
						nextObjeto1.addPieza(pza1);  // se añade definitivamente.
						nextObjeto1.addPieza(pza2);
						listapiezas1.remove(pza1);
						listapiezas1.remove(pza2);
						getOut = true;  // indica que ya acomodó 2 piezas.
						return;
					}
				} //termina de revisar posibles pzas 2.

				nextObjeto1.removePreliminarPieza(pza1);    //Ninguna pza2 entró con la posible pza1.  
				//Se borra el preliminar de pza1
			}
		}  //termina de revisar posibles pzas 1.

		return;
	}


	private void trespiezas(List<Piece> listapiezas1, Sheet nextObjeto1, String H_acomodo1, int w1)
	{
		HeuristicsPlacement acomodo = new HeuristicsPlacement();
		Piece pza1, pza2, pza3;
		boolean acomodo1 = false, acomodo2 = false, acomodo3 = false;
		int area0, area1, area2;  //guardará el área de las 3 piezas más grandes.
		int areaU1, areaU2;		  //guardará el área de las 2 piezas más pequeñas.
		int arealibre;
		int numObj;  
		pza1 = (Piece)listapiezas1.get(listapiezas1.size()-1);  
		pza2 = (Piece)listapiezas1.get(listapiezas1.size()-2);  
		areaU1 = pza1.getTotalSize();
		areaU2 = pza2.getTotalSize();
		arealibre = nextObjeto1.getFreeArea();
		numObj = nextObjeto1.getNumObjeto(); 

		//verificando si cabrían 3 piezas con ese desperdicio máximo permitido.
		// RECEIVES PIECES IN DESCENDING ORDER OF SIZE.
		pza1 = (Piece)listapiezas1.get(0);
		pza2 = (Piece)listapiezas1.get(1);
		pza3 = (Piece)listapiezas1.get(2);
		area0 = pza1.getTotalSize();
		area1 = pza2.getTotalSize();
		area2 = pza3.getTotalSize();
		if( (arealibre-area0-area1-area2) > w1)
		{
			return;
		}

		for(int i=0; i<listapiezas1.size(); i++)
		{
			acomodo1 = false;
			acomodo2 = false;
			acomodo3 = false;

			pza1 = (Piece)listapiezas1.get(i);
			if(arealibre-pza1.getTotalSize()-area0-area1 > w1)
			{
				break;  // esa pza 1 no es 'compatible' con ningun otro par de piezas
			}			// sin pasarse del desperdicio máximo permitido.
			if(pza1.getTotalSize() +areaU1 + areaU2> arealibre 
					 || puedeAcomodar[numObj][i] == 1 )   
			{
				continue;  	// a la sig. pza 1.  Pza1 + las2+chicas se pasarían del área libre.
			}				// o bien, ya se sabe que pza 1 no se puede acomodar.

			pza1.desRotar();
			acomodo1 = acomodo.HAcomodo(nextObjeto1, pza1, H_acomodo1);
			if (acomodo1)
			{
				nextObjeto1.addPreliminarPieza(pza1);  //se añade pza1 como 'borrador'
				// no altera el FreeArea de objeto.
				// si puede acomodar pza1, prueba con cuál pza2 entra simultáneamente.
				for(int j=0; j<listapiezas1.size(); j++)
				{
					pza2 = (Piece)listapiezas1.get(j);

					if(arealibre-pza1.getTotalSize()-pza2.getTotalSize()-area0 > w1)
					{
						break;  // las pzas 1-2 no son 'compatibles' con ninguna otra pieza
					}			// sin pasarse del desperdicio máximo permitido.

					if ( (pza1.getTotalSize() + pza2.getTotalSize()+areaU1) > arealibre  
							 || i == j  || puedeAcomodar[numObj][j] == 1    //Same warning from "dospiezas" method 
							 || puedeAcomodar2[numObj][i][j] == 1 )         
					{     
						continue;   // a la sig. pza 2.  Pza1+Pza2+MásChica se pasarían el área libre.
					}

					pza2.desRotar();
					acomodo2 = acomodo.HAcomodo(nextObjeto1, pza2, H_acomodo1);
					if (acomodo2)
					{
						nextObjeto1.addPreliminarPieza(pza2);  

						for(int k =0; k<listapiezas1.size(); k++)
						{
							pza3 = (Piece)listapiezas1.get(k);

							if ( (arealibre-pza1.getTotalSize()-pza2.getTotalSize()-pza3.getTotalSize()) > w1)
							{
								break;  // si con pza3 elegida se deja + w, con las sig. pzas3 (más chicas) también lo haría.
							}			// deja de revisar pzas3 y se pasa a la sig. pza2.

							if ( (pza1.getTotalSize()+pza2.getTotalSize()+pza3.getTotalSize()) > arealibre 
									 || i == k || j == k   
									 || puedeAcomodar[numObj][k] == 1  			
									 || (puedeAcomodar2[numObj][i][k] == 1)     
								     || (puedeAcomodar2[numObj][j][k] == 1)		
								     || (puedeAcomodar3[numObj][i][j][k] == 1 ) )  //Same warning from "dospiezas" method
							{  
								continue;   // a la sig. pza 3.
							}

							pza3.desRotar();
							acomodo3 = acomodo.HAcomodo(nextObjeto1, pza3, H_acomodo1);
							if (acomodo3)
							{
								nextObjeto1.removePreliminarPieza(pza1);  //se borra el pegado preliminar.
								nextObjeto1.removePreliminarPieza(pza2);
								nextObjeto1.addPieza(pza1);  // se añaden definitivamente.
								nextObjeto1.addPieza(pza2);
								nextObjeto1.addPieza(pza3);
								listapiezas1.remove(pza1);
								listapiezas1.remove(pza2);
								listapiezas1.remove(pza3);
								getOut = true;  // indica que ya acomodó 3 piezas.
								return;
							}else{
								puedeAcomodar3[numObj][i][j][k] = 1;							
							}

						} //termina de revisar posibles pzas 3.

						nextObjeto1.removePreliminarPieza(pza2);    //Ninguna pza3 entró con la posible pza1y2.  
					}else{ 
						puedeAcomodar2[numObj][i][j] = 1;  //pieces i & j cannot be placed in the object.
					}

				} //termina de revisar posibles pzas 2.

				nextObjeto1.removePreliminarPieza(pza1);    //Ninguna pza2y3 entró con la posible pza1.  
				//Se borra el preliminar de pza1
			}else{ 
				puedeAcomodar[numObj][i] = 1;  //piece i cannot be placed in the object.
			}
		}  //termina de revisar posibles pzas 1.

		return;
	}


	
	private static void trespiezas_1D(List<Piece> listapiezas1, Sheet nextObjeto1, String H_acomodo1, int w1)
	{
		HeuristicsPlacement acomodo = new HeuristicsPlacement();
		Piece pza1, pza2, pza3;
		boolean acomodo1 = false, acomodo2 = false, acomodo3 = false;
		int area0, area1, area2;  //guardará el área de las 3 piezas más grandes.
		int areaU1, areaU2;		  //guardará el área de las 2 piezas más pequeñas.
		int arealibre;
		pza1 = (Piece)listapiezas1.get(listapiezas1.size()-1);  
		pza2 = (Piece)listapiezas1.get(listapiezas1.size()-2);  
		areaU1 = pza1.getTotalSize();
		areaU2 = pza2.getTotalSize();
		arealibre = nextObjeto1.getFreeArea();

		//verificando si cabrían 3 piezas con ese desperdicio máximo permitido.
		// RECEIVES PIECES IN DESCENDING ORDER OF SIZE.
		pza1 = (Piece)listapiezas1.get(0);
		pza2 = (Piece)listapiezas1.get(1);
		pza3 = (Piece)listapiezas1.get(2);
		area0 = pza1.getTotalSize();
		area1 = pza2.getTotalSize();
		area2 = pza3.getTotalSize();
		if( (arealibre-area0-area1-area2) > w1)
		{
			return;
		}

		for(int i=0; i<listapiezas1.size(); i++)
		{
			acomodo1 = false;
			acomodo2 = false;
			acomodo3 = false;

			pza1 = (Piece)listapiezas1.get(i);
			if(arealibre-pza1.getTotalSize()-area0-area1 > w1)
			{
				break;  // esa pza 1 no es 'compatible' con ningun otro par de piezas
			}			// sin pasarse del desperdicio máximo permitido.
			if(pza1.getTotalSize() +areaU1 + areaU2> arealibre )
			{
				continue;  	// a la sig. pza 1.  Pza1 + las2+chicas se pasarían del área libre.
			}				// o bien, ya se sabe que pza 1 no se puede acomodar.

			pza1.desRotar();
			acomodo1 = acomodo.HAcomodo(nextObjeto1, pza1, H_acomodo1);
			if (acomodo1)
			{
				nextObjeto1.addPreliminarPieza(pza1);  //se añade pza1 como 'borrador'
				//no altera el FreeArea de objeto.
				// si puede acomodar pza1, prueba con cuál pza2 entra simultáneamente.
				for(int j=0; j<listapiezas1.size(); j++)
				{
					pza2 = (Piece)listapiezas1.get(j);

					if(arealibre-pza1.getTotalSize()-pza2.getTotalSize()-area0 > w1)
					{
						break;  // las pzas 1-2 no son 'compatibles' con ninguna otra pieza
					}			// sin pasarse del desperdicio máximo permitido.

					if ( (pza1.getTotalSize() + pza2.getTotalSize()+areaU1) > arealibre || i == j)
					{      
						continue;   // a la sig. pza 2.  Pza1+Pza2+MásChica se pasarían el área libre.
					}

					pza2.desRotar();
					acomodo2 = acomodo.HAcomodo(nextObjeto1, pza2, H_acomodo1);
					if (acomodo2)
					{
						nextObjeto1.addPreliminarPieza(pza2);  

						for(int k =0; k<listapiezas1.size(); k++)
						{
							pza3 = (Piece)listapiezas1.get(k);

							if ( (arealibre-pza1.getTotalSize()-pza2.getTotalSize()-pza3.getTotalSize()) > w1)
							{
								break;  // si con pza3 elegida se deja + w, con las sig. pzas3 (más chicas) también lo haría.
							}			// deja de revisar pzas3 y se pasa a la sig. pza2.

							if ( (pza1.getTotalSize()+pza2.getTotalSize()+pza3.getTotalSize()) > arealibre 
									 || i == k || j == k   ) //Same warning from method "dospiezas"
							{   
								continue;   // a la sig. pza 3.
							}

							pza3.desRotar();
							acomodo3 = acomodo.HAcomodo(nextObjeto1, pza3, H_acomodo1);
							if (acomodo3)
							{
								nextObjeto1.removePreliminarPieza(pza1);  //se borra el pegado preliminar.
								nextObjeto1.removePreliminarPieza(pza2);
								nextObjeto1.addPieza(pza1);  // se añaden definitivamente.
								nextObjeto1.addPieza(pza2);
								nextObjeto1.addPieza(pza3);
								listapiezas1.remove(pza1);
								listapiezas1.remove(pza2);
								listapiezas1.remove(pza3);
								getOut = true;  // indica que ya acomodó 3 piezas.
								return;
							}

						} //termina de revisar posibles pzas 3.

						nextObjeto1.removePreliminarPieza(pza2);    //Ninguna pza3 entró con la posible pza1y2.  
					}

				} //termina de revisar posibles pzas 2.

				nextObjeto1.removePreliminarPieza(pza1);    //Ninguna pza2y3 entró con la posible pza1.  
				//Se borra el preliminar de pza1
			}
		}  //termina de revisar posibles pzas 1.

		return;
	}


	

	// Next_Fit: acomoda la primera pieza en el último objeto (nextObjeto) si cabe, si no, abre un objeto nuevo 
	// para acomodar la pieza. 
	// El último objeto abierto es el único posible, los anteriores ya no están disponibles.
	public static void Next_Fit(List<Piece> listapiezas, List<Sheet> listaObjetos, int xObjeto, int yObjeto, String H_acomodo)
	{
		boolean acomodopieza = false;
		boolean encontroObjeto = false;
		HeuristicsPlacement acomodo = new HeuristicsPlacement();
		pza = (Piece)listapiezas.get(0);
		int j = listaObjetos.size()-1;
		nextObjeto = (Sheet)listaObjetos.get(j);

		//Revisa si la pieza cabe en el último objeto
		if (pza.getTotalSize() <= nextObjeto.getFreeArea())
		{
			pza.desRotar();
			acomodopieza = acomodo.HAcomodo(nextObjeto, pza, H_acomodo);
			if (acomodopieza)
			{
				nextObjeto.addPieza(pza);
				listapiezas.remove(pza);
				encontroObjeto = true;
			}
		}

		if (!encontroObjeto)
		{
			//Abre un nuevo Objeto vacio
			nextObjeto=abreNuevoObjeto(listaObjetos, xObjeto, yObjeto);
			pza.desRotar();
			acomodopieza = acomodo.HAcomodo(nextObjeto, pza, H_acomodo);
			if (acomodopieza)  //el objeto es nuevo, siempre debería poder acomodar la pza.
			{
				nextObjeto.addPieza(pza);
				listapiezas.remove(pza);
			}
		}
	} // fin de la heuristica



	// Antes de cada intento de acomodo,
	// pone la pieza en su posicion original (para llevar la bien la cuenta de las rotaciones).
	public static void Next_Fit_Decreasing(List<Piece> listapiezas, List<Sheet> listaObjetos, int xObjeto, int yObjeto, String H_acomodo)
	{
		boolean acomodopieza = false;
		boolean encontroObjeto = false;
		HeuristicsPlacement acomodo = new HeuristicsPlacement();
		pza = SearchGreatest(listapiezas);
		int j = listaObjetos.size()-1;
		nextObjeto = (Sheet)listaObjetos.get(j);

		//Revisa si la pieza cabe en el último objeto
		if (pza.getTotalSize() <= nextObjeto.getFreeArea())
		{
			pza.desRotar(); 
			acomodopieza = acomodo.HAcomodo(nextObjeto, pza, H_acomodo);
			if (acomodopieza)
			{
				nextObjeto.addPieza(pza);
				listapiezas.remove(pza);
				encontroObjeto = true;
			}
		}

		if (!encontroObjeto)
		{
			//Abre un nuevo Objeto vacio
			nextObjeto=abreNuevoObjeto(listaObjetos, xObjeto, yObjeto);
			pza.desRotar();
			acomodopieza = acomodo.HAcomodo(nextObjeto, pza, H_acomodo);
			if (acomodopieza)  //el objeto es nuevo, siempre debería poder acomodar la pza.
			{
				nextObjeto.addPieza(pza);
				listapiezas.remove(pza);
			}
		}
	} // fin de la heuristica


	// Best_Fit: acomoda la primera pieza en el primer objeto que quepa (ordenando los objetos de menor a mayor espacio disponible),
	// si no cabe en ninguno, abre un objeto nuevo y acomoda la pieza
	public static void Best_Fit(List<Piece> listapiezas, List<Sheet> listaObjetos, int xObjeto, int yObjeto, String H_acomodo)
	{
		boolean acomodopieza = false;
		boolean encontroObjeto = false;
		HeuristicsPlacement acomodo = new HeuristicsPlacement();
		int temp;
		Sheet objy, objym;
		int[] ordenObjetos = new int[listaObjetos.size()];
		pza = (Piece)listapiezas.get(0);

		for(int i = 0; i<listaObjetos.size(); i++)
		{
			ordenObjetos[i] = i;
		}
		//ordena objetos por área libre.
		for(int i =0; i<listaObjetos.size(); i++)
		{
			for(int j = 0; j<listaObjetos.size()-1; j++)
			{
				objy = (Sheet)listaObjetos.get(ordenObjetos[j]);
				objym = (Sheet)listaObjetos.get(ordenObjetos[j+1]);
				if(objy.getFreeArea() > objym.getFreeArea()) 
				{
					temp = ordenObjetos[j];
					ordenObjetos[j] = ordenObjetos[j+1];
					ordenObjetos[j+1] = temp;
				}
			}
		}


		//Acomoda la primera pieza.
		for(int i=0; i<listaObjetos.size(); i++)
		{
			nextObjeto = (Sheet)listaObjetos.get(ordenObjetos[i]);
			if (pza.getTotalSize() <= nextObjeto.getFreeArea())
			{
				pza.desRotar();
				acomodopieza = acomodo.HAcomodo(nextObjeto, pza, H_acomodo);
				if(acomodopieza)
				{
					nextObjeto.addPieza(pza);
					listapiezas.remove(pza);
					encontroObjeto = true;
					break;	 			
				}
			}
		}
		//si no cupo, abre un objeto nuevo.      
		if (!encontroObjeto)
		{
			nextObjeto=abreNuevoObjeto(listaObjetos, xObjeto, yObjeto);	
			pza.desRotar();
			acomodopieza = acomodo.HAcomodo(nextObjeto, pza, H_acomodo);
			if (acomodopieza)  //el objeto es nuevo, siempre debería poder acomodar la pza.
			{
				nextObjeto.addPieza(pza);
				listapiezas.remove(pza);
			}
		}

	} // fin de la heuristica



	// acomoda la pieza mayor en el primer objeto que quepa (ordenando los objetos de menor a mayor espacio disponible),
	// si no cabe en ninguno, abre un objeto nuevo y acomoda la pieza
	public void Best_Fit_Decreasing(List<Piece> listapiezas, List<Sheet> listaObjetos, int xObjeto, int yObjeto, String H_acomodo)
	{
		boolean acomodopieza = false;
		boolean encontroObjeto = false;
		HeuristicsPlacement acomodo = new HeuristicsPlacement();
		int temp;
		Sheet objy, objym;
		int[] ordenObjetos = new int[listaObjetos.size()];
		pza = SearchGreatest(listapiezas);

		for(int i = 0; i<listaObjetos.size(); i++)
		{
			ordenObjetos[i] = i;
		}
		//ordena objetos por área libre
		for(int i =0; i<listaObjetos.size(); i++)
		{
			for(int j = 0; j<listaObjetos.size()-1; j++)
			{
				objy = (Sheet)listaObjetos.get(ordenObjetos[j]);
				objym = (Sheet)listaObjetos.get(ordenObjetos[j+1]);
				if(objy.getFreeArea() > objym.getFreeArea())  
				{
					temp = ordenObjetos[j];
					ordenObjetos[j] = ordenObjetos[j+1];
					ordenObjetos[j+1] = temp;
				}
			}
		}

		for(int i=0; i<listaObjetos.size(); i++)
		{
			nextObjeto = (Sheet)listaObjetos.get(ordenObjetos[i]);
			if (pza.getTotalSize() <= nextObjeto.getFreeArea())
			{
				pza.desRotar();
				acomodopieza = acomodo.HAcomodo(nextObjeto, pza, H_acomodo);
				if(acomodopieza)
				{
					nextObjeto.addPieza(pza);
					listapiezas.remove(pza);
					encontroObjeto = true;
					break;	 			
				}
			}
		}

		if (!encontroObjeto)
		{
			nextObjeto=abreNuevoObjeto(listaObjetos, xObjeto, yObjeto);	
			pza.desRotar();
			acomodopieza = acomodo.HAcomodo(nextObjeto, pza, H_acomodo);
			if (acomodopieza)  //el objeto es nuevo, siempre debería poder acomodar la pza.
			{
				nextObjeto.addPieza(pza);
				listapiezas.remove(pza);
			}
		}
	} // fin de la heuristica



	public static void Worst_Fit(List<Piece> listapiezas, List<Sheet> listaObjetos, int xObjeto, int yObjeto, String H_acomodo)
	{
		boolean acomodopieza = false;
		boolean encontroObjeto = false;
		HeuristicsPlacement acomodo = new HeuristicsPlacement();
		int temp;
		Sheet objy, objym;
		int[] ordenObjetos = new int[listaObjetos.size()];
		pza = (Piece)listapiezas.get(0);

		for(int i = 0; i<listaObjetos.size(); i++)
		{
			ordenObjetos[i] = i;
		}
		//ordena objetos por área libre
		for(int i =0; i<listaObjetos.size(); i++)
		{
			for(int j = 0; j<listaObjetos.size()-1; j++)
			{
				objy = (Sheet)listaObjetos.get(ordenObjetos[j]);
				objym = (Sheet)listaObjetos.get(ordenObjetos[j+1]);
				if(objy.getFreeArea() < objym.getFreeArea())   
				{
					temp = ordenObjetos[j];
					ordenObjetos[j] = ordenObjetos[j+1];
					ordenObjetos[j+1] = temp;
				}
			}
		}


		//Acomoda la primera pieza.
		for(int i=0; i<listaObjetos.size(); i++)
		{
			nextObjeto = (Sheet)listaObjetos.get(ordenObjetos[i]);
			if (pza.getTotalSize() <= nextObjeto.getFreeArea())
			{
				pza.desRotar();
				acomodopieza = acomodo.HAcomodo(nextObjeto, pza, H_acomodo);
				if(acomodopieza)
				{
					nextObjeto.addPieza(pza);
					listapiezas.remove(pza);
					encontroObjeto = true;
					break;	 			
				}
			}
		}
		//si no cupo en objeto abierto, abre uno nuevo (para meter la pieza)
		if (!encontroObjeto)
		{
			nextObjeto=abreNuevoObjeto(listaObjetos, xObjeto, yObjeto);	
			pza.desRotar();
			acomodopieza = acomodo.HAcomodo(nextObjeto, pza, H_acomodo);
			if (acomodopieza)  //el objeto es nuevo, siempre debería poder acomodar la pza.
			{
				nextObjeto.addPieza(pza);
				listapiezas.remove(pza);
			}
		}
	} // fin de la heuristica



	private static Sheet abreNuevoObjeto(List<Sheet> listaObjetos, int xObjeto, int yObjeto)
	{
		int num = listaObjetos.size();
		Sheet nuevoObjeto = new Sheet(xObjeto, yObjeto, num);
		listaObjetos.add(nuevoObjeto);
		return nuevoObjeto;
	}


	// 1: Descendent,  other value: Ascending order.
	private static List<Piece> OrdenaPiezas(List<Piece> ListaPiezas, int Creciente)
	{
		Piece temporal;
		List<Piece> ListaOrdenadas = ListaPiezas;
		for (int x = 0; x < ListaOrdenadas.size(); x++)
		{
			for(int y=0; y < ListaOrdenadas.size()-1; y++)
			{
				if (Creciente == 1)
				{
					if (((Piece)(ListaOrdenadas.get(y))).getTotalSize()<((Piece)(ListaOrdenadas.get(y+1))).getTotalSize())
					{
						temporal=(Piece)(ListaOrdenadas.get(y));
						ListaOrdenadas.set(y, (Piece)(ListaOrdenadas.get(y+1)));
						ListaOrdenadas.set(y+1, temporal);	
					}
				}
				else
				{         
					if (((Piece)(ListaOrdenadas.get(y))).getTotalSize()>((Piece)(ListaOrdenadas.get(y+1))).getTotalSize())
					{
						temporal=(Piece)(ListaOrdenadas.get(y));
						ListaOrdenadas.set(y, (Piece)(ListaOrdenadas.get(y+1)));
						ListaOrdenadas.set(y+1, temporal);	
					}
				}
			}//for
		}//for
		return ListaOrdenadas;
	}


	private static List<Piece> AcomodoOriginalPiezas(List<Piece> ListaPiezas)
	{
		Piece temporal;
		List<Piece> ListaOrdenOriginal = ListaPiezas;
		for (int x = 0; x < ListaOrdenOriginal.size(); x++) 
		{
			for(int y=0; y < ListaOrdenOriginal.size()-1; y++)
			{
				if (((Piece)(ListaOrdenOriginal.get(y))).getnumber()>((Piece)(ListaOrdenOriginal.get(y+1))).getnumber())
				{
					temporal=(Piece)(ListaOrdenOriginal.get(y));
					ListaOrdenOriginal.set(y, (Piece)(ListaOrdenOriginal.get(y+1)));
					ListaOrdenOriginal.set(y+1, temporal);
				}
			}//for
		}//for
		return ListaOrdenOriginal;
	}

	private static Piece SearchGreatest(List<Piece> ListaPiezas)
	{	   
		Piece greatest = (Piece)ListaPiezas.get(0);
		for(int y=0; y < ListaPiezas.size(); y++)     
		{
			if (((Piece)(ListaPiezas.get(y))).getTotalSize()>greatest.getTotalSize())
			{
				greatest = (Piece)(ListaPiezas.get(y));
			}
		}
		return greatest;
	}

	private static Piece SearchSmallest(List<Piece> ListaPiezas)
	{
		Piece smallest = (Piece)ListaPiezas.get(0);
		for(int y=0; y < ListaPiezas.size(); y++)		
		{
			if (((Piece)(ListaPiezas.get(y))).getTotalSize()<(smallest).getTotalSize())
			{
				smallest = (Piece)(ListaPiezas.get(y));
			}
		}
		return smallest;
	}

}

