package dev.qixils.crowdcontrol.plugin.fabric.interfaces;

import org.ladysnake.cca.api.v3.component.Component;

public interface ViewerMob extends Component {

	boolean isViewerSpawned();

	void setViewerSpawned();

	void setViewerSpawned(boolean value);
}
