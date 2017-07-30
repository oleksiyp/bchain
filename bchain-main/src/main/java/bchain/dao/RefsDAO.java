package bchain.dao;

import bchain.domain.Hash;

public interface RefsDAO {
    Hash getHead();

    void prepareHeadOp(Hash newHash, OpType push);

    void assignHead(Hash newHead);


    Hash getMaster();

    void prepareMasterSwitch(Hash masterHash);

    void prepareMasterOp(Hash newHash, OpType opType);

    void assignMaster(Hash master);

    enum OpType {
        POP, PUSH
    }
}
