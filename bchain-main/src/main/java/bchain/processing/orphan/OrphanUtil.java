package bchain.processing.orphan;

import bchain.domain.Hash;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class OrphanUtil {
    public static List<Set<Hash>> gatherOrphanageTree(Hash hash,
                                                      Function<Hash, Set<Hash>> resolve) {
        List<Set<Hash>> deOrphaned = new ArrayList<>();
        Set<Hash> hashQ = new HashSet<>();
        hashQ.add(hash);

        Set<Hash> rememberSet = new HashSet<>();

        while (!hashQ.isEmpty()) {

            deOrphaned.add(hashQ);

            Set<Hash> hashQQ = new HashSet<>();
            for (Hash element : hashQ) {
                hashQQ.addAll(resolve.apply(element));
            }

            hashQQ.removeAll(rememberSet);
            rememberSet.addAll(hashQQ);

            hashQ = hashQQ;
        }

        return deOrphaned;
    }
}
