package wawa.wayfinder.color;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import org.joml.Vector3f;

import java.awt.*;
import java.util.List;

public record ColorPalette(Component displayName, List<Color> colors, Vector3f[] vecList) {
	public static final int SIZE = 9;

	public static final Codec<ColorPalette> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ComponentSerialization.CODEC.fieldOf("name").forGetter(ColorPalette::displayName),
			Codec.STRING.xmap(ColorPalette::stringToColor, ColorPalette::colorToString)
					.listOf(SIZE, SIZE).fieldOf("colors").forGetter(ColorPalette::colors)
	).apply(instance, ColorPalette::new));

	public static final ColorPalette GRAYSCALE;

	static {
		Color[] colors = new Color[SIZE];
		for (int i = 0; i < colors.length; i++) {
			float normalized = (float) i / colors.length;
			float color = (float) (Math.floor(normalized * colors.length) / colors.length);
			colors[i] = new Color(color, color, color);
		}

		GRAYSCALE = new ColorPalette(Component.literal("Grayscale"), List.of(colors));
	}

	public ColorPalette(Component displayName, List<Color> colors) {
		this(displayName, colors, createVectors(colors));
	}

	private static Vector3f[] createVectors(List<Color> colorList) {
		Vector3f[] list = new Vector3f[colorList.size()];
		for (int i = 0; i < colorList.size(); i++) {
			Color color = colorList.get(i);
			list[i] = new Vector3f(color.getColorComponents(null));
		}
		return list;
	}

	private static Color stringToColor(String string) {
		if(string.charAt(0) == '#') string = string.substring(1);
		return new Color((int) Long.parseLong(string, 16));
	}

	private static String colorToString(Color color) {
		return Integer.toHexString(color.getRGB());
	}
}