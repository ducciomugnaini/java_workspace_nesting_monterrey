package share;

import java.util.List;
import java.util.LinkedList;
import static java.lang.Math.*;

/*
 *  @author Dr Eunice López-Camacho
 * Tecnológico de Monterrey, Campus Monterrey
 * 
 * Please report any bugs to Eunice López-Camacho at eunice.lopez@itesm.mx
 * or lopezeunice@gmail.com
 */


public class HeuristicsPlacement{

	public HeuristicsPlacement(){}

	public boolean HAcomodo(Sheet objeto, Piece pieza, String Heur_Acom)
	{
		boolean acomodo=false;
		if(Heur_Acom.equals("BL"))
			acomodo = BLHeuristic(objeto, pieza);
		else if(Heur_Acom.equals("EC"))
			acomodo = ECHeuristic(objeto, pieza);
		else if(Heur_Acom.equals("EC2"))
			acomodo = EC2Heuristic(objeto, pieza);  
		else if(Heur_Acom.equals("MA"))
			acomodo = MAHeuristic(objeto, pieza);    

		return acomodo;
	}


	/** Heurística BL, trata de colocar pieza en objeto
	 *  Regresa:  true: si logra colocar pieza;  false: si no lo logra
	 */
	private boolean BLHeuristic(Sheet objeto, Piece pieza)
	{
		//Coloca la pieza en la parte superior derecha del objeto (justo afuera del objeto)
		pieza.moveToXY( objeto.getXmax(), objeto.getYmax(), 1);
		
		this.runIntoBottomLeftPlacement(objeto, pieza); 
	    
		if(pieza.getYmax() <= objeto.getYmax() && pieza.getXmax() <= objeto.getXmax()&& pieza.getXmin()>=0 && pieza.getYmin()>=0) 
		{
			if( posicionValida(objeto, pieza) )   // this is needed, BL  movement could end in overlapping
			{
				return true;
			}
		}
		return false;
	}



	/**
	 *  Heurística EC (enfoque constructivo), trata de colocar pieza en objeto
	 *  Regresa:  true: si logra colocar pieza
	 *			 false: si no lo logra	 */  
	private boolean ECHeuristic(Sheet objeto, Piece pieza)
	{
		boolean value;
		int[] posicion = new int[2]; 
		double angulo;
		double anguloPrevio = 0;
		double mejorRotacion = 0;		//guardará el ángulo que corresponde a la mejor posición.

		// Inicialmente, la 'mejor' posición es la esquina superior derecha del objeto.
		int mejorX = objeto.getXmax();   
		int mejorY = objeto.getYmax();  

		List<Piece> listapzas = new LinkedList<Piece>();
		List<int[]> listapuntos = new LinkedList<int[]>();
		List<Double> rotaciones = new LinkedList<Double>();
		int X;       	//variables temporales.
		int Y;

		listapzas = objeto.getPzasInside();
		listapuntos = objeto.getPosiciones();

		//rotaciones = rotacionesAProbar(pieza.getAngulosPieza(), objeto.getAngulos());   
		rotaciones = rotacionesAProbar4();  
		// ****  la primera pieza será acomodada.
		if (listapzas.isEmpty())
		{
			for (int i = 0; i < rotaciones.size(); i++)			{
				angulo = (double)rotaciones.get(i);
				if(i == 0)   
					pieza.rotate(angulo);
				else
					pieza.rotate(angulo-anguloPrevio);
				value = BLHeuristic(objeto, pieza);  // si 'pieza' será la primera pieza del objeto, la acomoda con BL.
				if( value ){
					X = pieza.getXmin();
					Y = pieza.getYmin();
					if(X < mejorX){
						mejorX = X;
						mejorY = Y;
						mejorRotacion = angulo;
					}
					if(X == mejorX && Y < mejorY){
						mejorY = Y;
						mejorRotacion = angulo;
					}
				}
				anguloPrevio = angulo;
			}  //termina de revisar rotaciones.

			if(mejorX == objeto.getXmax() && mejorY == objeto.getYmax())  //nunca hubo una posición válida  :(
			{
				return false;   //se supone que esto no va a pasar, porque a fuerzas debe poder meter la pieza en objeto vacío.
			}

			pieza.desRotar();  //lo vuelve a poner en rotación 0.
			pieza.rotate(mejorRotacion);
			pieza.moveToXY(mejorX, mejorY, 2); // pone la pieza en la posicion mejor. Ya está validada  :)
			return true;	
		} 

		// ****  no es la primer pieza a acomodar.
		// Encuentra la mejor posición (X,Y) para colocar la esquina inferior izquierda de la pieza.
		for (int i = 0; i < rotaciones.size(); i++)
		{
			angulo = (double)rotaciones.get(i);
			if(i== 0)   
				pieza.rotate(angulo);
			else
				pieza.rotate(angulo-anguloPrevio);
			for (int j = 0; j < listapuntos.size(); j++){
				posicion = (int[])listapuntos.get(j);
				pieza.moveToXY(posicion[0], posicion[1], 2); // pone la pieza en la posicion candidata (esquina inf. izq).
				runIntoBottomLeftPlacement(objeto, pieza);

				if( posicionValida(objeto, pieza) == false) 
					continue;

				X = pieza.getXmin();
				Y = pieza.getYmin();

				if(X < mejorX){
					mejorX = X;
					mejorY = Y;
					mejorRotacion = angulo;
					continue;
				}
				if(X == mejorX && Y < mejorY){
					mejorY = Y;
					mejorRotacion = angulo;
				}
			}//para una rotación, termina de revisar puntos.
			anguloPrevio = angulo;
		}//termina de revisar rotaciones
		if(mejorX == objeto.getXmax() && mejorY == objeto.getYmax())  //nunca hubo una posición válida  :(
		{
			return false;
		}

		pieza.desRotar();  //lo vuelve a poner en rotación 0.
		pieza.rotate(mejorRotacion);
		pieza.moveToXY(mejorX, mejorY, 2); // pone la pieza en la posicion mejor. Ya está validada  :)
		return true;	
	}


	/** Heurística EC2 (enfoque constructivo de mínima área), trata de colocar pieza en objeto.
	 *  Regresa:  true: si logra colocar pieza.  false: si no lo logra */
	private boolean EC2Heuristic(Sheet objeto, Piece pieza)
	{  
		boolean value;
		int[] posicion = new int[2]; 
		double angulo;
		double anguloPrevio = 0;

		// Inicialmente, la 'mejor' área es toda el área del objeto + 1.
		// y la 'mejor' posición se asigna en la esquina superior derecha del objeto.
		int menorAreaOcupada = objeto.getXmax() * objeto.getYmax() + 1;
		int mejorX = objeto.getXmax();   
		int mejorY = objeto.getYmax(); 
		double mejorRotacion = 0;		//guardará el ángulo que corresponde a la mejor posición.

		List<Piece> listapzas = new LinkedList<Piece>();
		List<int[]> listapuntos = new LinkedList<int[]>();
		List<Double> rotaciones = new LinkedList<Double>();
		int X;       	// variables temporales.  Serán las máximas coordenadas
		int Y;			// en X y Y de c/posición posible.

		listapzas = objeto.getPzasInside();
		listapuntos = objeto.getPosiciones();
		//rotaciones = rotacionesAProbar(pieza.getAngulosPieza(), objeto.getAngulos());
		rotaciones = rotacionesAProbar4();  


		// ****  la primera pieza será acomodada.
		if (listapzas.isEmpty())
		{
			for (int i = 0; i < rotaciones.size(); i++)
			{
				angulo = (double)rotaciones.get(i);

				if(i == 0)   
				{ 
					pieza.rotate(angulo);
				}else
				{
					pieza.rotate(angulo-anguloPrevio);
				}

				value = BLHeuristic(objeto, pieza);  // si 'pieza' será la primera pieza del objeto, la acomoda con BL.

				if( value ) 
				{
					X = pieza.getXmax();      
					Y = pieza.getYmax();      
					if(X*Y < menorAreaOcupada)
					{
						mejorX = pieza.getXmin();  //referencia abajo-izq.
						mejorY = pieza.getYmin();
						menorAreaOcupada = X*Y;
						mejorRotacion = angulo;
					}
				}

				anguloPrevio = angulo;

			}  //termina de revisar rotaciones.


			if(mejorX == objeto.getXmax() && mejorY == objeto.getYmax())  //nunca hubo una posición válida  :(
			{
				return false;   //se supone que esto no va a pasar, porque debe poder meter la pieza en objeto vacío.
			}

			pieza.desRotar();  //lo vuelve a poner en rotación 0.
			pieza.rotate(mejorRotacion);
			pieza.moveToXY(mejorX, mejorY, 2); // pone la pieza en la posicion mejor.
			return true;	
		}  


		// ****  no es la primer pieza a acomodar.
		// Encuentra la mejor posición (X,Y) para colocar la esquina inf. izq de la pieza.
		for (int i = 0; i < rotaciones.size(); i++)
		{   
			angulo = (double)rotaciones.get(i);

			if(i== 0)   
			{ 
				pieza.rotate(angulo);
			}else
			{
				pieza.rotate(angulo-anguloPrevio);
			}

			for (int j = 0; j < listapuntos.size(); j++)
			{
				posicion = (int[])listapuntos.get(j);
				pieza.moveToXY(posicion[0], posicion[1], 2); // pone la pieza en la posicion candidata (esquina inf. izq).

				runIntoBottomLeftPlacement(objeto, pieza);

				if( posicionValida(objeto, pieza) ) 
				{
					X = max(pieza.getXmax(), objeto.getMaximaX() );      
					Y = max(pieza.getYmax(), objeto.getMaximaY() );      
					if(X*Y < menorAreaOcupada)
					{
						mejorX = pieza.getXmin();
						mejorY = pieza.getYmin();
						menorAreaOcupada = X*Y;
						mejorRotacion = angulo;
					}
				}
			} //para una rotación, termina de revisar puntos.

			anguloPrevio = angulo;

		}//termina de revisar rotaciones


		if(mejorX == objeto.getXmax() && mejorY == objeto.getYmax())  //nunca hubo una posición válida  :(
		{
			return false;
		}

		pieza.desRotar();  //lo vuelve a poner en rotación 0.
		pieza.rotate(mejorRotacion);
		pieza.moveToXY(mejorX, mejorY, 2); // pone la pieza en la posicion mejor. Ya está validada  :)
		return true;	
	}



	/** Heurística MA (máxima adyacencia), trata de colocar pieza en objeto.
	 *  Con la lista de posiciones, elige la de mayor adyacencia.
	 *  La 1era pieza la acomoda con BL.  
	 *  Nota: Cada posición la prueba, antes y después de hacer runintobottomleft..
	 *  Regresa: true: si logra colocar pieza
	 *			 false: si no lo logra */
	private boolean MAHeuristic(Sheet objeto, Piece pieza)
	{
		boolean temp;
		int[] posicion = new int[2]; 
		int mejorAdyacencia = 0;
		int adyacencia = 0;
		double angulo;
		double anguloPrevio = 0;

		// Sólo para empezar con algo, la mejor posición inicial se fija ahí.
		int mejorX = objeto.getXmax();     	//el pto de referencia es la esq. inf izq.
		int mejorY = objeto.getYmax();  
		double mejorRotacion = 0;		//guardará el ángulo que corresponde a la mejor posición.

		List<int[]> listapuntos = new LinkedList<int[]>();
		List<Double> rotaciones = new LinkedList<Double>();
		int X;       	//variables temporales.
		int Y;
		listapuntos = objeto.getPosiciones();

		//rotaciones = rotacionesAProbar(pieza.getAngulosPieza(), objeto.getAngulos());   
		rotaciones = rotacionesAProbar4();  

		// Encuentra la posición (X,Y) de mayor adyacencia para colocar la esquina inferior izquierda de la pieza.
		for (int i = 0; i < rotaciones.size(); i++)
		{
			angulo = (double)rotaciones.get(i);

			if(i== 0)   
			{ 
				pieza.rotate(angulo);
			}else
			{
				pieza.rotate(angulo-anguloPrevio);
			}

			for (int j = 0; j < listapuntos.size(); j++)
			{
				posicion = (int[])listapuntos.get(j);

				// Las posiciones 0,1,2 y 3 de la lista corresponden a las esquinas del objeto.
				if(j == 1)  // es la esquina inf derecha del objeto
				{
					pieza.moveToXY(posicion[0], posicion[1], 1); // pone la pieza en la posicion candidata (esquina inf. der).
				}else if (j == 2)  // es la esquina sup derecha del objeto
				{
					pieza.moveToXY(posicion[0], posicion[1], 3); // pone la pieza en la posicion candidata (esquina sup. der).
				}else if (j == 3)  // es la esquina sup izq del objeto
				{
					pieza.moveToXY(posicion[0], posicion[1], 4); // pone la pieza en la posicion candidata (esquina sup. izq).
				}else 
				{
					pieza.moveToXY(posicion[0], posicion[1], 2); // pone la pieza en la posicion candidata (esquina inf. izq).
				}


				if(posicionValida(objeto, pieza))
				{
					adyacencia = adyacenciaOP(objeto, pieza);
					if( adyacencia == mejorAdyacencia) 
					{
						X = pieza.getXmin();      
						Y = pieza.getYmin();
						if(X < mejorX)
						{
							mejorX = X;
							mejorY = Y;
							mejorRotacion = angulo;
						}
						if(X == mejorX && Y < mejorY)
						{
							mejorY = Y;
							mejorRotacion = angulo;
						}
					}
					else if( adyacencia > mejorAdyacencia) 
					{
						mejorX = pieza.getXmin();      
						mejorY = pieza.getYmin();
						mejorAdyacencia = adyacencia;    
						mejorRotacion = angulo;
					}  
				}

				temp = this.runIntoBottomLeftPlacement(objeto, pieza);  //mover la pieza.
				if(temp)
				{
					adyacencia = adyacenciaOP(objeto, pieza);   
					if(posicionValida(objeto, pieza))
					{
						if( adyacencia == mejorAdyacencia) 
						{
							X = pieza.getXmin();      
							Y = pieza.getYmin();
							if(X < mejorX)
							{
								mejorX = X;
								mejorY = Y;
								mejorRotacion = angulo;
							}
							if(X == mejorX && Y < mejorY)
							{
								mejorY = Y;
								mejorRotacion = angulo;
							}
						}
						else if( adyacencia > mejorAdyacencia) 
						{
							mejorX = pieza.getXmin();      
							mejorY = pieza.getYmin();
							mejorAdyacencia = adyacencia;
							mejorRotacion = angulo;
						}  
					}
				}
			}  //para una rotación, termina de revisar puntos.

			anguloPrevio = angulo;

		}//termina de revisar rotaciones       	    

		if(mejorX == objeto.getXmax() && mejorY == objeto.getYmax())  //nunca hubo una posición válida  :(
		{
			return false;
		}

		pieza.desRotar();  //lo vuelve a poner en rotación 0.
		pieza.rotate(mejorRotacion);
		pieza.moveToXY(mejorX, mejorY, 2); // pone la pieza en la posicion mejor. Ya está validada  :)
		return true;	
	}



	//Mueve el ItemOut hasta una posicion estable lo mas abajo y a la izquierda posible
	//y devuelve TRUE si hubo movimiento y FALSE si no hubo.
	//Importante: Si originalmente la pieza está empalmada con otra pieza, la pieza 
	//  con la que está empalmada se ignora.  Esto tiene la ventaja de que el movimiento
	//  BL puede hacer que se salga de la pieza empalmada (y la desventaja de el mov. BL
	//  puede hacer que se quede empalmada otra vez con la misma pieza original). 
	//  Por eso, en las heurísticas, después del movimiento BL, debe revisarse si 
	//  la pieza se encuentra en posición válida.
	private boolean runIntoBottomLeftPlacement(Sheet objeto, Piece pieza)
	{
		int distVertical;
		int distHorizontal;
		int xpos = pieza.getXmin();
		int ypos = pieza.getYmin();
		int numgrande = 100000;  //es el valor que devuelven los métodos de cercanía cuando una pieza no alcanza a la otra.

		do 
		{
			distVertical = cercaniaVerOP(objeto, pieza);
			if(distVertical > 0 && distVertical < numgrande)
			{
				pieza.moveDistance(distVertical, 2);  //Down
			}

			distHorizontal = cercaniaHorOP(objeto, pieza);
			if(distHorizontal > 0 && distHorizontal < numgrande) 
			{
				pieza.moveDistance(distHorizontal, 3);  //Left
			}

		}while( (distHorizontal > 0 && distHorizontal < numgrande)  
				|| (distVertical > 0 && distVertical < numgrande)  );  

		if (xpos == pieza.getXmin() && ypos == pieza.getYmin())
		{    // si no seacomodo la pieza
			return false;
		}
		return true;
	}

	
	
	/* Dada una pieza, devuelve la pieza virtual que corresponde al área
	 * que está justo debajo */
	private static PieceVirtual piezaAbajo(Piece pza)
	{
		int[] punto1 = new int[2];
		int[] punto2 = new int[2];
		int vert1, vert2, vertices;
		int puntos = 0;  //núm. de vértices que tendrá la pieza virtual.
		PieceVirtual pzatemp;
		
		vertices = pza.getvertices();
		int[] coord1 = new int[vertices*2];		// coordenadas X y Y de la pieza repetidas 2 veces.
	    int[] coord2 = new int[vertices*2];
		for (int i=0; i < vertices; i++)
		{
			coord1[i]= pza.coordX[i];
			coord2[i]= pza.coordY[i];
			coord1[i+vertices]= pza.coordX[i];
			coord2[i+vertices]= pza.coordY[i];
		}
		punto1[0] = pza.getXmin();
		punto1[1] = coordenadaLB(pza);
		punto2[0] = pza.getXmax();
		punto2[1] = coordenadaRB(pza);

		vert1 = pza.numVertice(punto1);
		vert2 = pza.numVertice(punto2);
		for (int i=0; i < vertices; i++)
		{
			coord1[i]= pza.coordX[i];
			coord2[i]= pza.coordY[i];
			coord1[i+vertices]= pza.coordX[i];
			coord2[i+vertices]= pza.coordY[i];
		}

		if(vert1 > vert2)  
		{
			puntos = (vert2 + vertices) - vert1 + 3;
		}else   //vert1<vert2, ya que vert1 y vert2 no pueden ser iguales.
		{
			puntos = vert2 - vert1 + 3;
		}
		
		
		int[] coordenadas = new int[puntos*2];
		coordenadas[0] = punto1[0];
		coordenadas[1] = punto1[1];
		coordenadas[2] = punto1[0];
		coordenadas[3] = 0;
		coordenadas[4] = punto2[0];
		coordenadas[5] = 0;
		coordenadas[6] = punto2[0];
		coordenadas[7] = punto2[1];
		if(puntos > 4){
			for (int i=4; i < puntos; i++)
			{
				coordenadas[i*2]  = coord1[vert1+(puntos-4)];
				coordenadas[i*2+1]= coord2[vert1+(puntos-4)];
			}
		}
		pzatemp  = new PieceVirtual(coordenadas); 
        return pzatemp;
	}


	/* Dada una pieza, devuelve la pieza virtual que corresponde al área
	 * que está justo a la izquierda */
	private static PieceVirtual piezaIzq(Piece pza)
	{
		int[] punto1 = new int[2];
		int[] punto2 = new int[2];
		int vert1, vert2, vertices;
		int puntos = 0;  //núm. de vértices que tendrá la pieza virtual.
		PieceVirtual pzatemp;
		
		vertices = pza.getvertices();
		int[] coord1 = new int[vertices*2];		// coordenadas X y Y de la pieza repetidas 2 veces.
	    int[] coord2 = new int[vertices*2];
		for (int i=0; i < vertices; i++)
		{
			coord1[i]= pza.coordX[i];
			coord2[i]= pza.coordY[i];
			coord1[i+vertices]= pza.coordX[i];
			coord2[i+vertices]= pza.coordY[i];
		}
		punto1[0] = coordenadaTL(pza);
		punto1[1] = pza.getYmax();
		punto2[0] = coordenadaBL(pza);
		punto2[1] = pza.getYmin();

		vert1 = pza.numVertice(punto1);
		vert2 = pza.numVertice(punto2);
		for (int i=0; i < vertices; i++)
		{
			coord1[i]= pza.coordX[i];
			coord2[i]= pza.coordY[i];
			coord1[i+vertices]= pza.coordX[i];
			coord2[i+vertices]= pza.coordY[i];
		}

		if(vert1 > vert2)  
		{
			puntos = (vert2 + vertices) - vert1 + 3;
		}else   //vert1<vert2, ya que vert1 y vert2 no pueden ser iguales.
		{
			puntos = vert2 - vert1 + 3;
		}
		
		if(puntos == 4)
		{
			int[] coordenadas = {0, punto1[1], 0, punto2[1], punto2[0],punto2[1],
					             punto1[0],punto1[1]};  
			pzatemp  = new PieceVirtual(coordenadas);  
		}else if(puntos == 5)
		{
			int[] coordenadas = {0, punto1[1], 0, punto2[1],
					             punto2[0], punto2[1], 
					             coord1[vert1+1],coord2[vert1+1],
                                 punto1[0],punto1[1]};  
			pzatemp  = new PieceVirtual(coordenadas);
		}else
		{
			int[] coordenadas = new int[puntos*2];
			coordenadas[0] = 0;
			coordenadas[1] = punto1[1];
			coordenadas[2] = 0;
			coordenadas[3] = punto2[1];
			coordenadas[4] = punto2[0];
			coordenadas[5] = punto2[1];
			int j = puntos - 4;
			for(int i=3; i<puntos-1; i++)
			{
				coordenadas[i*2]   = coord1[vert1+j];
				coordenadas[i*2+1] = coord2[vert1+j];
				j--;
			}
			coordenadas[puntos*2-2] = punto1[0];
			coordenadas[puntos*2-1] = punto1[1];
			pzatemp  = new PieceVirtual(coordenadas);
		}
		return pzatemp;
		
	}
	
	
	
	// Compara contra todos las piezas del objeto (que están a su izq),
	// y encuentra cuál es la distancia horizontal que puede
	// moverse sin chocar con ninguna pieza.
	private static int cercaniaHorOP(Sheet objeto, Piece piezaOut)
	{	
		int distancia = 0;
		int minima = 100000;  //un número muy grande.
		Piece pzaIn;
		Piece pzaIzq;
		if(piezaOut.getXmin() == 0)
		{
			return 0;
		}
		
		// Pieza imaginaria que incluye la zona a la izquierda de piezaOut.
		PieceVirtual pzatemp = piezaIzq(piezaOut);
		List<Piece> pzasInside;    // piezas del objeto.
		List<Piece> pzasIzq = new LinkedList<Piece>();    // piezas a la izq. de piezaOut. Si desplazáramos piezaOut
		// indefinidamente hacia la izq. chocaría con pzasIzq.
		pzasInside = objeto.getPzasInside();

		// Caso en que el objeto está vacío.
		if (pzasInside.isEmpty())
		{
			minima = piezaOut.getXmin();  
		}
		else {   
			// Genera la lista de piezas a la izquierda
			for (int i = 0; i < pzasInside.size(); i++)
			{
				pzaIn = (Piece)pzasInside.get(i);
				if ( (interseccionPPV(pzaIn, pzatemp) || dentroPPV(pzaIn, pzatemp) )
						&& interseccionPP(pzaIn, piezaOut) == false   //esta condición es útil en EC, EC2 y MA.
						&& dentroPP(pzaIn, piezaOut) == false )		  //esta condición es útil en EC, EC2 y MA.
				{													  //ya que si la pza estuviera dentro de otra, puede salir.	
					pzasIzq.add(pzaIn);
				}
			}	

			// Termina de generar lista de piezas a la izquierda, empieza a medir.
			// Caso en que no hay piezas a la izquierda de piezaOut.
			if (pzasIzq.isEmpty())
			{
				minima = piezaOut.getXmin();  
			}
			else {
				// Calcula la distancia entre piezaOut y cada una de pzasIzq
				for (int i = 0; i < pzasIzq.size(); i++)
				{
					pzaIzq = (Piece)pzasIzq.get(i);
					distancia = cercaniaHorPP(pzaIzq, piezaOut);
					if ( distancia < minima)
					{ 
						minima = distancia;
					}
				} //for
			} // else
		} // else

		minima = Math.min(minima, piezaOut.getXmin());
		return minima;
	}


	// Compara contra todos las piezas del objeto (que están estrictamente abajo),
	// y encuentra cuál es la distancia vertical que puede
	// moverse sin chocar con ninguna pieza.
	private static int cercaniaVerOP(Sheet objeto, Piece piezaOut)
	{	
		int distancia = 0;
		int minima = 100000;  //un número muy grande.
		Piece pzaIn;
		Piece pzaAb;
		if(piezaOut.getYmin() == 0)
		{
			return 0;
		}
		
		// Pieza imaginaria que incluye la zona abajo de piezaOut (para cóncavas).
		PieceVirtual pzatemp  = piezaAbajo(piezaOut);
		List<Piece> pzasInside;    // piezas del objeto.
		List<Piece> pzasAb = new LinkedList<Piece>();  // piezas abajo de piezaOut. Si desplazáramos piezaOut
		// indefinidamente hacia abajo chocaría con alguna de pzasAb.
		pzasInside = objeto.getPzasInside();

		// Caso en que el objeto está vacío.
		if (pzasInside.isEmpty())
		{
			minima = piezaOut.getYmin();
		}
		else {
			// Genera la lista de piezas abajo.
			for (int i = 0; i < pzasInside.size(); i++)
			{
				pzaIn = (Piece)pzasInside.get(i);
				if ( (interseccionPPV(pzaIn, pzatemp) || dentroPPV(pzaIn, pzatemp) )
						&& interseccionPP(pzaIn, piezaOut) == false   //esta condición es útil en EC, EC2 y MA.
						&& dentroPP(pzaIn, piezaOut) == false )		  //esta condición es útil en EC, EC2 y MA.
				{
					pzasAb.add(pzaIn);
				}
			}
			// Termina de generar lista de piezas abajo, empieza a medir.
			// Caso en que no hay piezas abajo de piezaOut.
			if (pzasAb.isEmpty())
			{
				minima = piezaOut.getYmin();  
			}
			else {
				// Calcula la distancia entre piezaOut y cada una de pzasAb
				for (int i = 0; i < pzasAb.size(); i++)
				{
					pzaAb = (Piece)pzasAb.get(i);
					distancia = cercaniaVerPP(pzaAb, piezaOut);
					if ( distancia < minima)
					{ 
						minima = distancia;
					}
				} //for
			} //else
		} //else

		minima = Math.min(minima, piezaOut.getYmin());
		return minima;
	}


	// Para una pieza, considerando sólo su 1 o más vértices que comparten su Ymin
	// (el o los vértices de + abajo) devuelve la coordenada X del vértice + a la izq.
	private static int coordenadaBL(Piece pieza)
	{
		int yminimo = pieza.getYmin();
		int xminimo = pieza.getXmax();	 //valor máx. posible de la coordenada buscada.	

		for (int i = 0; i < pieza.getvertices(); i++)
		{
			if( pieza.coordY[i] == yminimo )
			{
				if( pieza.coordX[i] < xminimo )
				{
					xminimo = pieza.coordX[i];
				}
			}
		}
		return xminimo;
	}



	// Para una pieza, considerando sólo su 1 o más vértices que comparten su Ymax
	// (el o los vértices de + arriba) devuelve la coordenada X del vértice + a la izq.
	private static int coordenadaTL(Piece pieza)
	{
		int ymaximo = pieza.getYmax();
		int xminimo = pieza.getXmax();	 //valor máx. posible de la coordenada buscada.	

		for (int i = 0; i < pieza.getvertices(); i++)
		{
			if( pieza.coordY[i] == ymaximo )
			{
				if( pieza.coordX[i] < xminimo )
				{
					xminimo = pieza.coordX[i];
				}
			}
		}
		return xminimo;
	}


	// Para una pieza, considerando sólo su 1 o más vértices que comparten su Xmin
	// (el o los vértices de + a la izq.) devuelve la coordenada Y del vértice + abajo.
	private static int coordenadaLB(Piece pieza)
	{
		int xminimo = pieza.getXmin();	 	
		int yminimo = pieza.getYmax();   //valor máx. posible de la coordenada buscada.

		for (int i = 0; i < pieza.getvertices(); i++)
		{
			if( pieza.coordX[i] == xminimo )
			{
				if( pieza.coordY[i] < yminimo )
				{
					yminimo = pieza.coordY[i];
				}
			}
		}

		return yminimo;
	}


	// Para una pieza, considerando sólo su 1 o más vértices que comparten su Xmax
	// (el o los vértices de + a la derecha) devuelve la coordenada Y del vértice + abajo.
	private static int coordenadaRB(Piece pieza)
	{
		int xmaximo = pieza.getXmax();	 	
		int yminimo = pieza.getYmax();   //valor máx. posible de la coordenada buscada.

		for (int i = 0; i < pieza.getvertices(); i++)
		{
			if( pieza.coordX[i] == xmaximo )
			{
				if( pieza.coordY[i] < yminimo )
				{
					yminimo = pieza.coordY[i];
				}
			}
		}

		return yminimo;
	}



	private static int cercaniaHorPP(Piece pieza1, Piece pieza2)
	{	// Encuentra la distancia que pieza1 está a la izquierda de pieza2.
		// Si pieza1 no puede alcanzarse desde pieza2 a la izquierda, devuelve un núm. grande.
		// Para esto, encuentra la distancia horizontal de cada vertice
		// de pieza1 con cada lado de pieza2 y viceversa.
		// devuelve la menor distancia encontrada.
		// Nota: Algoritmo de orden polinomial respecto producto del núm. de vertices.

		int minima = 100000;    //un numero muy grande.
		int vertices1 = pieza1.getvertices();
		int vertices2 = pieza2.getvertices();
		int dist = 0;
		int i=0;
		int j=0;

		// Dist. de c/vertice de pieza 1 hacia cada segmento de pieza 2 (a excepción de un segm).
		for (i = 0; i < vertices1; i++)
		{  for (j = 0; j < vertices2-1; j++)
		{
			dist = -SegmentoPuntoH(pieza1.coordX[i], pieza1.coordY[i], 
					pieza2.coordX[j], pieza2.coordY[j],
					pieza2.coordX[j+1], pieza2.coordY[j+1]);
			if ( dist < 0)
			{ 
				continue;  //significa que ese vértice de pieza1 está a la derecha de pieza2 (se ignora).
			}  		

			if ( dist < minima)
			{ 
				minima = dist;
			}
		}
		}

		//Dist. de c/vértice de pieza1 hacia el segmento (ultimo vertice-primer vértice) de pieza2.
		for (i = 0; i < vertices1; i++)
		{
			dist = -SegmentoPuntoH(pieza1.coordX[i], pieza1.coordY[i], 
					pieza2.coordX[vertices2-1], pieza2.coordY[vertices2-1], 
					pieza2.coordX[0], pieza2.coordY[0]);
			if ( dist < 0)
			{ 
				continue;
			}  

			if ( dist < minima)
			{ 
				minima = dist;
			}
		}

		// Dist. de c/vertice de pieza 2 hacia cada segmento de pieza 1 (a excepción de uno).
		for (i = 0; i < vertices2; i++)
		{  for (j = 0; j < vertices1-1; j++)
		{
			dist = SegmentoPuntoH(pieza2.coordX[i], pieza2.coordY[i], 
					pieza1.coordX[j], pieza1.coordY[j], 
					pieza1.coordX[j+1], pieza1.coordY[j+1]);
			if ( dist < 0)
			{ 
				continue;
			}  

			if ( dist < minima)
			{ 
				minima = dist;
			}
		}
		}

		//Dist. de c/vértice de pieza1 hacia el segmento (ultimo vertice-primer vértice) de pieza2.
		for (i = 0; i < vertices2; i++)
		{
			dist = SegmentoPuntoH(pieza2.coordX[i], pieza2.coordY[i], 
					pieza1.coordX[vertices1-1], pieza1.coordY[vertices1-1], 
					pieza1.coordX[0], pieza1.coordY[0]);
			if ( dist < 0)
			{ 
				continue;
			}  

			if ( dist < minima)
			{ 
				minima = dist;
			}
		}

		return minima;
	}


	private static int cercaniaVerPP(Piece pieza1, Piece pieza2)
	{	
		// Encuentra la distancia que pieza1 está abajo de pieza2.
		// Si pieza1 no puede alcanzarse desde pieza2 hacia abajo, devuelve un núm. grande.
		// Para esto, encuentra la distancia vertical de cada vertice
		// de pieza1 con cada lado de pieza2 y viceversa.
		// devuelve la menor distancia encontrada.
		// Nota: Algoritmo de orden polinomial respecto producto del núm. de vertices.

		int minima = 100000;    //un numero muy grande.
		int vertices1 = pieza1.getvertices();
		int vertices2 = pieza2.getvertices();
		int dist = 0;
		int i=0;
		int j=0;

		// Dist. de c/vertice de pieza 1 hacia cada segmento de pieza 2 (a excepción de un segm).
		for (i = 0; i < vertices1; i++)
		{  for (j = 0; j < vertices2-1; j++)
		 {
			dist = -SegmentoPuntoV(pieza1.coordX[i], pieza1.coordY[i], 
					pieza2.coordX[j], pieza2.coordY[j],
					pieza2.coordX[j+1], pieza2.coordY[j+1]);

			if ( dist < 0)
			{ 
				continue; //el vértice de pieza1 está arriba de pieza2 (se ignora).
			}  
			if ( dist < minima)
			{ 
				minima = dist;
			}
		 }
		}      	

		//Dist. de c/vértice de pieza1 hacia el segmento (ultimo vertice-primer vértice) de pieza2.
		for (i = 0; i < vertices1; i++)
		{
			dist = -SegmentoPuntoV(pieza1.coordX[i], pieza1.coordY[i], 
					pieza2.coordX[vertices2-1], pieza2.coordY[vertices2-1], 
					pieza2.coordX[0], pieza2.coordY[0]);
			if ( dist < 0)
			{ 
				continue; //el vértice de pieza1 está arriba de pieza2 (se ignora).
			}  
			if ( dist < minima)
			{ 
				minima = dist;
			}
		}

		// Dist. de c/vertice de pieza 2 hacia cada segmento de pieza 1 (a excepción de uno).
		for (i = 0; i < vertices2; i++)
		{  for (j = 0; j < vertices1-1; j++)
		 {
			dist = SegmentoPuntoV(pieza2.coordX[i], pieza2.coordY[i], 
					pieza1.coordX[j], pieza1.coordY[j], 
					pieza1.coordX[j+1], pieza1.coordY[j+1]);
			if ( dist < 0)
			{ 
				continue; //el vértice de pieza2 está abajo de pieza1 (se ignora).
			}  
			if ( dist < minima)
			{ 
				minima = dist;
			}
		 }
		}

		//Dist. de c/vértice de pieza1 hacia el segmento (ultimo vertice-primer vértice) de pieza2.
		for (i = 0; i < vertices2; i++)
		{
			dist = SegmentoPuntoV(pieza2.coordX[i], pieza2.coordY[i], 
					pieza1.coordX[vertices1-1], pieza1.coordY[vertices1-1], 
					pieza1.coordX[0], pieza1.coordY[0]);
			if ( dist < 0)
			{ 
				continue; //el vértice de pieza2 está abajo de pieza1 (se ignora).
			}  
			if ( dist < minima)
			{ 
				minima = dist;
			}
		}

		return minima;
	}



	public static int SegmentoPuntoH(int X1, int Y1, int X2, int Y2, int X3, int Y3)
	{	// Encuentra la distancia horizontal del punto (x1, y1)
		// al segmento de (x2, y2) a (x3, y3)
		// El resultado es positivo si el punto está a la derecha del segmento.
		// El resultado es negativo si el punto está a la izquierda del segmento.

		int distancia = 0;
		double dist = 0;
		int numgrande = 100000;

		if( (Y1 < Y2 && Y1 < Y3) ||
				(Y1 > Y2 && Y1 > Y3) )
		{
			return numgrande;  
		}          // si horizontalmente el pto no llega al segmento, devuelve un núm grande.

		if( (Y1 == Y2 && Y1 == Y3) &&
				(X1 > X2  && X1 > X3) )
		{
			distancia = Math.min(X1-X2, X1-X3); 
			return distancia; 
		}     // el punto está alineado con el segmento y el punto está a la derecha.

		if( (Y1 == Y2 && Y1 == Y3) &&
				(X1 < X2  && X1 < X3) )
		{
			distancia = -Math.min(X2-X1, X3-X1); 
			return distancia; 
		}     // el punto está alineado con el segmento y el punto está a la izq.

		if(Y1 == Y2 && Y1 == Y3) 
		{
			distancia = 0; 
			return distancia; 
		}     // el punto está sobre el segmento.
		else 
		{
			dist = (double)(X1-X2) + ((double)(X2-X3)*(double)(Y2-Y1)/(double)(Y2-Y3));
			if (dist < 0)
			{
				distancia = (int)Math.ceil(dist);
			}
			else
			{
				distancia = (int)Math.floor(dist);
			}
		}

		return distancia;
	}



	public static int SegmentoPuntoV(int X1, int Y1, int X2, int Y2, int X3, int Y3)
	{	// Encuentra la distancia vertical del punto (x1, y1)
		// al segmento de (x2, y2) a (x3, y3)
		// El resultado es positivo si el punto está arriba del segmento.
		// El resultado es negativo si el punto está abajo del segmento.

		int distancia = 0;
		double dist = 0;
		int numgrande = 100000;

		if( (X1 < X2 && X1 < X3) ||
				(X1 > X2 && X1 > X3) )
		{
			return numgrande;  
		}          // si verticalmente el pto no llega al segmento, devuelve un núm grande.

		if( (X1 == X2 && X1 == X3) &&
				(Y1 > Y2  && Y1 > Y3) )
		{
			distancia = Math.min(Y1-Y2, Y1-Y3); 
			return distancia; 
		}     // el punto está alineado con el segmento y el punto está arriba.

		if( (X1 == X2 && X1 == X3) &&
				(Y1 < Y2  && Y1 < Y3) )
		{
			distancia = -Math.min(Y2-Y1, Y3-Y1); 
			return distancia; 
		}     // el punto está alineado con el segmento y el punto está abajo.

		if(X1 == X2 && X1 == X3) 
		{
			distancia = 0; 
			return distancia; 
		}     // el punto está sobre el segmento.
		else 
		{
			dist = (double)(Y1-Y2) + ((double)(Y2-Y3)*(double)(X2-X1)/(double)(X2-X3));
			if (dist < 0)
			{
				distancia = (int)Math.ceil(dist);
			}
			else
			{
				distancia = (int)Math.floor(dist);
			}
		}

		return distancia;
	}


	//Dado un objeto, indica si las coordenadas de 
	//la pieza son válidas para colocarse dentro de él.
	private static boolean posicionValida(Sheet objeto, Piece pieza)
	{
		if(pieza.getYmax() <= objeto.getYmax() && 
				pieza.getXmax() <= objeto.getXmax() && 
				pieza.getXmin()>=0 && pieza.getYmin()>=0 ) //la pieza no se sale de los límites del objeto.
		{  
			if( interseccionOP(objeto, pieza) == false)  //la pieza no tiene intersección con otra del objeto.
			{ 
				if( dentroOP(objeto, pieza) == false )	  //la pieza no está contenida en otra del objeto.
				{  
					return true;
				}
			}
		}

		return false;
	}


	private static boolean interseccionOP(Sheet objeto, Piece piezaOut)
	{
		Piece pzaIn;
		boolean value;
		List<Piece> pzasInside;    // piezas del objeto.
		pzasInside = objeto.getPzasInside();

		// Caso en que el objeto está vacío.
		if (pzasInside.isEmpty())
		{
			return false;
		}

		for (int i = 0; i < pzasInside.size(); i++)
		{  	
			pzaIn = (Piece)pzasInside.get(i);
			value = interseccionPP(piezaOut, pzaIn);
			if(value)
			{
				return true;
			}
		}

		return false;
	}



	/* Prueba la intersección de una pieza con otra.
	 * Para esto, prueba la intersección de cada lado de una 
	 * pieza con c/lado de la otra. True: Sí hay intersección.  */
	private static boolean interseccionPP(Piece pieza1, Piece pieza2)
	{
		int vertices1 = pieza1.getvertices();
		int vertices2 = pieza2.getvertices();
		boolean value;

		// Primero se descartan el caso de piezas que no tienen posibilidad de cruzarse.
		if ( (pieza1.getXmax() <= pieza2.getXmin())
				||(pieza2.getXmax() <= pieza1.getXmin())
				||(pieza1.getYmax() <= pieza2.getYmin())
				||(pieza2.getYmax() <= pieza1.getYmin()) )    
		{  
			return false;
		}      

		//los primeros n-1 lados de pieza1 vs todos los lados de pieza2.	
		for (int i = 0; i < vertices1-1; i++)
		{  for (int j = 0; j < vertices2-1; j++)
		{
			value = interseccionSS(pieza1.coordX[i], pieza1.coordY[i], 
					pieza1.coordX[i+1], pieza1.coordY[i+1], 
					pieza2.coordX[j], pieza2.coordY[j],
					pieza2.coordX[j+1], pieza2.coordY[j+1]);
			if ( value )
			{ 
				return true;  
			}  		
		}
		//vs. último lado de pieza2
		value = interseccionSS(pieza1.coordX[i], pieza1.coordY[i], 
				pieza1.coordX[i+1], pieza1.coordY[i+1], 
				pieza2.coordX[vertices2-1], pieza2.coordY[vertices2-1],
				pieza2.coordX[0], pieza2.coordY[0]);
		if ( value )
		 { 
			return true;  
		 }  		
		}


		//último lado de pieza1 vs todos los lados de pieza2 (excepto el último).	
		for (int j = 0; j < vertices2-1; j++)
		{
			value = interseccionSS(pieza1.coordX[vertices1-1], pieza1.coordY[vertices1-1], 
					pieza1.coordX[0], pieza1.coordY[0], 
					pieza2.coordX[j], pieza2.coordY[j],
					pieza2.coordX[j+1], pieza2.coordY[j+1]);
			if ( value )
			{ 
				return true;  
			}  		
		}


		//último lado de pieza1 vs. último lado de pieza2
		value = interseccionSS(pieza1.coordX[vertices1-1], pieza1.coordY[vertices1-1], 
				pieza1.coordX[0], pieza1.coordY[0], 
				pieza2.coordX[vertices2-1], pieza2.coordY[vertices2-1],
				pieza2.coordX[0], pieza2.coordY[0]);
		if ( value )
		{ 
			return true;  
		}  		

		return false;  
	}


	/* Prueba la intersección de una pieza con una pza virtual.
	 * Para esto, prueba la intersección de cada lado de una 
	 * pieza con c/lado de la otra. True: Sí hay intersección.  */
	private static boolean interseccionPPV(Piece pieza1, PieceVirtual pieza2)
	{
		int vertices1 = pieza1.getvertices();
		int vertices2 = pieza2.getvertices();
		boolean value;

		// Primero se descartan el caso de piezas que no tienen posibilidad de cruzarse.
		if ( (pieza1.getXmax() <= pieza2.getXmin())
				||(pieza2.getXmax() <= pieza1.getXmin())
				||(pieza1.getYmax() <= pieza2.getYmin())
				||(pieza2.getYmax() <= pieza1.getYmin()) )    
		{
			return false;
		}      

		//los primeros n-1 lados de pieza1 vs todos los lados de pieza2.	
		for (int i = 0; i < vertices1-1; i++)
		{  for (int j = 0; j < vertices2-1; j++)
		{
			value = interseccionSS(pieza1.coordX[i], pieza1.coordY[i], 
					pieza1.coordX[i+1], pieza1.coordY[i+1], 
					pieza2.coordX[j], pieza2.coordY[j],
					pieza2.coordX[j+1], pieza2.coordY[j+1]);
			if ( value )
			{ 
				return true;  
			}  		
		}
		//vs. último lado de pieza2
		value = interseccionSS(pieza1.coordX[i], pieza1.coordY[i], 
				pieza1.coordX[i+1], pieza1.coordY[i+1], 
				pieza2.coordX[vertices2-1], pieza2.coordY[vertices2-1],
				pieza2.coordX[0], pieza2.coordY[0]);
		if ( value )
		 { 
			return true;  
		 }  		
		}


		//último lado de pieza1 vs todos los lados de pieza2 (excepto el último).	
		for (int j = 0; j < vertices2-1; j++)
		{
			value = interseccionSS(pieza1.coordX[vertices1-1], pieza1.coordY[vertices1-1], 
					pieza1.coordX[0], pieza1.coordY[0], 
					pieza2.coordX[j], pieza2.coordY[j],
					pieza2.coordX[j+1], pieza2.coordY[j+1]);
			if ( value )
			{ 
				return true;  
			}  		
		}


		//último lado de pieza1 vs. último lado de pieza2
		value = interseccionSS(pieza1.coordX[vertices1-1], pieza1.coordY[vertices1-1], 
				pieza1.coordX[0], pieza1.coordY[0], 
				pieza2.coordX[vertices2-1], pieza2.coordY[vertices2-1],
				pieza2.coordX[0], pieza2.coordY[0]);
		if ( value )
		 { 
			return true;  
		 }  		

		return false;  
	}	
	
	

	/*  Prueba si el segmento (x1, y1) a (x2, y2) se intersecta con el 
	 *  segmento (x3, y3) a (x4, y4).  
	 *  Devuelve true si existe intersección y false si no.
	 *  Nota: Si un segmento toca al otro sólo en un extremo: devuelve false.
	 *		  Segmentos que pertenecen a la misma recta: devuelve false AUNQUE se traslapen.
	 *        4 puntos iguales: devolvería false   */
	private static boolean interseccionSS(int X1, int Y1, int X2, int Y2, int X3, int Y3, int X4, int Y4)
	{
		double m1, m2, x, y;

		// primero se descartan el caso de segmentos que no tienen posibilidad de cruzarse.
		if ( (Math.max(X1, X2) <= Math.min(X3, X4))
				||(Math.max(X3, X4) <= Math.min(X1, X2))
				||(Math.max(Y1, Y2) <= Math.min(Y3, Y4))
				||(Math.max(Y3, Y4) <= Math.min(Y1, Y2)) )    
		{
			return false;      //aquí caen los casos de 2 segm verticales: O son paralelos o pertenecen a la misma recta.  Pueden traslaparse o no.
		}

		// solo el primer segmento es vertical
		if (X1 == X2)    
		{
			if (Y3 == Y4)     // 2o segmento horizontal.
			{
				if( (X1 < Math.max(X3, X4) && X1 > Math.min(X3, X4))
						&&(Y3 < Math.max(Y1, Y2) && Y3 > Math.min(Y1, Y2)) )
				{
					return true;
				}
			}

			m2 = (double)(Y4-Y3)/ (double)(X4-X3);   //pendiente del segmento 2
			y = m2 * (double)(X1 - X3) + (double)(Y3);   // de la recta2: y=m(x-x3)+y3, encontrar la 
			// coordenada y que tiene la intersección de los segmentos.
			if( (y < Math.max(Y1, Y2) && y > Math.min(Y1, Y2)) 
					&& (y < Math.max(Y3, Y4) && y > Math.min(Y3, Y4)) )  //prueba si la coordenada y pertenece a los segmentos (entonces la coordenada x también)
			{
				return true;
			}
			else
			{
				return false;
			}
		}  

		// solo el 2o segmento es vertical
		if (X3 == X4)    
		{
			if (Y1 == Y2)     // 1er segmento horizontal.
			{
				if( (X3 < Math.max(X1, X2) && X3 > Math.min(X1, X2))
						&&(Y1 < Math.max(Y3, Y4) && Y1 > Math.min(Y3, Y4)) )
				{
					return true;
				}
			}


			m1 = (double)(Y2-Y1)/ (double)(X2-X1);   //pendiente del segmento 1
			y = m1 * (double)(X3 - X1) + (double)(Y1);   // de la recta2: y=m1(x-x1)+y1, encontrar la 
			// coordenada y que tiene la intersección de los segmentos.
			if( (y < Math.max(Y1, Y2) && y > Math.min(Y1, Y2)) 
					&& (y < Math.max(Y3, Y4) && y > Math.min(Y3, Y4)) )  //prueba si la coordenada y pertenece a los segmentos (entonces la coordenada x también)
			{
				return true;
			}
			else
			{
				return false;
			}
		}  

		// Ninguna recta es vertical.
		m1 = (double)(Y2-Y1)/ (double)(X2-X1);   //pendiente del segmento 1
		m2 = (double)(Y4-Y3)/ (double)(X4-X3);   //pendiente del segmento 2

		if (m1 == m2)    
		{
			return false;   //Segmentos paralelos o q pertenecen a la misma recta.  Pueden traslaparse o no. 
		}  

		x = (m1*(double)X1 - (double)Y1 - m2*(double)X3 + (double)Y3) / (m1-m2);   //coordenada X del punto de intersección de las dos rectas.
		x = redondeaSiCerca(x);  //el cálculo de las pendientes puede hacer que el punto de intersección quede
		//distorsionado por una factor aprox. de 10E-11, aparentando no estar en el extremo del segmento.

		//Prueba si el punto de intersección está en los segmentos.
		if( (x < Math.max(X1, X2) && x > Math.min(X1, X2)) 
				&& (x < Math.max(X3, X4) && x > Math.min(X3, X4)) )  //prueba si la coordenada X pertenece a los segmentos (entonces la coordenada Y también)
		{
			return true;
		}

		return false;  
	}

	// Indica si un segmento atraviesa a una pieza (TRUE).
	// Nota:  Si la toca tangencialmente o el segmento se empalma   
	// con uno de los lados, devolverá FALSE.
	public static boolean interseccionSP(int x1, int y1, int x2, int y2, Piece piezaOut)
	{	
		int vertices = piezaOut.getvertices();
		for(int i=0; i < vertices-1; i++)
		{
			if(  interseccionSS(x1, y1, x2, y2, piezaOut.coordX[i], piezaOut.coordY[i],
					            piezaOut.coordX[i+1], piezaOut.coordY[i+1])  )
			{
				return true;
			}
		}
		
		if(  interseccionSS(x1, y1, x2, y2, piezaOut.coordX[vertices-1], piezaOut.coordY[vertices-1],
	            piezaOut.coordX[0], piezaOut.coordY[0])  )
		{
			return true;
		}
		
		return false;
	}
	
	
	
	// Si un número double está muy muy cerca de su entero más cercano, lo redondea.
	private static double redondeaSiCerca(double x)
	{
		double tolerancia = 0.00001;
		if( Math.abs(x - Math.ceil(x)) < tolerancia )
		{
			x = 	Math.ceil(x);
		} else if( Math.abs(x - Math.floor(x)) < tolerancia )
		{
			x = 	Math.floor(x);	
		}

		return x;
	}



	/* Indica la longitud que el perímetro de la pieza es adyacente con las piezas
	 * del objeto y con el objeto mismo */
	private static int adyacenciaOP(Sheet objeto, Piece piezaOut)
	{
		Piece pzaIn;
		int[] vertices = {0,0, objeto.getXmax(),0,      
				objeto.getXmax(),objeto.getYmax(), 0,objeto.getYmax()};
		PieceVirtual pzaOb = new PieceVirtual(vertices);
		List<Piece> pzasInside;    // piezas del objeto.
		int adyacencia = 0;

		pzasInside = objeto.getPzasInside();
		adyacencia += adyacenciaPPV(piezaOut, pzaOb);   //Calcula adyacencia con los límites del objeto.
		if (pzasInside.isEmpty())
		{
			return adyacencia;
		}

		for (int i = 0; i < pzasInside.size(); i++)       //la función adyacenciaPP, 
		{												  //eficientemente descarta 
			pzaIn = (Piece)pzasInside.get(i);			  //las piezas lejanas que no 
			adyacencia += adyacenciaPP(piezaOut, pzaIn);  //tienen posibilidad de ser adyacentes.
		}

		return adyacencia;
	}




	/* Indica la longitud que dos piezas son adyacentes. */
	private static int adyacenciaPP(Piece pieza1, Piece pieza2)
	{
		int vertices1 = pieza1.getvertices();
		int vertices2 = pieza2.getvertices();
		int adyacencia = 0;

		// primero se descartan el caso de piezas que no 
		// tienen posibilidad de ser adyacentes.
		if ( (pieza1.getXmax() < pieza2.getXmin())
				||(pieza2.getXmax() < pieza1.getXmin())
				||(pieza1.getYmax() < pieza2.getYmin())
				||(pieza2.getYmax() < pieza1.getYmin()) )    
		{
			return 0;
		}     

		//los primeros n-1 lados de pieza1 vs todos los lados de pieza2.	
		for (int i = 0; i < vertices1-1; i++)
		{  for (int j = 0; j < vertices2-1; j++)
		{
			adyacencia += adyacenciaSS(pieza1.coordX[i], pieza1.coordY[i], 
					pieza1.coordX[i+1], pieza1.coordY[i+1], 
					pieza2.coordX[j], pieza2.coordY[j],
					pieza2.coordX[j+1], pieza2.coordY[j+1]);
		}
		//vs. último lado de pieza2
		adyacencia += adyacenciaSS(pieza1.coordX[i], pieza1.coordY[i], 
				pieza1.coordX[i+1], pieza1.coordY[i+1], 
				pieza2.coordX[vertices2-1], pieza2.coordY[vertices2-1],
				pieza2.coordX[0], pieza2.coordY[0]);
		}


		//último lado de pieza1 vs todos los lados de pieza2 (excepto el último).	
		for (int j = 0; j < vertices2-1; j++)
		{
			adyacencia += adyacenciaSS(pieza1.coordX[vertices1-1], pieza1.coordY[vertices1-1], 
					pieza1.coordX[0], pieza1.coordY[0], 
					pieza2.coordX[j], pieza2.coordY[j],
					pieza2.coordX[j+1], pieza2.coordY[j+1]);
		}

		//último lado de pieza1 vs. último lado de pieza2
		adyacencia += adyacenciaSS(pieza1.coordX[vertices1-1], pieza1.coordY[vertices1-1], 
				pieza1.coordX[0], pieza1.coordY[0], 
				pieza2.coordX[vertices2-1], pieza2.coordY[vertices2-1],
				pieza2.coordX[0], pieza2.coordY[0]);
		return adyacencia;  
	}



	/* Indica la longitud que dos piezas son adyacentes. */
	private static int adyacenciaPPV(Piece pieza1, PieceVirtual pieza2)
	{
		int vertices1 = pieza1.getvertices();
		int vertices2 = pieza2.getvertices();
		int adyacencia = 0;

		// primero se descartan el caso de piezas que no 
		// tienen posibilidad de ser adyacentes.
		if ( (pieza1.getXmax() < pieza2.getXmin())
				||(pieza2.getXmax() < pieza1.getXmin())
				||(pieza1.getYmax() < pieza2.getYmin())
				||(pieza2.getYmax() < pieza1.getYmin()) )    
		{
			return 0;
		}     

		//los primeros n-1 lados de pieza1 vs todos los lados de pieza2.	
		for (int i = 0; i < vertices1-1; i++)
		{  for (int j = 0; j < vertices2-1; j++)
		{
			adyacencia += adyacenciaSS(pieza1.coordX[i], pieza1.coordY[i], 
					pieza1.coordX[i+1], pieza1.coordY[i+1], 
					pieza2.coordX[j], pieza2.coordY[j],
					pieza2.coordX[j+1], pieza2.coordY[j+1]);
		}
		//vs. último lado de pieza2
		adyacencia += adyacenciaSS(pieza1.coordX[i], pieza1.coordY[i], 
				pieza1.coordX[i+1], pieza1.coordY[i+1], 
				pieza2.coordX[vertices2-1], pieza2.coordY[vertices2-1],
				pieza2.coordX[0], pieza2.coordY[0]);
		}


		//último lado de pieza1 vs todos los lados de pieza2 (excepto el último).	
		for (int j = 0; j < vertices2-1; j++)
		{
			adyacencia += adyacenciaSS(pieza1.coordX[vertices1-1], pieza1.coordY[vertices1-1], 
					pieza1.coordX[0], pieza1.coordY[0], 
					pieza2.coordX[j], pieza2.coordY[j],
					pieza2.coordX[j+1], pieza2.coordY[j+1]);
		}


		//último lado de pieza1 vs. último lado de pieza2
		adyacencia += adyacenciaSS(pieza1.coordX[vertices1-1], pieza1.coordY[vertices1-1], 
				pieza1.coordX[0], pieza1.coordY[0], 
				pieza2.coordX[vertices2-1], pieza2.coordY[vertices2-1],
				pieza2.coordX[0], pieza2.coordY[0]);
		return adyacencia;  
	}	
	
	

	/*  La idea es encontrar cuanto coinciden dos segmentos 
	 *  Deben tener la misma pendiente y la misma ordenada al origen (o sea, pertenecer 
	 *  a la misma recta).
	 *  Las rectas verticales tienen pendiente infinito, por lo que se tratan a parte.
	 *  Para la heurística de máxima adyacencia.	 */
	private static int adyacenciaSS(int X1, int Y1, int X2, int Y2, int X3, int Y3, int X4, int Y4)
	{
		int adyacencia = 0;
		double m1, m2, b1, b2;

		// primero se descartan el caso de segmentos que no tienen posibilidad de ser adyacentes.
		if ( (max(X1, X2) < min(X3, X4))
				||(max(X3, X4) < min(X1, X2))
				||(max(Y1, Y2) < min(Y3, Y4))
				||(max(Y3, Y4) < min(Y1, Y2)) )    
		{
			return 0;
		}

		
		// Dos segmentos verticales (que no se descartaron arriba).
		// Están sobre la misma vertical, hay que ver cuánto se traslapan.
		if (X1 == X2 && X3 == X4)    
		{
			// el segm 1 está contenido en el segm 2.
			if(   (Y1 <= Math.max(Y3,Y4)) && (Y1 >= Math.min(Y3,Y4)) 
					&&  (Y2 <= Math.max(Y3,Y4)) && (Y2 >= Math.min(Y3,Y4)) )
			{
				return Math.abs(Y2-Y1);
			}
			// el segm 2 está contenido en el segm 1.
			if(   (Y3 <= Math.max(Y1,Y2)) && (Y3 >= Math.min(Y1,Y2))
					&&  (Y4 <= Math.max(Y1,Y2)) && (Y4 >= Math.min(Y1,Y2)) )
			{
				return Math.abs(Y4-Y3);
			}
			// el segm 1 empieza más arriba que el segm 2.
			if(  Math.max(Y1,Y2) > Math.max(Y3,Y4) )
			{
				adyacencia = Math.max(Y3,Y4) - Math.min(Y1,Y2);
				return adyacencia;
			}
			// el segm 2 empieza más arriba que el segm 1.
			if(  Math.max(Y3,Y4) > Math.max(Y1,Y2) )
			{
				adyacencia = Math.max(Y1,Y2) - Math.min(Y3,Y4);
				return adyacencia;
			}
		}


		// un sólo segmento vertical (obvio, no son paralelos y no pueden 
		if (X1 == X2 || X3 == X4)    // ser adyacentes aunque se crucen).
		{
			return 0;
		} 

		// dos segmentos horizontales
		if (Y1 == Y2 && Y3 == Y4)    
		{
			// Están sobre la misma horizontal, hay que ver cuánto se traslapan.

			// el segm 1 está contenido en el segm 2.
			if(   (X1 <= Math.max(X3,X4)) && (X1 >= Math.min(X3,X4)) 
					&&  (X2 <= Math.max(X3,X4)) && (X2 >= Math.min(X3,X4)) )
			{
				return Math.abs(X2-X1);
			}
			// el segm 2 está contenido en el segm 1.
			if(   (X3 <= Math.max(X1,X2)) && (X3 >= Math.min(X1,X2))
					&&  (X4 <= Math.max(X1,X2)) && (X4 >= Math.min(X1,X2)) )
			{
				return Math.abs(X4-X3);
			}
			// el segm 1 empieza más a la der que el segm 2.
			if(  Math.max(X1,X2) > Math.max(X3,X4) )
			{
				adyacencia = Math.max(X3,X4) - Math.min(X1,X2);
				return adyacencia;
			}
			// el segm 2 empieza más a la der que el segm 1.
			if(  Math.max(X3,X4) > Math.max(X1,X2) )
			{
				adyacencia = Math.max(X1,X2) - Math.min(X3,X4);
				return adyacencia;
			}
		}


		// Ninguna recta es vertical ni horizontal.
		m1 = (double)(Y2-Y1)/ (double)(X2-X1);   //pendiente del segmento 1
		m2 = (double)(Y4-Y3)/ (double)(X4-X3);   //pendiente del segmento 2
		if (m1 != m2)    
		{
			return 0;   //Segmentos sin posibilidad de ser adyacentes. 
		} 

		b1 = (double)(Y1) - m1*(double)(X1);     //ordenada al origen del segmento 1
		b2 = (double)(Y3) - m2*(double)(X3);     //ordenada al origen del segmento 2
		if (b1 != b2)    
		{
			return 0;   //Segmentos paralelos que no pertenecen a la misma recta. 
		}

		//Casos de rectas inclinadas donde sí hay traslape.
		// el segm 1 está contenido en el segm 2.
		if(   (Y1 <= Math.max(Y3,Y4)) && (Y1 >= Math.min(Y3,Y4)) 
				&&  (Y2 <= Math.max(Y3,Y4)) && (Y2 >= Math.min(Y3,Y4)) )
		{
			adyacencia = (int)distPuntoPunto(X1, Y1, X2, Y2);
			return adyacencia;
		}
		// el segm 2 está contenido en el segm 1.
		if(   (Y3 <= Math.max(Y1,Y2)) && (Y3 >= Math.min(Y1,Y2))
				&&  (Y4 <= Math.max(Y1,Y2)) && (Y4 >= Math.min(Y1,Y2)) )
		{
			adyacencia = (int)distPuntoPunto(X3, Y3, X4, Y4);
			return adyacencia;
		}
		// el segm 1 empieza más arriba que el segm 2.
		if(  Math.max(Y1,Y2) > Math.max(Y3,Y4) )
		{
			if(m1 > 0)
			{
				adyacencia = (int)distPuntoPunto(max(X3,X4), max(Y3,Y4), min(X1,X2), min(Y1,Y2));
				return adyacencia;
			}
			adyacencia = (int)distPuntoPunto(min(X3,X4), max(Y3,Y4), max(X1,X2), min(Y1,Y2));
			return adyacencia;
		}
		// el segm 2 empieza más arriba que el segm 1.
		if(  Math.max(Y3,Y4) > Math.max(Y1,Y2) )
		{
			if(m1 > 0)
			{
				adyacencia = (int)distPuntoPunto(max(X1,X2), max(Y1,Y2), min(X3,X4), min(Y3,Y4));
				return adyacencia;
			}
			adyacencia = (int)distPuntoPunto(min(X1,X2), max(Y1,Y2), max(X3,X4), min(Y3,Y4));
			return adyacencia;
		}
		return adyacencia;
	}

	
	// Indica la longitud que un segmento coincide con los lados de una pieza.
	// Nota:  Si el segmento atraviesa la pieza o la toca en alguna esquina,
	// devolverá 0.
	public static int adyacenciaSP(int x1, int y1, int x2, int y2, Piece piezaOut)
	{	
		int vertices = piezaOut.getvertices();
		int distancia = 0;
		for(int i=0; i < vertices-1; i++)
		{
			distancia +=  adyacenciaSS(x1, y1, x2, y2, piezaOut.coordX[i], piezaOut.coordY[i],
					            piezaOut.coordX[i+1], piezaOut.coordY[i+1]);
		}
		
		distancia += adyacenciaSS(x1, y1, x2, y2, piezaOut.coordX[vertices-1], piezaOut.coordY[vertices-1],
	            piezaOut.coordX[0], piezaOut.coordY[0]);
	
		return distancia;
	}


	/* Prueba si una pieza o parte de ella está dentro de alguna pieza del objeto.
	 * Para esto, prueba si algún vértice de 'piezaOut' está dentro de alguna pieza del objeto.
	 * Puede darse el caso de polígonos semi-anidados donde los vértices de uno estén 
	 * sobre los vértices o lados del otro.
	 * Por eso también, se prueba para todos los lados de las dos piezas,
	 * si el punto medio de algún lado cae dentro de la otra pieza.
	 * True: sí está dentro.
	 * Nota: Esta función se usa después de ver que no hay intersección entre
	 *       las piezas.	 */
	private static boolean dentroOP(Sheet objeto, Piece piezaOut)
	{
		Piece pzaIn;
		boolean value;
		List<Piece> pzasInside;    // piezas del objeto.
		// piezas dentro del objeto que puedan alcanzarse desde 'pieza'
		// yendo hacia arriba o abajo Y hacia izquierda o derecha
		// Es decir, que los rectángulos que las circunscriben se intersecten.

		pzasInside = objeto.getPzasInside();

		//caso en que el objeto está vacío.
		if (pzasInside.isEmpty())
		{
			return false;
		}

		for (int i = 0; i < pzasInside.size(); i++)
		{
			pzaIn = (Piece)pzasInside.get(i);
			value = dentroPP(pzaIn, piezaOut);
			if(value)
			{
				return true;
			}
		}

		return false;

	}


	/* Devuelve true si una pieza (o una porción de pieza) está dentro de la otra.
	 * Para esto, prueba si algún vértice de una pza está dentro de la otra.
	 * Puede darse el caso de polígonos semi-anidados donde los vértices de uno estén 
	 * sobre los vértices o lados del otro.
	 * Por eso también, se prueba para todos los lados de las dos piezas,
	 * si el punto medio de algún lado cae dentro de la otra pieza. Como esto tampoco es 
	 * infalible, se prueba un punto cercano al punto medio de cada lado.
	 * See: López-Camacho, E., Ochoa, G., Terashima-Marín, H. and Burke, E. K. (2013), 
     * An effective heuristic for the two-dimensional irregular bin packing problem,
     * Annals of Operations Research. Volume 206, Issue 1, pp 241-264. DOI:10.1007/s10479-013-1341-4.
	 * True: sí está dentro.
	 * Nota: Esta función se usa después de ver que no hay intersección entre las pzas. */
	private static boolean dentroPP(Piece pieza1, Piece pieza2)
	{
		boolean value;
		int vertices = pieza1.getvertices();
		int vertices2 = pieza2.getvertices();
		int alto, ancho;

		if(  pieza1.getXmax() <= pieza2.getXmin() ||
				pieza2.getXmax() <= pieza1.getXmin() ||
				pieza1.getYmax() <= pieza2.getYmin() ||
				pieza2.getYmax() <= pieza1.getYmin() )
		{  //Los rectángulos que las circunscriben no se intersectan.
			return false;	
		} 

		//Del rectángulo horizontal que circunscribe a las 2 piezas.
		alto = Math.max(pieza1.getXmax(), pieza2.getXmax())-
		       Math.min(pieza1.getXmin(), pieza2.getXmin());
		ancho = Math.max(pieza1.getYmax(), pieza2.getYmax())-
	            Math.min(pieza1.getYmin(), pieza2.getYmin());
		if(alto * ancho < pieza1.getTotalSize() + pieza2.getTotalSize() )
		{  					//si el rectángulo que encierra las 2 pzas es menor 
			return true;    //que la suma del área de las piezas, entonces hay empalme.
		}
		
		//prueba los vértices de pieza1.
		for (int j = 0; j < vertices; j++)
		{
			value = dentroPuntoPieza(pieza1.coordX[j], pieza1.coordY[j], pieza2);
			if(value)
			{
				return true;
			}
		}

		//prueba los ptos medios de cada lado de pieza1.
		for (int j = 0; j < vertices-1; j++)
		{
			value = dentroPuntoPieza((pieza1.coordX[j]+pieza1.coordX[j+1])/2, 
					(pieza1.coordY[j]+pieza1.coordY[j+1])/2, pieza2);
			if(value)
			{
				return true;
			}
			// Un punto cercano a su punto medio.  Si ese punto cercano está dentro de
			// las 2 figuras
			value = dentroPuntoPieza((pieza1.coordX[j]+pieza1.coordX[j+1])/2 +2, 
					(pieza1.coordY[j]+pieza1.coordY[j+1])/2 +2, pieza1);
			if(value)
			{
				value = dentroPuntoPieza((pieza1.coordX[j]+pieza1.coordX[j+1])/2 +2, 
						(pieza1.coordY[j]+pieza1.coordY[j+1])/2 +2, pieza2);
				if(value)
				{
					return true;
				}
			}
		}

		//último pto medio de pieza1.
        value = dentroPuntoPieza((pieza1.coordX[vertices-1]+pieza1.coordX[0])/2, 
				(pieza1.coordY[vertices-1]+pieza1.coordY[0])/2, pieza2);
		if(value)
		{
			return true;
		}
		value = dentroPuntoPieza((pieza1.coordX[vertices-1]+pieza1.coordX[0])/2 +2, 
				(pieza1.coordY[vertices-1]+pieza1.coordY[0])/2 +2, pieza1);
		if(value)
		{
			value = dentroPuntoPieza((pieza1.coordX[vertices-1]+pieza1.coordX[0])/2 +2, 
					(pieza1.coordY[vertices-1]+pieza1.coordY[0])/2 +2, pieza2);
			if(value)
			{
				return true;
			}
		}
		
		
		//Si pto medio interior de pza1 está dentro de pza1, checar si cae dentro de pza2.
		value = dentroPuntoPieza((pieza1.getXmax()+pieza1.getXmin())/2, 
				(pieza1.getYmax()+pieza1.getYmin())/2, pieza1);
		if(value)
		{
			value = dentroPuntoPieza((pieza1.getXmax()+pieza1.getXmin())/2, 
					(pieza1.getYmax()+pieza1.getYmin())/2, pieza2);
			if(value)
			{			
				return true;
			}
		}
		
		
		//prueba los vértices de pieza2.
		for (int j = 0; j < vertices2; j++)
		{
			value = dentroPuntoPieza(pieza2.coordX[j], pieza2.coordY[j], pieza1);
			if(value)
			{
				return true;
			}
		}

		//prueba los ptos medios de cada lado de pieza2.
		for (int j = 0; j < vertices2-1; j++)
		{
			value = dentroPuntoPieza((pieza2.coordX[j]+pieza2.coordX[j+1])/2, 
					(pieza2.coordY[j]+pieza2.coordY[j+1])/2, pieza1);
			if(value)
			{
				return true;
			}
			// 1 punto cercano a su punto medio.  Si ese punto cercano está dentro de
			// las 2 figuras...
			value = dentroPuntoPieza((pieza2.coordX[j]+pieza2.coordX[j+1])/2 +2, 
					(pieza2.coordY[j]+pieza2.coordY[j+1])/2 +2, pieza2);
			if(value)
			{
				value = dentroPuntoPieza((pieza2.coordX[j]+pieza2.coordX[j+1])/2 +2, 
						(pieza2.coordY[j]+pieza2.coordY[j+1])/2 +2, pieza1);
				if(value)
				{
					return true;
				}
			}
		}

		//último pto medio de pieza2.
		value = dentroPuntoPieza((pieza2.coordX[vertices2-1]+pieza2.coordX[0])/2, 
				(pieza2.coordY[vertices2-1]+pieza2.coordY[0])/2, pieza1);
		if(value)
		{
			return true;
		}
		value = dentroPuntoPieza((pieza2.coordX[vertices2-1]+pieza2.coordX[0])/2 +2, 
				(pieza2.coordY[vertices2-1]+pieza2.coordY[0])/2 +2, pieza2);
		if(value)
		{
			value = dentroPuntoPieza((pieza2.coordX[vertices2-1]+pieza2.coordX[0])/2 +2, 
					(pieza2.coordY[vertices2-1]+pieza2.coordY[0])/2 +2, pieza1);
			if(value)
			{
				return true;
			}
		}
	
		
		//Si pto medio interior de pza2 está dentro de pza2, checar si cae dentro de pza1.
		value = dentroPuntoPieza((pieza2.getXmax()+pieza2.getXmin())/2, 
				(pieza2.getYmax()+pieza2.getYmin())/2, pieza2);
		if(value)
		{
			value = dentroPuntoPieza((pieza2.getXmax()+pieza2.getXmin())/2, 
					(pieza2.getYmax()+pieza2.getYmin())/2, pieza1);
			if(value)
			{			
				return true;
			}
		}
		return false;
	}

	
	/* Igual que dentroPP pero una de las piezas es PieceVirtual.*/
	private static boolean dentroPPV(Piece pieza1, PieceVirtual pieza2)
	{
		boolean value;
		int vertices = pieza1.getvertices();
		int vertices2 = pieza2.getvertices();
		int alto, ancho;

		if(  pieza1.getXmax() <= pieza2.getXmin() ||
				pieza2.getXmax() <= pieza1.getXmin() ||
				pieza1.getYmax() <= pieza2.getYmin() ||
				pieza2.getYmax() <= pieza1.getYmin() )
		{  //Los rectángulos que las circunscriben no se intersectan.
			return false;	
		} 
		
		//Del rectángulo horizontal que circunscribe a las 2 piezas.
		alto = Math.max(pieza1.getXmax(), pieza2.getXmax())-
		       Math.min(pieza1.getXmin(), pieza2.getXmin());
		ancho = Math.max(pieza1.getYmax(), pieza2.getYmax())-
	            Math.min(pieza1.getYmin(), pieza2.getYmin());
		if(alto * ancho < pieza1.getTotalSize() + pieza2.getTotalSize() )
		{  					//si el rectángulo que encierra las 2 pzas es menor 
			return true;    //que la suma del área de las piezas, entonces hay empalme.
		}


		//prueba los vértices de pieza1.
		for (int j = 0; j < vertices; j++)
		{
			value = dentroPuntoPiezaV(pieza1.coordX[j], pieza1.coordY[j], pieza2);
			if(value)
			{
				return true;
			}
		}

		//prueba los ptos medios de cada lado de pieza1.
		for (int j = 0; j < vertices-1; j++)
		{
			value = dentroPuntoPiezaV((pieza1.coordX[j]+pieza1.coordX[j+1])/2, 
					(pieza1.coordY[j]+pieza1.coordY[j+1])/2, pieza2);
			if(value)
			{
				return true;
			}
			value = dentroPuntoPieza((pieza1.coordX[j]+pieza1.coordX[j+1])/2 +2, 
					(pieza1.coordY[j]+pieza1.coordY[j+1])/2 +2, pieza1);
			if(value)
			{
				value = dentroPuntoPiezaV((pieza1.coordX[j]+pieza1.coordX[j+1])/2 +2, 
						(pieza1.coordY[j]+pieza1.coordY[j+1])/2 +2, pieza2);
				if(value)
				{
					return true;
				}
			}
		
		}

		//último pto medio de pieza1.
		value = dentroPuntoPiezaV((pieza1.coordX[vertices-1]+pieza1.coordX[0])/2, 
				(pieza1.coordY[vertices-1]+pieza1.coordY[0])/2, pieza2);
		if(value)
		{
			return true;
		}
		value = dentroPuntoPieza((pieza1.coordX[vertices-1]+pieza1.coordX[0])/2 +2, 
				(pieza1.coordY[vertices-1]+pieza1.coordY[0])/2 +2, pieza1);
		if(value)
		{
			value = dentroPuntoPiezaV((pieza1.coordX[vertices-1]+pieza1.coordX[0])/2 +2, 
					(pieza1.coordY[vertices-1]+pieza1.coordY[0])/2 +2, pieza2);
			if(value)
			{
				return true;
			}
		}
		
		//Si pto medio interior de pza1 está dentro de pza1, checar si cae dentro de pza2.
		value = dentroPuntoPieza((pieza1.getXmax()+pieza1.getXmin())/2, 
				(pieza1.getYmax()+pieza1.getYmin())/2, pieza1);
		if(value)
		{
			value = dentroPuntoPiezaV((pieza1.getXmax()+pieza1.getXmin())/2, 
					(pieza1.getYmax()+pieza1.getYmin())/2, pieza2);
			if(value)
			{			
				return true;
			}
		}

		//prueba los vértices de pieza2.
		for (int j = 0; j < vertices2; j++)
		{
			value = dentroPuntoPieza(pieza2.coordX[j], pieza2.coordY[j], pieza1);
			if(value)
			{
				return true;
			}
		}

		//prueba los ptos medios de cada lado de pieza2.
		for (int j = 0; j < vertices2-1; j++)
		{
			value = dentroPuntoPieza((pieza2.coordX[j]+pieza2.coordX[j+1])/2, 
					(pieza2.coordY[j]+pieza2.coordY[j+1])/2, pieza1);
			if(value)
			{
				return true;
			}
			value = dentroPuntoPiezaV((pieza2.coordX[j]+pieza2.coordX[j+1])/2 +2, 
					(pieza2.coordY[j]+pieza2.coordY[j+1])/2 +2, pieza2);
			if(value)
			{
				value = dentroPuntoPieza((pieza2.coordX[j]+pieza2.coordX[j+1])/2 +2, 
						(pieza2.coordY[j]+pieza2.coordY[j+1])/2 +2, pieza1);
				if(value)
				{
					return true;
				}
			}
		
		}

		//último pto medio de pieza2.
		value = dentroPuntoPieza((pieza2.coordX[vertices2-1]+pieza2.coordX[0])/2, 
				(pieza2.coordY[vertices2-1]+pieza2.coordY[0])/2, pieza1);
		if(value)
		{
			return true;
		}
		value = dentroPuntoPiezaV((pieza2.coordX[vertices2-1]+pieza2.coordX[0])/2 +2, 
				(pieza2.coordY[vertices2-1]+pieza2.coordY[0])/2 +2, pieza2);
		if(value)
		{
			value = dentroPuntoPieza((pieza2.coordX[vertices2-1]+pieza2.coordX[0])/2 +2, 
					(pieza2.coordY[vertices2-1]+pieza2.coordY[0])/2 +2, pieza1);
			if(value)
			{
				return true;
			}
		}
		
		
		//Si pto medio interior de pza2 está dentro de pza2, checar si cae dentro de pza1.
		value = dentroPuntoPiezaV((pieza2.getXmax()+pieza2.getXmin())/2, 
				(pieza2.getYmax()+pieza2.getYmin())/2, pieza2);
		if(value)
		{
			value = dentroPuntoPieza((pieza2.getXmax()+pieza2.getXmin())/2, 
					(pieza2.getYmax()+pieza2.getYmin())/2, pieza1);
			if(value)
			{			
				return true;
			}
		}

		return false;
	}
	

	/* Devuelve true si el punto (x1, y1) está dentro de una pieza.
	 * Si está sobre uno de los lados, devuelve false.
	 * Para esto, imaginariamente traza un rayo horizontal que 
	 * sale del punto hacia la derecha.  
	 * En general:
	 *    Si el rayo corta un núm. impar de veces a la figura, entonces está dentro.
	 *    Si el rayo corta un núm. par de veces a la figura, entonces está fuera.
	 * En el caso particular de figura convexas:
	 *   Si el rayo corta una sóla vez a la figura, entonces está dentro.
	 *   Si el rayo corta ninguna o dos veces, entonces está fuera.
	 * En esta función, el 'rayo' es el segmento de (x1, y1) hasta (num muy grande, y1). */
	public static boolean dentroPuntoPieza(int x1, int y1, Piece pieza)
	{
		int contador = 0;
		int numgrande = 100000; 
		int vertices = pieza.getvertices();
		int dFun1, dFun2;
		boolean value;
		if(x1 <= pieza.getXmin() || x1 >= pieza.getXmax() 
		   || y1 <= pieza.getYmin() || y1 >= pieza.getYmax())
		{
			return false;
		}
		
		
		// Prueba si el punto está sobre uno de los lados.
		for (int i = 0; i < vertices-1; i++)
		{
			value = dentroPuntoSegm(x1, y1,
					pieza.coordX[i], pieza.coordY[i],
					pieza.coordX[i+1], pieza.coordY[i+1]);
			if (value)
			{
				return false;
			}
		}
		value = dentroPuntoSegm(x1, y1,
				pieza.coordX[vertices-1], pieza.coordY[vertices-1],
				pieza.coordX[0], pieza.coordY[0]);
		if (value)
		{
			return false;
		}


		//cuenta cuántas veces el rayo intersecta cada arista de la pieza.
		for (int i = 0; i < vertices-1; i++)
		{
			value = interseccionSS(x1, y1, numgrande, y1, 
					pieza.coordX[i], pieza.coordY[i],
					pieza.coordX[i+1], pieza.coordY[i+1]);
			if (value)
			{
				contador++;
			}
		}
		//checa si el rayo intersecta la última arista de la pieza (que va del último vértice al primero).
		value = interseccionSS(x1, y1, numgrande, y1, 
				pieza.coordX[vertices-1], pieza.coordY[vertices-1],
				pieza.coordX[0], pieza.coordY[0]);
		if (value)
		{
			contador++;
		}

		
		//Si el rayo toca la pieza por alguno de los vértices, debe revisarse si la 
		//está tocando tangencialmente o la está atravesando.
		for (int i = 0; i < vertices; i++)
		{
			//el rayo atraviesa o toca el vértice, pero no es igual al vértice.
			value = ( dentroPuntoSegm(pieza.coordX[i], pieza.coordY[i],
					   x1, y1, numgrande, y1) 
					   && (x1 != pieza.coordX[i] || y1 != pieza.coordY[i]) );
		 
			if (value)
			{
				// Dfunction del vértice anterior respecto al rayo.
				if(i == 0)
				{
					dFun1 = ArrayOperations.dFunction(pieza.coordX[vertices-1], pieza.coordY[vertices-1],
							x1, y1, numgrande, y1);
				}else{
					dFun1 = ArrayOperations.dFunction(pieza.coordX[i-1], pieza.coordY[i-1],
							x1, y1, numgrande, y1);
				}
				// Dfunction  del siguiente vértice respecto al rayo.
				if(i == vertices - 1)
				{
					dFun2 = ArrayOperations.dFunction(pieza.coordX[0], pieza.coordY[0],
							x1, y1, numgrande, y1);
				}else{
					dFun2 = ArrayOperations.dFunction(pieza.coordX[i+1], pieza.coordY[i+1],
							x1, y1, numgrande, y1);
				}
				
				//Revisar si los vértices anterior y posterior al vértice por 
				//donde cruza el rayo están en lados opuestos al rayo.
				//Si están en lados opuestos (Dfuns signos contrarios): el rayo atraviesa 
				//a la figura (de adentro hacia afuera o viceversa) 
				//Si están del mismo lado (Dfuns mismo signo): el rayo no atraviesa a la 
				//figura, la toca tangencialmente por el vértice.
				//Algún Dfun = 0.  El rayo coincide con parte de alguno de los lados de la 
				//figura (o con la prolongación de alguno de los lados) y no cuenta como que 
				//la atraviesa.
				if( (dFun1 < 0 && dFun2 > 0) ||  (dFun1 > 0 && dFun2 < 0) )
				{
					contador ++;
				}
			}
		}
		
		
		//checa si contador es par.
		if (contador % 2 == 0)
		{
			return false;  //punto fuera de la pieza.
		}
		return true;  
	}


	
	/* Igual que dentroPuntoPieza, pero la pieza es PieceVirtual */
	private static boolean dentroPuntoPiezaV(int x1, int y1, PieceVirtual pieza)
	{
		int contador = 0;
		int numgrande = 100000; 
		int vertices = pieza.getvertices();
		int dFun1, dFun2;
		boolean value;

		if(x1 <= pieza.getXmin() || x1 >= pieza.getXmax() 
			   || y1 <= pieza.getYmin() || y1 >= pieza.getYmax())
		{
			return false;
		}
		
		// Prueba si el punto está sobre uno de los lados.
		for (int i = 0; i < vertices-1; i++)
		{
			value = dentroPuntoSegm(x1, y1,
					pieza.coordX[i], pieza.coordY[i],
					pieza.coordX[i+1], pieza.coordY[i+1]);
			if (value)
			{
				return false;
			}
		}
		value = dentroPuntoSegm(x1, y1,
				pieza.coordX[vertices-1], pieza.coordY[vertices-1],
				pieza.coordX[0], pieza.coordY[0]);
		if (value)
		{
			return false;
		}


		//cuenta cuántas veces el rayo intersecta cada arista de la pieza.
		for (int i = 0; i < vertices-1; i++)
		{
			value = interseccionSS(x1, y1, numgrande, y1, 
					pieza.coordX[i], pieza.coordY[i],
					pieza.coordX[i+1], pieza.coordY[i+1]);
			if (value)
			{
				contador++;
			}
		}

		//checa si el rayo intersecta la última arista de la pieza (que va del último vértice al primero).
		value = interseccionSS(x1, y1, numgrande, y1, 
				pieza.coordX[vertices-1], pieza.coordY[vertices-1],
				pieza.coordX[0], pieza.coordY[0]);

		if (value)
		{
			contador++;
		}

		
		//Si el rayo toca la pieza por alguno de los vértices, debe revisarse si la 
		//está tocando tangencialmente o la está atravesando.
		for (int i = 0; i < vertices; i++)
		{
			//el rayo atraviesa o toca el vértice, pero no es igual al vértice.
			value = ( dentroPuntoSegm(pieza.coordX[i], pieza.coordY[i],
					   x1, y1, numgrande, y1) 
					   && (x1 != pieza.coordX[i] || y1 != pieza.coordY[i]) );
		 
			if (value)
			{
				// Dfunction del vértice anterior respecto al rayo.
				if(i == 0)
				{
					dFun1 = ArrayOperations.dFunction(pieza.coordX[vertices-1], pieza.coordY[vertices-1],
							x1, y1, numgrande, y1);
				}else{
					dFun1 = ArrayOperations.dFunction(pieza.coordX[i-1], pieza.coordY[i-1],
							x1, y1, numgrande, y1);
				}
				// Dfunction  del siguiente vértice respecto al rayo.
				if(i == vertices - 1)
				{
					dFun2 = ArrayOperations.dFunction(pieza.coordX[0], pieza.coordY[0],
							x1, y1, numgrande, y1);
				}else{
					dFun2 = ArrayOperations.dFunction(pieza.coordX[i+1], pieza.coordY[i+1],
							x1, y1, numgrande, y1);
				}
				
				//Revisar si los vértices anterior y posterior al vértice por 
				//donde cruza el rayo están en lados opuestos al rayo.
				//Si están en lados opuestos (Dfuns signos contrarios): el rayo atraviesa 
				//a la figura (de adentro hacia afuera o viceversa) 
				//Si están del mismo lado (Dfuns mismo signo): el rayo no atraviesa a la 
				//figura, la toca tangencialmente por el vértice.
				//Algún Dfun = 0.  El rayo coincide con parte de alguno de los lados de la 
				//figura (o con la prolongación de alguno de los lados) y no cuenta como que 
				//la atraviesa.
				if( (dFun1 < 0 && dFun2 > 0) ||  (dFun1 > 0 && dFun2 < 0) )
				{
					contador ++;
				}
			}
		}	
		
			
		//checa si contador es par.
		if (contador % 2 == 0)
		{
			return false;  //punto fuera de la pieza.
		}
		return true;  
	}



	/* TRUE: Si el segmento toca tangencialmente algún vértice de la pieza.
	 * Nota: Ignora los casos en que un extremo del segmento coincida 
	 * con un vértice de la pieza.	 */
	public static boolean tangenteSegmPieza(int X1, int Y1, int X2, int Y2, Piece pza)
	{
		int vertices = pza.getvertices();
		int Xpza, Ypza;
		for(int i = 0; i < vertices; i++)
		{
			Xpza = pza.coordX[i];
			Ypza = pza.coordY[i];
			if( distPuntoPunto(X2, Y2, Xpza, Ypza) +
				    distPuntoPunto(X1, Y1, Xpza, Ypza)==
					distPuntoPunto(X1, Y1, X2, Y2)  &&
					(X2 != Xpza && Y2 != Ypza) &&
					(X1 != Xpza && Y1 != Ypza)   )
			{
				return true;
			}
		}
			
		return false;    
	}
	
	
	
	/* Devuelve true si el punto (x1, y1) pertenence al segmento.
	 * (x2, y2) a (x3, y3).  (puede estar en un vértice o dentro del segmento).
	 * Esto ocurre si la suma de las distancias del punto (x1, y1) a cada 
	 * extremo del segmento es igual a la longitud del segmento */
	private static boolean dentroPuntoSegm(int X1, int Y1, int X2, int Y2, int X3, int Y3)
	{
		if( distPuntoPunto(X1, Y1, X2, Y2)+
				distPuntoPunto(X1, Y1, X3, Y3)==
					distPuntoPunto(X2, Y2, X3, Y3) )
		{
			return true;
		}
		return false;    
	}


	/* Distancia entre dos puntos */
	private static double distPuntoPunto(int X1, int Y1, int X2, int Y2)
	{
		return sqrt(pow(X2-X1, 2)+pow(Y2-Y1, 2));
	}



	// Regresa una lista de 4 rotaciones a probar (0, 90, 180, 270).
	private static List<Double> rotacionesAProbar4()
	{
		List<Double> listaAngulos = new LinkedList<Double>();
		listaAngulos.add( (double)0 );
		listaAngulos.add((double)90);
		listaAngulos.add((double)180);
		listaAngulos.add((double)270);
		return listaAngulos;
	}


}

