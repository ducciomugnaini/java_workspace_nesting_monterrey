package share;

import javax.swing.JFrame;
import java.util.List;
import java.awt.Graphics;
import java.awt.Color;
import java.util.*;
import java.awt.*;
import java.lang.Integer;

/*
 *  @author Dr Eunice López-Camacho
 * Tecnológico de Monterrey, Campus Monterrey
 * 
 * Please report any bugs to Eunice López-Camacho at eunice.lopez@itesm.mx
 * or lopezeunice@gmail.com
 */

public class ResultVisual extends JFrame{

	private static final long serialVersionUID = 1L;
	Vector<Sheet> listaObj;
	Choice Selector;
	int objetoselec = 0;
	
	public ResultVisual(Vector<Sheet> listaObjetos)  
	{		
		super("Visual Solution");
		getContentPane().setBackground(Color.white);
		listaObj = listaObjetos;
		Panel p = new Panel();
        p.add( new Button( "Out") );
        getContentPane().add( "South",p );
        Selector = new Choice();
        Label etiq1 = new Label( "Sheets" );
        getContentPane().setLayout( new FlowLayout( FlowLayout.CENTER,10,580) );
        getContentPane().add( etiq1 );
        for(int i=0; i<listaObj.size(); i++)
		{
			Selector.addItem(String.valueOf(i));
		}
        getContentPane().add("East", Selector );
		
	}
	
   
    // Divide coordinates by 2, for visualization purposes of sheets 1000 x 1000.
    // Puts origin in the left-bottom corner
	public void paint( Graphics g)
	{
		Sheet obj;
		Piece pza;
		String name = "Sheet ";
		List<Piece> listapiezas;
		obj = (Sheet)listaObj.get(objetoselec);
		g.setColor(Color.white);
		g.fillRect(0,0,700,650);
		g.setFont( new Font( "Helvetica",Font.BOLD,18 ) );
		g.setColor(Color.black);
		g.drawString(name.concat(String.valueOf(objetoselec)), 55,45);
		g.setColor(Color.white);
        g.fillRect(50,50, obj.getXmax()/2,obj.getYmax()/2);
		g.setColor(Color.black);
        g.drawRect(50,50, obj.getXmax()/2,obj.getYmax()/2);
        listapiezas = obj.getPzasInside();
        
        for(int i=0; i<listapiezas.size(); i++)
        {
        	pza = (Piece)listapiezas.get(i);
        	int[] coordenadasX = new int[pza.getvertices()];
        	int[] coordenadasY = new int[pza.getvertices()];
        	String letrero;
        	
        	for(int j=0; j<pza.getvertices(); j++)
        	{
        		coordenadasX[j]=50+( pza.coordX[j] )/2;
        		coordenadasY[j]=50+( (obj.getYmax()-pza.coordY[j]) )/2;
        	}
        	g.setColor(Color.pink);
        	g.fillPolygon(coordenadasX, coordenadasY, pza.getvertices());
			g.setColor(Color.black);
        	g.drawPolygon(coordenadasX, coordenadasY, pza.getvertices());
        	g.setFont( new Font("Helvetica",Font.PLAIN,9) );
        	
        	letrero = String.valueOf(pza.getnumber());
      		letrero = letrero.concat(" - ");
      		letrero = letrero.concat( String.valueOf(pza.isRotated() ) );
        	g.drawString(letrero,50+(pza.getXmin()+pza.getXmax())/4,50+( obj.getYmax()-(pza.getYmin()+pza.getYmax())/2 )/2 );
        }
        
	}
	
	public boolean handleEvent( Event evt ) 
    {
	     switch( evt.id ) 
	     {
	         case Event.ACTION_EVENT:
	         {
	              if( "Out".equals( evt.arg ) ) 
	              {
	              	dispose();
	       			return true;
	              }
	          }
	          case Event.LIST_SELECT:
	          {
	          		String objeto1 = (String)evt.arg;
	       		 	objetoselec = Integer.parseInt(objeto1);
	        		repaint();
	        		return true;
	          }              
	          default: 
	              return false;
	     }    
    }	
}