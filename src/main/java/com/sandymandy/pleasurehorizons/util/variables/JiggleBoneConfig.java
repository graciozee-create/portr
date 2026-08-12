package com.sandymandy.pleasurehorizons.util.variables;

/**
 * Spring constants for one jiggling bone.
 *
 * @param boneName  bone in the geo model this applies to
 * @param stiffness how strongly the bone is pulled back to its rest rotation
 * @param damping   how quickly the oscillation dies down
 */
public record JiggleBoneConfig(String boneName, double stiffness, double damping) {}
