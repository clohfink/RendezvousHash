package com.csforge;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.hash.Funnel;
import com.google.common.hash.Funnels;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * For comparing the load differences between consistent hash and HRW
 */
public class Compare { 
	private static final HashFunction hfunc = Hashing.murmur3_128();
	private static final Funnel<CharSequence> strFunnel = Funnels.stringFunnel(Charset.defaultCharset());
	
	public static void main(String[] args) {
		Map<String, AtomicInteger> distribution = Maps.newHashMap(); 

		System.out.println("======: ConsistentHash :========");
		ConsistentHash<String, String> c = new ConsistentHash(hfunc, strFunnel, strFunnel, getNodes(distribution));
		for(int i = 0 ; i < 10000; i++) {
			distribution.get(c.get(""+i)).incrementAndGet();
		}
		for(Entry<String, AtomicInteger> e : distribution.entrySet()) {
			System.out.println(e.getKey() + "," + e.getValue().get());
			e.getValue().set(0);
		}
		System.out.println("====== remove 2 ========");
		for(int i = 0 ; i < 2; i ++) {
			c.remove("Node"+i);
			distribution.remove("Node"+i);
		}
		for(int i = 0 ; i < 10000; i++) {
			distribution.get(c.get(""+i)).incrementAndGet();
		}
		for(Entry<String, AtomicInteger> e : distribution.entrySet()) {
			System.out.println(e.getKey() + "," + e.getValue().get());
		}

		System.out.println("======: RendezvousHash :========");
		distribution = Maps.newHashMap();  
		RendezvousHash<String, String> r = new RendezvousHash(hfunc, strFunnel, strFunnel, getNodes(distribution));

		for(int i = 0 ; i < 10000; i++) {
			distribution.get(r.get(""+i)).incrementAndGet();
		}
		for(Entry<String, AtomicInteger> e : distribution.entrySet()) {
			System.out.println(e.getKey() + "," + e.getValue().get());
			e.getValue().set(0);
		}
		System.out.println("====== remove 2 ========");
		for(int i = 0 ; i < 2; i ++) {
			r.remove("Node"+i);
			distribution.remove("Node"+i);
		}
		for(int i = 0 ; i < 10000; i++) {
			distribution.get(r.get(""+i)).incrementAndGet();
		}
		for(Entry<String, AtomicInteger> e : distribution.entrySet()) {
			System.out.println(e.getKey() + "," + e.getValue().get());
		}
	}
	
	private static List<String> getNodes(Map<String, AtomicInteger> distribution) {
		List<String> nodes = Lists.newArrayList();
		for(int i = 0 ; i < 5; i ++) {
			nodes.add("Node"+i);
			distribution.put("Node"+i, new AtomicInteger());
		}
		return nodes;
	}
	
}
