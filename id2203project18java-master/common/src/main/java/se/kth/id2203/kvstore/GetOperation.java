package se.kth.id2203.kvstore;

import se.kth.id2203.networking.NetAddress;

public class GetOperation extends Operation{

    //simple get operation with desired key and address of requester
    public GetOperation(String key, NetAddress operationAddress) {
        super(key, operationAddress);
    }
}
