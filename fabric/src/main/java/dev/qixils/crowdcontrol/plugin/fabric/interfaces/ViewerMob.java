package dev.qixils.crowdcontrol.plugin.fabric.interfaces;

import dev.onyxstudios.cca.api.v3.component.Component;

public interface ViewerMob extends Component {

	boolean isViewerSpawned();

	void setViewerSpawned();
}
