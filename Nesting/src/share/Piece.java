package share;

import static java.lang.Math.*;

/*
 *  @author Dr Eunice López-Camacho
 * Tecnológico de Monterrey, Campus Monterrey
 * 
 * Please report any bugs to Eunice López-Camacho at eunice.lopez@itesm.mx
 * or lopezeunice@gmail.com
 */

//Note: Vertices must be ordered counterclockwise
//Computation of some piece features were deleted for this version of the program.


public class Piece 
{
	public int[] coordX;   // X coordinates
	public int[] coordY;
	private int[] coriX;    // original X coordinates
	private int[] coriY;
	private int vertices;  // number of vertices
	private int area;
	private int xmin;
	private int xmax;
	private int ymin;
	private int ymax;
	private int ancho;   // width  (xmax - xmin) 
	private int alto;    // height   (ymax - ymin)
	private int numero_Pieza;		// Piece number
	private double rotada;  	// angle of rotation

	public Piece(int[] coordenadas){
		this.vertices = coordenadas.length/2;
		int n = this.vertices;
		this.coordX = new int[n];
		this.coordY = new int[n];
		this.coriX = new int[n];
		this.coriY = new int[n];
		for(int i=0;i<n*2;i+=2){
			this.coordX[i/2]=coordenadas[i];
			this.coordY[i/2]=coordenadas[i+1];
			this.coriX[i/2]=coordenadas[i];
			this.coriY[i/2]=coordenadas[i+1];
		}
		this.xmax = ArrayOperations.Mayor(coordX);      
		this.ymax = ArrayOperations.Mayor(coordY);
		this.xmin = ArrayOperations.Menor(coordX);   
		this.ymin = ArrayOperations.Menor(coordY);
		this.ancho = this.xmax-this.xmin;
		this.alto = this.ymax-this.ymin;
		this.area = this.calculaArea();
		this.rotada = 0;
	}

	public void setnumber(int pnumber){
		numero_Pieza = pnumber;
	}

	public int getnumber(){
		return numero_Pieza;
	}

	public int getvertices(){   
		return vertices;
	}

	public int getxsize(){
		return xmax - xmin;
	}

	public int getysize(){
		return ymax - ymin;
	}

	public int getTotalSize(){
		return area;
	}

	public int getXmin(){
		return ArrayOperations.Menor(this.coordX);
	}

	public int getXmax(){
		return ArrayOperations.Mayor(this.coordX);
	}

	public int getYmin(){
		return ArrayOperations.Menor(this.coordY);
	}

	public int getYmax(){
		return ArrayOperations.Mayor(this.coordY);  
	}

	// This could be useful for measure irregularity.
	public double getRectangularidad(){
		return (double)area/(double)(alto*ancho);  
	}

    public boolean isNonConvex(){
    	if(anguloMayor() > 180)
    	{
    		return true;
    	}
    	return false;
    }

	public void moveToXY(int x, int y, int referencia){
		int despX = 0;   //movements along the X and Y axis.
		int despY = 0;
		switch(referencia)
		{
			//Bottom Right Corner
			case 1: 
				despX = x - getXmax();
				moveDistance(despX, 4);
				despY = y - getYmin();
				moveDistance(despY, 1);
				break;
			//Bottom Left Corner
			case 2: 
				despX = x - getXmin();
				moveDistance(despX, 4);
				despY = y - getYmin();
				moveDistance(despY, 1);
				break;
			//Top Right Corner
			case 3: 
				despX = x - getXmax();
				moveDistance(despX, 4);
				despY = y - getYmax();
				moveDistance(despY, 1);
				break;
			//Top Left Corner
			case 4: 
				despX = x - getXmin();
				moveDistance(despX, 4);
				despY = y - getYmax();
				moveDistance(despY, 1);
				break;
		}
	}



	public void moveDistance( int dist, int dir ){
		switch(dir)
		{
			//Up
			case 1:
				for(int i=0; i<vertices; i++)
				{
					coordY[i] += dist;
				}
				break;
				//Down
			case 2: 
				for(int i=0; i<vertices; i++)
				{
					coordY[i] -= dist;
				}
				break;
				//Left
			case 3:
				for(int i=0; i<vertices; i++)
				{
					coordX[i] -= dist;
				}
				break;
				//Right
			case 4:
				for(int i=0; i<vertices; i++)
				{
					coordX[i] += dist;
				}
				break;
		}
	}	



	// Rotate a piece (counterclockwise) a given angle.
	public void rotate(double angulo){
		double radianes = toRadians(angulo + rotada);  //total angle: the rotated until now + the new rotation angle
		double coseno = cos(radianes);
		double seno = sin(radianes);
		int tempXmin = xmin;
		int tempYmin = ymin;
		int tempX, tempY;
		for(int i=0; i<vertices; i++)
		{
			tempX = coriX[i];
			tempY = coriY[i];								
			coordX[i] = (int)( round( (double)(tempX)*coseno   //rotates the total angle
					- (double)(tempY)*seno  ));
			coordY[i] = (int)( round( (double)(tempX)*seno
					+ (double)(tempY)*coseno));
		}	

		rotada += angulo;
		moveToXY(tempXmin, tempYmin, 2);
	}


	public void desRotar(){
		int tempXmin = xmin;
		int tempYmin = ymin;
		for(int i=0; i<vertices; i++)
		{
			coordX[i] = coriX[i];
			coordY[i] = coriY[i];								
		}	
		rotada = 0;
		moveToXY(tempXmin, tempYmin, 2);
	}  

	public double isRotated(){
		return rotada;
	}


	public double anguloMayor()
	{
		double mayor = 0;
		double[] angulosInt = new double[vertices]; 
		for(int i=0; i<vertices; i++)
		{
			if(angulosInt[i] > mayor)
			{
				mayor = angulosInt[i];
			}
		}
		return mayor;
	}
	
	
	private int calculaArea(){
		int n = this.vertices;
		int suma = 0;
		for(int i=0;i<n-1;i++) {
			suma+=this.coordX[i]*this.coordY[i+1]-
			this.coordY[i]*this.coordX[i+1];
		}
		int i=n-1;
		suma+=this.coordX[i]*this.coordY[0]-
		this.coordY[i]*this.coordX[0];
		suma = Math.abs(suma)/2;
		return suma;
	}
	
	
	
   // Returns -1 if (X, Y) is not a vertex.
   // Returns the vertex index corresponding to (X, Y)
   // (vertices indexes goes from 0 to vertices-1.)
   // This method receives points belonging to the piece contour
   // (including vertices).
   public int numVertice(int[] punto)
   {
		for (int i=0; i < vertices; i++)
		{
			if(punto[0] == coordX[i] && punto[1] == coordY[i])
			{
				return i;
			}
		}
		
      return -1;
    }

}