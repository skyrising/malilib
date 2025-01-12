package fi.dy.masa.malilib.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import fi.dy.masa.malilib.MaLiLibConfigs;
import fi.dy.masa.malilib.gui.config.liteloader.DialogHandler;
import fi.dy.masa.malilib.gui.util.GuiUtils;
import fi.dy.masa.malilib.gui.widget.BaseTextFieldWidget;
import fi.dy.masa.malilib.gui.widget.BaseWidget;
import fi.dy.masa.malilib.gui.widget.InteractableWidget;
import fi.dy.masa.malilib.gui.widget.LabelWidget;
import fi.dy.masa.malilib.gui.widget.button.BaseButton;
import fi.dy.masa.malilib.gui.widget.button.ButtonActionListener;
import fi.dy.masa.malilib.listener.TextChangeListener;
import fi.dy.masa.malilib.message.MessageConsumer;
import fi.dy.masa.malilib.message.MessageRenderer;
import fi.dy.masa.malilib.message.MessageType;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.render.ShapeRenderUtils;
import fi.dy.masa.malilib.util.consumer.StringConsumer;

public abstract class BaseScreen extends GuiScreen implements MessageConsumer, StringConsumer
{
    public static final String TXT_AQUA = TextFormatting.AQUA.toString();
    public static final String TXT_BLACK = TextFormatting.BLACK.toString();
    public static final String TXT_BLUE = TextFormatting.BLUE.toString();
    public static final String TXT_GOLD = TextFormatting.GOLD.toString();
    public static final String TXT_GRAY = TextFormatting.GRAY.toString();
    public static final String TXT_GREEN = TextFormatting.GREEN.toString();
    public static final String TXT_RED = TextFormatting.RED.toString();
    public static final String TXT_WHITE = TextFormatting.WHITE.toString();
    public static final String TXT_YELLOW = TextFormatting.YELLOW.toString();

    public static final String TXT_BOLD = TextFormatting.BOLD.toString();
    public static final String TXT_ITALIC = TextFormatting.ITALIC.toString();
    public static final String TXT_RST = TextFormatting.RESET.toString();
    public static final String TXT_STRIKETHROUGH = TextFormatting.STRIKETHROUGH.toString();
    public static final String TXT_UNDERLINE = TextFormatting.UNDERLINE.toString();

    public static final String TXT_DARK_AQUA = TextFormatting.DARK_AQUA.toString();
    public static final String TXT_DARK_BLUE = TextFormatting.DARK_BLUE.toString();
    public static final String TXT_DARK_GRAY = TextFormatting.DARK_GRAY.toString();
    public static final String TXT_DARK_GREEN = TextFormatting.DARK_GREEN.toString();
    public static final String TXT_DARK_PURPLE = TextFormatting.DARK_PURPLE.toString();
    public static final String TXT_DARK_RED = TextFormatting.DARK_RED.toString();

    public static final String TXT_LIGHT_PURPLE = TextFormatting.LIGHT_PURPLE.toString();

    protected static final String BUTTON_LABEL_ADD = TXT_DARK_GREEN + "+" + TXT_RST;
    protected static final String BUTTON_LABEL_REMOVE = TXT_DARK_RED + "-" + TXT_RST;

    public static final int TOOLTIP_BACKGROUND   = 0xB0000000;
    public static final int COLOR_HORIZONTAL_BAR = 0xFF999999;

    public final Minecraft mc = Minecraft.getMinecraft();
    public final FontRenderer textRenderer = this.mc.fontRenderer;
    public final int fontHeight = this.textRenderer.FONT_HEIGHT;
    protected final List<Runnable> tasks = new ArrayList<>();
    private final List<BaseButton> buttons = new ArrayList<>();
    private final List<InteractableWidget> widgets = new ArrayList<>();
    private final MessageRenderer messageRenderer;
    protected InteractableWidget hoveredWidget = null;
    protected String title = "";
    @Nullable private GuiScreen parent;
    @Nullable protected DialogHandler dialogHandler;
    protected int backgroundColor = TOOLTIP_BACKGROUND;
    protected int borderColor = COLOR_HORIZONTAL_BAR;
    protected int customScreenScale;
    protected int x;
    protected int y;
    protected int lastMouseX = -1;
    protected int lastMouseY = -1;
    protected int screenWidth;
    protected int screenHeight;
    protected int titleX = 10;
    protected int titleY = 6;
    protected int titleColor = 0xFFFFFFFF;
    protected boolean renderBorder;
    protected boolean shouldCenter;
    protected boolean shouldRenderParent;
    protected boolean useTitleHierarchy = true;
    protected boolean useCustomScreenScaling;

    public BaseScreen()
    {
        this.messageRenderer = new MessageRenderer();
        this.messageRenderer.setBackgroundColor(0xDD000000).setNormalBorderColor(COLOR_HORIZONTAL_BAR);
        this.messageRenderer.setCentered(true, true);
        this.messageRenderer.setZLevel(100);

        int customScale = MaLiLibConfigs.Generic.CUSTOM_SCREEN_SCALE.getIntegerValue();
        this.useCustomScreenScaling = customScale != this.mc.gameSettings.guiScale && customScale > 0;
    }

    public BaseScreen setParent(@Nullable GuiScreen parent)
    {
        // Don't allow nesting the GUI with itself...
        if (parent == null || parent.getClass() != this.getClass())
        {
            this.parent = parent;
        }

        return this;
    }

    @Nullable
    public GuiScreen getParent()
    {
        return this.parent;
    }

    public String getTitle()
    {
        return (this.useTitleHierarchy && this.parent instanceof BaseScreen) ? (((BaseScreen) this.parent).getTitle() + " => " + this.title) : this.title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public void setShouldRenderParent(boolean render)
    {
        this.shouldRenderParent = render;
    }

    protected int getPopupGuiZLevelIncrement()
    {
        return 50;
    }

    @Override
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return this.getParent() != null && this.getParent().doesGuiPauseGame();
    }

    @Override
    public void setWorldAndResolution(Minecraft mc, int width, int height)
    {
        if (this.getParent() != null)
        {
            this.getParent().setWorldAndResolution(mc, width, height);
        }

        boolean initial = this.screenWidth == this.width && this.screenHeight == this.height;

        this.updateCustomScreenScale();

        if (this.useCustomScreenScaling())
        {
            width = this.width;
            height = this.height;
        }

        // Don't override custom screen sizes when the window is resized or whatever,
        // which calls this method again.
        if (initial)
        {
            this.setScreenWidthAndHeight(width, height);
        }

        if (this.shouldCenter)
        {
            this.centerOnScreen();
        }

        super.setWorldAndResolution(mc, width, height);
    }

    @Override
    public void initGui()
    {
        super.initGui();

        this.initScreen();
    }

    protected void initScreen()
    {
        this.clearElements();
        Keyboard.enableRepeatEvents(true);
    }

    protected boolean useCustomScreenScaling()
    {
        return this.useCustomScreenScaling;
    }

    protected void updateCustomScreenScale()
    {
        int currentValue = MaLiLibConfigs.Generic.CUSTOM_SCREEN_SCALE.getIntegerValue();

        if (currentValue != this.customScreenScale)
        {
            boolean oldUseCustomScale = this.useCustomScreenScaling;
            this.useCustomScreenScaling = currentValue > 0 && currentValue != this.mc.gameSettings.guiScale;
            this.customScreenScale = currentValue;

            if (oldUseCustomScale || this.useCustomScreenScaling())
            {
                this.setCustomScreenScale(currentValue);
            }
        }
    }

    protected void setCustomScreenScale(double scaleFactor)
    {
        int width = (int) Math.ceil((double) this.mc.displayWidth / scaleFactor);
        int height = (int) Math.ceil((double) this.mc.displayHeight / scaleFactor);
        boolean setScreenSize = this.screenWidth == this.width && this.screenHeight == this.height;

        if (this.width != width || this.height != height)
        {
            this.width = width;
            this.height = height;

            if (setScreenSize)
            {
                this.setScreenWidthAndHeight(width, height);
            }

            this.initScreen();
        }
    }

    protected void closeScreen(boolean showParent)
    {
        if (showParent)
        {
            this.mc.displayGuiScreen(this.parent);
        }
        else
        {
            this.mc.displayGuiScreen(null);
        }
    }

    protected void setScreenWidthAndHeight(int width, int height)
    {
        this.screenWidth = width;
        this.screenHeight = height;
    }

    public void setPosition(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public void centerOnScreen()
    {
        int x;
        int y;
        GuiScreen parent = this.getParent();

        if (parent instanceof BaseScreen)
        {
            BaseScreen parentBaseScreen = (BaseScreen) this.getParent();
            x = parentBaseScreen.x + parentBaseScreen.screenWidth / 2;
            y = parentBaseScreen.y + parentBaseScreen.screenHeight / 2;
        }
        else if (parent != null)
        {
            x = parent.width / 2;
            y = parent.height / 2;
        }
        else if (GuiUtils.getCurrentScreen() != null)
        {
            GuiScreen current = GuiUtils.getCurrentScreen();
            x = current.width / 2;
            y = current.height / 2;
        }
        else
        {
            x = GuiUtils.getScaledWindowWidth() / 2;
            y = GuiUtils.getScaledWindowHeight() / 2;
        }

        x -= this.screenWidth / 2;
        y -= this.screenHeight / 2;

        this.setPosition(x, y);
    }

    protected InteractableWidget getTopHoveredWidget(int mouseX, int mouseY, @Nullable InteractableWidget highestFoundWidget)
    {
        highestFoundWidget = InteractableWidget.getTopHoveredWidgetFromList(this.buttons, mouseX, mouseY, highestFoundWidget);
        highestFoundWidget = InteractableWidget.getTopHoveredWidgetFromList(this.widgets, mouseX, mouseY, highestFoundWidget);
        return highestFoundWidget;
    }

    protected void updateTopHoveredWidget(int mouseX, int mouseY, boolean isActiveGui)
    {
        this.hoveredWidget = isActiveGui ? this.getTopHoveredWidget(mouseX, mouseY, null) : null;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        if (this.getParent() != null && this.shouldRenderParent)
        {
            this.getParent().drawScreen(mouseX, mouseY, partialTicks);
        }

        // These are after the parent rendering, because the parent
        // can/will also both enable and disable the custom scale,
        // so it needs to be enabled here again in any case after
        // rendering the parent screen.
        this.updateCustomScreenScale();

        if (this.useCustomScreenScaling())
        {
            RenderUtils.setupScaledScreenRendering(this.customScreenScale);
            mouseX = Mouse.getX() * this.width / this.mc.displayWidth;
            mouseY = this.height - Mouse.getY() * this.height / this.mc.displayHeight - 1;
        }

        boolean isActiveGui = GuiUtils.getCurrentScreen() == this;
        int hoveredWidgetId = isActiveGui && this.hoveredWidget != null ? this.hoveredWidget.getId() : -1;

        this.drawScreenBackground(mouseX, mouseY);
        this.drawTitle(mouseX, mouseY, partialTicks);

        // Draw base widgets
        this.drawWidgets(mouseX, mouseY, isActiveGui, hoveredWidgetId);
        //super.drawScreen(mouseX, mouseY, partialTicks);

        this.drawContents(mouseX, mouseY, partialTicks);

        this.drawHoveredWidget(mouseX, mouseY, isActiveGui, hoveredWidgetId);
        this.drawGuiMessages();

        if (MaLiLibConfigs.Debug.GUI_DEBUG.getBooleanValue() && MaLiLibConfigs.Debug.GUI_DEBUG_KEY.isHeld())
        {
            this.renderDebug(mouseX, mouseY);
        }

        BaseWidget.renderDebugTextAndClear();

        if (this.useCustomScreenScaling())
        {
            RenderUtils.setupScaledScreenRendering(RenderUtils.getVanillaScreenScale());
        }
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int mouseWheelDelta = Mouse.getEventDWheel();

        boolean isActiveGui = GuiUtils.getCurrentScreen() == this;
        this.updateTopHoveredWidget(mouseX, mouseY, isActiveGui);

        if (mouseX != this.lastMouseX || mouseY != this.lastMouseY)
        {
            this.onMouseMoved(mouseX, mouseY);
            this.lastMouseX = mouseX;
            this.lastMouseY = mouseY;
        }

        if (mouseWheelDelta == 0 || this.onMouseScrolled(mouseX, mouseY, mouseWheelDelta) == false)
        {
            super.handleMouseInput();
        }

        // Update again after the input is handled
        isActiveGui = GuiUtils.getCurrentScreen() == this;
        this.updateTopHoveredWidget(mouseX, mouseY, isActiveGui);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        if (this.useCustomScreenScaling())
        {
            mouseX = Mouse.getX() * this.width / this.mc.displayWidth;
            mouseY = this.height - Mouse.getY() * this.height / this.mc.displayHeight - 1;
        }

        if (this.onMouseClicked(mouseX, mouseY, mouseButton) == false)
        {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton)
    {
        if (this.useCustomScreenScaling())
        {
            mouseX = Mouse.getX() * this.width / this.mc.displayWidth;
            mouseY = this.height - Mouse.getY() * this.height / this.mc.displayHeight - 1;
        }

        if (this.onMouseReleased(mouseX, mouseY, mouseButton) == false)
        {
            super.mouseReleased(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void keyTyped(char charIn, int keyCode) throws IOException
    {
        if (keyCode == 0 && charIn >= ' ')
        {
            keyCode = (int) charIn + 256;
        }

        if (this.onKeyTyped(keyCode, 0, 0) == false)
        {
            super.keyTyped(charIn, keyCode);
        }

        if (charIn >= ' ')
        {
            this.onCharTyped(charIn, 0);
        }
    }

    public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        List<BaseTextFieldWidget> textFields = this.getAllTextFields();
        InteractableWidget clickedWidget = null;

        // Clear the focus from all text fields
        for (BaseTextFieldWidget tf : textFields)
        {
            tf.setFocused(false);
        }

        if (this.hoveredWidget != null && this.hoveredWidget.tryMouseClick(mouseX, mouseY, mouseButton))
        {
            clickedWidget = this.hoveredWidget;
        }
        else
        {
            for (InteractableWidget widget : this.widgets)
            {
                if (widget.tryMouseClick(mouseX, mouseY, mouseButton))
                {
                    clickedWidget = widget;
                    break;
                }
            }
        }

        // Any widget didn't handle the click yet
        if (clickedWidget == null)
        {
            for (BaseButton button : this.buttons)
            {
                if (button.tryMouseClick(mouseX, mouseY, mouseButton))
                {
                    clickedWidget = button;
                    break;
                }
            }
        }

        this.runTasks();

        // Only call super if the click wasn't handled
        return clickedWidget != null;
    }

    public boolean onMouseReleased(int mouseX, int mouseY, int mouseButton)
    {
        for (InteractableWidget widget : this.widgets)
        {
            widget.onMouseReleased(mouseX, mouseY, mouseButton);
        }

        this.runTasks();

        return false;
    }

    public boolean onMouseScrolled(int mouseX, int mouseY, double mouseWheelDelta)
    {
        if (this.hoveredWidget != null && this.hoveredWidget.tryMouseScroll(mouseX, mouseY, mouseWheelDelta))
        {
            this.runTasks();
            return true;
        }

        boolean handled = false;

        for (BaseButton button : this.buttons)
        {
            if (button.tryMouseScroll(mouseX, mouseY, mouseWheelDelta))
            {
                // Don't call super if the button press got handled
                handled = true;
                break;
            }
        }

        for (InteractableWidget widget : this.widgets)
        {
            if (widget.tryMouseScroll(mouseX, mouseY, mouseWheelDelta))
            {
                // Don't call super if the action got handled
                handled = true;
                break;
            }
        }

        this.runTasks();

        return handled;
    }

    public boolean onMouseMoved(int mouseX, int mouseY)
    {
        boolean handled = false;

        if (this.hoveredWidget != null && this.hoveredWidget.onMouseMoved(mouseX, mouseY))
        {
            handled = true;
        }

        if (handled == false)
        {
            for (InteractableWidget widget : this.widgets)
            {
                if (widget.onMouseMoved(mouseX, mouseY))
                {
                    handled = true;
                    break;
                }
            }
        }

        this.runTasks();

        return handled;
    }

    public boolean onKeyTyped(int keyCode, int scanCode, int modifiers)
    {
        boolean handled = false;

        if (keyCode == Keyboard.KEY_ESCAPE && this.dialogHandler != null)
        {
            this.dialogHandler.closeDialog();
            handled = true;
        }
        else if (keyCode == Keyboard.KEY_TAB && GuiUtils.changeTextFieldFocus(this.getAllTextFields(), isShiftDown()))
        {
            handled = true;
        }

        if (handled == false && this.widgets.isEmpty() == false)
        {
            for (InteractableWidget widget : this.widgets)
            {
                if (widget.onKeyTyped(keyCode, scanCode, modifiers))
                {
                    // Don't call super if the button press got handled
                    handled = true;
                    break;
                }
            }
        }

        if (handled == false && keyCode == Keyboard.KEY_ESCAPE)
        {
            this.closeScreen(isShiftDown() == false);
            handled = true;
        }

        this.runTasks();

        return handled;
    }

    public boolean onCharTyped(char charIn, int modifiers)
    {
        boolean handled = false;

        for (InteractableWidget widget : this.widgets)
        {
            if (widget.onCharTyped(charIn, modifiers))
            {
                // Don't call super if the button press got handled
                handled = true;
                break;
            }
        }

        this.runTasks();

        return handled;
    }

    protected List<BaseTextFieldWidget> getAllTextFields()
    {
        List<BaseTextFieldWidget> textFields = new ArrayList<>();

        if (this.widgets.isEmpty() == false)
        {
            for (InteractableWidget widget : this.widgets)
            {
                textFields.addAll(widget.getAllTextFields());
            }
        }

        return textFields;
    }

    @Override
    public boolean consumeString(String string)
    {
        this.messageRenderer.addMessage(3000, string);
        return true;
    }

    @Override
    public void addMessage(MessageType type, String messageKey, Object... args)
    {
        this.addGuiMessage(type, 5000, messageKey, args);
    }

    @Override
    public void addMessage(MessageType type, int lifeTime, String messageKey, Object... args)
    {
        this.addGuiMessage(type, lifeTime, messageKey, args);
    }

    public void addGuiMessage(MessageType type, int displayTimeMs, String messageKey, Object... args)
    {
        this.messageRenderer.addMessage(type, displayTimeMs, messageKey, args);
    }

    public void setNextMessageType(MessageType type)
    {
        this.messageRenderer.setNextMessageType(type);
    }

    protected void drawGuiMessages()
    {
        this.messageRenderer.drawMessages(this.width / 2, this.height / 2, this.zLevel + 200);
    }

    public void bindTexture(ResourceLocation texture)
    {
        this.mc.getTextureManager().bindTexture(texture);
    }

    public BaseScreen setZLevel(float zLevel)
    {
        this.zLevel = zLevel;
        int parentZLevel = (int) this.zLevel;

        for (InteractableWidget widget : this.buttons)
        {
            widget.setZLevelBasedOnParent(parentZLevel);
        }

        for (InteractableWidget widget : this.widgets)
        {
            widget.setZLevelBasedOnParent(parentZLevel);
        }

        this.messageRenderer.setZLevel(parentZLevel + 100);

        return this;
    }

    public BaseScreen setPopupGuiZLevelBasedOn(@Nullable GuiScreen gui)
    {
        if (gui instanceof BaseScreen)
        {
            this.setZLevel(((BaseScreen) gui).zLevel + this.getPopupGuiZLevelIncrement());
        }

        return this;
    }

    public <T extends BaseButton> T addButton(T button, ButtonActionListener listener)
    {
        button.setActionListener(listener);
        this.buttons.add(button);
        button.onWidgetAdded((int) this.zLevel);
        return button;
    }

    public <T extends InteractableWidget> T addWidget(T widget)
    {
        this.widgets.add(widget);
        widget.setTaskQueue(this::addTask);
        widget.onWidgetAdded((int) this.zLevel);
        return widget;
    }

    public <T extends BaseTextFieldWidget> T addTextField(T widget, TextChangeListener listener)
    {
        widget.setListener(listener);
        this.addWidget(widget);
        return widget;
    }

    public LabelWidget addLabel(int x, int y, int textColor, String... lines)
    {
        return this.addLabel(x, y, -1, -1, textColor, Arrays.asList(lines));
    }

    public LabelWidget addLabel(int x, int y, int textColor, List<String> lines)
    {
        return this.addLabel(x, y, -1, -1, textColor, lines);
    }

    public LabelWidget addLabel(int x, int y, int width, int height, int textColor, String... lines)
    {
        return this.addLabel(x, y, width, height, textColor, Arrays.asList(lines));
    }

    public LabelWidget addLabel(int x, int y, int width, int height, int textColor, List<String> lines)
    {
        return this.addWidget(new LabelWidget(x, y, width, height, textColor, lines));
    }

    protected boolean removeWidget(InteractableWidget widget)
    {
        if (widget != null && this.widgets.contains(widget))
        {
            this.widgets.remove(widget);
            return true;
        }

        return false;
    }

    protected void clearElements()
    {
        this.clearWidgets();
        this.clearButtons();
    }

    protected void clearWidgets()
    {
        this.widgets.clear();
    }

    protected void clearButtons()
    {
        this.buttons.clear();
    }

    private void addTask(Runnable task)
    {
        this.tasks.add(task);
    }

    protected void runTasks()
    {
        if (this.tasks.isEmpty() == false)
        {
            for (Runnable task : this.tasks)
            {
                task.run();
            }

            this.tasks.clear();
        }
    }

    protected void drawScreenBackground(int mouseX, int mouseY)
    {
        if (this.renderBorder)
        {
            ShapeRenderUtils.renderOutlinedRectangle(this.x, this.y, this.zLevel, this.screenWidth, this.screenHeight, this.backgroundColor, this.borderColor);
        }
        else
        {
            ShapeRenderUtils.renderRectangle(this.x, this.y, this.zLevel, this.screenWidth, this.screenHeight, this.backgroundColor);
        }
    }

    protected void drawTitle(int mouseX, int mouseY, float partialTicks)
    {
        this.drawStringWithShadow(this.getTitle(), this.x + this.titleX, this.y + this.titleY, this.titleColor);
    }

    protected void drawContents(int mouseX, int mouseY, float partialTicks)
    {
    }

    protected void drawWidgets(int mouseX, int mouseY, boolean isActiveGui, int hoveredWidgetId)
    {
        if (this.widgets.isEmpty() == false)
        {
            for (InteractableWidget widget : this.widgets)
            {
                widget.renderAt(widget.getX(), widget.getY(), widget.getZLevel(), mouseX, mouseY, isActiveGui, hoveredWidgetId);
            }
        }

        if (this.buttons.isEmpty() == false)
        {
            for (InteractableWidget widget : this.buttons)
            {
                widget.renderAt(widget.getX(), widget.getY(), widget.getZLevel(), mouseX, mouseY, isActiveGui, hoveredWidgetId);
            }
        }
    }

    protected void drawHoveredWidget(int mouseX, int mouseY, boolean isActiveGui, int hoveredWidgetId)
    {
        if (this.hoveredWidget != null)
        {
            this.hoveredWidget.postRenderHovered(mouseX, mouseY, isActiveGui, hoveredWidgetId);
            RenderUtils.disableItemLighting();
        }
    }

    public int getStringWidth(String text)
    {
        return this.textRenderer.getStringWidth(text);
    }

    public void drawString(String text, int x, int y, int color)
    {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0f, 0f, this.zLevel + 0.1f);

        this.textRenderer.drawString(text, x, y, color);

        GlStateManager.popMatrix();
    }

    public void drawStringWithShadow(String text, int x, int y, int color)
    {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0f, 0f, this.zLevel + 0.1f);

        this.textRenderer.drawStringWithShadow(text, x, y, color);

        GlStateManager.popMatrix();
    }

    public static boolean openScreen(@Nullable GuiScreen screen)
    {
        Minecraft.getMinecraft().displayGuiScreen(screen);
        return true;
    }

    /**
     * Opens a popup GUI, which is meant to open on top of another GUI.
     * This will set the Z level on that GUI based on the current GUI
     */
    public static boolean openPopupScreen(BaseScreen screen)
    {
        return openPopupScreen(screen, true);
    }

    public static boolean openPopupScreen(BaseScreen screen, boolean shouldRenderParent)
    {
        screen.setPopupGuiZLevelBasedOn(GuiUtils.getCurrentScreen());
        screen.setShouldRenderParent(shouldRenderParent);
        Minecraft.getMinecraft().displayGuiScreen(screen);
        return true;
    }

    public static boolean isShiftDown()
    {
        return isShiftKeyDown();
    }

    public static boolean isCtrlDown()
    {
        return isCtrlKeyDown();
    }

    public static boolean isAltDown()
    {
        return isAltKeyDown();
    }

    public void renderDebug(int mouseX, int mouseY)
    {
        if (GuiUtils.getCurrentScreen() == this)
        {
            boolean renderAll = MaLiLibConfigs.Debug.GUI_DEBUG_ALL.getBooleanValue();
            boolean infoAlways = MaLiLibConfigs.Debug.GUI_DEBUG_INFO_ALWAYS.getBooleanValue();

            renderWidgetDebug(this.buttons, mouseX, mouseY, renderAll, infoAlways);
            renderWidgetDebug(this.widgets, mouseX, mouseY, renderAll, infoAlways);
        }
    }

    public static void renderWidgetDebug(List<? extends InteractableWidget> widgets, int mouseX, int mouseY, boolean renderAll, boolean infoAlways)
    {
        if (widgets.isEmpty() == false)
        {
            for (InteractableWidget widget : widgets)
            {
                boolean hovered = widget.isMouseOver(mouseX, mouseY);
                widget.renderDebug(mouseX, mouseY, hovered, renderAll, infoAlways);
            }
        }
    }
}
