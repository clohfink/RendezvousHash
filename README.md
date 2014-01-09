RendezvousHash
==============

Rendezvous or Highest Random Weight (HRW) hashing is an algorithm that allows clients to achieve distributed agreement on which site (or proxy) a given object is to be placed in. It accomplishes the same goal as consistent hashing, using an entirely different method.

```java
    RendezvousHash<String, String> h;
    h.add("node1");
    h.add("node2");
    
    String node = h.get("key");  //  "node1"
    h.remove(node);
    h.get("key"); //  "node2"
    
    h.add(node); 
    h.get("key"); //  "node1"
```
