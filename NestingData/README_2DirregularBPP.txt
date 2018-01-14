
Author: Dr Eunice López-Camacho
Tecnológico de Monterrey, Campus Monterrey

Acknowledgements: Juan Carlos Gómez Carranza, Claudia Jannet Farías Zárate and 
José Carlos Ortiz Bayliss.

This program is distributed on an "AS IS" BASIS,WITHOUT WARRANTIES OR CONDITIONS
OF ANY KIND, either express or implied, including but not limited to the 
warranties of merchantability, fitness for a particular purpose and noninfringement.
In no event shall the authors be liable for any claim, damages or other liability,
whether in an action of contract, tort or otherwise, arising from, out of or in 
connection with the software or the use or other dealings in the software.

This program solves a set of 2D irregular instances with several single 
heuristics.  Convex and non-convex shapes can be handled.
To use, you will need a file with a list of instances to be solved and an 
individual file with the definition of each instance.

The individual files for each instance must contain:
- first line: the number N of pieces;
- second line: the width and height of the rectangular objects where pieces are placed.
- each of next N lines: number of vertices and coordinates x1 y1 x2 y2 x3 y3 ... xN yN.  
  Coordinates are counterclockwise and pair numbers.

Some articles that have employed this program:
(Please, be sure to cite the corresponding article when applicable)
1)  López-Camacho, E., Ochoa, G., Terashima-Marín, H. and Burke, E. K. (2013), 
An effective heuristic for the two-dimensional irregular bin packing problem,
Annals of Operations Research. Volume 206, Issue 1, pp 241-264. DOI:10.1007/s10479-013-1341-4.
2)  López-Camacho, E., Terashima-Marín H., Ochoa, G. and Conant-Pablos, S. (2013), 
Understanding the structure of bin packing problems through principal component analysis,
International Journal of Production Economics, Special Issue on Cutting and Packing. 
DOI:10.1016/j.ijpe.2013.04.041. 
3)  López-Camacho, Eunice.  PhD. Dissertation.  Tecnológico de Monterrey, Campus Monterrey,
México.  An evolutionary framework for producing hyper-heuristics for solving the 2D irregular
bin packing problem. Main advisor: Dr. Hugo Terashima-Marín.


Please report any bugs to Eunice López-Camacho at eunice.lopez@itesm.mx
or lopezeunice@gmail.com

How to read the results:
 - Individual results file:
 Each row has the number of the heuristic, fitness and number of objects.
 *
- Aggregated results file:
 Each row has the name of the instance, best fitness, best number of objects 
 and the number of the heuristic to get the best result
 (in case of ties, the lowest-index heuristic is reported).
 
*/