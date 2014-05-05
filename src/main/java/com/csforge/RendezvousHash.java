package com.csforge;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.common.hash.Funnel;
import com.google.common.hash.HashFunction;

/**
 * <p>A high performance thread safe implementation of Rendezvous (Highest Random Weight, HRW) hashing is an algorithm that allows clients to achieve distributed agreement on which node (or proxy) a given
 * key is to be placed in. This implementation has the following properties.
 * <ul>
 * <li>Non-blocking reads : Determining which node a key belongs to is always non-blocking.  Adding and removing nodes however blocks each other</li>
 * <li>Low overhead: providing using a hash function of low overhead</li>
 * <li>Load balancing: Since the hash function is randomizing, each of the n nodes is equally likely to receive the key K. Loads are uniform across the sites.</li>
 * <li>High hit rate: Since all clients agree on placing an key K into the same node N , each fetch or placement of K into N yields the maximum utility in terms of hit rate. The key K will always be found unless it is evicted by some replacement algorithm at N.</li>
 * <li>Minimal disruption: When a node is removed, only the keys mapped to that node need to be remapped and they will be distributed evenly</li>
 * </ul>
 * </p>
 * source: https://en.wikipedia.org/wiki/Rendezvous_hashing 
 * 
 * @author Chris Lohfink
 * 
 * @param <K>
 *            type of key
 * @param <N>
 *            type node/site or whatever want to be returned (ie IP address or String)
 */
public class RendezvousHash<K, N> {

	/**
	 * A hashing function from guava, ie Hashing.murmur3_128()
	 */
	private HashFunction hasher;

	/**
	 * A funnel to describe how to take the key and add it to a hash.
	 * 
	 * @see com.google.common.hash.Funnel
	 */
	private Funnel<K> keyFunnel;

	/**
	 * Funnel describing how to take the type of the node and add it to a hash
	 */
	private Funnel<N> nodeFunnel;

	/**
	 * All the current nodes in the pool
	 */
	private Set<N> nodes;
	private N[] ordered;

	/**
	 * Creates a new RendezvousHash with a starting set of nodes provided by init. The funnels will be used when generating the hash that combines the nodes and
	 * keys. The hasher specifies the hashing algorithm to use.
	 */
	public RendezvousHash(HashFunction hasher, Funnel<K> keyFunnel, Funnel<N> nodeFunnel, Collection<N> init) {
		this.hasher = hasher;
		this.keyFunnel = keyFunnel;
		this.nodeFunnel = nodeFunnel;
		this.nodes = Sets.newHashSet();
		this.ordered = (N[]) new Object[0];
		nodes.addAll(init);
	}

	/**
	 * Removes a node from the pool. Keys that referenced it should after this be evenly distributed amongst the other nodes
	 * 
	 * @return true if the node was in the pool
	 */
	public synchronized boolean remove(N node) {
		boolean ret = nodes.remove(node);
		N[] tmp = (N[]) nodes.toArray();
		Arrays.sort(tmp);
		ordered = tmp;
		return ret;
	}

	/**
	 * Add a new node to pool and take an even distribution of the load off existing nodes
	 * 
	 * @return true if node did not previously exist in pool
	 */
	public synchronized boolean add(N node) {
		boolean ret = nodes.add(node);
		N[] tmp = (N[]) nodes.toArray();
		Arrays.sort(tmp);
		ordered = tmp;
		return ret;
	}

	/**
	 * return a node for a given key
	 */
	public N get(K key) {
		long maxValue = Long.MIN_VALUE;
		N max = null;
		for (N node : ordered) {
			long nodesHash = hasher.newHasher()
					.putObject(key, keyFunnel)
					.putObject(node, nodeFunnel)
					.hash().asLong();
			if (nodesHash > maxValue) {
				max = node;
				maxValue = nodesHash;
			}
		}
		return max;
	}
}
