package wawa.wayfinder.color;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.nio.FloatBuffer;
import java.util.List;

public record ColorPalette(Text displayName, List<Color> colors, @Nullable FloatBuffer buffer) {
	public static final int SIZE = 9;

	public static final Codec<ColorPalette> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			TextCodecs.CODEC.fieldOf("name").forGetter(ColorPalette::displayName),
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

		GRAYSCALE = new ColorPalette(Text.literal("Grayscale"), List.of(colors), true);
	}

	public ColorPalette(Text displayName, List<Color> colors) {
		this(displayName, colors, true);
	}

	public ColorPalette(Text displayName, List<Color> colors, boolean createBuffer) {
		this(displayName, colors, createBuffer ? createBuffer(colors) : null);
	}

	private static FloatBuffer createBuffer(List<Color> colorList) {
		FloatBuffer buffer = FloatBuffer.allocate(colorList.size() * 3);
		for (Color color : colorList) {
			float[] components = color.getColorComponents(null);
			buffer.put(components[0]);
			buffer.put(components[1]);
			buffer.put(components[2]);
		}
		return buffer;
	}

	private static Color stringToColor(String string) {
		if(string.charAt(0) == '#') string = string.substring(1);
		return new Color((int) Long.parseLong(string, 16));
	}

	private static String colorToString(Color color) {
		return Integer.toHexString(color.getRGB());
	}
}