package unsw.cse.framescript.pilotsim;

import java.awt.EventQueue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class Pilot {
	static final int MaxHeadingChangePerUpdate = 1;
	static final int MaxElevationChangePerUpdate = 10;
	static final int MaxSpeedChangePerUpdate = 1;
	static final int TimerPeriod = 50;
	static final int MaxTimeOnDangerousCourse = 5000;
	static final int ClearanceHeightOverMountains = 1000;
	static final int StartingAltitude = 1000;
	int currentHeading = 0;
	int currentAltitude = StartingAltitude;
	int currentSpeed = 0;
	
	int desiredHeading = 0;
	int desiredAltitude = StartingAltitude;
	int desiredSpeed = 0;
	
	Timer timer;
	
	Vector listeners;
	
	public Pilot() {
		timer = new Timer();
		listeners = new Vector();
		sortArtifacts();
	}
	
	public void startSimulation() {
		timer.scheduleAtFixedRate(new CheckTask(this), 0, TimerPeriod);
	}
	
	public void setHeading(int heading) {
		desiredHeading = heading;
	}
	
	public void setAltitude(int elevation) {
		desiredAltitude = elevation;
	}
	
	public void setSpeed(int speed) {
		desiredSpeed = speed;
	}
	
	public int getCurrentHeading() {
		return currentHeading;
	}
	
	public int getCurrentAltitude() {
		return currentAltitude;
	}
	
	public int getCurrentSpeed() {
		return currentSpeed;
	}
	
	public int getDesiredHeading() {
		return desiredHeading;
	}
	
	public int getDesiredAltitude() {
		return desiredAltitude;
	}
	
	public int getDesiredSpeed() {
		return desiredSpeed;
	}
	
	// populate the environment
	
	Mountain [] mountains = {new Mountain(20, 0.15, 1000, 20),
			new Mountain(50, 0.5, 2500, 30),
			new Mountain(80, 0.1, 500, 8),
			new Mountain(85, 0.2, 650, 6),
			new Mountain(92, 0.15, 475, 8),
			new Mountain(120, 0.3, 2000, 24),
			new Mountain(160, 0.4, 2500, 20),
			new Mountain(190, 0.2, 1500, 16),
			new Mountain(250, 0.2, 1500, 16),
			new Mountain(265, 0.4, 8000, 34),
			new Mountain(300, 0.35, 6500, 40),
			new Mountain(340, 0.5, 4000, 20)};
	House [] houses = {new House(5, 0.25), new House(10, 0.3),
			new House(13, 0.4), new House(43, 0.5), new House(50, 0.2),
			new House(54, 0.18), new House(58, 0.2), new House(70, 0.4),
			new House(80, 0.6), new House(110, 0.1), new House(112, 0.15),
			new House(130, 0.3), new House(160, 0.4), new House(165, 0.3),
			new House(195, 0.4), new House(200, 0.3), new House(240, 0.3),
			new House(255, 0.15), new House(270, 0.3), new House(295, 0.3),
			new House(315, 0.5), new House(320, 0.4), new House(324, 0.3),
			new House(332, 0.4), new House(335, 0.8), new House(355, 0.75)};
	
	long beganFacingObstacle = -1;
	void checkCourse() {
		if (currentHeading != desiredHeading || currentAltitude != desiredAltitude ||
				currentSpeed != desiredSpeed) {
//			System.err.println("ch=" + currentHeading + ", ce=" + currentElevation + ", cs=" + currentSpeed +
//					", dh=" + desiredHeading + ", de=" + desiredElevation + ", ds=" + desiredSpeed);
			if (currentHeading != desiredHeading) {
				int headingDiff = (360 + desiredHeading - currentHeading) % 360;
				if (headingDiff > 180)
					headingDiff -= 360;
//				System.err.println("hd=" + headingDiff);
				if (headingDiff > MaxHeadingChangePerUpdate)
					headingDiff = MaxHeadingChangePerUpdate;
				else if (headingDiff < -MaxHeadingChangePerUpdate)
					headingDiff = -MaxHeadingChangePerUpdate;
				currentHeading = (currentHeading + headingDiff + 360) % 360;
			}
			if (currentAltitude != desiredAltitude) {
				int elevationDiff = desiredAltitude - currentAltitude;
//				System.err.println("ed=" + elevationDiff);
				if (elevationDiff > MaxElevationChangePerUpdate)
					elevationDiff = MaxElevationChangePerUpdate;
				else if (elevationDiff < - MaxElevationChangePerUpdate)
					elevationDiff = -MaxElevationChangePerUpdate;
				currentAltitude += elevationDiff;
			}
			if (currentSpeed != desiredSpeed) {
				int speedDiff = desiredSpeed - currentSpeed;
//				System.err.println("sd=" + speedDiff);
				if (speedDiff > MaxSpeedChangePerUpdate)
					speedDiff = MaxSpeedChangePerUpdate;
				else if (speedDiff < MaxSpeedChangePerUpdate)
					speedDiff = - MaxSpeedChangePerUpdate;
				currentSpeed += speedDiff;
			}
			notifyListeners();
			beganFacingObstacle = -1;
		} else {
			Mountain obstacleInWay = null;
			for (int i = 0; obstacleInWay == null && i < mountains.length; i++) {
				int hDiff = (mountains[i].heading - currentHeading + 360 +
						mountains[i].width / 2) % 360;
				if (hDiff > 0 && hDiff < mountains[i].width &&
						currentAltitude < mountains[i].height + ClearanceHeightOverMountains) {
					obstacleInWay = mountains[i];
				}
			}
			if (obstacleInWay != null) {
				if (beganFacingObstacle == -1) {
					beganFacingObstacle = System.currentTimeMillis();
				} else if (System.currentTimeMillis() - beganFacingObstacle >
						MaxTimeOnDangerousCourse) {
					setAltitude(obstacleInWay.height + ClearanceHeightOverMountains);
					notifyListeners("to avoid collision with mountain");
				}
			} else
				beganFacingObstacle = -1;
		}
	}
	
	void sortArtifacts() {
		for (int i = 0; i < mountains.length; i++) {
			for (int j = mountains.length - 1; j > i; j--) {
				if (mountains[j-1].distance < mountains[j].distance) {
					Mountain tmp = mountains[j];
					mountains[j] = mountains[j-1];
					mountains[j-1] = tmp;
				}
			}
		}
		for (int i = 0; i < houses.length; i++) {
			for (int j = houses.length - 1; j > i; j--) {
				if (houses[j-1].distance < houses[j].distance) {
					House tmp = houses[j];
					houses[j] = houses[j-1];
					houses[j-1] = tmp;
				}
			}
		}
	}
	
	public void terminate() {
		timer.cancel();
	}
	
	synchronized public void addListener(PilotListener listener) {
		listeners.add(listener);
	}
	
	synchronized public void removeListener(PilotListener listener) {
		listeners.remove(listener);
	}
	synchronized private void notifyListeners() {
		notifyListeners(null);
	}
	synchronized private void notifyListeners(String reason) {
		for (int i = 0; i < listeners.size(); i++) {
			EventQueue.invokeLater(new ListenerNotifier((PilotListener)listeners.get(i), reason));
		}
	}
	
	class ListenerNotifier implements Runnable {
		PilotListener listener;
		String reason;
		
		ListenerNotifier(PilotListener listener, String reason) {
			this.listener = listener;
			this.reason = reason;
		}

		public void run() {
			if (reason == null)
				listener.courseChanged();
			else
				listener.alteringCourse(reason);
		}
	}
	
	class CheckTask extends TimerTask {
		Pilot pilot;
		
		CheckTask(Pilot pilot) {
			this.pilot = pilot;
		}
		
		public void run() {
			pilot.checkCourse();
		}
	}
	
	class Mountain {
		int heading;
		double distance;
		int height;
		int width;
		
		Mountain(int heading, double distance, int height, int width) {
			this.heading = heading;
			this.distance = distance;
			this.height = height;
			this.width = width;
		}
		
		public String toString() {
			return "Mountain("+ heading + ", " + distance + ", " + height + ", " + width + ")";
		}
	}
	
	class House {
		int heading;
		double distance;
		
		House(int heading, double distance) {
			this.heading = heading;
			this.distance = distance;
		}
		
		public String toString() {
			return "House("+ heading + ", " + distance + ")";
		}
	}
}
