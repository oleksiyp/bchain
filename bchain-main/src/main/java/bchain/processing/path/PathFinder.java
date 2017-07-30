package bchain.processing.path;

import bchain.domain.Hash;
import bchain.dao.BlockDAO;
import bchain.dao.BlockLevelDAO;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static bchain.processing.path.PathItem.Type.POP;
import static bchain.processing.path.PathItem.Type.PUSH;

@AllArgsConstructor
public class PathFinder {
    private final BlockDAO blockDAO;
    private final BlockLevelDAO levelDAO;

    public List<PathItem> findPath(Hash from, Hash to){
        if (from == null) {
            List<PathItem> result = new ArrayList<>();
            result.add(new PathItem(PathItem.Type.PUSH, to));
            return result;
        }

        int fromLevel = levelDAO.getBlockLevel(from);
        int toLevel = levelDAO.getBlockLevel(to);

        List<Hash> fromPath = new ArrayList<>();
        List<Hash> toPath = new ArrayList<>();

        while (toLevel > fromLevel) {
            toPath.add(to);
            to = blockDAO.parentBlockHash(to);
            toLevel--;
        }

        while (fromLevel > toLevel) {
            fromPath.add(from);
            from = blockDAO.parentBlockHash(from);
            fromLevel--;
        }

        while (!from.equals(to)) {
            fromPath.add(from);
            from = blockDAO.parentBlockHash(from);
            fromLevel--;

            toPath.add(to);
            to = blockDAO.parentBlockHash(to);
            toLevel--;
        }

        List<PathItem> result = new ArrayList<>();

        for (Hash hash : fromPath) {
            result.add(new PathItem(POP, hash));
        }
        Collections.reverse(toPath);
        for (Hash hash : toPath) {
            result.add(new PathItem(PUSH, hash));
        }

        return result;
    }
}
