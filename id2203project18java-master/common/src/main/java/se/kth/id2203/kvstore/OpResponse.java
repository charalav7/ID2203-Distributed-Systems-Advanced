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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import java.io.Serializable;
import java.util.UUID;
import se.sics.kompics.KompicsEvent;

/**
 *
 * @author Lars Kroll <lkroll@kth.se>
 */
public class OpResponse implements KompicsEvent, Serializable {

    private static final long serialVersionUID = -1668600257615491286L;

    public final UUID id;
    public final Code status;
    public final String operationResponse; //added response string

    public OpResponse(UUID id, Code status, String operationResponse) {
        this.id = id;
        this.status = status;
        this.operationResponse = operationResponse; //added operationResponse to constructor
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("status", status)
                .add("operationResponse", operationResponse) //added also here
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof OpResponse) {
            OpResponse that = (OpResponse) o;
            return Objects.equal(this.id, that.id) && Objects.equal(this.status, that.status);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + java.util.Objects.hashCode(this.id);
        hash = 23 * hash + java.util.Objects.hashCode(this.status);
        return hash;
    }

    public static enum Code {

        OK, NOT_FOUND, NOT_IMPLEMENTED;
    }
}