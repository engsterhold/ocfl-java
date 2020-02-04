/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 University of Wisconsin Board of Regents
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

package edu.wisc.library.ocfl.core.mapping;

import edu.wisc.library.ocfl.api.util.Enforce;
import edu.wisc.library.ocfl.core.encode.Encoder;
import edu.wisc.library.ocfl.core.encode.NoOpEncoder;

/**
 * Uses the entire object id as the encapsulation directory
 */
public class IdEncapsulator implements Encapsulator {

    private Encoder encoder;
    private boolean useEncodedId;

    /**
     * Uses the entire encoded id as the encapsulation directory
     *
     * @return encapsulator
     */
    public static IdEncapsulator useEncodedId() {
        return new IdEncapsulator(new NoOpEncoder(), true);
    }

    /**
     * Encodes the object id again and uses this value as the encapsulation directory
     *
     * @param encoder encoder
     * @return encapsulator
     */
    public static IdEncapsulator useOriginalId(Encoder encoder) {
        return new IdEncapsulator(encoder, false);
    }

    public IdEncapsulator(Encoder encoder, boolean useEncodedId) {
        this.encoder = Enforce.notNull(encoder, "encoder cannot be null");
        this.useEncodedId = useEncodedId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String encapsulate(String objectId, String encodedId) {
        if (useEncodedId) {
            return encodedId;
        }
        return encoder.encode(objectId);
    }

}
