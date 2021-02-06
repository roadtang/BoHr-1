/**
 * Copyright (c) 2019 The Bohr Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an API version, in the format of MAJOR.MINOR.PATCH.
 *
 * <ul>
 * <li>MAJOR version increases when backward compatibility is lost.</li>
 * <li>MINOR version increases when incremental improvements is introduced.</li>
 * <li>PATCH version is reserved for security patches</li>
 * </ul>
 */
public enum ApiVersion {

    v2_4_0("v2.4.0");

    public final static ApiVersion DEFAULT = v2_4_0;

    public final String prefix;

    private static Map<String, ApiVersion> versions = new HashMap<>();

    static {
        for (ApiVersion version : values()) {
            versions.put(version.prefix, version);
        }
    }

    ApiVersion(String prefix) {
        this.prefix = prefix;
    }

    public static ApiVersion of(String prefix) {
        return versions.get(prefix);
    }

}
