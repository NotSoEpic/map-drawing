package wawa.wayfinder.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class WayfinderClientConfig {
	public static final ModConfigSpec CONFIG_SPEC;

	public static final ModConfigSpec.EnumValue<ReducedDebugLevel> REDUCED_DEBUG_LEVEL;

	static {
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

		REDUCED_DEBUG_LEVEL = builder
				.comment("How much gameplay altering debug information should be hidden")
				.defineEnum("reduced_debug_level", ReducedDebugLevel.NONE, ReducedDebugLevel.values());

		CONFIG_SPEC = builder.build();
	}
}
