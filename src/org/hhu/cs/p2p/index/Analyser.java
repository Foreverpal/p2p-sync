package org.hhu.cs.p2p.index;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * The {@link Analyser} returns an {@link Analysis} by comparing two different
 * {@link Map}s of {@link Path} to {@link Attributes}.
 * 
 * Changes occur if a path exists on both maps but one is newer.
 * 
 * Conflicts occur if a path exist in one map but not the other.
 * 
 * @author Oliver Schrenk <oliver.schrenk@uni-duesseldorf.de>
 * 
 */
public class Analyser {

	/**
	 * @param local
	 *            the map of the local machine
	 * @param remote
	 *            the map of the cloud
	 * @return
	 */
	public Analysis compare(final Map<Path, Attributes> local,
			final Map<String, Attributes> remote) {
		Set<TreeConflict> conflicts = new HashSet<TreeConflict>();
		Set<Change> changes = new HashSet<Change>();

		// we need a copy of the paths
		Set<String> clonedRemoteKeys = new HashSet<String>();
		for (String s : remote.keySet()) {
			clonedRemoteKeys.add(s);
		}

		Iterator<Path> iter;
		Path p;
		Attributes attributes;

		iter = local.keySet().iterator();
		while (iter.hasNext()) {
			p = iter.next();
			attributes = remote.get(p.toString());
			// does not exist in remote
			if (attributes == null) {
				conflicts.add(new TreeConflict(p, Existence.LOCAL));
			}
			// exists in remote
			else {
				long localModified = local.get(p).lastModifiedTime();
				long remoteModified = attributes.lastModifiedTime();

				if (localModified < remoteModified) {
					changes
							.add(new Change(p, ChangeType.UPDATE,
									Direction.PULL));
				} else {
					changes
							.add(new Change(p, ChangeType.UPDATE,
									Direction.PUSH));
				}

				// remove key from cloned list to reduce costs of next loop
				clonedRemoteKeys.remove(p);
			}
		} // done ierating local

		// WARNING reusing objects p,e from above!
		// iterate rest of remote keys
		Iterator<String> clonedKeysIterator = clonedRemoteKeys.iterator();
		while (clonedKeysIterator.hasNext()) {
			p = iter.next();
			attributes = local.get(p);
			// does not exist in local
			if (attributes == null) {
				conflicts.add(new TreeConflict(p, Existence.REMOTE));
			}
		}

		return new Analysis(changes, conflicts);
	}
}
