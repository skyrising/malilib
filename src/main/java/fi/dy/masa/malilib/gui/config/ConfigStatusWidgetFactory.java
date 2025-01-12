package fi.dy.masa.malilib.gui.config;

import fi.dy.masa.malilib.config.option.ConfigInfo;
import fi.dy.masa.malilib.overlay.widget.BaseConfigStatusIndicatorWidget;
import fi.dy.masa.malilib.util.data.ConfigOnTab;

public interface ConfigStatusWidgetFactory<C extends ConfigInfo>
{
    BaseConfigStatusIndicatorWidget<? extends ConfigInfo> create(C config, ConfigOnTab configOnTab);
}
