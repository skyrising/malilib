package fi.dy.masa.malilib.gui.widget;

import fi.dy.masa.malilib.render.ShapeRenderUtils;

public abstract class BaseModelWidget extends BackgroundWidget
{
    protected int dimensions;
    protected int highlightColor;
    protected float scale = 1f;
    protected boolean doHighlight;

    public BaseModelWidget(int x, int y)
    {
        this(x, y, 16);
    }

    public BaseModelWidget(int x, int y, int dimensions)
    {
        super(x, y, dimensions, dimensions);

        this.dimensions = dimensions;
        this.setBorderWidth(0);

        if (dimensions > 0)
        {
            this.scale = (float) dimensions / 16.0f;
        }
    }

    public BaseModelWidget setDoHighlight(boolean doHighlight)
    {
        this.doHighlight = doHighlight;
        return this;
    }

    public BaseModelWidget setHighlightColor(int color)
    {
        this.highlightColor = color;
        return this;
    }

    public BaseModelWidget setScale(float scale)
    {
        this.scale = scale;
        return this;
    }

    @Override
    public void updateWidth()
    {
        int width = this.dimensions;

        if (this.backgroundEnabled)
        {
            width += this.padding.getLeft() + this.padding.getRight() + this.borderWidth * 2;
        }

        this.setWidth(width);
    }

    @Override
    public void updateHeight()
    {
        int height = this.dimensions;

        if (this.backgroundEnabled)
        {
            height += this.padding.getTop() + this.padding.getBottom() + this.borderWidth * 2;
        }

        this.setHeight(height);
    }

    protected abstract void renderModel(int x, int y, float z, float scale);

    @Override
    public void renderAt(int x, int y, float z, int mouseX, int mouseY, boolean isActiveGui, boolean hovered)
    {
        super.renderAt(x, y, z, mouseX, mouseY, isActiveGui, hovered);

        int width = this.getWidth();
        int height = this.getHeight();

        if (this.backgroundEnabled)
        {
            x += this.padding.getLeft() + this.borderWidth;
            y += this.padding.getTop() + this.borderWidth;
        }

        if (this.doHighlight && hovered)
        {
            ShapeRenderUtils.renderRectangle(x, y, z, width, height, this.highlightColor);
        }

        this.renderModel(x, y, z + 0.5f, this.scale);
    }
}
