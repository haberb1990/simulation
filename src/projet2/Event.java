/*	Auteurs: Hani Berchan et Lucky Khounvongsa
 * 	Event.java
 * 	Description:
 *  Classe pour les évènements qui a comme attribut le type d'évènement 
 *  tel que la naissance, la mort et l'accouplement de sims, un sim 
 *  spécifique et le temps de son occurence. 
 */

package projet2;

public class Event implements Comparable<Event>{
	public TypeEvent typeEvent;
	protected Sim subject;
	protected double time;
		
	public Event(TypeEvent typeEvent, Sim subject, double time) {
		this.typeEvent = typeEvent;
		this.subject = subject;
		this.time = time;
	}
	
	public TypeEvent getTypeEvent() {
		return typeEvent;
	}


	public void setTypeEvent(TypeEvent typeEvent) {
		this.typeEvent = typeEvent;
	}


	public Sim getSubject() {
		return subject;
	}

	public void setSubject(Sim subject) {
		this.subject = subject;
	}


	public double getTime() {
		return time;
	}


	public void setTime(double time) {
		this.time = time;
	}

	// Comparaison de temps d'évènement
	@Override
	public int compareTo(Event o) {
		return Double.compare(time, o.time);
	}

	@Override
	public String toString() {
		return time + "";
	}

}
