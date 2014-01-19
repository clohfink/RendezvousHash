package com.csforge;

import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.hash.Funnel;
import com.google.common.hash.HashFunction;

/**
 * Very simple implementation of consistent hash to compare against HRW hashing.  This does not include the vnode improvement which
 * would mitigate issues with uneven distribution but matches closely with what many implementations actually use.  This
 * implementation is not recommended for use outside testing.
 */
public class ConsistentHash<K, N> {

	private final HashFunction hashFunction; 
	private final SortedMap<Long, N> ring = new TreeMap<Long, N>();
	private Funnel<N> nodeFunnel;
	private Funnel<K> keyFunnel;

	public ConsistentHash(HashFunction hashFunction, Funnel<K> keyFunnel, Funnel<N> nodeFunnel, Collection<N> nodes) {
		this.hashFunction = hashFunction;
		this.nodeFunnel = nodeFunnel;
		this.keyFunnel = keyFunnel;
		for (N node : nodes) {
			add(node);
		}
	}

	public boolean add(N node) { 
		ring.put(hashFunction.newHasher().putObject(node, nodeFunnel).hash().asLong(), node); 
		return true;
	}

	public boolean remove(N node) {
		return node == ring.remove(hashFunction.newHasher().putObject(node, nodeFunnel).hash().asLong()); 
	}

	public N get(K key) { 
		Long hash = hashFunction.newHasher().putObject(key, keyFunnel).hash().asLong();
		if (!ring.containsKey(hash)) {
			SortedMap<Long, N> tailMap = ring.tailMap(hash);
			hash = tailMap.isEmpty() ? ring.firstKey() : tailMap.firstKey();
		}
		return ring.get(hash);
	}
}
