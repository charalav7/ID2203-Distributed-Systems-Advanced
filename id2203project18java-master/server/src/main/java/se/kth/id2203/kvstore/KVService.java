/*
 * The MIT License
 *
 * Copyright 2017 Lars Kroll <lkroll@kth.se>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package se.kth.id2203.kvstore;

import se.kth.id2203.kvstore.OpResponse.Code;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.kth.id2203.overlay.Routing;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;

import java.util.HashMap;

/**
 *
 * @author Lars Kroll <lkroll@kth.se>
 */
public class KVService extends ComponentDefinition {

    //******* Ports ******
    protected final Positive<Network> net = requires(Network.class);
    //protected final Positive<Routing> route = requires(Routing.class);

    //******* Fields ******
    final NetAddress self = config().getValue("id2203.project.address", NetAddress.class);
    private HashMap<Integer, String> KVstore = new HashMap<>();

    //******* Handlers ******
    /*protected final ClassMatchedHandler<Operation, Message> opHandler = new ClassMatchedHandler<Operation, Message>() {

        @Override
        public void handle(Operation content, Message context) {
            logger.info("Got operation {}! Now implement me please :)", content);
            trigger(new Message(self, context.getSource(), new OpResponse(content.id, Code.NOT_IMPLEMENTED, operationResponse)), net);
        }

    };*/

    //GetOperation event handler
    private final ClassMatchedHandler<GetOperation, Message> getOperationMessageClassMatchedHandler = new ClassMatchedHandler<GetOperation, Message>() {
        @Override
        public void handle(GetOperation getOperation, Message message) {
            logger.info("GET operation event arrived for address {}!", self);
            String operationResponse = KVstore.get(Integer.parseInt(getOperation.key));
            //respond with either a value or null
            trigger(new Message(self, getOperation.operationAddress, new OpResponse(getOperation.id, Code.OK, operationResponse)), net);
        }
    };

    //PutOperation event handler
    private final ClassMatchedHandler<PutOperation, Message> putOperationMessageClassMatchedHandler = new ClassMatchedHandler<PutOperation, Message>() {
        @Override
        public void handle(PutOperation putOperation, Message message) {
            logger.info("PUT operation event arrived for address {}!", self);
            String operationResponse;
            if (KVstore.get(Integer.parseInt(putOperation.key)) != null){
                //case the put key already exists
                operationResponse = "PUT Failed! Key-Value already exists.";
            } else {
                //case key is unique, so proceed with put
                KVstore.put(Integer.parseInt(putOperation.key), putOperation.value);
                operationResponse = "PUT Success for (key,value) = (" + putOperation.key + "," + putOperation.value + ")";
            }
            trigger(new Message(self, putOperation.operationAddress, new OpResponse(putOperation.id, Code.OK, operationResponse)), net);
        }
    };

    //CasOperation event handler
    private final ClassMatchedHandler<CasOperation, Message> casOperationMessageClassMatchedHandler = new ClassMatchedHandler<CasOperation, Message>() {
        @Override
        public void handle(CasOperation casOperation, Message message) {
            logger.info("CAS operation event arrived for address {}!", self);
            String operationResponse;
            if (KVstore.get(Integer.parseInt(casOperation.key)).compareTo(casOperation.oldValue) == 0) {
                KVstore.replace(Integer.parseInt(casOperation.key), casOperation.newValue);
                operationResponse = "CAS SUCCESS! New value: " + casOperation.newValue + ", for key: " + casOperation.key;
            } else {
                operationResponse = "CAS FAILED! Old value:" + casOperation.oldValue + ", not found!";
            }
            trigger(new Message(self, casOperation.operationAddress, new OpResponse(casOperation.id, Code.OK, operationResponse)), net);
        }
    };

    {
        //subscribe(opHandler, net);
        subscribe(getOperationMessageClassMatchedHandler, net); // subscribe get handler to net
        subscribe(putOperationMessageClassMatchedHandler, net); // subscribe put handler to net
        subscribe(casOperationMessageClassMatchedHandler, net); // subscribe cas handler to net
    }

}
