package com.massivecraft.factions.integration.spout;

import java.util.concurrent.CopyOnWriteArrayList;

import org.getspout.spoutapi.gui.GenericPopup;
import org.getspout.spoutapi.gui.GenericWidget;

import com.massivecraft.factions.P;

public class GenericBox {
	public CopyOnWriteArrayList<GenericWidget> widgets;
	public CopyOnWriteArrayList<GenericBox> boxes;
	public GenericPopup popup;
	public int X, Y;

	public GenericBox() {
		widgets = new CopyOnWriteArrayList<GenericWidget>();
		boxes = new CopyOnWriteArrayList<GenericBox>();
	}

	public GenericBox addWidget(GenericWidget widget) {
		widgets.add(widget);
		widget.setX(widget.getX() + this.X);
		widget.setY(widget.getY() + this.Y);
		popup.attachWidget(P.p, widget);
		return this;
	}

	public GenericBox addBox(GenericBox box) {
		boxes.add(box);
		box.addPopup(popup);

		return this;
	}

	public GenericBox addPopup(GenericPopup widget) {
		popup = widget;
		for (GenericWidget widget1 : widgets) {
			if (popup != null)
				popup.attachWidget(P.p, widget1);
			;
		}
		return this;
	}

	public GenericBox removeWidget(GenericWidget widget) {
		if (widget != null) {
			widgets.remove(widget);
			popup.removeWidget(widget);
		}
		return this;
	}

	public void remove() {
		for (GenericWidget widget : widgets) {
			widgets.remove(widget);
			popup.removeWidget(widget);
		}
	}

	public GenericBox hide() {
		for (GenericWidget widget : widgets) {
			widget.setVisible(false);
		}
		for (GenericBox box : boxes) {
			box.hide();
		}
		return this;
	}

	public GenericBox show() {
		for (GenericWidget widget : widgets) {
			widget.setVisible(true);
		}
		for (GenericBox box : boxes) {
			box.show();
		}
		return this;
	}

	public GenericBox setX(int X) {
		int tmpX = this.X - X;
		this.X = X;
		for (GenericWidget widget : widgets) {
			widget.setX(widget.getX() + tmpX);
		}
		return this;
	}

	public GenericBox setY(int Y) {
		int tmpY = this.Y - Y;
		this.Y = Y;
		for (GenericWidget widget : widgets) {
			widget.setY(widget.getY() + tmpY);
		}
		return this;
	}

}
