package weights;

import java.io.Serializable;

public class RegularWeightsSet extends WeightsSet implements Serializable {

    private long[] longs;

    public RegularWeightsSet(long[] longs) {
        this.longs = longs;
    }
}
