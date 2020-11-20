package org.rif.notifier.models.entities;

import org.rif.notifier.constants.BlockTypes;

import javax.persistence.*;
import java.math.BigInteger;

@Entity
@Table(name = "datafetcher")
public class DataFetcherEntity {

    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private int id;

    @Column(name = "last_block")
    private BigInteger lastBlock;

    @Enumerated(EnumType.STRING)
    private BlockTypes blockType;

    public DataFetcherEntity() {}

    public DataFetcherEntity(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public BigInteger getLastBlock() {
        return lastBlock;
    }

    public void setLastBlock(BigInteger lastBlock) {
        this.lastBlock = lastBlock;
    }

    public BlockTypes getBlockType() {
        return blockType;
    }

    public void setBlockType(BlockTypes blockType) {
        this.blockType = blockType;
    }
}
