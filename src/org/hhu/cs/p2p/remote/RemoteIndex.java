package org.hhu.cs.p2p.remote;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hhu.cs.p2p.io.PathAttributes;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;

/**
 * The remote index using hazelcast
 * 
 * @author Oliver Schrenk <oliver.schrenk@uni-duesseldorf.de>
 * 
 */
public class RemoteIndex {

	private Logger logger = Logger.getLogger(RemoteIndex.class);

	private static final String MAP_NAME = "p2p";

	private IMap<String, PathAttributes> map;

	/**
	 * Default constructor
	 */
	public RemoteIndex() {
		logger.info("IndexService created.");

		logger.info("Getting hazelcast map.");
		map = Hazelcast.getMap(MAP_NAME);
		map.addEntryListener(new RemoteIndexWatcher(), true);

		Set<Member> members = Hazelcast.getCluster().getMembers();
		Iterator<Member> iter = members.iterator();
		while (iter.hasNext()) {
			Member m = iter.next();
			m.getInetSocketAddress();
		}
	}

	public void add(Path path, PathAttributes pathAttributes) {
		synchronized (map) {
			put(path.toString(), pathAttributes);
		}
	}

	public void update(Path path, PathAttributes pathAttributes) {
		synchronized (map) {
			put(path.toString(), pathAttributes);
		}
	}

	public void delete(Path path) {
		synchronized (map) {
			map.remove(path);
		}
	}

	private void put(String path, PathAttributes pathAttributes) {
		map.put(path, pathAttributes);
	}

	@Override
	protected void finalize() throws Throwable {
		Hazelcast.shutdownAll();
		super.finalize();
	}
}
