package bchain_poc.dao.mem;

import bchain_poc.Factory;
import bchain_poc.mining.Miner;
import bchain_poc.mining.SimpleMiner;
import bchain_poc.processing.path.HeadSwitcher;
import bchain_poc.processing.path.MasterSwitcher;
import bchain_poc.processing.path.PathFinder;

public class InMemDaoFactory implements Factory {

    private InMemDao dao;

    private PathFinder pathFinder;

    private MasterSwitcher masterSwitcher;
    private HeadSwitcher headSwitcher;

    private SimpleMiner miner;

    public InMemDaoFactory() {
        dao = new InMemDao();

        pathFinder = new PathFinder(dao, dao);

        masterSwitcher = new MasterSwitcher(this);

        headSwitcher = new HeadSwitcher(this);

        miner = new SimpleMiner(this);
    }

    @Override
    public <T> T create(Class<T> type) {
        if (type.isAssignableFrom(dao.getClass())) {
            return type.cast(dao);
        } else if (type == PathFinder.class) {
            return type.cast(pathFinder);
        } else if (type == MasterSwitcher.class) {
            return type.cast(masterSwitcher);
        } else if (type == HeadSwitcher.class) {
            return type.cast(headSwitcher);
        } else if (type == Miner.class) {
            return type.cast(miner);
        } else {
            throw new RuntimeException("can't create: " + type.getName());
        }
    }
}
