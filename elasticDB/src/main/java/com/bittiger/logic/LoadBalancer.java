package com.bittiger.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bittiger.client.ClientEmulator;

public class LoadBalancer {
	private List<Server> readQueue = new ArrayList<Server>();
	
	//Record the latency
	private HashMap<Server,Long> serverLatency = new HashMap<Server, Long>();
	
	private Server writeQueue = null;
	private List<Server> candidateQueue = new ArrayList<Server>();
	private int nextReadServer = 0;
	private static transient final Logger LOG = LoggerFactory
			.getLogger(LoadBalancer.class);

	public LoadBalancer(ClientEmulator ce) {
		writeQueue = new Server(ce.getTpcw().writeQueue);
		for (int i = 0; i < ce.getTpcw().readQueue.length; i++) {
			readQueue.add(new Server(ce.getTpcw().readQueue[i]));
			//put server into serverLatency
			serverLatency.put(new Server(ce.getTpcw().readQueue[i]), Long.MAX_VALUE - 1);
		}
		for (int i = 0; i < ce.getTpcw().candidateQueue.length; i++) {
			candidateQueue.add(new Server(ce.getTpcw().candidateQueue[i]));
		}
	}
	
	public void setServerLatency(Server server,long latency) {
		serverLatency.put(server, latency);
	}

	// there is only one server in the writequeue.
	public Server getWriteQueue() {
		return writeQueue;
	}

	public synchronized Server getNextReadServer() {
		nextReadServer = (nextReadServer + 1) % readQueue.size();
		Server server = readQueue.get(nextReadServer);
		LOG.debug("choose read server as " + server.getIp());
		return server;
	}
	
	//Get Random Server
	public synchronized Server getNextRandomReadServer() {
		Random rand = new Random();
		nextReadServer = rand.nextInt(readQueue.size());
		Server server = readQueue.get(nextReadServer);
		LOG.debug("choose read server as " + server.getIp());
		return server;
	}
	
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

	public synchronized void addServer(Server server) {
		readQueue.add(server);
	}

	public synchronized Server removeServer() {
		Server server = readQueue.remove(readQueue.size() - 1);
		candidateQueue.add(server);
		return server;
	}

	public synchronized List<Server> getReadQueue() {
		return readQueue;
	}

	// readQueue is shared by the UserSessions and Executor.
	// However, candidateQueue is only called by Executor.
	public List<Server> getCandidateQueue() {
		return candidateQueue;
	}

}
