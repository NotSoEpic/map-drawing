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

// this whole class is awful and hardcoded to hell and back
public class MapBackground {
    private static final int[] sizesA =  new int[]{8, 32, 96};
    private static final int[] sizesB =  new int[]{8, 16, 48};
    private static final int minSize = Arrays.stream(sizesA).min().getAsInt();

    public final int topMargin;
    public final int leftMargin;
    public final int rightMargin;
    public final int bottomMargin;

    private final FullEdge topEdge = new FullEdge(Edge.TOP);
    private final FullEdge leftEdge = new FullEdge(Edge.LEFT);
    private final FullEdge rightEdge = new FullEdge(Edge.RIGHT);
    private final FullEdge bottomEdge = new FullEdge(Edge.BOTTOM);

    public MapBackground(final int width, final int height,
                         final int topMargin, final int leftMargin, final int rightMargin, final int bottomMargin) {
        this.topMargin = topMargin;
        this.leftMargin = leftMargin;
        this.rightMargin = rightMargin;
        this.bottomMargin = bottomMargin;

        final Random rand = new Random((long) width * height);
        this.topEdge.buildEdge(width - leftMargin - rightMargin, rand);
        this.leftEdge.buildEdge(height - topMargin - bottomMargin, rand);
        this.rightEdge.buildEdge(height - topMargin - bottomMargin, rand);
        this.bottomEdge.buildEdge(width - leftMargin - rightMargin, rand);
    }

    public int innerWidth(final int width) {
        return width - this.leftMargin - this.rightMargin;
    }

    public int innerHeight(final int height) {
        return height - this.topMargin - this.bottomMargin;
    }

    public int getTrueWidth() {
        return this.topEdge.trueSize;
    }

    public int getTrueHeight() {
        return this.leftEdge.trueSize;
    }

    public void render(final GuiGraphics guiGraphics, final int x, final int y, final int width, final int height, final Vector2dc backgroundTranslation, final int blitOffset) {
        final ShaderProgram backgroundProgram = VeilRenderSystem.setShader(Rendering.Shaders.BACKGROUND);
        if (backgroundProgram == null) return;
        backgroundProgram.getOrCreateUniform("ScreenCenter").setVectorI(x + width / 2, y + height / 2);
        backgroundProgram.getOrCreateUniform("Translation").setVector((float) backgroundTranslation.x(), (float) backgroundTranslation.y());

        RenderType sprite = VeilRenderType.get(Rendering.RenderTypes.BACKGROUND, MapwrightClient.id("textures/gui/sprites/background/corner.png"));
        if (sprite == null) return;
        Rendering.renderTypeBlitUV1(guiGraphics, sprite,
                x, y,
                this.leftMargin, this.topMargin,
                this.leftMargin + this.rightMargin, this.topMargin + this.bottomMargin,
                blitOffset, 0, 0
        );
        Rendering.renderTypeBlitUV1(guiGraphics, sprite,
                x + width - this.rightMargin, y,
                this.rightMargin, this.topMargin,
                this.leftMargin + this.rightMargin, this.topMargin + this.bottomMargin,
                blitOffset, this.leftMargin, 0
        );
        Rendering.renderTypeBlitUV1(guiGraphics, sprite,
                x, y + height - this.bottomMargin,
                this.leftMargin, this.bottomMargin,
                this.leftMargin + this.rightMargin, this.topMargin + this.bottomMargin,
                blitOffset, 0, this.topMargin
        );
        Rendering.renderTypeBlitUV1(guiGraphics, sprite,
                x + width - this.rightMargin, y + height - this.bottomMargin,
                this.rightMargin, this.bottomMargin,
                this.leftMargin + this.rightMargin, this.topMargin + this.bottomMargin,
                blitOffset, this.leftMargin, this.topMargin
        );

        sprite = VeilRenderType.get(Rendering.RenderTypes.BACKGROUND, MapwrightClient.id("textures/gui/sprites/background/center.png"));
        if (sprite == null) return;
        Rendering.renderTypeBlitUV1(guiGraphics, sprite,
                x + this.leftMargin, y + this.topMargin,
                this.innerWidth(width), this.innerHeight(height), 1, 1,
                blitOffset, 0, 0
        );

        this.topEdge.render(guiGraphics, Rendering.RenderTypes.BACKGROUND,
                x + this.leftMargin, y,
                this.topMargin, blitOffset);
        this.leftEdge.render(guiGraphics, Rendering.RenderTypes.BACKGROUND,
                x, y + this.topMargin,
                this.leftMargin, blitOffset);
        this.rightEdge.render(guiGraphics, Rendering.RenderTypes.BACKGROUND,
                x + width - this.rightMargin, y + this.topMargin,
                this.rightMargin, blitOffset);
        this.bottomEdge.render(guiGraphics, Rendering.RenderTypes.BACKGROUND,
                x + this.rightMargin, y + height - this.bottomMargin,
                this.bottomMargin, blitOffset);
    }

    enum Edge {
        TOP("top", true, sizesB),
        LEFT("left", false, sizesB),
        RIGHT("right", false, sizesB),
        BOTTOM("bottom", true, sizesA);

        final boolean horizontal;
        final int[] textureSizes;
        final ResourceLocation[] textures;
        Edge(final String prefix, final boolean horizontal, final int[] textureSizes) {
            this.horizontal = horizontal;
            this.textureSizes = textureSizes;
            this.textures = Arrays.stream(textureSizes)
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

        // this is. not a good algorithm. boowomp
        public void buildEdge(final int targetSize, final Random random) {
            this.clear();
            int currentSize = 0;
            final int[] sizeCount = new int[this.edge.textureSizes.length];
            while (currentSize + minSize <= targetSize) {
                final int candidateIndex = random.nextInt(sizeCount.length);
                if (this.edge.textureSizes[candidateIndex] + currentSize <= targetSize) {
                    currentSize += this.edge.textureSizes[candidateIndex];
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

        public void render(final GuiGraphics guiGraphics, final ResourceLocation renderType, int x, int y, final int size, final int blitOffset) {
            if (this.edge.horizontal) {
                for (final EdgeTexture edge : this) {
                    final int width = this.edge.textureSizes[edge.sizeIndex];
                    final RenderType sprite = VeilRenderType.get(renderType, this.edge.textures[edge.sizeIndex]);
                    if (sprite == null) continue;
                    Rendering.renderTypeBlitUV1(guiGraphics, sprite,
                            x, y, width, size,
                            width, size * EdgeTexture.variations,
                            blitOffset, 0, edge.index * size
                    );

                    x += width;
                }
            } else {
                for (final EdgeTexture edge : this) {
                    final int height = this.edge.textureSizes[edge.sizeIndex];
                    final RenderType sprite = VeilRenderType.get(renderType, this.edge.textures[edge.index]);
                    if (sprite == null) continue;
                    Rendering.renderTypeBlitUV1(guiGraphics, sprite,
                            x, y, size, height,
                            size * EdgeTexture.variations, height,
                            blitOffset, edge.index * size, 0
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
