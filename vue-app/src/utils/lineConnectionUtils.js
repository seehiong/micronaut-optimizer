// src/utils/lineConnectionUtils.js
import store from '@/store';
import { useToast } from "vue-toastification";
import { findNodeById } from "@/utils/nodeUtils";

const toast = useToast();

const nodeTypes = {
    TextInput: {
        inputTypes: [""],
        outputTypes: ["textInput"],
    },
    KeyInput: {
        inputTypes: [""],
        outputTypes: ["keyInput"],
    },
    SubkeyInput: {
        inputTypes: [""],
        outputTypes: ["subkeyInput"],
    },
    TextOutput: {
        inputTypes: ["any"],
        outputTypes: [""],
    },
    DistanceMatrixConstraint: {
        inputTypes: ["double[][]"],
        outputTypes: ["distanceMatrixConstraint"],
    },
    SolveTimeConstraint: {
        inputTypes: ["string"],
        outputTypes: ["solveTimeConstraint"],
    },
    TSPInput: {
        inputTypes: ["distanceMatrixConstraint", "solveTimeConstraint"],
        outputTypes: ["TSPInput"],
    },
    TSPProblem: {
        inputTypes: ["TSPInput"],
        outputTypes: ["TSPOutput"],
    },
};

const typeCompatibility = {
    // Primitive type compatibility rules
    double: ["double", "double[]", "double[][]"],
    "double[]": ["double[]", "double[][]"],
    "double[][]": ["double[][]"],
    int: ["int", "int[]", "int[][]"],
    "int[]": ["int[]", "int[][]"],
    "int[][]": ["int[][]"],
    boolean: ["boolean", "boolean[]", "boolean[][]"],
    "boolean[]": ["boolean[]", "boolean[][]"],
    "boolean[][]": ["boolean[][]"],
    string: ["string", "string[]", "string[][]"],
    "string[]": ["string[]", "string[][]"],
    "string[][]": ["string[][]"],
    any: ["any"],
};

/**
 * Get the type of a port.
 * @param {string} portId - The port ID (e.g., "n1-i0").
 * @param {Object} node - The node object.
 * @returns {string} - The port type.
 */
export function getPortType(portId, node) {
    const [, type, indexStr] = portId.match(/^[^-]+-([io])(\d+)$/);
    const index = parseInt(indexStr);
    const portType = type === "i" ? node.inputTypes[index] : node.outputTypes[index];
    return portType.replace(/\s+/g, ""); // Normalize by removing spaces
}

/**
 * Check if two types are compatible.
 * @param {string} sourceType - The source type.
 * @param {string} targetType - The target type.
 * @returns {boolean} - True if the types are compatible, false otherwise.
 */
export function isTypeCompatible(sourceType, targetType) {
    const normalizedSourceType = sourceType.replace(/\s+/g, "");
    const normalizedTargetType = targetType.replace(/\s+/g, "");

    if (normalizedTargetType === "any") {
        return true;
    }

    if (typeCompatibility[normalizedSourceType]) {
        const compatibleTypes = typeCompatibility[normalizedSourceType] || [];
        return compatibleTypes.includes(normalizedTargetType);
    }

    for (const nodeType in nodeTypes) {
        if (nodeTypes[nodeType].outputTypes.includes(normalizedSourceType)) {
            const compatibleTypes = nodeTypes[nodeType].outputTypes || [];
            return compatibleTypes.includes(normalizedTargetType);
        }
    }

    toast.warning(`No compatibility rules found for types: ${normalizedSourceType}, ${normalizedTargetType}`);
    return false;
}

/**
 * Check if an input port is already connected.
 * @param {string} targetId - The target port ID.
 * @returns {boolean} - True if the input port is already connected, false otherwise.
 */
export function isInputPortConnected(targetId) {
    const lines = store.getters.lines;
    return lines.some((line) => line.targetId === targetId);
}

/**
 * Check if a line already exists between two ports.
 * @param {string} sourceId - The source port ID.
 * @param {string} targetId - The target port ID.
 * @returns {boolean} - True if the line exists, false otherwise.
 */
export function isLineExist(sourceId, targetId) {
    const lines = store.getters.lines;
    return lines.some(
        (line) =>
            (line.sourceId === sourceId && line.targetId === targetId) ||
            (line.sourceId === targetId && line.targetId === sourceId)
    );
}

/**
 * Check if a port already has a line.
 * @param {string} portId - The port ID to check.
 * @param {string} type - The port type ("input" or "output").
 * @returns {boolean} - True if the port has a line, false otherwise.
 */
export function isPortConnected(portId, type) {
    const lines = store.getters.lines;
    return lines.some(
        (line) =>
            (type === "input" && line.targetId === portId) || // Check for input port
            (type === "output" && line.sourceId === portId) // Check for output port
    );
}

/**
 * Validate a connection between two ports.
 * @param {string} sourceId - The source port ID.
 * @param {string} targetId - The target port ID.
 * @returns {boolean} - True if the connection is valid, false otherwise.
 */
export function isValidConnection(sourceId, targetId) {
    const sourceNode = findNodeById(sourceId.substring(0, sourceId.lastIndexOf('-')));
    const targetNode = findNodeById(targetId.substring(0, targetId.lastIndexOf('-')));

    if (!sourceNode || !targetNode) {
        toast.error("Source or target node not found.");
        return false;
    }

    const sourceOutputType = getPortType(sourceId, sourceNode);
    const targetInputType = getPortType(targetId, targetNode);

    if (!isTypeCompatible(sourceOutputType, targetInputType)) {
        toast.warning("Types are not compatible.");
        return false;
    }

    if (isInputPortConnected(targetId)) {
        toast.warning("Input port is already connected to another output port.");
        return false;
    }

    return true;
}