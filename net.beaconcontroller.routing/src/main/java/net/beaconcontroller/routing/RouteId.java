/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
package net.beaconcontroller.routing;

import org.openflow.util.HexString;

/**
 * Stores the endpoints of a route, in this case datapath ids
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class RouteId implements Cloneable, Comparable<RouteId> {
    protected Long src;
    protected Long dst;

    public RouteId(Long src, Long dst) {
        super();
        this.src = src;
        this.dst = dst;
    }

    public Long getSrc() {
        return src;
    }

    public void setSrc(Long src) {
        this.src = src;
    }

    public Long getDst() {
        return dst;
    }

    public void setDst(Long dst) {
        this.dst = dst;
    }

    @Override
    public int hashCode() {
        final int prime = 2417;
        int result = 1;
        result = prime * result + ((dst == null) ? 0 : dst.hashCode());
        result = prime * result + ((src == null) ? 0 : src.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RouteId other = (RouteId) obj;
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
        return true;
    }

    @Override
    public String toString() {
        return "RouteId [src=" + HexString.toHexString(this.src) + " dst="
                + HexString.toHexString(this.dst) + "]";
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public int compareTo(RouteId o) {
        int result = src.compareTo(o.getSrc());
        if (result != 0)
            return result;
        return dst.compareTo(o.getDst());
    }
}
