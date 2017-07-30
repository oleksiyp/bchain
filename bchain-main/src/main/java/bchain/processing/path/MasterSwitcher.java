package bchain.processing.path;

import bchain.Factory;
import bchain.dao.ChainDAO;
import bchain.dao.RefsDAO;
import bchain.domain.Block;

public class MasterSwitcher extends PathSwitcher {
    private final RefsDAO refsDAO;

    public MasterSwitcher(Factory factory) {
        super(factory);

        refsDAO = factory.create(RefsDAO.class);
    }

    @Override
    public boolean perform() {
        refsDAO.prepareMasterSwitch(path.get(path.size() - 1).getHash());
        return super.perform();
    }

    @Override
    protected void commitOp(Block itemBlock) {
        refsDAO.assignMaster(itemBlock.getHash());
    }

    @Override
    protected void prepareOp(Block itemBlock, boolean isPush) {
        refsDAO.prepareMasterOp(itemBlock.getHash(),
                isPush ? RefsDAO.OpType.PUSH : RefsDAO.OpType.POP);
    }

    @Override
    protected boolean validate(int i, Block itemBlock) {
        return true;
    }
}
