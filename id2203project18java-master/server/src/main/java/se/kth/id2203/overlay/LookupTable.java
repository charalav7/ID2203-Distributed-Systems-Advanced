/*
 * The MIT License
 *
 * Copyright 2017 Lars Kroll <lkroll@kth.se>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package se.kth.id2203.overlay;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.TreeMultimap;

import java.util.*;

import se.kth.id2203.bootstrapping.NodeAssignment;
import se.kth.id2203.networking.NetAddress;

import static se.sics.kompics.Kompics.logger;


/**
 *
 * @author Lars Kroll <lkroll@kth.se>
 */
public class LookupTable implements NodeAssignment {

    private static final long serialVersionUID = -8766981433378303267L;
    private final static int REPLICATION_DELTA = 3; //added replication degree
    private final static int RANGE = 10; //range of assigned keys for each partition

    private final TreeMultimap<Integer, NetAddress> partitions = TreeMultimap.create();

    public Collection<NetAddress> lookup(String key) {
        //int keyHash = key.hashCode();
        int keyHash = Integer.parseInt(key);
        Integer partition = partitions.keySet().floor(keyHash); //partition number less than or equal to key
        if (partition == null) {
            partition = partitions.keySet().last();
        }
        return partitions.get(partition);
    }

    public Collection<NetAddress> getNodes() {
        return partitions.values();
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LookupTable(\n");
        for (Integer key : partitions.keySet()) {
            sb.append(key);
            sb.append(" -> ");
            sb.append(Iterables.toString(partitions.get(key)));
            sb.append("\n");
        }
        sb.append(")");
        return sb.toString();
    }

    //change accordingly the lookuptable generation
    static LookupTable generate(ImmutableSet<NetAddress> nodes) {
        LookupTable lookupTable = new LookupTable();
        int numPartitions = nodes.size() / REPLICATION_DELTA; //the number of partition groups
        logger.info("NodesSize {}", nodes.size());
        ArrayList<NetAddress> netAddressArrayList = new ArrayList<>();
        netAddressArrayList.addAll(nodes);
        int key = 0; //the key of the partition
        for (int i=0; i<numPartitions; i++){
            for (int j=0; j<REPLICATION_DELTA; j++){
                logger.info("Address node {}", netAddressArrayList.get(i*REPLICATION_DELTA + j));
                lookupTable.partitions.put(key, netAddressArrayList.get(i*REPLICATION_DELTA + j)); //put the (key,address) to partitions
            }
            key += RANGE; //increase the key with the given hardcoded range
        }
        //lookupTable.partitions.putAll(0, nodes);
        return lookupTable;
    }

    //a method to return all the keys from the partitions
    public Set<Integer> getAllKeys() {
        return partitions.keySet();
    }

    //add method for returning the key of a specific net address
    public String getNodeKey(NetAddress netAddress) {
        for (int key : getAllKeys()) {
            if (partitions.get(key).contains(netAddress)) {
                return Integer.toString(key);
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof LookupTable) {
            LookupTable that = (LookupTable) o;
            return Objects.equals(this.partitions, that.partitions);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.partitions);
        return hash;
    }

}
