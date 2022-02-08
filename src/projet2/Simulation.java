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
	 * Lors de la naissance de sim x à temps t, on fait le suivant:
	 * [n1] on tire une durée de vie D au hasard – enfiler nouvel événement de mort pour x, à temps t+D. 
	 * [n2] si x est une fille, alors on tire un temps d’attente A jusqu’à reproduction 
	 * – enfiler nouvel événement de reproduction pour x, à temps t+A.
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
	 * Lors de la mort de sim x à temps  t, on le retire de la PQ.
	 */
	public void mort(Event E) {
		PQ.deleteMin();
	}
	
	/* Reproduction: 
	 * Un événement de reproduction de sim x (la mère) à temps t se traite par la démarche suivante:
	 * [r1] si x est morte, alors rien à faire
	 * [r2] si x est d’age de reproduction (sinon, juste passer à r3), alors 
	 * choisir un partenaire y pour avoir un bébé avec:
	 * créer le sim bébé avec sexe au hasard, temps de naissance t, et enfiler l’événement de sa naissance
	 * enregistrer x et y comme derniers partenaires l’une à l’autre
	 * [r3] on tire un nouveau temps d’attente A jusqu’à reproduction 
	 * – enfiler nouvel événement de reproduction pour x, à temps t+A (également si elle est d’âge ou non)
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
		double A = AgeModel.randomWaitingTime(RND, rateR); //  on tire un nouveau temps d’attente A jusqu’à reproduction 
		Event reproduction = new Event(TypeEvent.ACCOUPLEMENT,mom,time+A); //enfiler nouvel événement de reproduction pour x, à temps t+A
		eventQ.insert(reproduction);
	}
	
	
/*  Règles de choisir le père:
 *  Pour une mère x déjà choisie, on sélectionne le père y au hasard, par un paramètre f 
 *  de «fidélité» du modèle (par défaut, 90%):
 *  [p1] si x est dans une relation avec z (bébé précédent avec z qui est toujours vivant, 
 *  et n’a pas triché avec une autre femme) :
 *  [p1.1] choisir z avec probabilité f
 *  [p1.2] ou choisir un autre homme adéquat (=vivant et d’age de reproduction) uniformément, 
 *  avec probabilité 1-f (l’homme sollicité accepte toujours l’offre, même s’il a un partenaire précédent)
 *  [p2] si x n’est pas dans une relation (aucun bébé précédent, ou x a un partenaire mort/infidèle), 
 *  alors elle sollicite des candidats  jusqu’à acceptance: choisir un homme adéquat y uniformément au hasard 
 *  – s’il n’est pas dans une relation, il accepte sans hésitation, ou s’il est dans une relation, 
 *  il accepte avec probabilité 1-f. 
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
	eventQ = new PQ<Event>(); // file de priorité
	int counter = 0;
	for (int i = 0; i < n; i++) {
		Sim fondateur = new Sim(model.randomSex(RND)); // sexe au hasard, naissance à 0.0
		Event E = new Event(TypeEvent.NAISSANCE, fondateur, 0.0); // nouvel événement de naissance pour fondateur à 0.0
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
		

	// méthode pour enregister les données dans un fichier Excel .csv
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
