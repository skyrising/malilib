package fi.dy.masa.malilib.render;

import java.util.List;
import javax.annotation.Nullable;
import org.lwjgl.opengl.GL11;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.storage.MapData;
import fi.dy.masa.malilib.gui.BaseScreen;
import fi.dy.masa.malilib.gui.util.GuiUtils;
import fi.dy.masa.malilib.render.overlay.InventoryOverlay;
import fi.dy.masa.malilib.util.PositionUtils;
import fi.dy.masa.malilib.util.PositionUtils.HitPart;
import fi.dy.masa.malilib.util.data.Color4f;
import fi.dy.masa.malilib.util.inventory.InventoryUtils;

public class RenderUtils
{
    public static final ResourceLocation TEXTURE_MAP_BACKGROUND = new ResourceLocation("textures/map/map_background.png");
    //private static final Vec3d LIGHT0_POS = (new Vec3d( 0.2D, 1.0D, -0.7D)).normalize();
    //private static final Vec3d LIGHT1_POS = (new Vec3d(-0.2D, 1.0D,  0.7D)).normalize();

    public static void setupBlend()
    {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    }

    public static void setupBlendSimple()
    {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }

    public static void bindTexture(ResourceLocation texture)
    {
        mc().getTextureManager().bindTexture(texture);
    }

    public static void color(float r, float g, float b, float a)
    {
        GlStateManager.color(r, g, b, a);
    }

    public static void disableItemLighting()
    {
        RenderHelper.disableStandardItemLighting();
    }

    public static void enableItemLighting()
    {
        RenderHelper.enableStandardItemLighting();
    }

    public static void enableGuiItemLighting()
    {
        RenderHelper.enableGUIStandardItemLighting();
    }

    /**
     * Gets the BufferBuilder from the vanilla Tessellator and initializes
     * it in the given mode/format.
     * @param glMode
     * @param format
     * @return
     */
    public static BufferBuilder startBuffer(int glMode, VertexFormat format, boolean useTexture)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        if (useTexture)
        {
            GlStateManager.enableTexture2D();
        }
        else
        {
            GlStateManager.disableTexture2D();
        }

        RenderUtils.setupBlend();

        buffer.begin(glMode, format);

        return buffer;
    }

    /**
     * Draws the buffer in the vanilla Tessellator,
     * and then enables Texture2D mode and disables blending
     */
    public static void drawBuffer()
    {
        Tessellator.getInstance().draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void setupScaledScreenRendering(double scaleFactor)
    {
        Minecraft mc = Minecraft.getMinecraft();
        double width = mc.displayWidth / scaleFactor;
        double height = mc.displayHeight / scaleFactor;

        setupScaledScreenRendering(width, height);
    }

    public static int getVanillaScreenScale()
    {
        Minecraft mc = Minecraft.getMinecraft();
        int displayWidth = mc.displayWidth;
        int displayHeight = mc.displayHeight;
        int scale = Math.min(displayWidth / 320, displayHeight / 240);
        scale = Math.min(scale, mc.gameSettings.guiScale);
        scale = Math.max(scale, 1);

        if (mc.isUnicode() && (scale & 0x1) != 0 && scale > 1)
        {
            --scale;
        }

        return scale;
    }

    public static void setupScaledScreenRendering(double width, double height)
    {
        GlStateManager.clear(256);
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, width, height, 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);
    }

    public static void renderSprite(int x, int y, int z, int width, int height, String texture)
    {
        if (texture != null)
        {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0f, 0f, z);

            GlStateManager.disableLighting();
            TextureAtlasSprite sprite = mc().getTextureMapBlocks().getAtlasSprite(texture);
            mc().ingameGUI.drawTexturedModalRect(x, y, sprite, width, height);

            GlStateManager.popMatrix();
        }
    }

    public static void renderNineSplicedTexture(int x, int y, float z, int u, int v, int width, int height,
                                                int texWidth, int texHeight, int edgeThickness)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        int e = edgeThickness;
        
        ShapeRenderUtils.renderTexturedRectangle(x, y             , z, u, v                , e, e, buffer); // top left
        ShapeRenderUtils.renderTexturedRectangle(x, y + height - e, z, u, v + texHeight - e, e, e, buffer); // bottom left

        ShapeRenderUtils.renderTexturedRectangle(x + width - e, y             , z, u + texWidth - e, v                , e, e, buffer); // top right
        ShapeRenderUtils.renderTexturedRectangle(x + width - e, y + height - e, z, u + texWidth - e, v + texHeight - e, e, e, buffer); // bottom right

        // Texture is smaller than the requested width, repeat stuff horizontally
        if (texWidth < width)
        {
            final int repeatableWidth = texWidth - 2 * e;
            final int requiredWidth = width - 2 * e;

            for (int doneWidth = 0, tmpX = x + e, tmpW; doneWidth < requiredWidth; )
            {
                tmpW = Math.min(repeatableWidth, requiredWidth - doneWidth);

                ShapeRenderUtils.renderTexturedRectangle(tmpX, y             , z, u + e, v                , tmpW, e, buffer); // top center
                ShapeRenderUtils.renderTexturedRectangle(tmpX, y + height - e, z, u + e, v + texHeight - e, tmpW, e, buffer); // bottom center

                tmpX += tmpW;
                doneWidth += tmpW;
            }
        }
        // Texture is wide enough, no need to repeat horizontally
        else
        {
            ShapeRenderUtils.renderTexturedRectangle(x + e, y             , z, u + e, v                , width - 2 * e, e, buffer); // top center
            ShapeRenderUtils.renderTexturedRectangle(x + e, y + height - e, z, u + e, v + texHeight - e, width - 2 * e, e, buffer); // bottom center
        }

        // Texture is smaller than the requested height, repeat stuff vertically
        if (texHeight < height)
        {
            final int repeatableHeight = texHeight - 2 * e;
            final int requiredHeight = height - 2 * e;

            for (int doneHeight = 0, tmpY = y + e, tmpH; doneHeight < requiredHeight; )
            {
                tmpH = Math.min(repeatableHeight, requiredHeight - doneHeight);

                ShapeRenderUtils.renderTexturedRectangle(x            , tmpY, z, u               , v + e, e, tmpH, buffer); // left center
                ShapeRenderUtils.renderTexturedRectangle(x + width - e, tmpY, z, u + texWidth - e, v + e, e, tmpH, buffer); // right center

                tmpY += tmpH;
                doneHeight += tmpH;
            }
        }
        // Texture is tall enough, no need to repeat vertically
        else
        {
            ShapeRenderUtils.renderTexturedRectangle(x            , y + e, z, u               , v + e, e, height - 2 * e, buffer); // left center
            ShapeRenderUtils.renderTexturedRectangle(x + width - e, y + e, z, u + texWidth - e, v + e, e, height - 2 * e, buffer); // right center
        }

        // The center part needs to be repeated
        if (texWidth < width || texHeight < height)
        {
            final int repeatableWidth = texWidth - 2 * e;
            final int requiredWidth = width - 2 * e;

            for (int doneWidth = 0, tmpX = x + e, tmpW; doneWidth < requiredWidth; )
            {
                final int repeatableHeight = texHeight - 2 * e;
                final int requiredHeight = height - 2 * e;
                tmpW = Math.min(repeatableWidth, requiredWidth - doneWidth);

                for (int doneHeight = 0, tmpY = y + e, tmpH; doneHeight < requiredHeight; )
                {
                    tmpH = Math.min(repeatableHeight, requiredHeight - doneHeight);

                    ShapeRenderUtils.renderTexturedRectangle(tmpX, tmpY, z, u + e, v + e, tmpW, tmpH, buffer); // center

                    tmpY += tmpH;
                    doneHeight += tmpH;
                }

                tmpX += tmpW;
                doneWidth += tmpW;
            }
        }
        else
        {
            ShapeRenderUtils.renderTexturedRectangle(x + e, y + e, z, u + e, v + e, width - 2 * e, height - 2 * e, buffer); // center
        }

        tessellator.draw();
    }

    public static void renderBlockTargetingOverlay(Entity entity, BlockPos pos, EnumFacing side, Vec3d hitVec,
                                                   Color4f color, float partialTicks)
    {
        EnumFacing playerFacing = entity.getHorizontalFacing();
        HitPart part = PositionUtils.getHitPart(side, playerFacing, pos, hitVec);

        double dx = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        double dy = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        double dz = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;

        double x = pos.getX() + 0.5d - dx;
        double y = pos.getY() + 0.5d - dy;
        double z = pos.getZ() + 0.5d - dz;

        GlStateManager.pushMatrix();

        blockTargetingOverlayTranslations(x, y, z, side, playerFacing);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        float quadAlpha = 0.18f;
        float ha = color.a;
        float hr = color.r;
        float hg = color.g;
        float hb = color.b;

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        // White full block background
        buffer.pos(x - 0.5, y - 0.5, z).color(1f, 1f, 1f, quadAlpha).endVertex();
        buffer.pos(x + 0.5, y - 0.5, z).color(1f, 1f, 1f, quadAlpha).endVertex();
        buffer.pos(x + 0.5, y + 0.5, z).color(1f, 1f, 1f, quadAlpha).endVertex();
        buffer.pos(x - 0.5, y + 0.5, z).color(1f, 1f, 1f, quadAlpha).endVertex();

        switch (part)
        {
            case CENTER:
                buffer.pos(x - 0.25, y - 0.25, z).color(hr, hg, hb, ha).endVertex();
                buffer.pos(x + 0.25, y - 0.25, z).color(hr, hg, hb, ha).endVertex();
                buffer.pos(x + 0.25, y + 0.25, z).color(hr, hg, hb, ha).endVertex();
                buffer.pos(x - 0.25, y + 0.25, z).color(hr, hg, hb, ha).endVertex();
                break;
            case LEFT:
                buffer.pos(x - 0.50, y - 0.50, z).color(hr, hg, hb, ha).endVertex();
                buffer.pos(x - 0.25, y - 0.25, z).color(hr, hg, hb, ha).endVertex();
                buffer.pos(x - 0.25, y + 0.25, z).color(hr, hg, hb, ha).endVertex();
                buffer.pos(x - 0.50, y + 0.50, z).color(hr, hg, hb, ha).endVertex();
                break;
            case RIGHT:
                buffer.pos(x + 0.50, y - 0.50, z).color(hr, hg, hb, ha).endVertex();
                buffer.pos(x + 0.25, y - 0.25, z).color(hr, hg, hb, ha).endVertex();
                buffer.pos(x + 0.25, y + 0.25, z).color(hr, hg, hb, ha).endVertex();
                buffer.pos(x + 0.50, y + 0.50, z).color(hr, hg, hb, ha).endVertex();
                break;
            case TOP:
                buffer.pos(x - 0.50, y + 0.50, z).color(hr, hg, hb, ha).endVertex();
                buffer.pos(x - 0.25, y + 0.25, z).color(hr, hg, hb, ha).endVertex();
                buffer.pos(x + 0.25, y + 0.25, z).color(hr, hg, hb, ha).endVertex();
                buffer.pos(x + 0.50, y + 0.50, z).color(hr, hg, hb, ha).endVertex();
                break;
            case BOTTOM:
                buffer.pos(x - 0.50, y - 0.50, z).color(hr, hg, hb, ha).endVertex();
                buffer.pos(x - 0.25, y - 0.25, z).color(hr, hg, hb, ha).endVertex();
                buffer.pos(x + 0.25, y - 0.25, z).color(hr, hg, hb, ha).endVertex();
                buffer.pos(x + 0.50, y - 0.50, z).color(hr, hg, hb, ha).endVertex();
                break;
            default:
        }

        tessellator.draw();

        GlStateManager.glLineWidth(1.6f);

        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);

        // Middle small rectangle
        buffer.pos(x - 0.25, y - 0.25, z).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(x + 0.25, y - 0.25, z).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(x + 0.25, y + 0.25, z).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(x - 0.25, y + 0.25, z).color(1f, 1f, 1f, 1f).endVertex();
        tessellator.draw();

        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        // Bottom left
        buffer.pos(x - 0.50, y - 0.50, z).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(x - 0.25, y - 0.25, z).color(1f, 1f, 1f, 1f).endVertex();

        // Top left
        buffer.pos(x - 0.50, y + 0.50, z).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(x - 0.25, y + 0.25, z).color(1f, 1f, 1f, 1f).endVertex();

        // Bottom right
        buffer.pos(x + 0.50, y - 0.50, z).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(x + 0.25, y - 0.25, z).color(1f, 1f, 1f, 1f).endVertex();

        // Top right
        buffer.pos(x + 0.50, y + 0.50, z).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(x + 0.25, y + 0.25, z).color(1f, 1f, 1f, 1f).endVertex();
        tessellator.draw();

        GlStateManager.popMatrix();
    }

    public static void renderBlockTargetingOverlaySimple(Entity entity, BlockPos pos, EnumFacing side,
                                                         Color4f color, float partialTicks)
    {
        EnumFacing playerFacing = entity.getHorizontalFacing();

        double dx = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        double dy = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        double dz = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;

        double x = pos.getX() + 0.5d - dx;
        double y = pos.getY() + 0.5d - dy;
        double z = pos.getZ() + 0.5d - dz;

        GlStateManager.pushMatrix();

        blockTargetingOverlayTranslations(x, y, z, side, playerFacing);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        float a = color.a;
        float r = color.r;
        float g = color.g;
        float b = color.b;

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        // Simple colored quad
        buffer.pos(x - 0.5, y - 0.5, z).color(r, g, b, a).endVertex();
        buffer.pos(x + 0.5, y - 0.5, z).color(r, g, b, a).endVertex();
        buffer.pos(x + 0.5, y + 0.5, z).color(r, g, b, a).endVertex();
        buffer.pos(x - 0.5, y + 0.5, z).color(r, g, b, a).endVertex();

        tessellator.draw();

        GlStateManager.glLineWidth(1.6f);

        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);

        // Middle rectangle
        buffer.pos(x - 0.375, y - 0.375, z).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(x + 0.375, y - 0.375, z).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(x + 0.375, y + 0.375, z).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(x - 0.375, y + 0.375, z).color(1f, 1f, 1f, 1f).endVertex();

        tessellator.draw();

        GlStateManager.popMatrix();
    }

    private static void blockTargetingOverlayTranslations(double x, double y, double z,
                                                          EnumFacing side, EnumFacing playerFacing)
    {
        GlStateManager.translate(x, y, z);

        switch (side)
        {
            case DOWN:
                GlStateManager.rotate(180f - playerFacing.getHorizontalAngle(), 0, 1f, 0);
                GlStateManager.rotate( 90f, 1f, 0, 0);
                break;
            case UP:
                GlStateManager.rotate(180f - playerFacing.getHorizontalAngle(), 0, 1f, 0);
                GlStateManager.rotate(-90f, 1f, 0, 0);
                break;
            case NORTH:
                GlStateManager.rotate(180f, 0, 1f, 0);
                break;
            case SOUTH:
                GlStateManager.rotate(   0, 0, 1f, 0);
                break;
            case WEST:
                GlStateManager.rotate(-90f, 0, 1f, 0);
                break;
            case EAST:
                GlStateManager.rotate( 90f, 0, 1f, 0);
                break;
        }

        GlStateManager.translate(-x, -y, -z + 0.501);
    }

    public static void renderMapPreview(ItemStack stack, int x, int y, int dimensions)
    {
        if (stack.getItem() instanceof ItemMap && BaseScreen.isShiftDown())
        {
            GlStateManager.pushMatrix();
            GlStateManager.disableLighting();
            color(1f, 1f, 1f, 1f);

            int screenWidth = GuiUtils.getScaledWindowWidth();
            int screenHeight = GuiUtils.getScaledWindowHeight();
            int y1 = Math.max(y - dimensions - 20, 2);

            if (x + dimensions + 10 > screenWidth)
            {
                x = Math.max(x - dimensions - 10, 2);
            }

            if (y1 + dimensions + 2 > screenHeight)
            {
                y1 = screenHeight - dimensions - 2;
            }

            int x1 = x + 8;
            int x2 = x1 + dimensions;
            int y2 = y1 + dimensions;
            int z = 300;

            bindTexture(fi.dy.masa.malilib.render.RenderUtils.TEXTURE_MAP_BACKGROUND);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();

            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(x1, y2, z).tex(0.0D, 1.0D).endVertex();
            buffer.pos(x2, y2, z).tex(1.0D, 1.0D).endVertex();
            buffer.pos(x2, y1, z).tex(1.0D, 0.0D).endVertex();
            buffer.pos(x1, y1, z).tex(0.0D, 0.0D).endVertex();

            tessellator.draw();

            MapData mapdata = Items.FILLED_MAP.getMapData(stack, mc().world);

            if (mapdata != null)
            {
                x1 += 8;
                y1 += 8;
                z = 310;
                double scale = (double) (dimensions - 16) / 128.0D;
                GlStateManager.translate(x1, y1, z);
                GlStateManager.scale(scale, scale, 0);
                mc().entityRenderer.getMapItemRenderer().renderMap(mapdata, false);
            }

            GlStateManager.enableLighting();
            GlStateManager.popMatrix();

            color(1f, 1f, 1f, 1f);
        }
    }

    public static void renderShulkerBoxPreview(ItemStack stack, int x, int y, boolean useBgColors)
    {
        if (stack.hasTagCompound())
        {
            NonNullList<ItemStack> items = InventoryUtils.getStoredItems(stack, -1);

            if (items.size() == 0)
            {
                return;
            }

            GlStateManager.pushMatrix();
            disableItemLighting();
            GlStateManager.translate(0F, 0F, 300F);

            InventoryOverlay.InventoryRenderType type = InventoryOverlay.getInventoryType(stack);
            InventoryOverlay.InventoryProperties props = InventoryOverlay.getInventoryPropsTemp(type, items.size());

            int screenWidth = GuiUtils.getScaledWindowWidth();
            int screenHeight = GuiUtils.getScaledWindowHeight();
            int z = 0;
            x += 8;
            y = Math.max(y - (props.height + 18), 2);

            if (x + props.width + 2 > screenWidth)
            {
                x = Math.max(x - props.width - 16, 2);
            }

            if (y + props.height + 2 > screenHeight)
            {
                y = screenHeight - props.height - 2;
            }

            if (stack.getItem() instanceof ItemShulkerBox)
            {
                setShulkerBoxBackgroundTintColor((BlockShulkerBox) ((ItemBlock) stack.getItem()).getBlock(), useBgColors);
            }
            else
            {
                color(1f, 1f, 1f, 1f);
            }

            InventoryOverlay.renderInventoryBackground(type, x, y, z, props.slotsPerRow, items.size(), mc());

            enableGuiItemLighting();
            GlStateManager.enableDepth();
            GlStateManager.enableRescaleNormal();

            IInventory inv = InventoryUtils.getAsInventory(items);
            InventoryOverlay.renderInventoryStacks(type, inv, x + props.slotOffsetX, y + props.slotOffsetY, z + 1, props.slotsPerRow, 0, -1, mc());

            GlStateManager.disableDepth();
            GlStateManager.popMatrix();

            color(1f, 1f, 1f, 1f);
        }
    }

    /**
     * Calls RenderUtils.color() with the dye color of the provided shulker box block's color
     * @param block
     * @param useBgColors
     */
    public static void setShulkerBoxBackgroundTintColor(@Nullable BlockShulkerBox block, boolean useBgColors)
    {
        // In 1.13+ there is the separate uncolored Shulker Box variant,
        // which returns null from getColor().
        // In that case don't tint the background.
        if (useBgColors && block != null && block.getColor() != null)
        {
            final EnumDyeColor dye = block.getColor();
            final float[] colors = dye.getColorComponentValues();
            color(colors[0], colors[1], colors[2], 1f);
        }
        else
        {
            color(1f, 1f, 1f, 1f);
        }
    }

    public static void renderModelInGui(int x, int y, float zLevel, IBakedModel model, IBlockState state)
    {
        if (state.getBlock() == Blocks.AIR)
        {
            return;
        }

        GlStateManager.pushMatrix();

        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.01F);
        setupBlendSimple();
        color(1f, 1f, 1f, 1f);

        setupGuiTransform(x, y, model.isGui3d(), zLevel);
        //model.getItemCameraTransforms().applyTransform(ItemCameraTransforms.TransformType.GUI);
        GlStateManager.rotate( 30, 1, 0, 0);
        GlStateManager.rotate(225, 0, 1, 0);
        GlStateManager.scale(0.625, 0.625, 0.625);

        renderModel(model, state, zLevel);

        GlStateManager.disableAlpha();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
        GlStateManager.popMatrix();

        color(1f, 1f, 1f, 1f);
    }

    public static void setupGuiTransform(int xPosition, int yPosition, boolean isGui3d, float zLevel)
    {
        GlStateManager.translate(xPosition, yPosition, 100.0F + zLevel);
        GlStateManager.translate(8.0F, 8.0F, 0.0F);
        GlStateManager.scale(1.0F, -1.0F, 1.0F);
        GlStateManager.scale(16.0F, 16.0F, 16.0F);

        if (isGui3d)
        {
            GlStateManager.enableLighting();
        }
        else
        {
            GlStateManager.disableLighting();
        }
    }

    private static void renderModel(IBakedModel model, IBlockState state, float zLevel)
    {
        GlStateManager.pushMatrix();
        GlStateManager.translate(-0.5F, -0.5F, -0.5F);
        int color = 0xFFFFFFFF;

        if (model.isBuiltInRenderer() == false)
        {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);

            for (EnumFacing enumfacing : EnumFacing.values())
            {
                renderQuads(bufferbuilder, model.getQuads(state, enumfacing, 0L), state, color);
            }

            renderQuads(bufferbuilder, model.getQuads(state, null, 0L), state, color);
            tessellator.draw();
        }

        GlStateManager.popMatrix();
    }

    public static void renderQuads(BufferBuilder renderer, List<BakedQuad> quads, IBlockState state, int color)
    {
        for (BakedQuad quad : quads)
        {
            renderQuad(renderer, quad, state, 0xFFFFFFFF);
        }
    }

    public static void renderQuad(BufferBuilder buffer, BakedQuad quad, IBlockState state, int color)
    {
        buffer.addVertexData(quad.getVertexData());
        buffer.putColor4(color);

        if (quad.hasTintIndex())
        {
            BlockColors blockColors = mc().getBlockColors();
            int m = blockColors.colorMultiplier(state, null, null, quad.getTintIndex());

            float r = (float) (m >>> 16 & 0xFF) / 255F;
            float g = (float) (m >>>  8 & 0xFF) / 255F;
            float b = (float) (m        & 0xFF) / 255F;
            buffer.putColorMultiplier(r, g, b, 4);
            buffer.putColorMultiplier(r, g, b, 3);
            buffer.putColorMultiplier(r, g, b, 2);
            buffer.putColorMultiplier(r, g, b, 1);
        }

        putQuadNormal(buffer, quad);
    }

    public static void putQuadNormal(BufferBuilder buffer, BakedQuad quad)
    {
        Vec3i direction = quad.getFace().getDirectionVec();
        buffer.putNormal(direction.getX(), direction.getY(), direction.getZ());
    }

    /**
     * Renders the given model to the given vertex consumer.
     * Needs a vertex consumer initialized with mode GL11.GL_QUADS and DefaultVertexFormats.ITEM
     */
    public static void renderModelBrightnessColor(IBakedModel model, Vec3d pos, BufferBuilder buffer)
    {
        renderModelBrightnessColor(model, pos, null, 1f, 1f, 1f, 1f, buffer);
    }

    /**
     * Renders the given model to the given vertex consumer.
     * Needs a vertex consumer initialized with mode GL11.GL_QUADS and DefaultVertexFormats.ITEM
     */
    public static void renderModelBrightnessColor(IBakedModel model, Vec3d pos, @Nullable IBlockState state,
                                                  float brightness, float r, float g, float b, BufferBuilder buffer)
    {
        for (EnumFacing side : PositionUtils.ALL_DIRECTIONS)
        {
            renderQuads(model.getQuads(state, side, 0L), pos, brightness, r, g, b, buffer);
        }

        renderQuads(model.getQuads(state, null, 0L), pos, brightness, r, g, b, buffer);
    }

    /**
     * Renders the given quads to the given vertex consumer.
     * Needs a vertex consumer initialized with mode GL11.GL_QUADS and DefaultVertexFormats.ITEM
     */
    public static void renderQuads(List<BakedQuad> quads, Vec3d pos, float brightness,
                                   float red, float green, float blue, BufferBuilder buffer)
    {
        for (BakedQuad quad : quads)
        {
            buffer.addVertexData(quad.getVertexData());

            if (quad.hasTintIndex())
            {
                buffer.putColorRGB_F4(red * brightness, green * brightness, blue * brightness);
            }
            else
            {
                buffer.putColorRGB_F4(brightness, brightness, brightness);
            }

            buffer.putPosition(pos.x, pos.y, pos.z);
            putQuadNormal(buffer, quad);
        }
    }

    private static Minecraft mc()
    {
        return Minecraft.getMinecraft();
    }

    /*
    public static void enableGUIStandardItemLighting(float scale)
    {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(-30.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(165.0F, 1.0F, 0.0F, 0.0F);

        enableStandardItemLighting(scale);

        GlStateManager.popMatrix();
    }

    public static void enableStandardItemLighting(float scale)
    {
        GlStateManager.enableLighting();
        GlStateManager.enableLight(0);
        GlStateManager.enableLight(1);
        GlStateManager.enableColorMaterial();
        RenderUtils.colorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);
        GlStateManager.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, RenderHelper.setColorBuffer((float) LIGHT0_POS.x, (float) LIGHT0_POS.y, (float) LIGHT0_POS.z, 0.0f));

        float lightStrength = 0.3F * scale;
        GlStateManager.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, RenderHelper.setColorBuffer(lightStrength, lightStrength, lightStrength, 1.0F));
        GlStateManager.glLight(GL11.GL_LIGHT0, GL11.GL_AMBIENT, RenderHelper.setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
        GlStateManager.glLight(GL11.GL_LIGHT0, GL11.GL_SPECULAR, RenderHelper.setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
        GlStateManager.glLight(GL11.GL_LIGHT1, GL11.GL_POSITION, RenderHelper.setColorBuffer((float) LIGHT1_POS.x, (float) LIGHT1_POS.y, (float) LIGHT1_POS.z, 0.0f));
        GlStateManager.glLight(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, RenderHelper.setColorBuffer(lightStrength, lightStrength, lightStrength, 1.0F));
        GlStateManager.glLight(GL11.GL_LIGHT1, GL11.GL_AMBIENT, RenderHelper.setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
        GlStateManager.glLight(GL11.GL_LIGHT1, GL11.GL_SPECULAR, RenderHelper.setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));

        GlStateManager.shadeModel(GL11.GL_FLAT);

        float ambientLightStrength = 0.4F;
        GlStateManager.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, RenderHelper.setColorBuffer(ambientLightStrength, ambientLightStrength, ambientLightStrength, 1.0F));
    }
    */
}
