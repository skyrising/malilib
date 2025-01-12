package fi.dy.masa.malilib.overlay.widget;

import java.util.List;
import fi.dy.masa.malilib.config.option.HotkeyConfig;
import fi.dy.masa.malilib.render.text.StyledTextLine;
import fi.dy.masa.malilib.render.text.TextStyle;
import fi.dy.masa.malilib.util.data.ConfigOnTab;

public class HotkeyConfigStatusWidget extends BaseConfigStatusIndicatorWidget<HotkeyConfig>
{
    protected TextStyle style = TextStyle.builder().withColor(0xFFFFA000).build();
    protected List<Integer> lastValue;

    public HotkeyConfigStatusWidget(HotkeyConfig config, ConfigOnTab configOnTab)
    {
        super(config, configOnTab);

        this.updateValue();
    }

    @Override
    public void updateState(boolean force)
    {
        if (force || this.lastValue.equals(this.config.getValue().getKeys()) == false)
        {
            this.updateValue();
        }
    }

    protected void updateValue()
    {
        this.lastValue = this.config.getValue().getKeys();
        this.valueDisplayText = StyledTextLine.of(this.config.getValue().getKeysDisplayString(), this.style);
        this.valueRenderWidth = this.valueDisplayText.renderWidth;
    }
}
