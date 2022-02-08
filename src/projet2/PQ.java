/* 	Auteurs: Hani Berchan et Lucky Khounvongsa
 * 	PQ.java
 * 	Description:
 * 	Classe de file de priorité (PriorityQueue).
 * 	Implémentation de tas binaire avec d = 4.
 * 	Reference: https://docs.oracle.com/javase/7/docs/api/java/util/PriorityQueue.html
 * 			   https://docs.oracle.com/javase/tutorial/java/generics/types.html
 */
package projet2;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;


public class PQ<K extends Comparable<K>> {
    K[] T;
    int size;


    public PQ() {
        T = (K[]) new Comparable[4]; // d = 4
        size = 1;
    }
	//***************************************************//
	//						Size						 //
	//***************************************************//	
    
    public int size() {
		return size;
	}
    
    public boolean isEmpty() {
        return size == 1;
    }

   
    //***************************************************//
  	//					Modifie Size					 //
  	//***************************************************//	
    
 // méthode pour augmenter l'espace du tableau
    private void expandSize() {
        if (size == 0) {
            T = (K[]) new Comparable[1];
        } else {
            K[] newT = (K[]) new Comparable[T.length*2];
            for (int i = 0; i < size; i++) {
                newT[i] = T[i];
            }
            T = newT;

        }
    }
 // méthode pour diminuer l'espace du tableau
	private void shrinkSize() {
		if (size == 0) {
			T = (K[]) new Comparable[1];
		} else {
			K[] newT = (K[]) new Comparable[T.length / 2];
			for (int i = 0; i < size; i++) {
				newT[i] = T[i];
			}
			T = newT;
		}
	}

 // méthode pour verifier le size du tableau
    private void verifierSize(int delta) {
        int newSize = size + delta;

        if (newSize >= T.length) {
            expandSize();
        } else if (newSize <= T.length / 4) {
            shrinkSize();
        }
    }
    
    //***************************************************//
  	//				    Parent & Child				     //
  	//***************************************************//  
     
    private int parent(int index) {
        return Math.floorDiv(index, 2);
    }


    private int minChild(int index) {
        int numChild = 0; 
        int child1 = (index * 2);
        int child2 = (index * 2) + 1; 
        if ( child1  <= size - 1) { 
        	numChild = child1 ;
            if (child2 <= size - 1 && T[child2].compareTo(T[child1]) < 0) { 
            	numChild = child2;
            }
        }
        return numChild;
    }
    //***************************************************//
  	//				    Priority Queue	    		     //
  	//***************************************************//
    
    // méthode swim
    private void swim(K event, int index) {
        int varParent = parent(index);
        while (varParent != 0 && T[varParent].compareTo(event) > 0) {
            T[index] = T[varParent];
            index = varParent;
            varParent = parent(index);
        }
        T[index] = event;
    }
    
    // méthode sink
    private void sink(K event, int index) {
        int varChild = minChild(index);
        while (varChild  != 0 && T[varChild].compareTo(event) < 0) {
            T[index] = T[varChild];
            index = varChild ;
            varChild  = minChild(varChild); 
        }
        T[index] = event;
    }

    // méthode insert
    void insert(K event) {
        verifierSize(1);	// verifier le size du tableau
       T[size] = event;
        swim(event, size);
        size++;
    }
    
 
    
 // méthode deleteMin()
    public K deleteMin() {
        K r = T[1];
        if (size > 1) {
            K v = T[size - 1];
            T[size - 1] = null;
            size--;
            sink(v, 1);
        }
        verifierSize(-1);
        return r;
    }

	//***************************************************//
	//					Population						 //
	//***************************************************//	
    
	// méthode pour trouver un Sim aléatoire
	public Sim randomSim() {
		Random rand = new Random();
		int randomSim = rand.nextInt(size-1)+1; // pas de position 0
		return (Sim) T[randomSim];
	}

		// méthpde pour séparer les hommes et le femmes pour plustard faire la coalescence
		public PriorityQueue[] splitPopulation(PQ<Sim> person) {
			PriorityQueue[] pop = new PriorityQueue[2];
			PriorityQueue<Sim> malePopulation = new PriorityQueue<Sim>(new Comparator<Sim>() {
				@Override
				public int compare(Sim o1, Sim o2) {
					return o1.compareBirth(o2);
				}
			});
			
			PriorityQueue<Sim> femalePopulation = new PriorityQueue<Sim>(new Comparator<Sim>() {
				@Override
				public int compare(Sim o1, Sim o2) {
					return o1.compareBirth(o2);
				}
			});

			while (!person.isEmpty()) {
				Sim s = (Sim) person.deleteMin();
				if (s.getSex() == Sim.Sex.M) {
					malePopulation.add(s);
				} else {
					femalePopulation.add(s);
				}
			}
			
			pop[0] = malePopulation;
			pop[1] = femalePopulation;
			
			return pop;
		}

		
	//***************************************************//
	//						String						 //
	//***************************************************//			
	
    @Override
    public String toString() {
        return "{" +
                "T=" + Arrays.toString(T) +
                ", size=" + size +
                '}';
    }



}

