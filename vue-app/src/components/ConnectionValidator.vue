<template>
    <template v-if="false"></template> <!-- Empty root template -->
</template>

<script>
/* eslint-disable */
const nodeTypes = {
    TextInput: {
        inputTypes: [""],
        outputTypes: ["textInput"],
    },
    TextOutput: {
        inputTypes: ["Any"], // Accepts any input type
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
        inputTypes: ["distanceMatrixConstraint","solveTimeConstraint"],
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
    string: ["string"],
    Any: ["Any"], // Allow "Any" to match any type
};

export default {
    props: {
        nodes: Array,
        lines: Array,
    },
    methods: {
        // Find a node by its ID
        findNodeById(nodeId) {
            const node = this.nodes.find((node) => node.id === nodeId);
            if (!node) {
                console.error(`Node with ID "${nodeId}" not found.`); // Debugging
            }
            return node;
        },

        getPortType(portId, node) {
            const [, type, indexStr] = portId.match(/^[^-]+-([io])(\d+)$/);
            const index = parseInt(indexStr);
            const portType = type === "i" ? node.inputTypes[index] : node.outputTypes[index];

            // Normalize the port type (optional, if needed)
            return portType.replace(/\s+/g, ""); // Remove spaces
        },

        isTypeCompatible(sourceType, targetType) {
            // Normalize the types (optional, if needed)
            const normalizedSourceType = sourceType.replace(/\s+/g, ""); // Remove spaces
            const normalizedTargetType = targetType.replace(/\s+/g, ""); // Remove spaces

            // If the target type is "Any", it is compatible with any source type
            if (normalizedTargetType === "Any") {
                return true;
            }

            // Check for primitive type compatibility
            if (typeCompatibility[normalizedSourceType]) {
                const compatibleTypes = typeCompatibility[normalizedSourceType] || [];
                return compatibleTypes.includes(normalizedTargetType);
            }

            // Check for custom type compatibility using nodeTypes
            for (const nodeType in nodeTypes) {
                if (nodeTypes[nodeType].outputTypes.includes(normalizedSourceType)) {
                    const compatibleTypes = nodeTypes[nodeType].outputTypes || [];
                    return compatibleTypes.includes(normalizedTargetType);
                }
            }

            // If no compatibility rules are found, assume incompatibility
            console.warn("No compatibility rules found for types:", normalizedSourceType, normalizedTargetType);
            return false;
        },

        isValidConnection(sourceId, targetId) {
            // Find the source and target nodes
            const sourceNode = this.findNodeById(sourceId.substring(0, sourceId.lastIndexOf('-')));
            const targetNode = this.findNodeById(targetId.substring(0, targetId.lastIndexOf('-')));

            if (!sourceNode || !targetNode) {
                console.error("Source or target node not found.");
                return false;
            }

            // Get the types of the source output and target input ports
            const sourceOutputType = this.getPortType(sourceId, sourceNode);
            const targetInputType = this.getPortType(targetId, targetNode);

            // Check if the types are compatible
            if (!this.isTypeCompatible(sourceOutputType, targetInputType)) {
                console.warn("Types are not compatible.");
                return false;
            }

            // Check if the input port is already connected to another output port
            if (this.isInputPortConnected(targetId)) {
                console.warn("Input port is already connected to another output port.");
                return false;
            }

            return true;
        },

        // Check if a port already has a line
        doesPortHaveLine(portId, type) {
            return this.lines.some(
                (line) =>
                    (type === "input" && line.targetId === portId) || // Check for input port
                    (type === "output" && line.sourceId === portId) // Check for output port
            );
        },

        // Check if a line already exists between two ports
        doesLineExist(sourceId, targetId) {
            return this.lines.some(
                (line) =>
                    (line.sourceId === sourceId && line.targetId === targetId) ||
                    (line.sourceId === targetId && line.targetId === sourceId)
            );
        },

        // Check if the input port is already connected to another output port
        isInputPortConnected(targetId) {
            return this.lines.some((line) => line.targetId === targetId);
        },
    },
};
</script>