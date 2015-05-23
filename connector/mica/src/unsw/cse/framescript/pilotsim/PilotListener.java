package unsw.cse.framescript.pilotsim;

public interface PilotListener {
	public void courseChanged();
	public void alteringCourse(String reason);
}