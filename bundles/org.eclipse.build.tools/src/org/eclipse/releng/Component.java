/*
 * Created on Dec 10, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.releng;

/**
 * @author kmoir
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

import java.util.Vector;

public class Component {

	/**
	 * 
	 */
	public Component() {
		super();
		// TODO Auto-generated constructor stub
		maps = new Vector();
	}

	public static void main(String[] args) {
	}
	
	private String componentName;
	private Vector maps;
	/**
	 * @return Returns the component.
	 */
	public String getComponentName() {
		return componentName;
	}

	/**
	 * @param component The component to set.
	 */
	public void setComponentName(String component) {
		this.componentName = component;
	}

	/**
	 * @return Returns the maps.
	 */
	public Vector getMaps() {
		return maps;
	}

	/**
	 * @param maps The maps to set.
	 */
	public void setMaps(Vector maps) {
		this.maps = maps;
	}

}
