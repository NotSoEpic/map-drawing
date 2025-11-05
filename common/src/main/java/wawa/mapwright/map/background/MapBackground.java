package wawa.mapwright.map.background;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2dc;
import wawa.mapwright.MapwrightClient;
import wawa.mapwright.Rendering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class MapBackground {
    private static final int[] sizes =  new int[]{160, 80, 40};
    private static final int minSize = Arrays.stream(sizes).min().getAsInt();

    private final FullEdge topEdge = new FullEdge(Edge.TOP);
    private final FullEdge leftEdge = new FullEdge(Edge.LEFT);
    private final FullEdge rightEdge = new FullEdge(Edge.RIGHT);
    private final FullEdge bottomEdge = new FullEdge(Edge.BOTTOM);

    public MapBackground(final int width, final int height) {
        final Random rand = new Random((long) width * height);
        this.topEdge.buildEdge(width, rand);
        this.leftEdge.buildEdge(height, rand);
        this.rightEdge.buildEdge(height, rand);
        this.bottomEdge.buildEdge(width, rand);
    }

    public int getTrueWidth() {
        return this.topEdge.trueSize;
    }

    public int getTrueHeight() {
        return this.leftEdge.trueSize;
    }

    public void render(final GuiGraphics guiGraphics, final int x, final int y, final int cornerSize, final int width, final int height, final Vector2dc backgroundTranslation, final int blitOffset) {
        final ShaderProgram backgroundProgram = VeilRenderSystem.setShader(Rendering.Shaders.BACKGROUND);
        if (backgroundProgram == null) return;
        backgroundProgram.getOrCreateUniform("ScreenCenter").setVectorI(x + width / 2, y + height / 2);
        backgroundProgram.getOrCreateUniform("Translation").setVector((float) backgroundTranslation.x(), (float) backgroundTranslation.y());

        RenderType sprite = VeilRenderType.get(Rendering.RenderTypes.BACKGROUND, MapwrightClient.id("textures/gui/sprites/background/corner.png"));
        if (sprite == null) return;
        Rendering.renderTypeBlitUV1(guiGraphics, sprite,
                x, y, cornerSize, cornerSize, cornerSize * 2, cornerSize * 2,
                blitOffset, 0, 0
        );
        Rendering.renderTypeBlitUV1(guiGraphics, sprite,
                x + width - cornerSize, y, cornerSize, cornerSize, cornerSize * 2, cornerSize * 2,
                blitOffset, cornerSize, 0
        );
        Rendering.renderTypeBlitUV1(guiGraphics, sprite,
                x, y + height - cornerSize, cornerSize, cornerSize, cornerSize * 2, cornerSize * 2,
                blitOffset, 0, cornerSize
        );
        Rendering.renderTypeBlitUV1(guiGraphics, sprite,
                x + width - cornerSize, y + height - cornerSize, cornerSize, cornerSize, cornerSize * 2, cornerSize * 2,
                blitOffset, cornerSize, cornerSize
        );

        sprite = VeilRenderType.get(Rendering.RenderTypes.BACKGROUND, MapwrightClient.id("textures/gui/sprites/background/center.png"));
        if (sprite == null) return;
        Rendering.renderTypeBlitUV1(guiGraphics, sprite,
                x + cornerSize, y + cornerSize,
                width - cornerSize * 2, height - cornerSize * 2, 1, 1,
                blitOffset, 0, 0
        );

        this.topEdge.render(guiGraphics, Rendering.RenderTypes.BACKGROUND, x + cornerSize, y, cornerSize, blitOffset);
        this.leftEdge.render(guiGraphics, Rendering.RenderTypes.BACKGROUND, x, y + cornerSize, cornerSize, blitOffset);
        this.rightEdge.render(guiGraphics, Rendering.RenderTypes.BACKGROUND, x + width - cornerSize, y + cornerSize, cornerSize, blitOffset);
        this.bottomEdge.render(guiGraphics, Rendering.RenderTypes.BACKGROUND, x + cornerSize, y + height - cornerSize, cornerSize, blitOffset);
    }

    enum Edge {
        TOP("top", true),
        LEFT("left", false),
        RIGHT("right", false),
        BOTTOM("bottom", true);

        final boolean horizontal;
        final ResourceLocation[] textures;
        Edge(final String prefix, final boolean horizontal) {
            this.horizontal = horizontal;
            this.textures = Arrays.stream(sizes)
                    .mapToObj(i -> MapwrightClient.id("textures/gui/sprites/background/" + prefix + "/" + i + ".png"))
//                    .mapToObj(i -> MapwrightClient.id("background/" + prefix + "/" + i))
                    .toArray(ResourceLocation[]::new);
        }
    }

    private static class FullEdge extends ArrayList<EdgeTexture> {
        public final Edge edge;
        private int trueSize = 0;
        public FullEdge(final Edge edge) {
            this.edge = edge;
        }

        public void buildEdge(final int targetSize, final Random random) {
            this.clear();
            int currentSize = 0;
            final int[] sizeCount = new int[sizes.length];
            while (currentSize + minSize <= targetSize) {
                final int candidateIndex = random.nextInt(sizeCount.length);
                if (sizes[candidateIndex] + currentSize <= targetSize) {
                    currentSize += sizes[candidateIndex];
                    sizeCount[candidateIndex]++;
                }
            }
            this.trueSize = currentSize;

            for (int i = 0; i < sizeCount.length; i++) {
                for (int j = 0; j < sizeCount[i]; j++) {
                    this.add(new EdgeTexture(i, random.nextInt(EdgeTexture.variations)));
                }
            }
            Collections.shuffle(this, random);
        }

        public void render(final GuiGraphics guiGraphics, final ResourceLocation renderType, int x, int y, final int cornerSize, final int blitOffset) {
            if (this.edge.horizontal) {
                for (final EdgeTexture edge : this) {
                    final int width = MapBackground.sizes[edge.sizeIndex];
                    final RenderType sprite = VeilRenderType.get(renderType, this.edge.textures[edge.sizeIndex]);
                    if (sprite == null) continue;
                    Rendering.renderTypeBlitUV1(guiGraphics, sprite,
                            x, y, width, cornerSize,
                            width, cornerSize * EdgeTexture.variations,
                            blitOffset, 0, edge.index * cornerSize
                    );

                    x += width;
                }
            } else {
                for (final EdgeTexture edge : this) {
                    final int height = MapBackground.sizes[edge.sizeIndex];
                    final RenderType sprite = VeilRenderType.get(renderType, this.edge.textures[edge.index]);
                    if (sprite == null) continue;
                    Rendering.renderTypeBlitUV1(guiGraphics, sprite,
                            x, y, cornerSize, height,
                            cornerSize * EdgeTexture.variations, height,
                            blitOffset, edge.index * cornerSize, 0
                    );

                    y += height;
                }
            }
        }
    }

    private record EdgeTexture(int sizeIndex, int index) {
        public static final int variations = 3;
    }
}
