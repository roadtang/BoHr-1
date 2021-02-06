package org.bohr.gui.laf;

import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.OceanTheme;
import java.awt.*;

public class MyDefaultMetalTheme  extends OceanTheme {
    public ColorUIResource getWindowTitleInactiveBackground() {
        return new ColorUIResource(Color.black);
    }

    public ColorUIResource getWindowTitleBackground() {
        return new ColorUIResource(java.awt.Color.black);
    }

    public ColorUIResource getPrimaryControlHighlight() {
        return new ColorUIResource(java.awt.Color.black);
    }

    public ColorUIResource getPrimaryControlDarkShadow() {
        return new ColorUIResource(java.awt.Color.black);
    }

    public ColorUIResource getPrimaryControl() {
        return new ColorUIResource(new Color(0xFFAD00));
    }

    public ColorUIResource getControlHighlight() {
        return new ColorUIResource(java.awt.Color.black);
    }

    public ColorUIResource getControlDarkShadow() {
        return new ColorUIResource(Color.black);
    }

    public ColorUIResource getControl() {
        return new ColorUIResource(java.awt.Color.black);
    }


    @Override
    public ColorUIResource getPrimaryControlShadow() {
        return new ColorUIResource(0x000000);
    }

    @Override
    public ColorUIResource getPrimaryControlInfo() {
        return new ColorUIResource(0x000000);
    }

    @Override
    public ColorUIResource getControlShadow() {
        return new ColorUIResource(0x000000);
    }

    @Override
    public ColorUIResource getControlInfo() {
        return new ColorUIResource(0x000000);
    }

}

