/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
package net.beaconcontroller.topology;

import java.util.Comparator;

import net.beaconcontroller.core.IOFSwitch;

/**
 *
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class LinkTuple implements Comparator<LinkTuple>, Comparable<LinkTuple> {
    protected SwitchPortTuple src;
    protected SwitchPortTuple dst;
    protected int cost;
    /**
     * @param src
     * @param dst
     * @param cost
     */
    
    public LinkTuple() {
        // Default constructor for TreeSet creation
      }

    public LinkTuple(SwitchPortTuple src, SwitchPortTuple dst) {
        this.src = src;
        this.dst = dst;
        this.cost = 0;
    }

    public LinkTuple(SwitchPortTuple src, SwitchPortTuple dst, int cost) {
        this.src = src;
        this.dst = dst;
        this.cost = cost;
    }
    
    public LinkTuple(IOFSwitch src, Short srcPort, IOFSwitch dst, Short dstPort) {
        this.src = new SwitchPortTuple(src, srcPort);
        this.dst = new SwitchPortTuple(dst, dstPort);
        this.cost = 0;
    }
    
    public LinkTuple(IOFSwitch src, Short srcPort, IOFSwitch dst, Short dstPort, int cost) {
        this.src = new SwitchPortTuple(src, srcPort);
        this.dst = new SwitchPortTuple(dst, dstPort);
        this.cost = cost;
    }

    /**
     * Convenience constructor, ports are cast to shorts
     * @param srcId
     * @param srcPort
     * @param dstId
     * @param dstPort
     */
    public LinkTuple(IOFSwitch src, Integer srcPort, IOFSwitch dst, Integer dstPort) {
        this.src = new SwitchPortTuple(src, srcPort);
        this.dst = new SwitchPortTuple(dst, dstPort);
        this.cost = 0;
    }

    public LinkTuple(IOFSwitch src, Integer srcPort, IOFSwitch dst, Integer dstPort, int cost) {
        this.src = new SwitchPortTuple(src, srcPort);
        this.dst = new SwitchPortTuple(dst, dstPort);
        this.cost = cost;
    }
    /**
     * @return the src
     */
    public SwitchPortTuple getSrc() {
        return src;
    }

    /**
     * @param src the src to set
     */
    public void setSrc(SwitchPortTuple src) {
        this.src = src;
    }

    /**
     * @return the dst
     */
    public SwitchPortTuple getDst() {
        return dst;
    }

    /**
     * @param dst the dst to set
     */
    public void setDst(SwitchPortTuple dst) {
        this.dst = dst;
    }
    
    /**
     * @return the cost
     */
    public int getCost() {
        return cost;
    }
    
    /**
     * @param cost the cost to set
     */
    public void setCost(int cost) {
        this.cost = cost;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 2221;
        int result = 1;
        result = prime * result + ((dst == null) ? 0 : dst.hashCode());
        result = prime * result + ((src == null) ? 0 : src.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof LinkTuple))
            return false;
        LinkTuple other = (LinkTuple) obj;
        if (dst == null) {
            if (other.dst != null)
                return false;
        } else if (!dst.equals(other.dst))
            return false;
        if (src == null) {
            if (other.src != null)
                return false;
        } else if (!src.equals(other.src))
            return false;
        if (cost != other.cost)
            return false;
        return true;
    }
    
    public int compare(LinkTuple o1, LinkTuple o2) {
        // Used for comparisions during add/remove operations
        return o1.compareTo(o2); 
      }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "LinkTuple [src=" + src + ", dst=" + dst + ", cost=" + cost + "]";
    }

    @Override
    public int compareTo(LinkTuple other) {

        if (this.cost == other.cost &&
                 this.src.toString().equals(other.src.toString()) &&
                 this.dst.toString().equals(other.dst.toString()))
            return(0);

        else if(this.cost > other.cost)
            return(1);
           
        return -1;
    }
}