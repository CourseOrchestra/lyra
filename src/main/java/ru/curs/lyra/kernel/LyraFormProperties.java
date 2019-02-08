package ru.curs.lyra.kernel;

import java.util.Optional;
import java.util.function.Function;

/**
 * Lyra form properties.
 *
 * TODO: it is now unclear if we really need 'parent' functionality here
 * maybe this functionality should be removed
 */

public class LyraFormProperties {
    private Optional<String> gridwidth = Optional.empty();
    private Optional<String> gridheight = Optional.empty();
    private Optional<String> footer = Optional.empty();
    private Optional<String> header = Optional.empty();

    private Optional<Boolean> visibleColumnsHeader = Optional.ofNullable(true);
    private Optional<Boolean> allowTextSelection = Optional.ofNullable(true);


    private final Optional<LyraFormProperties> parent;

    public LyraFormProperties() {
        this.parent = Optional.empty();
    }

    public LyraFormProperties(LyraFormProperties parent) {
        this.parent = Optional.ofNullable(parent);
    }

    private <T> T getValue(Optional<T> val, Function<LyraFormProperties, T> f) {
        return val.orElse(parent.map(f).orElse(null));
    }


    /**
     * Grid width in HTML units.
     */
    public String getGridwidth() {
        return getValue(gridwidth, LyraFormProperties::getGridwidth);
    }

    /**
     * Sets grid width.
     *
     * @param gridwidth grid width in HTML units.
     */
    public LyraFormProperties setGridwidth(String gridwidth) {
        this.gridwidth = Optional.ofNullable(gridwidth);
        return this;
    }

    /**
     * Grid height in HTML units.
     */
    public String getGridheight() {
        return getValue(gridheight, LyraFormProperties::getGridheight);
    }

    /**
     * Sets grid height in HTML units.
     *
     * @param gridheight grid height in pixels.
     */
    public LyraFormProperties setGridheight(String gridheight) {
        this.gridheight = Optional.ofNullable(gridheight);
        return this;
    }

    /**
     * Gets form's footer.
     */
    public String getFooter() {
        return getValue(footer, LyraFormProperties::getFooter);
    }

    /**
     * Set form's footer.
     *
     * @param footer new form's footer.
     */
    public LyraFormProperties setFooter(String footer) {
        this.footer = Optional.ofNullable(footer);
        return this;
    }

    /**
     * Gets form's header.
     *
     * @return form's header.
     */
    public String getHeader() {
        return getValue(header, LyraFormProperties::getHeader);
    }

    /**
     * Sets form's header.
     *
     * @param header new form's header.
     */
    public LyraFormProperties setHeader(String header) {
        this.header = Optional.ofNullable(header);
        return this;
    }


    public Boolean getVisibleColumnsHeader() {
        return getValue(visibleColumnsHeader, LyraFormProperties::getVisibleColumnsHeader);
    }

    public LyraFormProperties setVisibleColumnsHeader(Boolean visibleColumnsHeader) {
        this.visibleColumnsHeader = Optional.ofNullable(visibleColumnsHeader);
        return this;
    }

    public Boolean getAllowTextSelection() {
        return getValue(allowTextSelection, LyraFormProperties::getAllowTextSelection);
    }

    public LyraFormProperties setAllowTextSelection(Boolean allowTextSelection) {
        this.allowTextSelection = Optional.ofNullable(allowTextSelection);
        return this;
    }

}
