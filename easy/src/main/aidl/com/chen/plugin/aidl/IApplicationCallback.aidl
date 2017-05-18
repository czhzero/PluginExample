

package com.chen.plugin.aidl;

/**
 * API for package data change related callbacks from the Package Manager.
 * Some usage scenarios include deletion of cache directory, generate
 * statistics related to code, data, cache usage
 * {@hide}
 */
interface IApplicationCallback {
      Bundle onCallback(in Bundle extra);
}
