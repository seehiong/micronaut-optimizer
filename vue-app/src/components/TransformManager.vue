<template>
    <template v-if="false"></template> <!-- Empty root template -->
</template>

<script>
export default {
    props: {
        nodes: Array,
        lines: Array,
    },
    methods: {
        onTrigger(payload, nodes, lines, visitedNodes = new Set()) {
            const { nodeId, data } = payload;
            // Check if the node has already been visited
            if (visitedNodes.has(nodeId)) {
                console.warn(`Circular connection detected at node ${nodeId}. Stopping recursion.`);
                return;
            }
            // Mark the current node as visited
            visitedNodes.add(nodeId);
            // Find the node that triggered the transformation
            const node = nodes.find((n) => n.id === nodeId);
            if (!node) {
                console.warn(`Node with ID ${nodeId} not found.`);
                return;
            }

            if (node.triggerAction === "O") {
                if (node.type === "invoke-api") {
                    // Send the data to the backend API
                    this.processInvokeApi(data, node.apiUrl);
                }
            }

            // Find connected downstream nodes
            const connectedNodes = this.findConnectedNodes(nodeId, nodes, lines);
            // If there are no connected nodes, stop recursion
            if (connectedNodes.length === 0) {
                return;
            }
            // Process each connected downstream node
            connectedNodes.forEach((downstreamNode) => {
                if (downstreamNode.triggerAction === "A") { // Apply transformation when triggerAction is auto
                    this.applyTransform(data, nodes, lines, visitedNodes, downstreamNode);
                } else {
                    downstreamNode.nodeData = data; // Pass the data as is
                }
            });
        },

        applyTransform(data, nodes, lines, visitedNodes, downstreamNode) {
            // Perform transformation based on the downstream node's type
            let transformedData;
            if (downstreamNode.type === "matrix-of-doubles") {
                transformedData = this.processMatrixOfDoubles(data);
            } else if (downstreamNode.type === "cast-to-string") {
                transformedData = this.processCastToString(data);
            } else if (downstreamNode.type === "json-formatter") {
                transformedData = this.processJsonFormatter(
                    data,
                    downstreamNode.formatterKey,
                    downstreamNode.outputTypes[0]
                );
            } else if (downstreamNode.type === "json-aggregator") {
                // Pass the existing nodeData of the json-aggregator node
                transformedData = this.processJsonAggregator(data, downstreamNode.nodeData || {});
            } else if (downstreamNode.type === "any") {
                transformedData = this.processOutputText(data);
            } else {
                // Default: forward the data as-is
                transformedData = data;
            }
            // Update the downstream node's nodeData
            downstreamNode.nodeData = transformedData;
            // Recursively call onTrigger for the downstream node
            this.onTrigger(
                {
                    nodeId: downstreamNode.id,
                    action: downstreamNode.triggerAction,
                    data: transformedData,
                },
                nodes,
                lines,
                new Set(visitedNodes) // Pass a new set of visited nodes to avoid shared state
            );
        },

        findConnectedNodes(nodeId, nodes, lines) {
            if (!lines || lines.length === 0) {
                console.warn("No lines found.");
                return [];
            }
            return nodes.filter((node) =>
                lines.some(
                    (line) =>
                        this.extractNodeId(line.sourceId) === nodeId && // Compare source nodeId
                        this.extractNodeId(line.targetId) === node.id // Compare target nodeId
                )
            );
        },

        processOutputText(text) {
            try {
                // If the input is already a 2D array, format it directly
                if (Array.isArray(text) && text.every(row => Array.isArray(row))) {
                    // Compactly format the 2D array
                    return JSON.stringify(text)
                        .replace(/\[\[/g, "[\n\t[") // Add newline & tab after outer opening bracket
                        .replace(/\]\]/g, "]\n]") // Add newline before outer closing bracket
                        .replace(/,/g, ", ") // Add consistent spacing after commas
                        .replace(/],\s*\[/g, "],\n\t["); // Add newline & tab between arrays
                }
                // If the input is a string, try to parse it as JSON
                if (typeof text === "string") {
                    return text;
                }
                // For other cases, return the input as-is
                return JSON.stringify(text, null, 2);
            } catch (error) {
                console.error("Invalid JSON input:", error);
                return "Invalid JSON input";
            }
        },

        processMatrixOfDoubles(text) {
            try {
                const matrix = JSON.parse(text);
                if (
                    Array.isArray(matrix) &&
                    matrix.every((row) => Array.isArray(row) && row.every((val) => typeof val === "number"))
                ) {
                    return matrix; // Valid double[][]
                } else {
                    throw new Error("Input must be a 2D array of numbers.");
                }
            } catch (error) {
                console.error("Invalid matrix input:", error);
                return null;
            }
        },

        processCastToString(value) {
            if (value === null || value === undefined) {
                return ''; // Return an empty string for null or undefined
            }
            // Handle arrays by joining elements with a space
            if (Array.isArray(value)) {
                return value.join(' ');
            }
            // Handle objects by converting them to JSON strings
            if (typeof value === 'object') {
                return JSON.stringify(value, null, 2); // Pretty-print JSON
            }
            // Handle other types (numbers, booleans, etc.) using toString
            return value.toString();
        },

        processJsonFormatter(data, formatterKey, outputType) {
            try {
                // Wrap the data in the desired JSON structure
                const jsonOutput = {
                    [outputType]: {
                        [formatterKey]: data,
                    },
                };
                // Pretty-print the JSON
                return JSON.stringify(jsonOutput, null, 2);
            } catch (error) {
                console.error("Invalid input for JSON formatter:", error);
                return "Invalid input for JSON formatter";
            }
        },

        processJsonAggregator(data, existingData = {}) {
            try {
                // If the input is a string, try to parse it as JSON
                if (typeof data === 'string') {
                    data = JSON.parse(data);
                }
                // If the input is a single JSON object, merge it with the existing data
                if (typeof data === 'object' && !Array.isArray(data)) {
                    const mergedData = { ...existingData, ...data }; // Merge new data with existing data
                    return mergedData;
                }
                // If the input is an array of JSON objects, merge them with the existing data
                if (Array.isArray(data) && data.every(item => typeof item === 'object')) {
                    const mergedData = data.reduce((acc, jsonObj) => {
                        return { ...acc, ...jsonObj }; // Merge each object into the accumulator
                    }, existingData); // Start with the existing data
                    return mergedData;
                }
                throw new Error("Input must be a JSON object or an array of JSON objects.");
            } catch (error) {
                console.error("Invalid input for JSON aggregator:", error);
                return "Invalid input for JSON aggregator";
            }
        },

        async processInvokeApi(data, apiUrl) {
            try {
                // Early return if data is null or undefined
                if (data === null || data === undefined) {
                    console.warn("Data is null or undefined. Skipping API request.");
                    return;
                }
                // Validate the API URL
                if (!apiUrl || typeof apiUrl !== 'string') {
                    throw new Error("Invalid API URL");
                }
                // Validate the request data
                if (!data || typeof data !== 'object') {
                    throw new Error("Invalid request data");
                }
                // Send the data to the backend API using fetch
                const response = await fetch(apiUrl, {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify(data),
                });
                // Check if the response is OK
                if (!response.ok) {
                    const errorText = await response.text();
                    throw new Error(`API request failed with status ${response.status}: ${errorText}`);
                }
                // Manually process the SSE stream
                const reader = response.body.getReader();
                const decoder = new TextDecoder();

                // eslint-disable-next-line no-constant-condition
                while (true) {
                    const { done, value } = await reader.read();
                    if (done) break;

                    const chunk = decoder.decode(value);
                    // Split the chunk into individual events
                    const events = chunk.split('\n\n');
                    for (const event of events) {
                        if (event.startsWith('data:')) {
                            const eventData = event.replace('data:', '').trim();
                            if (eventData) {
                                const result = JSON.parse(eventData); // Parse the event data as JSON
                                // Save the streamed data to nodeData
                                this.nodeData = result; // Update the reactive property
                                // Propagate the data to connected nodes
                                this.propagateDataToDownstreamNodes(result);
                            }
                        }
                    }
                }
            } catch (error) {
                console.error("API request failed:", error);
                return "API request failed";
            }
        },

        propagateDataToDownstreamNodes(data) {
            // Find the node that triggered the transformation (e.g., the TSPProblem node)
            const tspNode = this.nodes.find((node) => node.type === "invoke-api");
            if (!tspNode) {
                console.warn("TSPProblem node not found.");
                return;
            }

            // Propagate the data to connected downstream nodes
            const connectedNodes = this.findConnectedNodes(tspNode.id, this.nodes, this.lines);
            connectedNodes.forEach((downstreamNode) => {
                if (downstreamNode.triggerAction === "A") {
                    this.applyTransform(data, this.nodes, this.lines, new Set(), downstreamNode);
                } else {
                    downstreamNode.nodeData = data; // Pass the data as-is
                }
            });
        },

        extractNodeId(id) {
            // Split the ID by '-' and return the first part (nodeId)
            return id.split("-")[0];
        },
    },
};
</script>