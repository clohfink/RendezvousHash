package com.csforge;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.common.hash.Funnel;
import com.google.common.hash.HashFunction;


public class RendezvousHash<K, N> { 

	private HashFunction hasher;
	private Funnel<K> keyFunnel;
	private Funnel<N> nodeFunnel;
	private Set<N> nodes;
	
	public RendezvousHash(HashFunction hasher, Funnel<K> keyFunnel, Funnel<N> nodeFunnel, List<N> init) {
		this.hasher = hasher;
		this.keyFunnel = keyFunnel;
		this.nodeFunnel = nodeFunnel;
		this.nodes = Sets.newCopyOnWriteArraySet();
		nodes.addAll(init);
	}
	
	public boolean remove(N node) {
		return nodes.remove(node);
	}
	
	public boolean add(N node) {
		return nodes.add(node);
	}
	
	public N get(K key) {
		long maxValue = Long.MIN_VALUE;
		N max = null;
		for(N node : nodes) {
			long nodesHash = hasher.newHasher()
					.putObject(key, keyFunnel)
					.putObject(node, nodeFunnel)
					.hash().asLong();
			if(nodesHash > maxValue) {
				max = node;
				maxValue = nodesHash;
			}
		}
		return max;
	}
}
