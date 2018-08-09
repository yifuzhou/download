# Week 8 Practice
* Zhengyang Zhou

题目1（必做）：

Make the following modification on sql.getBestSellers in the "sql-mysql.properties" file.

sql.getBestSellers="SELECT i_id, i_title, a_fname, a_lname " +\
                 "FROM item, author, order_line " +\
                 "WHERE item.i_id = order_line.ol_i_id " +\
                 "AND item.i_a_id = author.a_id " +\
                 "AND order_line.ol_o_id > (SELECT _*_  FROM orders) " +\
                 "AND item.i_subject = ? " +\
                 "GROUP BY i_id, i_title, a_fname, a_lname " +\
                 "ORDER BY SUM(ol_qty) DESC " +\
                 "limit *10*"


题目2 balancer:
In LoadBalancer class, I have the following method for getting random server
```java
//Get Random Server
	public synchronized Server getNextRandomReadServer() {
		Random rand = new Random();
		nextReadServer = rand.nextInt(readQueue.size());
		Server server = readQueue.get(nextReadServer);
		LOG.debug("choose read server as " + server.getIp());
		return server;
	}
```

For latency one as following. Of course, I also have to record the latency in user session, and initiate a hashmap in LoadBalancer class to do the latency comparison.
```java
//Get server based on latency
	public synchronized Server getNextLeastLatencyReadServer() {
		long min = Long.MAX_VALUE;
		Server winner = null;
		for (Server server : this.serverLatency.keySet()) {
			if (serverLatency.get(server) < min) {
				min = serverLatency.get(server);
				winner = server;
			}
		}
		//Server server = readQueue.get(nextReadServer);
		LOG.debug("choose read server as " + winner.getIp());
		return winner;
	}
```
