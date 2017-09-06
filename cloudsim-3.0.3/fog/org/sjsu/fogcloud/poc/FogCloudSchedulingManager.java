package org.sjsu.fogcloud.poc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;

public class FogCloudSchedulingManager {
	public void schedule(final List<? extends Cloudlet> list, final List<DatacenterBroker> brokerSlaves){
		Map<Integer, DatacenterBroker> map = new HashMap<Integer, DatacenterBroker>();
		for(DatacenterBroker d : brokerSlaves){
			map.put(d.getId(), d);
		}
		Objects.requireNonNull(list);
		Objects.requireNonNull(brokerSlaves);
		for(Cloudlet c : list){
			map.get(c.getUserId()).getCloudletList().add(c);
		}
	}
}
