// src/utils/nodeUtils.js
import store from '@/store';
import { useToast } from "vue-toastification";
import { applyTransform } from '@/utils/transformUtils';

const toast = useToast();

/**
 * Extracts the node ID from a port ID.
 * @param {string} portId - The port ID (e.g., "n1-i0" or "n2-o1").
 * @returns {string} - The node ID (e.g., "n1").
 */
export function extractNodeId(portId) {
    return portId.split('-')[0];
}

/**
 * Extracts the port type and index from a port ID.
 * @param {string} portId - The port ID (e.g., "n1-i0" or "n2-o1").
 * @returns {Object} - An object containing the port type ("input" or "output") and index.
 */
export function extractPortInfo(portId) {
    const parts = portId.split('-');
    const portType = parts[1][0] === 'i' ? 'input' : 'output'; // 'i' for input, 'o' for output
    const portIndex = parseInt(parts[1].slice(1), 10); // Extract the number after 'i' or 'o'
    return { portType, portIndex };
}

/**
 * Extracts the port index from a port ID.
 * @param {string} portId - The port ID (e.g., "n1-i2" or "n2-o1").
 * @returns {number} - The port index (e.g., 2).
 */
export function extractPortIndex(portId) {
    const parts = portId.split('-');
    if (parts.length < 2 || !/^[io]\d+$/.test(parts[1])) {
        console.error("Invalid portId format:", portId);
        return undefined; // or throw an error
    }
    return parseInt(parts[1].slice(1), 10);
}

/**
 * Generates a port ID from a node ID, port type, and port index.
 * @param {string} nodeId - The node ID (e.g., "n1").
 * @param {string} portType - The port type ("input" or "output").
 * @param {number} portIndex - The port index.
 * @returns {string} - The port ID (e.g., "n1-i0" or "n2-o1").
 */
export function generatePortId(nodeId, portType, portIndex) {
    const portPrefix = portType === 'input' ? 'i' : 'o';
    return `${nodeId}-${portPrefix}${portIndex}`;
}

/**
 * Find a node by its ID.
 * @param {string} nodeId - The ID of the node to find.
 * @returns {Object|null} - The found node or null if not found.
 */
export function findNodeById(nodeId) {
    const nodes = store.getters.nodes;
    const node = nodes.find((node) => node.id === nodeId);
    if (!node) {
        toast.error(`Node with ID "${nodeId}" not found.`);
    }
    return node;
}

/**
 * Finds the next connected node in a list of lines (connections).
 * @param {string} nodeId - The ID of the current node.
 * @returns {Object|null} - An object containing the next node ID and port index, or null if no connection is found.
 */
export function findNextNode(nodeId) {
    const nodes = store.getters.nodes;
    const lines = store.getters.lines;
    try {
        const connectedLine = lines.find((line) => {
            const sourceNodeId = extractNodeId(line.sourceId);
            return sourceNodeId === nodeId;
        });

        if (connectedLine) {
            const { targetId } = connectedLine;
            const nextNodeId = extractNodeId(targetId);
            const nextNode = nodes.find((node) => node.id === nextNodeId);
            const nextPortIndex = extractPortIndex(targetId);
            return { nextNode: nextNode, nextPortIndex: nextPortIndex };
        }

        return null;
    } catch (error) {
        const errorMessage = error instanceof Error ? error.message : "An error occurred while finding the next node.";
        console.error(errorMessage);
        return null; // Return null to indicate failure
    }
}

/**
 * Finds the first node connected to a given node by input type.
 * @param {string} nodeId - The ID of the node to find connections for.
 * @param {string} inputType - The input type to filter by.
 * @returns {Object|null} - The connected node or null if none found.
 */
export function findConnectedNodeByInputType(nodeId, inputType) {
    // Use findConnectedNodes to get all nodes connected to the specified input type
    const connectedNodes = findConnectedNodes(nodeId, inputType);

    // Return the first connected node (if any)
    return connectedNodes.length > 0 ? connectedNodes[0] : null;
}

/**
 * Finds all nodes connected to a given node.
 * @param {string} nodeId - The ID of the node to find connections for.
 * @param {string|null} inputType - The input type to filter by (optional).
 * @returns {Array} - An array of connected nodes.
 * @example
 * // Find all nodes connected to node "n1"
 * const connectedNodes = findConnectedNodes("n1");
 *
 * // Find all nodes connected to node "n1" with input type "inputA"
 * const connectedNodes = findConnectedNodes("n1", "inputA");
 */
export function findConnectedNodes(nodeId, inputType = null) {
    const nodes = store.getters.nodes;
    const lines = store.getters.lines;
    if (typeof nodeId !== "string" || nodeId.trim() === "") {
        throw new Error("Invalid nodeId. Expected a non-empty string.");
    }
    if (!Array.isArray(lines)) {
        toast.warning("Invalid lines data. Expected an array.");
        return [];
    }
    if (lines.length === 0) {
        return [];
    }
    return nodes.filter((node) =>
        lines.some(
            (line) =>
                extractNodeId(line.sourceId) === nodeId && // Compare source nodeId
                extractNodeId(line.targetId) === node.id && // Compare target nodeId
                (!inputType || line.targetInputType === inputType) // Filter by input type if provided
        )
    );
}

/**
 * Finds all nodes connected to a given node, including their connection details.
 * @param {string} nodeId - The ID of the node to find connections for.
 * @param {string|null} inputType - The input type to filter by (optional).
 * @returns {Array<{nextNode: Object, nextPortIndex: number}>} - An array of objects containing connected nodes and their port indices.
 * @example
 * // Find all connected nodes with connection details for node "n1"
 * const connections = findNodeConnections("n1");
 */
export function findNodeConnections(nodeId, inputType = null) {
    const nodes = store.getters.nodes;
    const lines = store.getters.lines;
    if (typeof nodeId !== "string" || nodeId.trim() === "") {
        throw new Error("Invalid nodeId. Expected a non-empty string.");
    }
    if (!Array.isArray(lines)) {
        toast.warning("Invalid lines data. Expected an array.");
        return [];
    }
    if (lines.length === 0) {
        return [];
    }
    return lines
        .filter(line =>
            extractNodeId(line.sourceId) === nodeId &&
            (!inputType || line.targetInputType === inputType)
        )
        .map(line => {
            const nextNodeId = extractNodeId(line.targetId);
            const nextNode = nodes.find(node => node.id === nextNodeId);
            const nextPortIndex = extractPortIndex(line.targetId);

            return {
                nextNode,
                nextPortIndex
            };
        })
        .filter(connection => connection.nextNode != null); // Filter out any connections where the node wasn't found
}

/**
 * Processes the submit action for a given node by finding the next connected node
 * and propagating the output data to it. Additionally, it displays a success toast
 * notification upon successful submission.
 * 
 * @param {string} nodeId - The ID of the current node initiating the submit action.
 * @param {any} outData - The output data from the current node to be propagated.
 * @returns {void}
 */
export function processSubmitAction(nodeId, outData) {
    const result = findNextNode(nodeId);
    if (result) {
        const { nextNode, nextPortIndex } = result;
        propagateData(nextNode, nextPortIndex, outData);
    }
    toast.success(`Submitted input for node: ${nodeId}`);
}

/**
 * Propagates data from one node's outData to the next node's input port.
 * If the node has an auto-transformation trigger ("A"), it processes the transformation
 * and propagates the transformed data to all connected downstream nodes.
 * 
 * @param {object} node - The node to propagate data to.
 * @param {number} portIndex - The index of the input port to update.
 * @param {any} outData - The data to propagate.
 * @returns {void}
 */
export function propagateData(node, portIndex, outData) {
    try {
        // Ensure the node's inputData is an array
        if (!Array.isArray(node.inputData)) {
            node.inputData = [];
        }
        // Update the input data at the specified port index
        node.inputData = [
            ...node.inputData.slice(0, portIndex),
            outData,
            ...node.inputData.slice(portIndex + 1),
        ];
        // If the node has an auto-transformation trigger, process the transformation
        if (node.triggerAction === "A") {
            processAutoTransformation(node, portIndex);
        }
    } catch (error) {
        // Handle errors and display a toast notification
        const errorMessage = error instanceof Error ? error.message : "Failed to propagate data";
        toast.error(`Data propagation error:\n${errorMessage}`);
    }
}

/**
 * Processes auto-transformation for a node by applying the transformation logic
 * and propagating the transformed output data to all connected downstream nodes.
 * 
 * @param {object} node - The node to process auto-transformation for.
 * @param {number} portIndex - The index of the input port containing the data to transform.
 * @returns {void}
 */
export function processAutoTransformation(node, portIndex) {
    // Apply transformation logic to the input data
    const transformedData = applyTransform(node, node.inputData[portIndex]);
    node.outputData = transformedData;

    // Propagate the data to connected nodes
    propagateDataToDownstreamNodes(node);
}

/**
 * Processes an API invocation for a node by sending the input data to the specified API endpoint.
 * This function handles the API request, processes the Server-Sent Events (SSE) stream, and updates
 * the node's output data with the received results. The output data is then propagated to downstream nodes.
 * 
 * @param {object} node - The node for which the API invocation is being processed.
 * @returns {Promise<void>} - A promise that resolves when the API request and data propagation are complete.
 */
export async function processApiStreamResponse(node) {
    try {
        const data = node.inputData[0]; // Get the input data from the first input port
        const apiEndpoint = node.apiEndpoint; // Get the API endpoint from the node

        // Early return if data is null or undefined
        if (data === null || data === undefined) {
            toast.warning("Data is null or undefined. Skipping API request.");
            return;
        }
        // Validate the API URL
        if (!apiEndpoint || typeof apiEndpoint !== 'string') {
            throw new Error("Invalid API URL");
        }
        // Validate the request data
        if (!data || typeof data !== 'object') {
            throw new Error("Invalid request data");
        }
        // Send the data to the backend API using fetch
        const response = await fetch(apiEndpoint, {
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

        toast.success(`Optimized problem for node: ${node.id}`);
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
                        node.outputData = result; // Update the reactive property
                        // Propagate the data to connected nodes
                        propagateDataToDownstreamNodes(node);
                    }
                }
            }
        }
    } catch (error) {
        // Handle errors and display a toast notification
        const errorMessage = error instanceof Error ? error.message : "Failed to process invoke API";
        toast.error(`API request failed:\n${errorMessage}`);
    }
}

/**
 * Sends Chart.js data and a prompt to OpenAI's API and processes the response.
 * 
 * @param {object} node - The node for which the API invocation is being processed.
 * @returns {Promise<void>} - A promise that resolves with the LLM's response.
 */
export async function sendChartDataToOpenAI(node) {
    try {
        const apiEndpoint = process.env.VUE_APP_OPENAI_API_ENDPOINT;
        const apiKey = process.env.VUE_APP_OPENAI_API_KEY;
        if (!apiEndpoint || !apiKey) {
            throw new Error("Missing required environment variables: VUE_APP_OPENAI_API_ENDPOINT or VUE_APP_OPENAI_API_KEY");
        }

        const data = node.inputData[0];
        const prompt = node.inputData[1];
        const fullPrompt = `${prompt}\n\nData:\n\n###${JSON.stringify(data, null, 2)}###`;

        const requestBody = {
            model: "gpt-4o-mini",
            messages: [{ role: "user", content: fullPrompt }],
            temperature: 0.7,
            max_tokens: 200,
        };

        const response = await fetch(apiEndpoint, {
            method: "POST",
            headers: { "Content-Type": "application/json", Authorization: `Bearer ${apiKey}` },
            body: JSON.stringify(requestBody),
        });

        if (!response.ok) {
            throw new Error(`OpenAI request failed: ${await response.text()}`);
        }

        const result = await response.json();
        if (!result.choices || result.choices.length === 0) {
            throw new Error("Invalid OpenAI response");
        }

        node.outputData = result.choices[0].message.content;
        propagateDataToDownstreamNodes(node);
        toast.success("LLM processed the data successfully!");
    } catch (error) {
        toast.error(`LLM request failed: ${error.message}`);
        console.error(error);
    }
}

/**
 * Sends Chart.js data and a prompt to a local LLM via the API and processes the response.
 * 
 * @param {object} node - The node for which the API invocation is being processed.
 * @returns {Promise<void>} - A promise that resolves with the LLM's response.
 */
export async function sendChartDataToLocalLLM(node) {
    try {
        const apiEndpoint = process.env.VUE_APP_LOCAL_LLM_API_ENDPOINT;
        if (!apiEndpoint) {
            throw new Error("Missing required environment variables: VUE_APP_LOCAL_LLM_API_ENDPOINT");
        }

        const data = node.inputData[0];
        const prompt = node.inputData[1];
        const fullPrompt = `${prompt}\n\nData:\n\n###${JSON.stringify(data, null, 2)}###`;

        const requestBody = {
            model: "deepseek-r1:1.5b",
            prompt: fullPrompt,
            stream: false,
        };

        const response = await fetch(apiEndpoint, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(requestBody),
        });

        if (!response.ok) {
            throw new Error(`Local LLM request failed: ${await response.text()}`);
        }

        const result = await response.json();
        node.outputData = result.response;
        propagateDataToDownstreamNodes(node);
        toast.success("Local LLM processed the data successfully!");
    } catch (error) {
        toast.error(`Local LLM request failed: ${error.message}`);
        console.error(error);
    }
}

/**
 * Propagates the transformed output data from a node to all connected downstream nodes.
 * This function finds all connections originating from the given node and forwards the
 * output data to the corresponding input ports of the connected downstream nodes.
 * 
 * @param {object} node - The node whose output data needs to be propagated.
 * @returns {void}
 */
export function propagateDataToDownstreamNodes(node) {
    // Find all connected downstream nodes with their connection details
    const connections = findNodeConnections(node.id);

    // For each connected downstream node, propagate the transformed data
    connections.forEach(({ nextNode, nextPortIndex }) => {
        propagateData(nextNode, nextPortIndex, node.outputData);
    });
}