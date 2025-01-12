package fi.dy.masa.malilib.gui.icon;

import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.render.ShapeRenderUtils;

public interface MultiIcon extends Icon
{
    /**
     * Get the U coordinate for the given icon variant
     * @param variantIndex
     * @return
     */
    int getVariantU(int variantIndex);

    /**
     * Get the V coordinate for the given icon variant
     * @param variantIndex
     * @return
     */
    int getVariantV(int variantIndex);

    /**
     * Renders the icon at the given location, using an icon variant chosen
     * by the given enabled and hover status.
     * @param x
     * @param y
     * @param z
     * @param enabled
     * @param hovered
     */
    default void renderAt(int x, int y, float z, boolean enabled, boolean hovered)
    {
        int variantIndex = this.getVariantIndex(enabled, hovered);
        this.renderAt(x, y, z, variantIndex);
    }

    /**
     * Returns the icon variant index to use for the given status of the icon.
     * By default a disabled icon is at index 0, an enabled, non-hovered icon
     * is at index 1 and an enabled, hovered icon is at index 2.
     * Thus the hover status has no effect for disabled icons.
     * @param enabled
     * @param hovered
     * @return
     */
    default int getVariantIndex(boolean enabled, boolean hovered)
    {
        if (enabled == false)
        {
            return 0;
        }

        return hovered ? 2 : 1;
    }

    /**
     * Renders the icon at the given location, using the given icon variant index.
     * The variant index is basically an offset from the base UV location.
     * The implementation can define where and how the position is offset
     * from the base location.
     * @param x
     * @param y
     * @param z
     * @param variantIndex
     */
    default void renderAt(int x, int y, float z, int variantIndex)
    {
        int width = this.getWidth();
        int height = this.getHeight();

        if (width == 0 || height == 0)
        {
            return;
        }

        int u = this.getVariantU(variantIndex);
        int v = this.getVariantV(variantIndex);

        RenderUtils.color(1f, 1f, 1f, 1f);
        RenderUtils.bindTexture(this.getTexture());
        ShapeRenderUtils.renderTexturedRectangle(x, y, z, u, v, width, height);
    }

    /**
     * Renders a composite (smaller) icon by using a rectangular area
     * of each of the 4 corners of the texture. The width and height
     * arguments define what size texture is going to be rendered.
     * @param x
     * @param y
     * @param z
     * @param width the width of the icon to render
     * @param height the height of the icon to render
     * @param enabled
     * @param hovered
     */
    default void renderFourSplicedAt(int x, int y, float z, int width, int height, boolean enabled, boolean hovered)
    {
        int textureWidth = this.getWidth();
        int textureHeight = this.getHeight();

        if (textureWidth == 0 || textureHeight == 0)
        {
            return;
        }

        int variantIndex = this.getVariantIndex(enabled, hovered);
        int u = this.getVariantU(variantIndex);
        int v = this.getVariantV(variantIndex);
        int w1 = width / 2;
        int w2 = (width & 0x1) != 0 ? w1 + 1 : w1;
        int h1 = height / 2;
        int h2 = (height & 0x1) != 0 ? h1 + 1 : h1;

        RenderUtils.color(1f, 1f, 1f, 1f);
        RenderUtils.bindTexture(this.getTexture());

        ShapeRenderUtils.renderTexturedRectangle(x, y     , z, u, v                     , w1, h1); // top left
        ShapeRenderUtils.renderTexturedRectangle(x, y + h1, z, u, v + textureHeight - h2, w1, h2); // bottom left

        ShapeRenderUtils.renderTexturedRectangle(x + w1, y     , z, u + textureWidth - w2, v                     , w2, h1); // top right
        ShapeRenderUtils.renderTexturedRectangle(x + w1, y + h1, z, u + textureWidth - w2, v + textureHeight - h2, w2, h2); // bottom right
    }
}
