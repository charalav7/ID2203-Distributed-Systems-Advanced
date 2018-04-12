package se.kth.id2203.kvstore;

import com.google.common.base.MoreObjects;
import se.kth.id2203.networking.NetAddress;

public class CasOperation extends Operation {
    public final String oldValue;
    public final String newValue;

    //cas operation by providing the key, address, old value to change, and the new value
    public CasOperation(String key, NetAddress operationAddress, String oldValue, String newValue) {
        super(key, operationAddress);
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @Override
    public String toString(){
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("key", key)
                .add("operationAddress", operationAddress)
                .add("oldValue", oldValue)
                .add("newValue", newValue)
                .toString();
    }
}
