package share;

import java.util.List;
import java.lang.Math;

/*
 *  @author Dr Eunice López-Camacho
 * Tecnológico de Monterrey, Campus Monterrey
 * 
 * Please report any bugs to Eunice López-Camacho at eunice.lopez@itesm.mx
 * or lopezeunice@gmail.com
 */

// In this class, the heuristics to be applied must be defined.

public class ControlHeuristics{

	Heuristics h = new Heuristics();

	public ControlHeuristics(){}
	
	public void ejecutaHeuristica(List<Piece> listapiezas, List<Sheet> listaObjetos, int heuristica) {   

		Sheet ObjetoMuestra = (Sheet)listaObjetos.get(0);
		int xObjeto = ObjetoMuestra.getXmax();
		int yObjeto = ObjetoMuestra.getYmax();

		
		switch(heuristica){
		case 0:
			h.First_Fit_Decreasing(listapiezas, listaObjetos, xObjeto, yObjeto, "BL");
			break;
		case 1:
			h.Filler(listapiezas, listaObjetos, xObjeto, yObjeto, "EC");
			break;
		case 2:
			h.Best_Fit_Decreasing(listapiezas, listaObjetos, xObjeto, yObjeto, "EC2");
			break;
		// Djang_and_Finch (fills at least 1/4 of the object in the initial stage)
		case 3:
			h.Djang_and_Finch(listapiezas, listaObjetos, xObjeto, yObjeto, "MA", 0.25);
			break;
		// Djang_and_Finch (fills at least 1/3 of the object in the initial stage)
		case 4:
			h.Djang_and_Finch(listapiezas, listaObjetos, xObjeto, yObjeto, "BL", 0.3333);
			break;
	    // Djang_and_Finch (fills at least 1/4 of the object in the initial stage)
		case 5:
			h.Djang_and_Finch(listapiezas, listaObjetos, xObjeto, yObjeto, "EC2", 0.5);
			break;
		default:
			h.Djang_and_Finch(listapiezas, listaObjetos, xObjeto, yObjeto, "BL", 0.3333);
		break;
		}
	}


	
	public void Imprime_Resultado(List<Sheet> listaObjetos)
	{
		Sheet objtemp;
		List<Piece> ListaPiezaInside;
		System.out.println(listaObjetos.size());
		for(int i=0; i< listaObjetos.size(); i++)
		{
			objtemp= (Sheet)listaObjetos.get(i);
			ListaPiezaInside=objtemp.getPzasInside();
			for(int j=0; j< ListaPiezaInside.size(); j++)
			{
				Piece piezatemp=(Piece)ListaPiezaInside.get(j);
				System.out.println("Sheet " + i + "= " + piezatemp.getnumber());
			}		
		}
		for(int i=0; i< listaObjetos.size(); i++)
		{
			objtemp= (Sheet)listaObjetos.get(i);
			ListaPiezaInside=objtemp.getPzasInside();
			for(int j=0; j< ListaPiezaInside.size(); j++)
			{
				Piece piezatemp=(Piece)ListaPiezaInside.get(j);
				System.out.println("Coordenada (xmax, ymax)" + piezatemp.getnumber() + " = " + piezatemp.getXmax() +" , "+ piezatemp.getYmax());
				System.out.println("Coordenada (xmin, ymin)" + piezatemp.getnumber() + " = " + piezatemp.getXmin() + " , "+ piezatemp.getYmin());
				int resta1 = piezatemp.getXmax()-piezatemp.getXmin();
				int resta2 = piezatemp.getYmax()-piezatemp.getYmin();
				System.out.println("RESTA "+ piezatemp.getnumber()+ "= " + resta1 + " " + resta2);
			}		
		}
		for(int i=0; i< listaObjetos.size(); i++)
		{
			objtemp= (Sheet)listaObjetos.get(i);
			ListaPiezaInside=objtemp.getPzasInside();
			for(int j=0; j< ListaPiezaInside.size(); j++)
			{
				Piece piezatemp=(Piece)ListaPiezaInside.get(j);
				if(piezatemp.getnumber() == 66)
				{

					System.out.println("Coordenada (xmax, ymax)" + piezatemp.getnumber() + " = " + piezatemp.getXmax() +" , "+ piezatemp.getYmax());
					System.out.println("Coordenada (xmin, ymin)" + piezatemp.getnumber() + " = " + piezatemp.getXmin() + " , "+ piezatemp.getYmin());
					int resta1 = piezatemp.getXmax()-piezatemp.getXmin();
					int resta2 = piezatemp.getYmax()-piezatemp.getYmin();
					System.out.println("RESTA "+ piezatemp.getnumber()+ "= " + resta1 + " " + resta2);
				}	
			}		
		}
	}

	public double calcularAptitud(List<Sheet> listaObjetos)
	{
		Sheet objtemp;
		List<Piece> ListaPiezasInside;
		Piece piezatemp;
		double [] Pu;
		double aux, aux2;
		double aptitud=0;
		int  api, ao;

		Pu=new double[listaObjetos.size()];
		for(int i=0; i<listaObjetos.size(); i++)
		{
			objtemp=(Sheet)listaObjetos.get(i);
			ListaPiezasInside=objtemp.getPzasInside();
			Pu[i]=0;
			ao=objtemp.gettotalsize();
			for(int j=0; j<ListaPiezasInside.size(); j++)
			{
				piezatemp=(Piece)ListaPiezasInside.get(j);
				api=piezatemp.getTotalSize();
				aux=(double)api/ao;
				Pu[i]=Pu[i]+aux;
			}
			aux2=Pu[i];
			Pu[i]=Math.pow(aux2, 2);
			aptitud=aptitud + Pu[i];	
		}
		aptitud=(double)aptitud/listaObjetos.size();
		return aptitud;
	}


}