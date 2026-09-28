package soka;

import java.util.*;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.*;

public class CloudEnv {
    // 4 VM kecil + 4 VM besar
    public static final double[] VM_MIPS = {500, 500, 500, 500, 1000, 1000, 1000, 1000};

    public static List<Cloudlet> simulate(int[] mapping, List<Long> miList) throws Exception {
        CloudSim.init(1, Calendar.getInstance(), false);

        // Host
        int[][] hostSpec = {{1000, 2048}, {1000, 2048}, {2000, 4096}, {2000, 4096}};
        List<Host> hosts = new ArrayList<>();
        for (int i = 0; i < hostSpec.length; i++) {
            List<Pe> pes = new ArrayList<>();
            pes.add(new Pe(0, new PeProvisionerSimple(hostSpec[i][0])));
            hosts.add(new Host(i, new RamProvisionerSimple(hostSpec[i][1]),
                    new BwProvisionerSimple(10000), 1_000_000, pes, new VmSchedulerTimeShared(pes)));
        }
        DatacenterCharacteristics dc = new DatacenterCharacteristics(
                "x86", "Linux", "Xen", hosts, 10.0, 3.0, 0.05, 0.001, 0.0);
        new Datacenter("DC_0", dc, new VmAllocationPolicySimple(hosts), new LinkedList<Storage>(), 0);

        DatacenterBroker broker = new DatacenterBroker("Broker");

        // VM
        List<Vm> vms = new ArrayList<>();
        for (int i = 0; i < VM_MIPS.length; i++) {
            boolean small = VM_MIPS[i] == 500;
            vms.add(new Vm(i, broker.getId(), VM_MIPS[i], 1, small ? 512 : 1024,
                    1000, 10000, "Xen", new CloudletSchedulerSpaceShared()));
        }
        broker.submitGuestList(vms);

        // Cloudlet
        List<Cloudlet> cloudlets = new ArrayList<>();
        UtilizationModel full = new UtilizationModelFull();
        for (int i = 0; i < miList.size(); i++) {
            Cloudlet c = new Cloudlet(i, miList.get(i), 1, 300, 300, full, full, full);
            c.setUserId(broker.getId());
            cloudlets.add(c);
        }
        broker.submitCloudletList(cloudlets);

        for (int i = 0; i < mapping.length; i++)
            broker.bindCloudletToVm(cloudlets.get(i).getCloudletId(), mapping[i]);

        CloudSim.startSimulation();
        List<Cloudlet> done = broker.getCloudletReceivedList();
        CloudSim.stopSimulation();
        return done;
    }
}