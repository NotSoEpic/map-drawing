package wawa.mapwright.config;

import net.minecraft.client.Minecraft;

public enum ReducedDebugLevel {
	NONE,
	COORDINATES,
	COORDINATES_AND_ROTATION,
	ALL;

	public boolean allowsCoordinates() {
		return this == NONE || Minecraft.getInstance().player.isCreative() || Minecraft.getInstance().player.isSpectator();
	}

	public boolean allowsRotation() {
		return (this != ALL && this != COORDINATES_AND_ROTATION) || Minecraft.getInstance().player.isCreative() || Minecraft.getInstance().player.isSpectator();
	}
}
