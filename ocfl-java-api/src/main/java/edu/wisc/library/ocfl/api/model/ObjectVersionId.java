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

package edu.wisc.library.ocfl.api.model;

import edu.wisc.library.ocfl.api.util.Enforce;

import java.util.Objects;

/**
 * Points to a specific version of an object, encapsulating an object identifier and version identifier. When HEAD
 * is specified, then it points to whatever the most recent version of the object is.
 */
public class ObjectVersionId {

    private final String objectId;
    private final VersionId versionId;

    /**
     * Creates an ObjectId instance that points to the HEAD version of the object
     *
     * @param objectId the id of the object
     */
    public static ObjectVersionId head(String objectId) {
        return new ObjectVersionId(objectId);
    }

    /**
     * Creates an ObjectId instance that points to a specific version of an object
     *
     * @param objectId the id of the object
     * @param versionId the OCFL version id of the version
     */
    public static ObjectVersionId version(String objectId, String versionId) {
        return new ObjectVersionId(objectId, VersionId.fromString(versionId));
    }

    /**
     * Creates an ObjectId instance that points to a specific version of an object
     *
     * @param objectId the id of the object
     * @param versionId the OCFL version id of the version
     */
    public static ObjectVersionId version(String objectId, VersionId versionId) {
        return new ObjectVersionId(objectId, versionId);
    }

    private ObjectVersionId(String objectId) {
        this.objectId = Enforce.notBlank(objectId, "objectId cannot be blank");
        versionId = null;
    }

    private ObjectVersionId(String objectId, VersionId versionId) {
        this.objectId = Enforce.notBlank(objectId, "objectId cannot be blank");
        this.versionId = Enforce.notNull(versionId, "versionId cannot be null");
    }

    /**
     * The object id
     *
     * @return the object id
     */
    public String getObjectId() {
        return objectId;
    }

    /**
     * The version id
     *
     * @return the versionId or null if no version is specified
     */
    public VersionId getVersionId() {
        return versionId;
    }

    /**
     * Returns true if versionId is NOT set
     *
     * @return true if the HEAD version is referenced
     */
    public boolean isHead() {
        return versionId == null;
    }

    @Override
    public String toString() {
        return "ObjectId{" +
                "objectId='" + objectId + '\'' +
                ", versionId='" + versionId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectVersionId that = (ObjectVersionId) o;
        return objectId.equals(that.objectId) &&
                versionId.equals(that.versionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectId, versionId);
    }

}
