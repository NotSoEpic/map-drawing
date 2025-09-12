package wawa.wayfinder.config;

public enum ReducedDebugLevel {
	NONE,
	COORDINATES,
	COORDINATES_AND_ROTATION,
	ALL;

	public boolean allowsCoordinates() {
		return this == NONE;
	}

	public boolean allowsRotation() {
		return this != ALL && this != COORDINATES_AND_ROTATION;
	}
}
