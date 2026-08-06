package com.yunfie.illustia.pallasync

object PallaSyncConflictResolver {
    
    /**
     * Compare two version vectors to determine causality.
     * Returns 1 if v1 dominates v2
     * Returns -1 if v2 dominates v1
     * Returns 0 if they are concurrent (conflict) or identical
     */
    fun compareVectors(v1: Map<String, Long>, v2: Map<String, Long>): Int {
        var v1Dominates = false
        var v2Dominates = false
        
        val allKeys = v1.keys + v2.keys
        for (key in allKeys) {
            val val1 = v1[key] ?: 0L
            val val2 = v2[key] ?: 0L
            
            if (val1 > val2) v1Dominates = true
            if (val2 > val1) v2Dominates = true
        }
        
        return when {
            v1Dominates && !v2Dominates -> 1
            !v1Dominates && v2Dominates -> -1
            else -> 0 // Concurrent or identical
        }
    }
    
    /**
     * Resolves a concurrent conflict using the deterministic LWW tuple:
     * (lamport, device_id_raw, device_seq)
     * Returns true if event1 wins over event2.
     */
    fun resolveConflict(
        lamport1: Long, deviceId1: String, seq1: Long,
        lamport2: Long, deviceId2: String, seq2: Long
    ): Boolean {
        if (lamport1 != lamport2) return lamport1 > lamport2
        if (deviceId1 != deviceId2) return deviceId1 > deviceId2
        return seq1 > seq2
    }
}
