package fi.dy.masa.malilib.gui.position;

import java.util.function.ToIntFunction;
import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.value.BaseOptionListConfigValue;

public class VerticalAlignment extends BaseOptionListConfigValue
{
    public static final VerticalAlignment TOP    = new VerticalAlignment("top",    "malilib.label.vertical_alignment.top",    (h) -> 0, (ch, vh, o) -> o);
    public static final VerticalAlignment BOTTOM = new VerticalAlignment("bottom", "malilib.label.vertical_alignment.bottom", (h) -> -h, (ch, vh, o) -> vh - ch - o);
    public static final VerticalAlignment CENTER = new VerticalAlignment("center", "malilib.label.vertical_alignment.center", (h) -> -h / 2, (ch, vh, o) -> vh / 2 - ch / 2 + o);

    public static final ImmutableList<VerticalAlignment> VALUES = ImmutableList.of(TOP, BOTTOM, CENTER);

    protected final ToIntFunction<Integer> yOffsetSource;
    protected final StartYPositionSource startYPositionSource;

    public VerticalAlignment(String name, String translationKey, ToIntFunction<Integer> yOffsetSource, StartYPositionSource startYPositionSource)
    {
        super(name, translationKey);

        this.yOffsetSource = yOffsetSource;
        this.startYPositionSource = startYPositionSource;
    }

    public int getStartY(int contentHeight, int viewportHeight, int offset)
    {
        return this.startYPositionSource.getStartY(contentHeight, viewportHeight, offset);
    }

    public int getYStartOffset(int contentHeight)
    {
        return this.yOffsetSource.applyAsInt(contentHeight);
    }

    public interface StartYPositionSource
    {
        int getStartY(int contentHeight, int viewportHeight, int offset);
    }
}
