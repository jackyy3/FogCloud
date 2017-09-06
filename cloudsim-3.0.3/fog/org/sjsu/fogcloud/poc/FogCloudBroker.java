package org.sjsu.fogcloud.poc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Vm;

public class FogCloudBroker extends DatacenterBroker {

	final List<DatacenterBroker> brokerSlaves = new ArrayList<DatacenterBroker>();

	public FogCloudBroker(final String name) throws Exception {
		super(name);
	}

	@Override
	public void submitCloudletList(final List<? extends Cloudlet> list) {
		if (brokerSlaves.isEmpty()) {
			getCloudletList().addAll(list);
			return;
		}
		FogCloudSchedulingManager schManager = new FogCloudSchedulingManager();
		schManager.schedule(list, brokerSlaves);
	}

	public boolean registerBroker(final DatacenterBroker broker) {
		return this.brokerSlaves.add(broker);
	}

	public boolean unRegisterBroker(final DatacenterBroker broker) {
		return this.brokerSlaves.remove(broker);
	}

	private void assignVmsToClusters(final List<? extends Vm> list) {
		if (brokerSlaves.isEmpty()) {
			getVmList().addAll(list);
			return;
		}
		int b = brokerSlaves.size();
		Random ran = new Random();
		for(Vm v : list){
			brokerSlaves.get(ran.nextInt(b)).getVmList().add(v);
		}
	}

}
