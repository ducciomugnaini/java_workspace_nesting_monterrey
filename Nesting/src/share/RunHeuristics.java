package share;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/*
 * @author Dr Eunice López-Camacho
 * Tecnológico de Monterrey, Campus Monterrey
 * 
 * Acknowledgements: Juan Carlos Gómez Carranza, Claudia Jannet Farías Zárate and 
 * José Carlos Ortiz Bayliss.
 * 
 * This program is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
 * CONDITIONS OF ANY KIND, either express or implied, including but not limited
 * to the warranties of merchantability, fitness for a particular purpose and
 * noninfringement.  In no event shall the authors be liable for any claim, 
 * damages or other liability, whether in an action of contract, tort or 
 * otherwise, arising from, out of or in connection with the software or the
 * use or other dealings in the software.
 * 
 * This program solves a set of 2D irregular instances with several single 
 * heuristics.  Convex and non-convex shapes can be handled.
 * To use, you will need a file with a list of instances to be solved and an 
 * individual file with the definition of each instance.
 * 
 * The individual files for each instance must contain:
 * - first line: the number N of pieces;
 * - second line: the width and height of the rectangular objects where pieces are placed.
 * - each of next N lines: number of vertices and coordinates x1 y1 x2 y2 x3 y3 ... xN yN.  
 *   Coordinates are counterclockwise and pair numbers.
 * 
 * Some articles that have employed this program:
 * (Please, be sure to cite the corresponding article when applicable)
 * 1)  López-Camacho, E., Ochoa, G., Terashima-Marín, H. and Burke, E. K. (2013), 
 * An effective heuristic for the two-dimensional irregular bin packing problem,
 * Annals of Operations Research. Volume 206, Issue 1, pp 241-264. DOI:10.1007/s10479-013-1341-4.
 * 2)  López-Camacho, E., Terashima-Marín H., Ochoa, G. and Conant-Pablos, S. (2013), 
 * Understanding the structure of bin packing problems through principal component analysis,
 * International Journal of Production Economics, Special Issue on Cutting and Packing. 
 * DOI:10.1016/j.ijpe.2013.04.041. 
 * 3)  López-Camacho, Eunice.  PhD. Dissertation.  Tecnológico de Monterrey, Campus Monterrey,
 * México.  An evolutionary framework for producing hyper-heuristics for solving the 2D irregular
 * bin packing problem. Main advisor: Dr. Hugo Terashima-Marín.
 * 
 * Please report any bugs to Eunice López-Camacho at eunice.lopez@itesm.mx
 * or lopezeunice@gmail.com
 * 
 * How to read the results:
 *  - Individual results file:
 *  Each row has the number of the heuristic, fitness and number of objects.
 *
 * - Aggregated results file:
 *  Each row has the name of the instance, best fitness, best number of objects 
 *  and the number of the heuristic to get the best result
 *  (in case of ties, the lowest-index heuristic is reported).
 *  
*/

public class RunHeuristics {
	
	public static void main(String[] args) throws Exception{
        
		/*File f = null;
	      boolean bool = false;
	      
	      try {
	      
	         // returns pathnames for files and directory
	         f = new File("C:/Users/mugna/Documents/GIT/workspace_nesting_monterrey/NestingData/tables/");
	         
	         // create
	         bool = f.mkdir();
	         
	         // print
	         System.out.print("Directory created? "+bool);
	         
	      } catch(Exception e) {
	      
	         // if any error occurs
	         e.printStackTrace();
	      }
		*/
		
		String trainingSet = "listOfInstances";  //List of instances to solve
        
		// Path where a .txt has the list of instances to solve:
        File dirEntrada = new File("C:/Users/mugna/Documents/GIT/workspace_nesting_monterrey/NestingData/");
        
        // Path where all instances in .txt are:
        File dirEntradaPart = new File("C:/Users/mugna/Documents/GIT/workspace_nesting_monterrey/NestingData/");
        
        // The aggregated results will fall here:
        File dirSalida = new File("C:/Users/mugna/Documents/GIT/workspace_nesting_monterrey/NestingData/results/"+ trainingSet+"/");
        
        // The single instances results will fall here:
        File dirSalidaPart = new File("C:/Users/mugna/Documents/GIT/workspace_nesting_monterrey/NestingData/results/");
 
        // Parameters
        int numHeuristics = 6;  //Number of heuristics to solve the set of instances
        boolean repetition = false;   // true = solve again and overwrite results files
        boolean graficar = true;    // true = graph results

        if(!dirSalidaPart.exists())
        	dirSalidaPart.mkdir();
        
        if(!dirSalida.exists())
        	dirSalida.mkdir();
        
        File archivoProblemas = new File(dirEntrada,trainingSet+".txt");
        
        System.out.println("Solving instances: "+trainingSet);
        RunHeuristics.run(
        		dirEntradaPart,dirSalida, dirSalidaPart, archivoProblemas, 
        		trainingSet, numHeuristics, repetition, graficar);
        System.out.println("Finish");
        System.out.println();
	}
	
	public RunHeuristics(){}

	public static void run(
			File directorioEntrada, File directorioSalidaGral, File directorioSalida, File archivoProblemas, 
			String archivo, int numHeuristicas, boolean repeticion, boolean graficar
			) throws Exception{
		
		RWfiles rw = new RWfiles();
		List<String> problemas = new ArrayList<String>();
		File archivoSalida0 = new File(directorioSalidaGral,"salida_"+archivo);
		File archivoSalida = new File(directorioSalidaGral,"salida_"+archivo+".txt");
		
		if(!archivoSalida.exists() || archivoSalida.length() ==0 || repeticion){
			try{
				
				problemas = rw.leerProblemas(archivoProblemas);
				
			}catch (Exception e){
				System.err.println("Error al leer el archivo de problemas "+archivoProblemas);
				System.exit(0);
			}
			
			int numProblemas = problemas.size();   
			double[][] aptitudes= new double[numProblemas][numHeuristicas]; 
			int[][] numObjetos= new int[numProblemas][numHeuristicas];
			int[][] executionTime = new int[numProblemas][numHeuristicas];
			int indice = 0;
			Iterator<String> iter = problemas.iterator();
			while (iter.hasNext()){
				Instance p = new Instance(numProblemas*numHeuristicas);
				String problema = iter.next();
				File archivoOut = new File(directorioSalida,"salida_"+problema);
				if(!archivoOut.exists() || archivoOut.length()==0 || repeticion==true){
					PrintWriter imprimirSalida = new PrintWriter(archivoOut);
					for(int i=0; i<numHeuristicas; i++){   // solve with the heuristics
						System.out.println("Solving "+ problema +" with heuristic "+ i);
						p.obtenerProblema(new File(directorioEntrada,problema));
						long start = System.currentTimeMillis();
						aptitudes[indice][i]=p.ejecutaAccion(i, indice, graficar);  
						numObjetos[indice][i]=p.numeroObjetos();
						long stop = System.currentTimeMillis();
						executionTime[indice][i]=(int)(stop-start);
						imprimirSalida.println(i+","+aptitudes[indice][i]+","+numObjetos[indice][i]);
					}
					imprimirSalida.close();

				}
				else{
					try{
						BufferedReader reader = new BufferedReader(new FileReader(archivoOut));
						String line = null;
						String[] lineBreak;
						for(int i=0;i<numHeuristicas;i++){
							line = reader.readLine();
							lineBreak = line.split(",");
							aptitudes[indice][i]=Double.valueOf(lineBreak[1]);
							numObjetos[indice][i]=Integer.valueOf(lineBreak[2]);
						}
						reader.close();
					}catch (Exception e){
						System.err.println("No se puede leer el archivo : "+archivoOut);
					}
				}
				indice++;
			} //finish solving instance by instance
			
			
			
			int[] indiceMejores=rw.buscarMejor(aptitudes, numObjetos, indice, numHeuristicas);
			try{
				rw.archivoSalida(archivoSalida0, archivoSalida, problemas, aptitudes, numObjetos, executionTime, indiceMejores, indice);
			}catch (Exception e){
				System.err.println("Error al escribir el archivo de salida de heuristicas simples");
				System.exit(0);
			}
			
			
		}
	}


}
