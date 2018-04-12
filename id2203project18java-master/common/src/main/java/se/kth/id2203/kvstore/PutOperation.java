package se.kth.id2203.kvstore;

import com.google.common.base.MoreObjects;
import se.kth.id2203.networking.NetAddress;

public class PutOperation extends Operation {
    public final String value;

    //put operation by providing the key, address of requester and value to store in the kv-store
    public PutOperation(String key, NetAddress operationAddress, String value) {
        super(key, operationAddress);
        this.value = value;
    }


    @Override
    public String toString(){
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("key", key)
                .add("operationAddress", operationAddress)
                .add("value", value)
                .toString();
    }

}
