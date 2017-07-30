package bchain.processing.path;

import bchain.Factory;
import bchain.dao.ChainDAO;
import bchain.dao.RefsDAO;
import bchain.domain.Block;

public class HeadSwitcher extends PathSwitcher {
    private final RefsDAO refsDAO;

    private final ChainDAO chainDAO;

    public HeadSwitcher(Factory factory) {
        super(factory);

        refsDAO = factory.create(RefsDAO.class);
        chainDAO = factory.create(ChainDAO.class);
    }

    @Override
    protected void commitOp(Block itemBlock) {
        refsDAO.assignHead(itemBlock.getHash());
    }

    @Override
    protected void prepareOp(Block itemBlock, boolean isPush) {
        refsDAO.prepareHeadOp(itemBlock.getHash(),
                isPush ? RefsDAO.OpType.PUSH : RefsDAO.OpType.POP);
    }

    @Override
    protected boolean validate(int i, Block itemBlock) {
        if (!itemBlock.validate()) {
            for (int j = i; j < path.size(); j++) {
                chainDAO.declare(
                        path.get(j).getHash(),
                        ChainDAO.BlockType.INVALID);
            }
            return false;
        }
        return true;
    }
}
