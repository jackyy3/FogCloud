package org.sjsu.fogcloud.poc;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.NetworkTopology;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class FogCloudPoc {
	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList1;
	private static List<Cloudlet> cloudletList2;

	/** The vmlist. */
	private static List<Vm> vmlist;
	private static List<Vm> vmlist1;
	private static List<Vm> vmlist2;
	private static List<Vm> vmlist3;

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {

		Log.printLine("Fog cloud demo...");

		try {
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = 2; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			// Datacenters are the resource providers in CloudSim. We need at
			// list one of them to run a CloudSim simulation
			Datacenter datacenter0 = createDatacenter("Datacenter_1");
			Datacenter datacenter1 = createDatacenter("Datacenter_2");
			Datacenter datacenter2 = createDatacenter("Datacenter_3");

			DatacenterBroker broker1 = createBroker(1);
			int brokerId1 = broker1.getId();

			DatacenterBroker broker2 = createBroker(2);
			int brokerId2 = broker2.getId();

			DatacenterBroker broker3 = createBroker(3);
			int brokerId3 = broker3.getId();

			FogCloudBroker broker = new FogCloudBroker("Broker_Master");

			vmlist = new ArrayList<Vm>();
			vmlist1 = new ArrayList<Vm>();
			vmlist2 = new ArrayList<Vm>();
			vmlist3 = new ArrayList<Vm>();

			// VM description
			long size = 10000; // image size (MB)
			int mips = 250;
			int ram = 512; // vm memory (MB)
			long bw = 1000;
			int pesNumber = 1; // number of cpus
			String vmm = "Xen"; // VMM name

			// create two VMs: the first one belongs to user1
			Vm vm1 = new Vm(1, brokerId1, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());

			// the second VM: this one belongs to user2
			Vm vm2 = new Vm(2, brokerId2, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());

			Vm vm3 = new Vm(3, brokerId3, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
			Vm vm4 = new Vm(4, broker1.getId(), mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
			// add the VMs to the vmlists
			vmlist1.add(vm1);
			vmlist2.add(vm2);
			vmlist3.add(vm3);
			vmlist1.add(vm4);

			// vmlist.add(vm4);

			// submit vm list to the broker
			broker1.submitVmList(vmlist1);
			broker2.submitVmList(vmlist2);
			broker3.submitVmList(vmlist3);

			broker.registerBroker(broker1);
			broker.registerBroker(broker2);
			broker.registerBroker(broker3);

			cloudletList1 = new ArrayList<Cloudlet>();

			Random random = new Random();
			// Cloudlet properties
			long length = 40000;
			long fileSize = 300;
			long outputSize = 300;
			UtilizationModel utilizationModel = new UtilizationModelFull();

			for (int i = 0; i < 10; i++) {
				Cloudlet cloudlet1 = new Cloudlet(i, length + random.nextInt(1000), pesNumber,
						fileSize + random.nextInt(300), outputSize + random.nextInt(300), utilizationModel,
						utilizationModel, utilizationModel);
				cloudlet1.setUserId((i % 3) + 5);
				cloudletList1.add(cloudlet1);
			}

			// submit cloudlet list to the brokers
			broker.submitCloudletList(cloudletList1);

			// Sixth step: configure network
			// load the network topology file
			NetworkTopology.buildNetworkTopology("topology.brite");

			// maps CloudSim entities to BRITE entities
			// Datacenter0 will correspond to BRITE node 0
			int briteNode = 0;
			NetworkTopology.mapNode(datacenter0.getId(), briteNode);

			// Datacenter1 will correspond to BRITE node 2
			briteNode = 1;
			NetworkTopology.mapNode(datacenter1.getId(), briteNode);

			briteNode = 2;
			NetworkTopology.mapNode(datacenter2.getId(), briteNode);

			// Broker1 will correspond to BRITE node 3
			briteNode = 3;
			NetworkTopology.mapNode(broker1.getId(), briteNode);

			// Broker2 will correspond to BRITE node 4
			briteNode = 4;
			NetworkTopology.mapNode(broker2.getId(), briteNode);

			briteNode = 5;
			NetworkTopology.mapNode(broker3.getId(), briteNode);

			briteNode = 6;
			NetworkTopology.mapNode(broker.getId(), briteNode);

			// Sixth step: Starts the simulation
			CloudSim.startSimulation();

			// Final step: Print results when simulation is over
			List<Cloudlet> newList1 = broker1.getCloudletReceivedList();
			List<Cloudlet> newList2 = broker2.getCloudletReceivedList();
			List<Cloudlet> newList3 = broker3.getCloudletReceivedList();

			CloudSim.stopSimulation();

			Log.print("=============> User " + (brokerId1 - 4) + "    ");
			printCloudletList(newList1);

			Log.print("=============> User " + (brokerId2 - 4) + "    ");
			printCloudletList(newList2);

			Log.print("=============> User " + (brokerId3 - 4) + "    ");
			printCloudletList(newList3);

			Log.printLine("Demo finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}
	}

	private static Datacenter createDatacenter(String name) {

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store
		// our machine
		List<Host> hostList = new ArrayList<Host>();

		// 2. A Machine contains one or more PEs or CPUs/Cores.
		// In this example, it will have only one core.
		List<Pe> peList = new ArrayList<Pe>();
		List<Pe> peList1 = new ArrayList<Pe>();
		List<Pe> peList2 = new ArrayList<Pe>();

		int mips = 1000;

		// 3. Create PEs and add these into a list.
		peList.add(new Pe(0, new PeProvisionerSimple(mips)));
		peList.add(new Pe(1, new PeProvisionerSimple(mips)));
		peList.add(new Pe(2, new PeProvisionerSimple(mips)));

		peList1.add(new Pe(0, new PeProvisionerSimple(mips)));
		peList1.add(new Pe(1, new PeProvisionerSimple(mips)));
		peList1.add(new Pe(2, new PeProvisionerSimple(mips)));

		peList2.add(new Pe(0, new PeProvisionerSimple(mips)));
		peList2.add(new Pe(1, new PeProvisionerSimple(mips)));
		peList2.add(new Pe(2, new PeProvisionerSimple(mips)));

		// 4. Create Host with its id and list of PEs and add them to the list
		// of machines
		int hostId = 0;
		int ram = 2048; // host memory (MB)
		long storage = 1000000; // host storage
		int bw = 10000;

		hostList.add(new Host(hostId, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList,
				new VmSchedulerSpaceShared(peList)));
//		hostList.add(new Host(1, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList1,
//				new VmSchedulerSpaceShared(peList)));
//		hostList.add(new Host(2, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList2,
//				new VmSchedulerSpaceShared(peList)));

		// 5. Create a DatacenterCharacteristics object that stores the
		// properties of a data center: architecture, OS, list of
		// Machines, allocation policy: time- or space-shared, time zone
		// and its price (G$/Pe time unit).
		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this
										// resource
		double costPerBw = 0.0; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are
																		// not
																		// adding
																		// SAN
																		// devices
																		// by
																		// now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone,
				cost, costPerMem, costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	// We strongly encourage users to develop their own broker policies, to
	// submit vms and cloudlets according
	// to the specific rules of the simulated scenario
	private static DatacenterBroker createBroker(int id) {

		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker("Broker" + id);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects
	 * 
	 * @param list
	 *            list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + "Time"
				+ indent + "Start Time" + indent + "Finish Time");

		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");

				DecimalFormat dft = new DecimalFormat("###.##");
				Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId()
						+ indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent
						+ dft.format(cloudlet.getExecStartTime()) + indent + indent
						+ dft.format(cloudlet.getFinishTime()));
			}
		}

	}
}
