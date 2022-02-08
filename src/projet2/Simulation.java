/* 	Auteurs: Hani Berchan et Lucky Khounvongsa
 * 	Simulation.java
 * 	Description:
 * 	Classe qui fait la simulation
 */

package projet2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.TreeMap;


public class Simulation {

	AgeModel model = new AgeModel();
	PQ<Event> eventQ;
	PQ<Sim> PQ = new PQ<Sim>();
	Random RND = new Random();
	
	TreeMap<Double, Integer> grandfathers = new TreeMap<Double,Integer>(); //aieux
	TreeMap<Double, Integer> grandmothers = new TreeMap<Double,Integer>(); //aieules
	TreeMap<Double, Integer> totalPQSize = new TreeMap<Double,Integer>();  // total population
	static ArrayList<Integer> logCounter = new ArrayList<Integer>();
	
	
	/* Naissance: 
	 * Lors de la naissance de sim x � temps t, on fait le suivant:
	 * [n1] on tire une dur�e de vie D au hasard � enfiler nouvel �v�nement de mort pour x, � temps t+D. 
	 * [n2] si x est une fille, alors on tire un temps d�attente A jusqu�� reproduction 
	 * � enfiler nouvel �v�nement de reproduction pour x, � temps t+A.
	 * [n3] on enregistre x dans la PQ 
	 */
	
	public void naissance(Event event) {
		//[n1]
		double d = model.randomAge(RND);
		event.subject.setDeathTime(event.time+d);
		eventQ.insert(new Event(TypeEvent.MORT,event.subject,event.time+d));
		//[n2]
		if(event.subject.getSex()==Sim.Sex.F) {
			double rateR = 2/model.expectedParenthoodSpan(Sim.MIN_MATING_AGE_F, Sim.MAX_MATING_AGE_F);
			double A = AgeModel.randomWaitingTime(RND, rateR);
			Event event2 = new Event(TypeEvent.ACCOUPLEMENT,event.subject,event.time+A);
			eventQ.insert(event2);
		}
		//[n3]
		PQ.insert(event.subject);
	}
	
	/* Mort: 
	 * Lors de la mort de sim x � temps  t, on le retire de la PQ.
	 */
	public void mort(Event E) {
		PQ.deleteMin();
	}
	
	/* Reproduction: 
	 * Un �v�nement de reproduction de sim x (la m�re) � temps t se traite par la d�marche suivante:
	 * [r1] si x est morte, alors rien � faire
	 * [r2] si x est d�age de reproduction (sinon, juste passer � r3), alors 
	 * choisir un partenaire y pour avoir un b�b� avec:
	 * cr�er le sim b�b� avec sexe au hasard, temps de naissance t, et enfiler l��v�nement de sa naissance
	 * enregistrer x et y comme derniers partenaires l�une � l�autre
	 * [r3] on tire un nouveau temps d�attente A jusqu�� reproduction 
	 * � enfiler nouvel �v�nement de reproduction pour x, � temps t+A (�galement si elle est d��ge ou non)
	 */
	
	public void reproduction(Event event) {
		Sim mom = event.subject;
		double time = event.time;
		//[r2]
		if(mom.isMatingAge(time)) {
			Sim partner = choixPere(time, mom); // choisir le partenaire
			Sim child = new Sim(mom,partner,time,model.randomSex(RND)); // sim bebe au hasard
			Event birth = new Event(TypeEvent.NAISSANCE,child,time); // nouvel event naissance
			eventQ.insert(birth);  // enfiler l'evenement de sa naissance
			// enregistrer partenaire l'un de l'autre
			mom.setMate(partner); 
			partner.setMate(mom);
		}
		//[r3]
		double rateR = 2/model.expectedParenthoodSpan(Sim.MIN_MATING_AGE_F, Sim.MAX_MATING_AGE_F);
		double A = AgeModel.randomWaitingTime(RND, rateR); //  on tire un nouveau temps d�attente A jusqu�� reproduction 
		Event reproduction = new Event(TypeEvent.ACCOUPLEMENT,mom,time+A); //enfiler nouvel �v�nement de reproduction pour x, � temps t+A
		eventQ.insert(reproduction);
	}
	
	
/*  R�gles de choisir le p�re:
 *  Pour une m�re x d�j� choisie, on s�lectionne le p�re y au hasard, par un param�tre f 
 *  de �fid�lit� du mod�le (par d�faut, 90%):
 *  [p1] si x est dans une relation avec z (b�b� pr�c�dent avec z qui est toujours vivant, 
 *  et n�a pas trich� avec une autre femme) :
 *  [p1.1] choisir z avec probabilit� f
 *  [p1.2] ou choisir un autre homme ad�quat (=vivant et d�age de reproduction) uniform�ment, 
 *  avec probabilit� 1-f (l�homme sollicit� accepte toujours l�offre, m�me s�il a un partenaire pr�c�dent)
 *  [p2] si x n�est pas dans une relation (aucun b�b� pr�c�dent, ou x a un partenaire mort/infid�le), 
 *  alors elle sollicite des candidats  jusqu�� acceptance: choisir un homme ad�quat y uniform�ment au hasard 
 *  � s�il n�est pas dans une relation, il accepte sans h�sitation, ou s�il est dans une relation, 
 *  il accepte avec probabilit� 1-f. 
 */
	

public Sim choixPere(double time, Sim x) {
	Sim y = null;
	double fidelity = model.getFidelity();

	if (!x.isInARelationship(time) || RND.nextDouble() > fidelity) {
		do {
			Sim z = PQ.randomSim();
			if (z.getSex() != x.getSex() && z.isMatingAge(time)) {
				if (!z.isInARelationship(time) || RND.nextDouble() > fidelity || x.isInARelationship(time)) { 
					y = z;
				}
			}
		} while (y == null);
	} else {
		y = x.getMate(); 
	}
		
	return y;
}
	
public void simulate(int n, double Tmax) {
	eventQ = new PQ<Event>(); // file de priorit�
	int counter = 0;
	for (int i = 0; i < n; i++) {
		Sim fondateur = new Sim(model.randomSex(RND)); // sexe au hasard, naissance � 0.0
		Event E = new Event(TypeEvent.NAISSANCE, fondateur, 0.0); // nouvel �v�nement de naissance pour fondateur � 0.0
		eventQ.insert(E);
	}

	while (!eventQ.isEmpty()) {
		Event E = (Event) eventQ.deleteMin();
		if (E.getTime() > counter) {
			logCounter.add(PQ.size());
			counter += 100;
		}
		totalPQSize.put(E.time, PQ.size);

		if (E.time > Tmax)
			break;
		if (E.subject.getDeathTime() >= E.getTime()) {
			if (E.typeEvent == TypeEvent.MORT) {
				mort(E);
			} else if (E.typeEvent == TypeEvent.ACCOUPLEMENT) {
				reproduction(E);
			} else if (E.typeEvent == TypeEvent.NAISSANCE) {
				naissance(E);
			}
		}

	}

}

	//***************************************************//
	//				      Coalescence				     //
	//***************************************************//  


	public void grandFathers(PriorityQueue<Sim> total) {
		while(!total.isEmpty()) {
			Sim s = (Sim) total.remove();
			Sim f = s.getFather();
			if(f != null) {
			if(!total.contains(f)) {
				total.add(f);
			} else {
			grandfathers.put(s.getBirthTime(), total.size());
			}
			}
		}
	}
	
	
	public void grandMothers(PriorityQueue<Sim> total) {
		while(!total.isEmpty()) {
			Sim s = (Sim) total.remove();
			Sim m = s.getMother();
			if(m != null) {
			if(!total.contains(m)) {
				total.add(m);
			} else {
			grandmothers.put(s.getBirthTime(), total.size());
			}
			}
		}
	}
	
	
	// Coalescence
	public void coalescence() {
		PriorityQueue[] total = PQ.splitPopulation(PQ);	
		grandFathers(total[0]);
		grandMothers(total[1]);
	}
		

	// m�thode pour enregister les donn�es dans un fichier Excel .csv
	// pour faire les graphiques
	public void savefile(int n,int tmax) throws IOException {
		// le chemin du fichier
		String path = new File("").getAbsolutePath() + "/sortie/";
		String name = String.format("sim_%d%d.csv",n,tmax);
		String fileName = path + name;
		File file = new File(fileName);
		
		FileWriter filewriter = new FileWriter(file);
		BufferedWriter sortie = new BufferedWriter(filewriter);
		
		sortie.write("Time,Size,Sex,Total\n" );
		 for (Double time : grandfathers.keySet()) {
             sortie.write(time + "," + grandfathers.get(time) + "," + "M" + "," + totalPQSize.get(time) +"\n");
         }
		 
         for (Double time : grandmothers.keySet()) {
             sortie.write(time + "," + grandmothers.get(time) + "," + "F" + "," + totalPQSize.get(time) + "\n");
         }  
		sortie.close();
	}
	
	public void print() {
		 for (Double time : grandfathers.keySet()) {
             System.out.println(time + "," + grandfathers.get(time) + "," + "M" + "," + totalPQSize.get(time) +"\n");
         }
		 
         for (Double time : grandmothers.keySet()) {
        	 System.out.println(time + "," + grandmothers.get(time) + "," + "F" + "," + totalPQSize.get(time) + "\n");
         }  
	}

	
	public static void main(String[] args) throws IOException {
		  Simulation simulation = new Simulation();
	        simulation.simulate(5000, 20000);  
	        simulation.coalescence();
//	        simulation.savefile(5000, 20000);
	        simulation.print();

	}
	
}
